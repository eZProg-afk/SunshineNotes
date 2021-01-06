package spiral.bit.dev.sunshinenotes.activities.restore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.shashank.sony.fancytoastlib.FancyToast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.other.BaseActivity;
import spiral.bit.dev.sunshinenotes.data.NoteInFolderDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashNoteInFolderDatabase;
import spiral.bit.dev.sunshinenotes.databinding.ActivityRestoreFolderBinding;
import spiral.bit.dev.sunshinenotes.fragments.NotesFragment;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.NoteInFolder;
import spiral.bit.dev.sunshinenotes.models.trash.TrashFolder;
import spiral.bit.dev.sunshinenotes.models.trash.TrashNoteInFolder;
import spiral.bit.dev.sunshinenotes.other.AdWorker;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_CHECK_LIST_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.SHOW_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.hideKeyboard;

public class RestoreFolderActivity extends AppCompatActivity {

    private EditText inputNoteTitle;
    private ActivityRestoreFolderBinding restoreFolderBinding;
    private TextView textDateTime;
    private View viewSubTitleIndicator;
    private SharedPreferences prefTimesEdited, preferencesSettings, prefChoice;
    private SharedPreferences.Editor editorTimes;
    private ImageView imageNote;
    private String selectedImgPath = "", selectedNoteColor = "#333333";
    private LinearLayout layoutMisc;
    private AlertDialog dialogDeleteNote, dialogInfoNote;
    private TrashFolder alreadyDeletedFolder;
    private Folder alreadyAvailableFolder;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private InterstitialAd mInterstitialAd;
    private List<TrashNoteInFolder> taskArrayList;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreFolderBinding = ActivityRestoreFolderBinding.inflate(getLayoutInflater());
        View view = restoreFolderBinding.getRoot();
        setContentView(view);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.admob_interstitial_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
            }

            @Override
            public void onAdOpened() {
                SharedPreferences.Editor editorPrefSettings = preferencesSettings.edit();
                editorPrefSettings.putBoolean("time_block_ads", true);
                editorPrefSettings.apply();
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AdWorker.class)
                        .setInitialDelay(15, TimeUnit.MINUTES).build();
                WorkManager workManager = WorkManager.getInstance(RestoreFolderActivity.this);
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdClicked() {
                //Dis ads
                SharedPreferences.Editor editorPrefSettings = preferencesSettings.edit();
                editorPrefSettings.putBoolean("time_block_ads", true);
                editorPrefSettings.apply();
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AdWorker.class)
                        .setInitialDelay(15, TimeUnit.MINUTES).build();
                WorkManager workManager = WorkManager.getInstance(RestoreFolderActivity.this);
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            }
        });


        //prefs
        prefTimesEdited = getSharedPreferences("timesEditedPref", 0);
        preferencesSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefChoice = getSharedPreferences("choice", 0);
        editorTimes = prefTimesEdited.edit();

        taskArrayList = new ArrayList<>();

        //Vars
        layoutMisc = findViewById(R.id.layout_misc_restore);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);
        inputNoteTitle = restoreFolderBinding.inputFolderTitle;
        textDateTime = restoreFolderBinding.folderDateTime;
        viewSubTitleIndicator = restoreFolderBinding.folderSubTitleIndicator;
        imageNote = restoreFolderBinding.imageFolder;
        final AdView mAdView = restoreFolderBinding.banner;

        if (getIntent().hasExtra("setViewOrUpdate")) {
            alreadyDeletedFolder = (TrashFolder) getIntent().getSerializableExtra("trash_folder");
            setViewOrUpdateCheckList();
        }

        getTasks(SHOW_CODE);

        textDateTime.setText(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));

        layoutMisc = findViewById(R.id.layout_misc_restore);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);

        if (getIntent().getBooleanExtra("setViewOrUpdate", false)) {
            alreadyAvailableFolder = (Folder) getIntent().getSerializableExtra("folder");
            setViewOrUpdateCheckList();
        }

        //Listeners
        restoreFolderBinding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(RestoreFolderActivity.this);
                onBackPressed();
            }
        });

        restoreFolderBinding.imageInfoNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alreadyAvailableFolder != null) {
                    showInfoNoteDialog(alreadyAvailableFolder.getDateTime(),
                            "0",
                            0,
                            alreadyAvailableFolder.getName().length());
                } else {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(
                            RestoreFolderActivity.this,
                            getString(R.string.info_img_error_toast),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false).show();
                }
            }
        });

        restoreFolderBinding.imgRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.img_remove_image).setVisibility(View.GONE);
                selectedImgPath = "";
            }
        });

        MobileAds.initialize(RestoreFolderActivity.this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                if (!preferencesSettings.getBoolean("time_block_ads", false)) {
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
                SharedPreferences.Editor editorPrefSettings = preferencesSettings.edit();
                editorPrefSettings.putBoolean("time_block_ads", true);
                editorPrefSettings.apply();
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AdWorker.class)
                        .setInitialDelay(15, TimeUnit.MINUTES).build();
                WorkManager workManager = WorkManager.getInstance(RestoreFolderActivity.this);
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdClicked() {
                //Dis ads
                SharedPreferences.Editor editorPrefSettings = preferencesSettings.edit();
                editorPrefSettings.putBoolean("time_block_ads", true);
                editorPrefSettings.apply();
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AdWorker.class)
                        .setInitialDelay(15, TimeUnit.MINUTES).build();
                WorkManager workManager = WorkManager.getInstance(RestoreFolderActivity.this);
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            }
        });

        if (SettingsFragment.getIsPurchased(RestoreFolderActivity.this))
            mAdView.setVisibility(View.GONE);

        initMisc();
        setSubTitleIndicator();
    }

    private void initMisc() {
        layoutMisc.findViewById(R.id.layout_delete_note_forever).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteNoteDialog();
            }
        });
        layoutMisc.findViewById(R.id.layout_restore_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restoreFolder();
            }
        });
    }

    private void showInfoNoteDialog(String dateTimeCreated, String dateTimeEdited, int timesEdited, int lengthOfCymbals) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.layout_info_about_note,
                        (ViewGroup) findViewById(R.id.layout_info_about_note_container));
        builder.setView(view);
        dialogInfoNote = builder.create();
        if (dialogInfoNote.getWindow() != null) {
            dialogInfoNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        TextView created = findViewById(R.id.date_note_created);
        TextView edited = findViewById(R.id.date_note_edit);
        TextView times = findViewById(R.id.date_times_edit);
        TextView cymbalsLength = view.findViewById(R.id.cymbals_in_note);
        created.setText(dateTimeCreated);
        if (dateTimeEdited != null) {
            if (!dateTimeEdited.isEmpty()) {
                edited.setText(dateTimeEdited);
            }
        } else edited.setText(getString(R.string.this_note_not_has_been_edited));
        times.setText(String.valueOf(timesEdited));
        cymbalsLength.setText(String.valueOf(lengthOfCymbals));
        view.findViewById(R.id.text_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogInfoNote.dismiss();
            }
        });
        dialogInfoNote.show();
    }

    private void setSubTitleIndicator() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubTitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }


    @SuppressLint("StaticFieldLeak")
    class DeleteCheckListAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            for (TrashNoteInFolder trashNoteInFolder : taskArrayList) {
                TrashNoteInFolderDatabase.getNoteDatabase(getApplicationContext()).getNoteDAO()
                        .deleteNote(trashNoteInFolder);
            }
            TrashNoteInFolderDatabase.getNoteDatabase(getApplicationContext()).getNoteDAO()
                    .deleteFolder(alreadyDeletedFolder);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent(RestoreFolderActivity.this, BaseActivity.class);
            intent.putExtra("isNoteDeleted", true);
            intent.putExtra("noteTitle", alreadyDeletedFolder.getName());
            hideKeyboard(RestoreFolderActivity.this);
            if (!SettingsFragment.getIsPurchased(RestoreFolderActivity.this)) {
                if (mInterstitialAd.isLoaded() && !preferencesSettings.getBoolean("time_block_ads", false)) {
                    mInterstitialAd.show();
                }
            }
            hideKeyboard(RestoreFolderActivity.this);
            startActivity(intent);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RestoreFolderActivity.this, BaseActivity.class);
        intent.putExtra("isFromBackKey", true);
        startActivity(intent);
    }

    private void setViewOrUpdateCheckList() {
        inputNoteTitle.setText(alreadyDeletedFolder.getName());
        textDateTime.setText(alreadyDeletedFolder.getDateTime());
        editorTimes.putInt("timesEdited", prefTimesEdited.getInt("timesEdited", 0) + 1);
        editorTimes.apply();

        if (alreadyDeletedFolder.getImagePath() != null && !alreadyDeletedFolder.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyDeletedFolder.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            restoreFolderBinding.imgRemoveImage.setVisibility(View.VISIBLE);
            selectedImgPath = alreadyDeletedFolder.getImagePath();
        }
    }

    private void restoreFolder() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(
                    RestoreFolderActivity.this,
                    getString(R.string.toast_error_title_empty),
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false).show();
            return;
        }

        final Folder folder = new Folder();
        folder.setName(inputNoteTitle.getText().toString());
        folder.setDateTime(textDateTime.getText().toString());
        folder.setColor(alreadyDeletedFolder.getColor());
        folder.setImagePath(selectedImgPath);
        folder.setSubTitle(alreadyDeletedFolder.getSubTitle());

        if (alreadyDeletedFolder != null) {
            folder.setId(alreadyDeletedFolder.getId());
//            folder.setDateTimeEdit(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
//                    .format(new Date()));
        }

        @SuppressLint("StaticFieldLeak")
        class SaveFolderAsyncTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                NoteInFolderDatabase.getNoteDatabase(getApplicationContext()).getNoteDAO()
                        .insertFolder(folder);
                for (TrashNoteInFolder trashNoteInFolder : taskArrayList) {
                    NoteInFolder noteInFolder = new NoteInFolder();
                    noteInFolder.setChildId(trashNoteInFolder.getChildId());
                    noteInFolder.setColor(trashNoteInFolder.getColor());
                    noteInFolder.setDateTime(trashNoteInFolder.getDateTime());
                    noteInFolder.setDateTimeEdit(trashNoteInFolder.getDateTimeEdit());
                    noteInFolder.setDateTimeRemind(trashNoteInFolder.getDateTimeRemind());
                    noteInFolder.setDelete(trashNoteInFolder.isDelete());
                    noteInFolder.setDrawPath(trashNoteInFolder.getDrawPath());
                    noteInFolder.setFontStyle(trashNoteInFolder.getFontStyle());
                    noteInFolder.setId(trashNoteInFolder.getId());
                    noteInFolder.setImagePath(trashNoteInFolder.getImagePath());
                    noteInFolder.setImgTag(trashNoteInFolder.getImgTag());
                    noteInFolder.setNoteColor(trashNoteInFolder.getNoteColor());
                    noteInFolder.setNoteText(trashNoteInFolder.getNoteText());
                    noteInFolder.setSubTitle(trashNoteInFolder.getSubTitle());
                    noteInFolder.setTextSize(trashNoteInFolder.getTextSize());
                    noteInFolder.setTitle(trashNoteInFolder.getTitle());
                    noteInFolder.setWebLink(trashNoteInFolder.getWebLink());
                    NoteInFolderDatabase.getNoteDatabase(getApplicationContext())
                            .getNoteDAO().insertNote(noteInFolder);
                    TrashNoteInFolderDatabase.getNoteDatabase(getApplicationContext()).
                            getNoteDAO().deleteNote(trashNoteInFolder);
                }
                TrashNoteInFolderDatabase.getNoteDatabase(getApplicationContext()).
                        getNoteDAO().deleteFolder(alreadyDeletedFolder);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideKeyboard(RestoreFolderActivity.this);
                Intent intent = new Intent(RestoreFolderActivity.this, BaseActivity.class);
                if (!SettingsFragment.getIsPurchased(RestoreFolderActivity.this)) {
                    if (mInterstitialAd.isLoaded() && !preferencesSettings.getBoolean("time_block_ads", false)) {
                        mInterstitialAd.show();
                    }
                }
                hideKeyboard(RestoreFolderActivity.this);
                startActivity(intent);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveFolderAsyncTask().execute();
    }

    private void showDeleteNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.layout_delete_note,
                        (ViewGroup) findViewById(R.id.layout_delete_note_container));
        builder.setView(view);
        dialogDeleteNote = builder.create();
        if (dialogDeleteNote.getWindow() != null) {
            dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        view.findViewById(R.id.text_delete_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RestoreFolderActivity.DeleteCheckListAsyncTask().execute();
            }
        });
        view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogDeleteNote.dismiss();
            }
        });
        dialogDeleteNote.show();
    }

    private void getTasks(final int requestCode) {
        @SuppressLint("StaticFieldLeak")
        class GetAllNotesAsyncTask extends AsyncTask<Void, Void, List<TrashNoteInFolder>> {
            @Override
            protected List<TrashNoteInFolder> doInBackground(Void... voids) {
                return TrashNoteInFolderDatabase.getNoteDatabase(getApplicationContext())
                        .getNoteDAO().getAllNotesInFolder(alreadyDeletedFolder.getId());
            }

            @Override
            protected void onPostExecute(List<TrashNoteInFolder> trashNoteInFolders) {
                super.onPostExecute(trashNoteInFolders);
                if (requestCode == SHOW_CODE) {
                    taskArrayList.addAll(trashNoteInFolders);
                } else if (requestCode == ADD_CHECK_LIST_CODE) {
                    taskArrayList.add(0, trashNoteInFolders.get(0));
                }
            }
        }
        new GetAllNotesAsyncTask().execute();
    }
}
