package spiral.bit.dev.sunshinenotes.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.xeoh.android.texthighlighter.TextHighlighter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.other.ChangeBackActivity;
import spiral.bit.dev.sunshinenotes.activities.other.SettingsActivity;
import spiral.bit.dev.sunshinenotes.activities.create.CreateFolderActivity;
import spiral.bit.dev.sunshinenotes.adapter.FolderAdapter;
import spiral.bit.dev.sunshinenotes.data.NoteInFolderDatabase;
import spiral.bit.dev.sunshinenotes.data.StatisticDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashNoteInFolderDatabase;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.listeners.EditListener;
import spiral.bit.dev.sunshinenotes.listeners.FoldersListener;
import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.NoteInFolder;
import spiral.bit.dev.sunshinenotes.models.other.Statistic;
import spiral.bit.dev.sunshinenotes.models.trash.TrashFolder;
import spiral.bit.dev.sunshinenotes.models.trash.TrashNoteInFolder;
import spiral.bit.dev.sunshinenotes.other.AdWorker;
import spiral.bit.dev.sunshinenotes.activities.other.BaseActivity;
import static android.app.Activity.RESULT_OK;
import static spiral.bit.dev.sunshinenotes.fragments.CheckListFragment.REQUEST_CODE_ENABLE;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_FOLDER_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.CHANGE_BACK_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.DELETE_NOTES_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.SHOW_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_FOLDER_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.hideKeyboard;
import static spiral.bit.dev.sunshinenotes.other.Utils.returnBitmap;

public class FoldersFragment extends Fragment implements FoldersListener, EditListener {

    private static RecyclerView foldersRecyclerView;
    private FolderAdapter adapter;
    private List<Folder> foldersList;
    private List<NoteInFolder> noteInFolders;
    private int clickedFolderPosition = -1;
    private ImageView imgClear;
    private SharedPreferences preferenceSettings;
    private LottieAnimationView nowHereEmpty;
    private TextView labelEmptyNow, labelHint;
    private ArrayList<Folder> tempList;
    private LinearLayout menuDelete;
    private ArrayList<Integer> clickedFolderPositions;
    private ImageView imageAddNoteMain;
    private ConstraintLayout mainBack;
    private boolean isDeletedModeEnabled = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_folders, container, false);
        BaseActivity.enableBar();
        preferenceSettings = PreferenceManager
                .getDefaultSharedPreferences(getContext().getApplicationContext());
        mainBack = view.findViewById(R.id.main_back);
        setBackImg();
//        if (preferenceSettings.getBoolean("dark", false)) AppCompatDelegate
//                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        menuDelete = view.findViewById(R.id.menu_delete_note);
        tempList = new ArrayList<>();
        noteInFolders = new ArrayList<>();
        clickedFolderPositions = new ArrayList<>();
        nowHereEmpty = view.findViewById(R.id.now_empty_view);
        foldersRecyclerView = view.findViewById(R.id.folders_recycler_view);
        foldersList = new ArrayList<>();
        adapter = new FolderAdapter(foldersList, this, this);
        foldersRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        foldersRecyclerView.setAdapter(adapter);
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

        if (SettingsFragment.getIsPurchased(getContext())) mAdView.setVisibility(View.GONE);
        imgClear = view.findViewById(R.id.ic_clear);
        imageAddNoteMain = view.findViewById(R.id.icon_add_note_main);
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
                if (foldersList.size() != 0) {
                    adapter.searchFolder(editable.toString());
                    new TextHighlighter()
                            .setBackgroundColor(Color.parseColor("#FFFF00"))
                            .addTarget(view.findViewById(R.id.text_title))
                            .highlight(searchEditText.getText().toString(), TextHighlighter.BASE_MATCHER);
                }
            }
        });
        imgClear.setVisibility(View.GONE);

        final ImageView settingsImg = view.findViewById(R.id.icon_settings);
        settingsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SettingsActivity.class));
            }
        });

        ImageView imageChangeBack = view.findViewById(R.id.icon_change_back);
        if (SettingsFragment.getIsPurchased(getContext())) imageChangeBack.setVisibility(View.VISIBLE);
        else imageChangeBack.setVisibility(View.GONE);
        imageChangeBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChangeBackActivity.class);
                intent.putExtra("from", "folders");
                startActivityForResult(intent, CHANGE_BACK_CODE);
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
        if (requestCode == ADD_FOLDER_CODE && resultCode == RESULT_OK) {
            getNotes(ADD_FOLDER_CODE, false);
        } else if (resultCode == RESULT_OK && requestCode == UPDATE_FOLDER_CODE) {
            if (data != null) {
                getNotes(UPDATE_FOLDER_CODE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if (requestCode == REQUEST_CODE_ENABLE && resultCode == RESULT_OK) {
            if (preferenceSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(
                    getContext(),
                    "Пин-код успешно задан!",
                    FancyToast.LENGTH_LONG,
                    FancyToast.SUCCESS,
                    false).show();
        }  else if (requestCode == CHANGE_BACK_CODE && resultCode == RESULT_OK) {
            if (preferenceSettings.contains("back_folder")) {
                int imageId = preferenceSettings.getInt("back_folder", 0);
                mainBack.setBackgroundResource(imageId);
            } else if (preferenceSettings.contains("color_folder")) {
                int color = preferenceSettings.getInt("color_folder",0);
                mainBack.setBackgroundColor(color);
            } else if (preferenceSettings.contains("picture_folder")) {
                String selectedImgUri = preferenceSettings.getString("picture_folder", "");
                InputStream is;
                try {
                    is = getContext().getContentResolver().openInputStream(Uri.fromFile(new File(selectedImgUri)));
                    Bitmap bitmap = returnBitmap(BitmapFactory.decodeStream(is), 800, 1400);
                    Drawable picture = new BitmapDrawable(getResources(), bitmap);
                    mainBack.setBackground(picture);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetAllFoldersAsyncTask extends AsyncTask<Void, Void, List<Folder>> {
            @Override
            protected List<Folder> doInBackground(Void... voids) {
                return NoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext())
                        .getNoteDAO().getAllFolders();
            }

            @Override
            protected void onPostExecute(List<Folder> folders) {
                super.onPostExecute(folders);
                if (requestCode == SHOW_CODE) {
                    foldersList.addAll(folders);
                    adapter.notifyDataSetChanged();
                } else if (requestCode == ADD_FOLDER_CODE) {
                    foldersList.add(0, folders.get(0));
                    adapter.notifyItemInserted(0);
                    foldersRecyclerView.smoothScrollToPosition(0);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.replaced_container, new FoldersFragment())
                            .commit();
                } else if (requestCode == UPDATE_FOLDER_CODE) {
                    foldersList.remove(clickedFolderPosition);
                    if (isNoteDeleted) {
                        adapter.notifyItemRemoved(clickedFolderPosition);
                    } else {
                        foldersList.add(clickedFolderPosition, folders.get(clickedFolderPosition));
                        adapter.notifyItemChanged(clickedFolderPosition);
                    }
                } else if (requestCode == DELETE_NOTES_CODE) {
                    foldersList.remove(clickedFolderPosition);
                    clickedFolderPositions = new ArrayList<>();
                    tempList = new ArrayList<>();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.replaced_container, new FoldersFragment())
                            .commit();
                }
                if (foldersList.isEmpty()) {
                    if (preferenceSettings.getInt("back_folder", 0) == 0
                            || preferenceSettings.getInt("picture_folder", 0) == 0) {
                        nowHereEmpty.setVisibility(View.VISIBLE);
                        labelEmptyNow.setVisibility(View.VISIBLE);
                        labelHint.setVisibility(View.VISIBLE);
                        foldersRecyclerView.setVisibility(View.GONE);
                        nowHereEmpty.playAnimation();
                    }
                } else {
                    nowHereEmpty.setVisibility(View.GONE);
                    labelHint.setVisibility(View.GONE);
                    labelEmptyNow.setVisibility(View.GONE);
                    foldersRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        }
        new GetAllFoldersAsyncTask().execute();
    }

    private void setBackImg() {
        if (preferenceSettings.contains("back_folder")) {
            int imageId = preferenceSettings.getInt("back_folder", 0);
            mainBack.setBackgroundResource(imageId);
        } else if (preferenceSettings.contains("color_folder")) {
            int color = preferenceSettings.getInt("color_folder",0);
            mainBack.setBackgroundColor(color);
        } else if (preferenceSettings.contains("picture_folder")) {
            String selectedImgUri = preferenceSettings.getString("picture_folder", "");
            InputStream is;
            try {
                is = getContext().getContentResolver().openInputStream(Uri.fromFile(new File(selectedImgUri)));
                Bitmap bitmap = returnBitmap(BitmapFactory.decodeStream(is), 800, 1400);
                Drawable picture = new BitmapDrawable(getResources(), bitmap);
                mainBack.setBackground(picture);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFolderClicked(Folder folder, int position) {
        if (isDeletedModeEnabled) {
            if (tempList.contains(folder)) {
                adapter.disableOneClickDeleteMode();
                clickedFolderPosition = position;
                if (tempList.size() == 0 || tempList.size() == 1) {
                    tempList = new ArrayList<>();
                    clickedFolderPositions = new ArrayList<>();
                    menuDelete.setVisibility(View.GONE);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.replaced_container, new FoldersFragment())
                            .commit();
                    isDeletedModeEnabled = false;
                    adapter.disableOneClickDeleteMode();
                } else {
                    tempList.remove(folder);
                    //clickedNotePositions.remove(clickedNotePosition); хуй знает вроде и без неё работает
                }
            } else {
                adapter.setOneClickDeleteMode();
                clickedFolderPosition = position;
                tempList.add(folder);
                clickedFolderPositions.add(clickedFolderPosition);
            }
        } else {
            clickedFolderPosition = position;
            Bundle bundle = new Bundle();
            bundle.putBoolean("isViewOrUpdate", true);
            bundle.putSerializable("folder", folder);
            InFolderFragment inFolderFragment = new InFolderFragment();
            inFolderFragment.setArguments(bundle);
            getParentFragmentManager().setFragmentResult(String.valueOf(UPDATE_FOLDER_CODE), bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.replaced_container, inFolderFragment)
                    .commit();
        }
    }

    @Override
    public void onLongFolderClicked(Folder folder, int position) {
        isDeletedModeEnabled = true;
        clickedFolderPosition = position;
        tempList.add(folder);
        clickedFolderPositions.add(clickedFolderPosition);
        menuDelete.setVisibility(View.VISIBLE);
        menuDelete.findViewById(R.id.image_close_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempList = new ArrayList<>();
                clickedFolderPositions = new ArrayList<>();
                menuDelete.setVisibility(View.GONE);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.replaced_container, new FoldersFragment())
                        .commit();
                isDeletedModeEnabled = false;
                adapter.disableOneClickDeleteMode();
            }
        });
        menuDelete.findViewById(R.id.image_accept_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MultipleDeleteAsyncTask().execute();
                menuDelete.setVisibility(View.GONE);
                isDeletedModeEnabled = false;
                adapter.disableOneClickDeleteMode();
            }
        });
    }

    @Override
    public void onEdit(Folder folder, int position) {
        clickedFolderPosition = position;
        Intent intent = new Intent(getContext(), CreateFolderActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("isNoteDeleted", false);
        intent.putExtra("folder", folder);
        startActivityForResult(intent, UPDATE_FOLDER_CODE);
    }

    @SuppressLint("StaticFieldLeak")
    class MultipleDeleteAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            for (Folder folder : tempList) {
                TrashFolder trashFolder = new TrashFolder();
                trashFolder.setName(folder.getName());
                trashFolder.setSubTitle(folder.getSubTitle());
                trashFolder.setImagePath(folder.getImagePath());
                trashFolder.setDateTime(folder.getDateTime());
                trashFolder.setId(folder.getId());
                trashFolder.setColor(folder.getColor());

                Statistic statistic = new Statistic();

                SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
                String date = sdf.format(Calendar.getInstance().getTime());
                statistic.setDateText(date);

                statistic.setTypeText(getString(R.string.folder_label));
                statistic.setItemSubText(folder.getSubTitle());
                statistic.setItemText(folder.getName());
                statistic.setActionText(getString(R.string.deleted_action));

                TrashNoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext())
                        .getNoteDAO().insertFolder(trashFolder);
                noteInFolders = NoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext())
                        .getNoteDAO().getAllNotesInFolder(folder.getId());
                for (NoteInFolder noteInFolder : noteInFolders) {
                    TrashNoteInFolder trashNoteInFolder = new TrashNoteInFolder();
                    trashNoteInFolder.setChildId(noteInFolder.getChildId());
                    trashNoteInFolder.setColor(noteInFolder.getColor());
                    trashNoteInFolder.setDateTime(noteInFolder.getDateTime());
                    trashNoteInFolder.setDateTimeEdit(noteInFolder.getDateTimeEdit());
                    trashNoteInFolder.setDateTimeRemind(noteInFolder.getDateTimeRemind());
                    trashNoteInFolder.setDelete(noteInFolder.isDelete());
                    trashNoteInFolder.setDrawPath(noteInFolder.getDrawPath());
                    trashNoteInFolder.setId(noteInFolder.getId());
                    trashNoteInFolder.setFontStyle(noteInFolder.getFontStyle());
                    trashNoteInFolder.setImagePath(noteInFolder.getImagePath());
                    trashNoteInFolder.setImgTag(noteInFolder.getImgTag());
                    trashNoteInFolder.setNoteColor(noteInFolder.getNoteColor());
                    trashNoteInFolder.setNoteText(noteInFolder.getNoteText());
                    trashNoteInFolder.setSubTitle(noteInFolder.getSubTitle());
                    trashNoteInFolder.setTextSize(noteInFolder.getTextSize());
                    trashNoteInFolder.setTitle(noteInFolder.getTitle());
                    trashNoteInFolder.setWebLink(noteInFolder.getWebLink());
                    Statistic statistic2 = new Statistic();

                    SimpleDateFormat sdf2 = new SimpleDateFormat("dd MM yyyy");
                    String date2 = sdf2.format(Calendar.getInstance().getTime());
                    statistic2.setDateText(date2);

                    statistic2.setTypeText(getString(R.string.note_in_folder_label));
                    statistic2.setItemSubText(noteInFolder.getSubTitle());
                    statistic2.setItemText(noteInFolder.getTitle());
                    statistic2.setActionText(getString(R.string.deleted_action));
                    StatisticDatabase.getStatisticDatabase(getContext().getApplicationContext())
                            .getStatisticDAO().insertStatistic(statistic2);
                    TrashNoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext())
                            .getNoteDAO().insertNote(trashNoteInFolder);
                }
            }
            NoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext())
                    .getNoteDAO().deleteMultiplyNotes(noteInFolders);
            NoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext())
                    .getNoteDAO().deleteMultipleFolders(foldersList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getNotes(DELETE_NOTES_CODE, true);
        }
    }
}