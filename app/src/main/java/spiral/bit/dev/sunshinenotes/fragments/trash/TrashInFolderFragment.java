package spiral.bit.dev.sunshinenotes.fragments.trash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
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
import com.xeoh.android.texthighlighter.TextHighlighter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.other.BaseActivity;
import spiral.bit.dev.sunshinenotes.activities.other.SettingsActivity;
import spiral.bit.dev.sunshinenotes.adapter.trash.TrashNoteInFolderAdapter;
import spiral.bit.dev.sunshinenotes.data.trash.TrashNoteInFolderDatabase;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.listeners.trash.TrashNotesInFolderListener;
import spiral.bit.dev.sunshinenotes.models.trash.TrashFolder;
import spiral.bit.dev.sunshinenotes.models.trash.TrashNoteInFolder;
import spiral.bit.dev.sunshinenotes.other.AdWorker;

import static android.app.Activity.RESULT_OK;
import static spiral.bit.dev.sunshinenotes.fragments.CheckListFragment.REQUEST_CODE_ENABLE;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_NOTE_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.DELETE_NOTES_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.SHOW_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_FOLDER_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_NOTE_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.hideKeyboard;

public class TrashInFolderFragment extends Fragment implements TrashNotesInFolderListener {

    private static RecyclerView noteRecyclerView;
    private TrashNoteInFolderAdapter adapter;
    private List<TrashNoteInFolder> notesList;
    private int clickedNotePosition = -1;
    private ImageView imgClear;
    private SharedPreferences prefPass, checkedPref, prefPassword, graphicPref, preferenceSettings;
    private SharedPreferences.Editor editorIsShowed;
    private LottieAnimationView nowHereEmpty;
    private TextView labelEmptyNow, labelHint;
    public ImageView imageAddNoteMain;
    private TrashFolder alreadyAvailableFolder;
   // private TrashNoteInFolder alreadyAvailableTrashNote;
    private ArrayList<TrashNoteInFolder> tempList;
    private ArrayList<Integer> clickedNotePositions;
    private LinearLayout menuDelete;
    private boolean isDeletedModeEnabled = false;

//    @Override
//    public void onResume() {
//        super.onResume();
//        if (preferenceSettings.getBoolean("dark", false)) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        }
//    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_in_folder, container, false);


        BaseActivity.disableBar();
        getParentFragmentManager().setFragmentResultListener(String.valueOf(UPDATE_FOLDER_CODE),
                getViewLifecycleOwner(), new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                        alreadyAvailableFolder = (TrashFolder) bundle.getSerializable("trash_folder");
                        //getNotes(UPDATE_NOTE_CODE, false);
                    }
                });

        Bundle bundle = getArguments();
        if (bundle != null) {
            alreadyAvailableFolder = (TrashFolder) bundle.getSerializable("trash_folder");
        }
        tempList = new ArrayList<>();
        clickedNotePositions = new ArrayList<>();
        menuDelete = view.findViewById(R.id.menu_delete_note);

        preferenceSettings = PreferenceManager
                .getDefaultSharedPreferences(getContext().getApplicationContext());

        //Prefs

//        if (preferenceSettings.getBoolean("dark", false)) AppCompatDelegate
//                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        checkedPref = getContext().getSharedPreferences("check", 0);
        editorIsShowed = checkedPref.edit();
        prefPass = getContext().getSharedPreferences("pass", 0);
        prefPassword = getContext().getSharedPreferences("password", 0);
        graphicPref = getContext().getSharedPreferences("graphic", 0);

        //Vars

        // FIXME: 09.12.2020
        view.findViewById(R.id.icon_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        nowHereEmpty = view.findViewById(R.id.now_empty_view);
        noteRecyclerView = view.findViewById(R.id.notes_recycler_view);

        notesList = new ArrayList<>();
        adapter = new TrashNoteInFolderAdapter(notesList, this);
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
        imgClear = view.findViewById(R.id.ic_clear);

        final EditText searchEditText = view.findViewById(R.id.search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                imgClear.setVisibility(View.VISIBLE);
                imgClear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchEditText.setText("");
                        hideKeyboard(getActivity());
                    }
                });
                adapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                if (notesList.size() != 0) {
                    adapter.searchNote(editable.toString());
                    new TextHighlighter()
                            .setBackgroundColor(Color.parseColor("#FFFF00"))
                            .addTarget(view.findViewById(R.id.text_title))
                            .highlight(searchEditText.getText().toString(), TextHighlighter.BASE_MATCHER);
                }
            }
        });
        imgClear.setVisibility(View.GONE);

        view.findViewById(R.id.icon_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SettingsActivity.class));
            }
        });

        TextView labelFolderName = view.findViewById(R.id.label_tv_my_notes_in_folder);
        labelFolderName.setText(alreadyAvailableFolder.getName());
        getNotes(SHOW_CODE, false);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ADD_NOTE_CODE) {
            getNotes(ADD_NOTE_CODE, false);
        } else if (resultCode == RESULT_OK && requestCode == UPDATE_NOTE_CODE) {
            if (data != null) {
                getNotes(UPDATE_NOTE_CODE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if (requestCode == REQUEST_CODE_ENABLE && resultCode == RESULT_OK) {
            if (preferenceSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(
                    getContext(),
                    "Пин-код успешно задан!",
                    FancyToast.LENGTH_LONG,
                    FancyToast.SUCCESS,
                    false).show();
        }
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetAllNotesAsyncTask extends AsyncTask<Void, Void, List<TrashNoteInFolder>> {
            @Override
            protected List<TrashNoteInFolder> doInBackground(Void... voids) {
                return TrashNoteInFolderDatabase.getNoteDatabase(getActivity().getApplicationContext())
                        .getNoteDAO().getAllNotesInFolder(alreadyAvailableFolder.getId());
            }

            @Override
            protected void onPostExecute(List<TrashNoteInFolder> notes) {
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
                } else if (requestCode == DELETE_NOTES_CODE) {
                    notesList.remove(clickedNotePositions);
                    clickedNotePositions = new ArrayList<>();
                    tempList = new ArrayList<>();
                    TrashInFolderFragment inFolderFragment = new TrashInFolderFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("folder", alreadyAvailableFolder);
                    inFolderFragment.setArguments(bundle);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.replaced_container, inFolderFragment)
                            .commit();
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
    public void onNoteClicked(TrashNoteInFolder trashNoteInFolder, int position) {
        FancyToast.makeText(
                getContext(),
                "Вы не можете восстановить \n заметку из папки отдельно!",
                FancyToast.LENGTH_LONG,
                FancyToast.WARNING,
                false).show();
    }

    @Override
    public void onLongNoteClicked(TrashNoteInFolder trashNoteInFolder, int position) {
        FancyToast.makeText(
                getContext(),
                "Вы не можете удалить \n заметки без папки!",
                FancyToast.LENGTH_LONG,
                FancyToast.WARNING,
                false).show();
    }
}