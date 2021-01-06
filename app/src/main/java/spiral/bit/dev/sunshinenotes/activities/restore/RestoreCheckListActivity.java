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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import spiral.bit.dev.sunshinenotes.activities.other.TrashActivity;
import spiral.bit.dev.sunshinenotes.adapter.trash.TrashTaskAdapter;
import spiral.bit.dev.sunshinenotes.data.CheckListDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashCheckListDatabase;
import spiral.bit.dev.sunshinenotes.databinding.ActivityRestoreCheckListBinding;
import spiral.bit.dev.sunshinenotes.fragments.NotesFragment;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.models.CheckList;
import spiral.bit.dev.sunshinenotes.models.Task;
import spiral.bit.dev.sunshinenotes.models.trash.TrashCheckList;
import spiral.bit.dev.sunshinenotes.models.trash.TrashTask;
import spiral.bit.dev.sunshinenotes.other.AdWorker;
import spiral.bit.dev.sunshinenotes.activities.other.BaseActivity;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_CHECK_LIST_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.SHOW_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_CHECK_LIST_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.hideKeyboard;

public class RestoreCheckListActivity extends AppCompatActivity {

    private EditText inputNoteTitle;
    private ActivityRestoreCheckListBinding restoreCheckListBinding;
    private TextView textDateTime, textWebUrlRef;
    private View viewSubTitleIndicator;
    private SharedPreferences prefTimesEdited, preferencesSettings, prefChoice;
    private SharedPreferences.Editor editorTimes;
    private ImageView imageNote;
    private String selectedImgPath = "", selectedNoteColor = "#333333";
    private LinearLayout layoutMisc;
    private AlertDialog dialogDeleteNote, dialogInfoNote;
    private TrashCheckList alreadyDeletedCheckList;
    private CheckList alreadyAvailableChecklist;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private InterstitialAd mInterstitialAd;
    private RecyclerView taskRecycler;
    private TrashTaskAdapter taskAdapter;
    private List<TrashTask> taskArrayList;
    private int clickedNotePosition = -1;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreCheckListBinding = ActivityRestoreCheckListBinding.inflate(getLayoutInflater());
        View view = restoreCheckListBinding.getRoot();
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
                WorkManager workManager = WorkManager.getInstance(RestoreCheckListActivity.this);
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
                WorkManager workManager = WorkManager.getInstance(RestoreCheckListActivity.this);
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

        //Vars
        layoutMisc = findViewById(R.id.layout_misc_restore);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);
        inputNoteTitle = restoreCheckListBinding.inputTaskTitle;
        textDateTime = restoreCheckListBinding.checkTextDateTime;
        viewSubTitleIndicator = restoreCheckListBinding.checkViewSubTitleIndicator;
        imageNote = restoreCheckListBinding.imageNote;
        final AdView mAdView = restoreCheckListBinding.banner;

        if (getIntent().hasExtra("setViewOrUpdate")) {
            alreadyDeletedCheckList = (TrashCheckList) getIntent().getSerializableExtra("trash_check_list");
            setViewOrUpdateCheckList();
        }

        taskArrayList = new ArrayList<>();
        taskRecycler = restoreCheckListBinding.checklistRecycler;
        taskRecycler.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TrashTaskAdapter(taskArrayList);
        taskRecycler.setAdapter(taskAdapter);
        taskRecycler.setHasFixedSize(true);


        textDateTime.setText(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));

        layoutMisc = findViewById(R.id.layout_misc_restore);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);

        if (getIntent().getBooleanExtra("setViewOrUpdate", false)) {
            alreadyAvailableChecklist = (CheckList) getIntent().getSerializableExtra("checklist");
            setViewOrUpdateCheckList();
        }

        if (alreadyDeletedCheckList != null) getTasks(SHOW_CODE, false);

        //Listeners
        restoreCheckListBinding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(RestoreCheckListActivity.this);
                onBackPressed();
            }
        });

        restoreCheckListBinding.imageInfoNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alreadyAvailableChecklist != null) {
                    showInfoNoteDialog(alreadyAvailableChecklist.getDateTime(),
                            alreadyAvailableChecklist.getDateTimeEdit(), prefTimesEdited.getInt("timesEdited", 0),
                                    alreadyAvailableChecklist.getTitle().length());
                } else {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(
                            RestoreCheckListActivity.this,
                            getString(R.string.info_img_error_toast),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false).show();
                }
            }
        });

        restoreCheckListBinding.imgRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.img_remove_image).setVisibility(View.GONE);
                selectedImgPath = "";
            }
        });

        MobileAds.initialize(RestoreCheckListActivity.this, new OnInitializationCompleteListener() {
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
                WorkManager workManager = WorkManager.getInstance(RestoreCheckListActivity.this);
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
                WorkManager workManager = WorkManager.getInstance(RestoreCheckListActivity.this);
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            }
        });

        if (SettingsFragment.getIsPurchased(RestoreCheckListActivity.this)) mAdView.setVisibility(View.GONE);

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
                    restoreCheckList();
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
            for (TrashTask trashTask : taskArrayList) {
                TrashCheckListDatabase.getNoteDatabase(getApplicationContext()).getTrashCheckListDAO()
                        .deleteTrashTask(trashTask);
            }
            TrashCheckListDatabase.getNoteDatabase(getApplicationContext())
                    .getTrashCheckListDAO().deleteTrashCheckList(alreadyDeletedCheckList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent(RestoreCheckListActivity.this, BaseActivity.class);
            intent.putExtra("isNoteDeleted", true);
            intent.putExtra("noteTitle", alreadyDeletedCheckList.getTitle());
            hideKeyboard(RestoreCheckListActivity.this);
            if (!SettingsFragment.getIsPurchased(RestoreCheckListActivity.this)) {
                if (mInterstitialAd.isLoaded() && !preferencesSettings.getBoolean("time_block_ads", false)) {
                    mInterstitialAd.show();
                }
            }
            hideKeyboard(RestoreCheckListActivity.this);
            startActivity(intent);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RestoreCheckListActivity.this, TrashActivity.class);
        intent.putExtra("isFromBackKey", true);
        startActivity(intent);
    }

    private void setViewOrUpdateCheckList() {
        inputNoteTitle.setText(alreadyDeletedCheckList.getTitle());
        textDateTime.setText(alreadyDeletedCheckList.getDateTime());
        editorTimes.putInt("timesEdited", prefTimesEdited.getInt("timesEdited", 0) + 1);
        editorTimes.apply();

        if (alreadyDeletedCheckList.getImagePath() != null && !alreadyDeletedCheckList.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyDeletedCheckList.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            restoreCheckListBinding.imgRemoveImage.setVisibility(View.VISIBLE);
            selectedImgPath = alreadyDeletedCheckList.getImagePath();
        }
    }

    private void restoreCheckList() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(
                    RestoreCheckListActivity.this,
                    getString(R.string.toast_error_title_empty),
                    FancyToast.LENGTH_LONG,
                    FancyToast.WARNING,
                    false).show();
            return;
        }

        final CheckList checkList = new CheckList();
        checkList.setTitle(inputNoteTitle.getText().toString());
        checkList.setDateTime(textDateTime.getText().toString());
        checkList.setCheckListColor(alreadyDeletedCheckList.getCheckListColor());
        checkList.setImagePath(selectedImgPath);

        if (alreadyDeletedCheckList != null) {
            checkList.setCheckListId(alreadyDeletedCheckList.getCheckListId());
            checkList.setDateTimeEdit(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                    .format(new Date()));
        }

        @SuppressLint("StaticFieldLeak")
        class SaveCheckListAsyncTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                CheckListDatabase.getCheckListDatabase(getApplicationContext()).getCheckDAO()
                        .insertCheckList(checkList);
                for (TrashTask trashTask : taskArrayList) {
                    Task task = new Task();
                    task.setCompleted(trashTask.isCompleted());
                    task.setDateTime(trashTask.getDateTime());
                    task.setId(trashTask.getId());
                    task.setParentId(trashTask.getParentId());
                    task.setTitle(trashTask.getTitle());
                    CheckListDatabase.getCheckListDatabase(getApplicationContext()).getCheckDAO()
                            .insertTask(task);
                    TrashCheckListDatabase.getNoteDatabase(getApplicationContext()).getTrashCheckListDAO()
                            .deleteTrashTask(trashTask);
                }
                TrashCheckListDatabase.getNoteDatabase(getApplicationContext()).getTrashCheckListDAO()
                        .deleteTrashCheckList(alreadyDeletedCheckList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideKeyboard(RestoreCheckListActivity.this);
                Intent intent = new Intent(RestoreCheckListActivity.this, BaseActivity.class);
                if (!SettingsFragment.getIsPurchased(RestoreCheckListActivity.this)) {
                    if (mInterstitialAd.isLoaded() && !preferencesSettings.getBoolean("time_block_ads", false)) {
                        mInterstitialAd.show();
                    }
                }
                hideKeyboard(RestoreCheckListActivity.this);
                startActivity(intent);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveCheckListAsyncTask().execute();
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
                new RestoreCheckListActivity.DeleteCheckListAsyncTask().execute();
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

    private void getTasks(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetAllNotesAsyncTask extends AsyncTask<Void, Void, List<TrashTask>> {
            @Override
            protected List<TrashTask> doInBackground(Void... voids) {
                return TrashCheckListDatabase.getNoteDatabase(getApplicationContext())
                        .getTrashCheckListDAO().getAllTrashTasks(alreadyDeletedCheckList.getCheckListId());
            }

            @Override
            protected void onPostExecute(List<TrashTask> checkList) {
                super.onPostExecute(checkList);
                if (requestCode == SHOW_CODE) {
                    taskArrayList.addAll(checkList);
                    taskAdapter.notifyDataSetChanged();
                } else if (requestCode == ADD_CHECK_LIST_CODE) {
                    taskArrayList.add(0, checkList.get(0));
                    taskAdapter.notifyItemInserted(0);
                    taskRecycler.smoothScrollToPosition(0);
                } else if (requestCode == UPDATE_CHECK_LIST_CODE) {
                    taskArrayList.remove(clickedNotePosition);
                    if (isNoteDeleted) {
                        taskAdapter.notifyItemRemoved(clickedNotePosition);
                    } else {
                        taskArrayList.add(clickedNotePosition, checkList.get(clickedNotePosition));
                        taskAdapter.notifyItemChanged(clickedNotePosition);
                    }
                }
            }
        }
        new GetAllNotesAsyncTask().execute();
    }
}
