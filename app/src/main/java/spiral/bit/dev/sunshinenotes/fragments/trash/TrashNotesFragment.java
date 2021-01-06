package spiral.bit.dev.sunshinenotes.fragments.trash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.restore.RestoreNoteActivity;
import spiral.bit.dev.sunshinenotes.adapter.trash.TrashAdapter;
import spiral.bit.dev.sunshinenotes.data.NoteDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashDatabase;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.listeners.trash.TrashListener;
import spiral.bit.dev.sunshinenotes.models.SimpleNote;
import spiral.bit.dev.sunshinenotes.models.trash.TrashNote;
import spiral.bit.dev.sunshinenotes.other.AdWorker;

import static android.app.Activity.RESULT_OK;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_NOTE_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.SHOW_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_NOTE_CODE;

public class TrashNotesFragment extends Fragment implements TrashListener {

    private static RecyclerView noteRecyclerView;
    private TrashAdapter adapter;
    private List<TrashNote> notesList;
    private int clickedNotePosition = -1;
    private ImageView imgClear;
    private SharedPreferences preferenceSettings;
    private LottieAnimationView nowHereEmpty;
    private TextView labelEmptyNow, labelHint;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_trash_notes, container, false);

        preferenceSettings = PreferenceManager
                .getDefaultSharedPreferences(getContext().getApplicationContext());
//        if (preferenceSettings.getBoolean("dark", false)) AppCompatDelegate
//                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        getNotes(SHOW_CODE, false);
        nowHereEmpty = view.findViewById(R.id.now_empty_view);
        noteRecyclerView = view.findViewById(R.id.notes_recycler_view);
        notesList = new ArrayList<>();
        adapter = new TrashAdapter(notesList, this);
        noteRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteRecyclerView.setAdapter(adapter);
        labelEmptyNow = view.findViewById(R.id.label_empty);
        labelHint = view.findViewById(R.id.label_hint);
        final AdView mAdView = view.findViewById(R.id.adView);

        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                if (!preferenceSettings.getBoolean("time_block_ads", false)) {
                    mAdView.setVisibility(View.VISIBLE);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView.loadAd(adRequest);
                } else {
                    mAdView.setVisibility(View.GONE);
                }
            }
        });

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
            }

            @Override
            public void onAdOpened() {
                SharedPreferences.Editor editorPrefSettings = preferenceSettings.edit();
                editorPrefSettings.putBoolean("time_block_ads", true);
                editorPrefSettings.apply();
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AdWorker.class)
                        .setInitialDelay(15, TimeUnit.MINUTES).build();
                WorkManager workManager = WorkManager.getInstance(getContext());
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdClicked() {
                //Dis ads
                SharedPreferences.Editor editorPrefSettings = preferenceSettings.edit();
                editorPrefSettings.putBoolean("time_block_ads", true);
                editorPrefSettings.apply();
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AdWorker.class)
                        .setInitialDelay(15, TimeUnit.MINUTES).build();
                WorkManager workManager = WorkManager.getInstance(getContext());
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            }
        });
        if (SettingsFragment.getIsPurchased(getActivity())) mAdView.setVisibility(View.GONE);

        view.findViewById(R.id.icon_delete_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TrashNotesFragment.DeleteNoteAsyncTask().execute();
            }
        });
        view.findViewById(R.id.icon_restore_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TrashNotesFragment.RestoreAllNotesAsyncTask().execute();
            }
        });
        return view;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if (preferenceSettings.getBoolean("dark", false)) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ADD_NOTE_CODE) {
            getNotes(ADD_NOTE_CODE, false);
        } else if (resultCode == RESULT_OK && requestCode == UPDATE_NOTE_CODE) {
            if (data != null) {
                getNotes(UPDATE_NOTE_CODE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetAllNotesAsyncTask extends AsyncTask<Void, Void, List<TrashNote>> {
            @Override
            protected List<TrashNote> doInBackground(Void... voids) {
                return TrashDatabase.getNoteDatabase(getContext().getApplicationContext())
                        .getTrashDAO().getAllTrashNotes();
            }

            @Override
            protected void onPostExecute(List<TrashNote> notes) {
                super.onPostExecute(notes);
                if (requestCode == SHOW_CODE) {
                    notesList.addAll(notes);
                    adapter.notifyDataSetChanged();
                } else if (requestCode == ADD_NOTE_CODE) {
                    notesList.add(0, notes.get(0));
                    adapter.notifyItemInserted(0);
                    noteRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == UPDATE_NOTE_CODE) {
                    notesList.remove(clickedNotePosition);
                    if (isNoteDeleted) {
                        adapter.notifyItemRemoved(clickedNotePosition);
                    } else {
                        notesList.add(clickedNotePosition, notes.get(clickedNotePosition));
                        adapter.notifyItemChanged(clickedNotePosition);
                    }
                }
                if (notesList.isEmpty()) {
                    nowHereEmpty.setVisibility(View.VISIBLE);
                    labelEmptyNow.setVisibility(View.VISIBLE);
                    labelHint.setVisibility(View.VISIBLE);
                    noteRecyclerView.setVisibility(View.GONE);
                    nowHereEmpty.playAnimation();
                } else {
                    nowHereEmpty.setVisibility(View.GONE);
                    labelHint.setVisibility(View.GONE);
                    labelEmptyNow.setVisibility(View.GONE);
                    noteRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        }
        new GetAllNotesAsyncTask().execute();
    }

    @Override
    public void onTrashNoteClicked(TrashNote trashNote, int position) {
        clickedNotePosition = position;
        Intent intent = new Intent(getContext(), RestoreNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("trash_note", trashNote);
        startActivityForResult(intent, UPDATE_NOTE_CODE);
    }

    @Override
    public void onTrashNoteLongClicked(TrashNote trashNote, int position) {

    }

    @SuppressLint("StaticFieldLeak")
    class DeleteNoteAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            TrashDatabase.getNoteDatabase(getContext().getApplicationContext())
                    .getTrashDAO().autoClearTrash();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            FancyToast.makeText(
                    getContext(),
                    "Корзина очищена!",
                    FancyToast.LENGTH_LONG,
                    FancyToast.SUCCESS,
                    false).show();
            getNotes(SHOW_CODE, false);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class RestoreAllNotesAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < notesList.size(); i++) {
                SimpleNote simpleNote = new SimpleNote();
                simpleNote.setColor(notesList.get(i).getColor());
                simpleNote.setDateTime(notesList.get(i).getDateTime());
                simpleNote.setDateTimeEdit(notesList.get(i).getDateTimeEdit());
                simpleNote.setDateTimeRemind(notesList.get(i).getDateTimeRemind());
                simpleNote.setDrawPath(notesList.get(i).getDrawPath());
                simpleNote.setFontStyle(notesList.get(i).getFontStyle());
                simpleNote.setId(notesList.get(i).getId());
                simpleNote.setImagePath(notesList.get(i).getImagePath());
                simpleNote.setNoteColor(notesList.get(i).getNoteColor());
                simpleNote.setNoteText(notesList.get(i).getNoteText());
                simpleNote.setSubTitle(notesList.get(i).getSubTitle());
                simpleNote.setTextSize(notesList.get(i).getTextSize());
                simpleNote.setTitle(notesList.get(i).getTitle());
                simpleNote.setWebLink(notesList.get(i).getWebLink());
                NoteDatabase.getNoteDatabase(getContext().getApplicationContext())
                        .getNoteDAO().insertNote(simpleNote);
            }
            TrashDatabase.getNoteDatabase(getContext().getApplicationContext())
                    .getTrashDAO().autoClearTrash();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            FancyToast.makeText(
                    getContext(),
                    "Все заметки восстановлены!",
                    FancyToast.LENGTH_LONG,
                    FancyToast.SUCCESS,
                    false).show();
            getNotes(SHOW_CODE, false);
        }
    }
}