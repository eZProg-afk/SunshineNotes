package spiral.bit.dev.sunshinenotes.fragments;

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
import spiral.bit.dev.sunshinenotes.activities.other.SettingsActivity;
import spiral.bit.dev.sunshinenotes.activities.create.CreateNoteActivity;
import spiral.bit.dev.sunshinenotes.adapter.NoteInFolderAdapter;
import spiral.bit.dev.sunshinenotes.data.NoteInFolderDatabase;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.listeners.NotesInFolderListener;
import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.NoteInFolder;
import spiral.bit.dev.sunshinenotes.other.AdWorker;
import spiral.bit.dev.sunshinenotes.activities.other.BaseActivity;
import static android.app.Activity.RESULT_OK;
import static spiral.bit.dev.sunshinenotes.fragments.CheckListFragment.REQUEST_CODE_ENABLE;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_NOTE_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.DELETE_NOTES_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.SHOW_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_FOLDER_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_NOTE_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.hideKeyboard;

public class InFolderFragment extends Fragment implements NotesInFolderListener {

    private static RecyclerView noteRecyclerView;
    private NoteInFolderAdapter adapter;
    private List<NoteInFolder> notesList;
    private int clickedNotePosition = -1;
    private ImageView imgClear;
    private SharedPreferences prefPass, checkedPref, prefPassword, graphicPref, preferenceSettings;
    private SharedPreferences.Editor editorIsShowed;
    private LottieAnimationView nowHereEmpty;
    private TextView labelEmptyNow, labelHint;
    public ImageView imageAddNoteMain;
    private Folder alreadyAvailableFolder;
    private NoteInFolder alreadyAvailableNote;
    private ArrayList<NoteInFolder> tempList;
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
                        alreadyAvailableFolder = (Folder) bundle.getSerializable("folder");
                        //getNotes(UPDATE_NOTE_CODE, false);
                    }
                });

        Bundle bundle = getArguments();
        if (bundle != null) {
            alreadyAvailableFolder = (Folder) bundle.getSerializable("folder");
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

        imageAddNoteMain = view.findViewById(R.id.icon_add_note_in_folder);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddBtnClicked();
            }
        });

        nowHereEmpty = view.findViewById(R.id.now_empty_view);
        noteRecyclerView = view.findViewById(R.id.notes_recycler_view);

        notesList = new ArrayList<>();
        adapter = new NoteInFolderAdapter(notesList, this);
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

    private void onAddBtnClicked() {
        Intent intent = new Intent(getContext(), CreateNoteActivity.class);
        intent.putExtra("folder", alreadyAvailableFolder);
        startActivityForResult(intent, ADD_NOTE_CODE);
    }

    @Override
    public void onNoteClicked(NoteInFolder noteInFolder, int position) {
        if (isDeletedModeEnabled) {
            if (tempList.contains(noteInFolder)) {
                adapter.disableOneClickDeleteMode();
                clickedNotePosition = position;
                if (tempList.size() == 0 || tempList.size() == 1) {
                    tempList = new ArrayList<>();
                    clickedNotePositions = new ArrayList<>();
                    menuDelete.setVisibility(View.GONE);
                    isDeletedModeEnabled = false;
                    adapter.disableOneClickDeleteMode();
                    InFolderFragment inFolderFragment = new InFolderFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("folder", alreadyAvailableFolder);
                    inFolderFragment.setArguments(bundle);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.replaced_container, inFolderFragment)
                            .commit();
                } else {
                    tempList.remove(noteInFolder);
                    //clickedNotePositions.remove(clickedNotePosition); хуй знает вроде и без неё работает
                }
            } else {
                adapter.setOneClickDeleteMode();
                clickedNotePosition = position;
                tempList.add(noteInFolder);
                clickedNotePositions.add(clickedNotePosition);
            }
        } else {
            clickedNotePosition = position;
            Intent intent = new Intent(getContext(), CreateNoteActivity.class);
            intent.putExtra("isViewOrUpdate", true);
            intent.putExtra("note_in_folder", noteInFolder);
            intent.putExtra("folder", alreadyAvailableFolder);
            startActivityForResult(intent, UPDATE_NOTE_CODE);
        }
    }

    @Override
    public void onLongNoteClicked(NoteInFolder noteInFolder, int position) {
        isDeletedModeEnabled = true;
        clickedNotePosition = position;
        tempList.add(noteInFolder);
        clickedNotePositions.add(clickedNotePosition);
        menuDelete.setVisibility(View.VISIBLE);
        menuDelete.findViewById(R.id.image_close_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempList = new ArrayList<>();
                clickedNotePositions = new ArrayList<>();
                menuDelete.setVisibility(View.GONE);
                isDeletedModeEnabled = false;
                adapter.disableOneClickDeleteMode();
                InFolderFragment inFolderFragment = new InFolderFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("folder", alreadyAvailableFolder);
                inFolderFragment.setArguments(bundle);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.replaced_container, inFolderFragment)
                        .commit();
            }
        });
        menuDelete.findViewById(R.id.image_accept_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new InFolderFragment.MultipleDeleteAsyncTask().execute();
                menuDelete.setVisibility(View.GONE);
                isDeletedModeEnabled = false;
                adapter.disableOneClickDeleteMode();
            }
        });
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
        class GetAllNotesAsyncTask extends AsyncTask<Void, Void, List<NoteInFolder>> {
            @Override
            protected List<NoteInFolder> doInBackground(Void... voids) {
                return NoteInFolderDatabase.getNoteDatabase(getActivity().getApplicationContext())
                        .getNoteDAO().getAllNotesInFolder(alreadyAvailableFolder.getId());
            }

            @Override
            protected void onPostExecute(List<NoteInFolder> notes) {
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
                    InFolderFragment inFolderFragment = new InFolderFragment();
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

    @SuppressLint("StaticFieldLeak")
    class MultipleDeleteAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            NoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext())
                    .getNoteDAO().deleteMultiplyNotes(tempList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getNotes(DELETE_NOTES_CODE, true);
        }
    }
}