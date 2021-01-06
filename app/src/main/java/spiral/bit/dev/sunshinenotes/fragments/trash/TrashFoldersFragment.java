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
import spiral.bit.dev.sunshinenotes.activities.restore.RestoreFolderActivity;
import spiral.bit.dev.sunshinenotes.adapter.trash.TrashFolderAdapter;
import spiral.bit.dev.sunshinenotes.data.NoteInFolderDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashNoteInFolderDatabase;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.listeners.trash.TrashEditListener;
import spiral.bit.dev.sunshinenotes.listeners.trash.TrashFolderListener;
import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.trash.TrashFolder;
import spiral.bit.dev.sunshinenotes.other.AdWorker;

import static android.app.Activity.RESULT_OK;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_CHECK_LIST_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.SHOW_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_CHECK_LIST_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_FOLDER_CODE;

public class TrashFoldersFragment extends Fragment implements TrashFolderListener, TrashEditListener {

    private static RecyclerView noteRecyclerView;
    private TrashFolderAdapter adapter;
    private List<TrashFolder> trashFolderList;
    private int clickedNotePosition = -1;
    private ImageView imgClear;
    private SharedPreferences preferenceSettings;
    private LottieAnimationView nowHereEmpty;
    private TextView labelEmptyNow, labelHint;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_trash_folders, container, false);

        preferenceSettings = PreferenceManager
                .getDefaultSharedPreferences(getContext().getApplicationContext());
//        if (preferenceSettings.getBoolean("dark", false)) AppCompatDelegate
//                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        nowHereEmpty = view.findViewById(R.id.now_empty_view);
        noteRecyclerView = view.findViewById(R.id.notes_recycler_view);
        trashFolderList = new ArrayList<>();
        adapter = new TrashFolderAdapter(trashFolderList, this, this);
        noteRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteRecyclerView.setAdapter(adapter);
        getNotes(SHOW_CODE, false);
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
                new TrashFoldersFragment.DeleteNoteAsyncTask().execute();
            }
        });
        view.findViewById(R.id.icon_restore_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TrashFoldersFragment.RestoreAllNotesAsyncTask().execute();
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
        if (resultCode == RESULT_OK && requestCode == ADD_CHECK_LIST_CODE) {
            getNotes(ADD_CHECK_LIST_CODE, false);
        } else if (resultCode == RESULT_OK && requestCode == UPDATE_CHECK_LIST_CODE) {
            if (data != null) {
                getNotes(UPDATE_CHECK_LIST_CODE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetAllNotesAsyncTask extends AsyncTask<Void, Void, List<TrashFolder>> {
            @Override
            protected List<TrashFolder> doInBackground(Void... voids) {
                return TrashNoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext())
                        .getNoteDAO().getAllFolders();
            }

            @Override
            protected void onPostExecute(List<TrashFolder> folders) {
                super.onPostExecute(folders);
                if (requestCode == SHOW_CODE) {
                    trashFolderList.addAll(folders);
                    adapter.notifyDataSetChanged();
                } else if (requestCode == ADD_CHECK_LIST_CODE) {
                    trashFolderList.add(0, folders.get(0));
                    adapter.notifyItemInserted(0);
                    noteRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == UPDATE_CHECK_LIST_CODE) {
                    trashFolderList.remove(clickedNotePosition);
                    if (isNoteDeleted) {
                        adapter.notifyItemRemoved(clickedNotePosition);
                    } else {
                        trashFolderList.add(clickedNotePosition, folders.get(clickedNotePosition));
                        adapter.notifyItemChanged(clickedNotePosition);
                    }
                }
                if (trashFolderList.isEmpty()) {
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
    public void onTrashFolderClicked(TrashFolder trashFolder, int position) {
        clickedNotePosition = position;
        Bundle bundle = new Bundle();
        TrashInFolderFragment inFolderFragment = new TrashInFolderFragment();
        bundle.putSerializable("trash_folder", trashFolder);
        bundle.putBoolean("setViewOrUpdate", true);
        inFolderFragment.setArguments(bundle);
        getParentFragmentManager().setFragmentResult(String.valueOf(UPDATE_FOLDER_CODE), bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.replaced_trash, inFolderFragment)
                .commit();
    }

    @Override
    public void onTrashFolderLongClicked(TrashFolder trashFolder, int position) {

    }

    @Override
    public void onEdit(TrashFolder folder, int position) {
        clickedNotePosition = position;
        Intent intent = new Intent(getContext(), RestoreFolderActivity.class);
        intent.putExtra("setViewOrUpdate", true);
        intent.putExtra("trash_folder", folder);
        startActivityForResult(intent, UPDATE_CHECK_LIST_CODE);
    }

    @SuppressLint("StaticFieldLeak")
    class DeleteNoteAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            TrashNoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext())
                    .getNoteDAO().autoClearNotesTrash();
            TrashNoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext())
                    .getNoteDAO().autoClearFoldersTrash();
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
            for (int i = 0; i < trashFolderList.size(); i++) {
                Folder folder = new Folder();
                folder.setColor(trashFolderList.get(i).getColor());
                folder.setId(trashFolderList.get(i).getId());
                folder.setDateTime(trashFolderList.get(i).getDateTime());
                folder.setImagePath(trashFolderList.get(i).getImagePath());
                folder.setName(trashFolderList.get(i).getName());
                NoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext())
                        .getNoteDAO().insertFolder(folder);
            }
            TrashNoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext())
                    .getNoteDAO().autoClearFoldersTrash();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            FancyToast.makeText(
                    getContext(),
                    "Все папки восстановлены!",
                    FancyToast.LENGTH_LONG,
                    FancyToast.SUCCESS,
                    false).show();
            getNotes(SHOW_CODE, false);
        }
    }
}