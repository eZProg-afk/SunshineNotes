package spiral.bit.dev.sunshinenotes.activities.create;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.BaseActivity;
import spiral.bit.dev.sunshinenotes.adapter.CheckAdapter;
import spiral.bit.dev.sunshinenotes.data.CheckListDatabase;
import spiral.bit.dev.sunshinenotes.fragments.CheckListFragment;
import spiral.bit.dev.sunshinenotes.fragments.SettingsFragment;
import spiral.bit.dev.sunshinenotes.models.CheckList;
import spiral.bit.dev.sunshinenotes.models.Task;
import spiral.bit.dev.sunshinenotes.other.AlarmReceiver;

import static spiral.bit.dev.sunshinenotes.fragments.NotesFragment.REQUEST_CODE_ENABLE;
import static spiral.bit.dev.sunshinenotes.fragments.NotesFragment.hideKeyboard;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_CHECK_LIST_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_CHECK_LIST_CODE;

public class CreateCheckListActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH = 125;
    private static final int PERMISSION_STORAGE_CODE = 11;
    public static final int PERMISSION_RECORD_CODE = 111;
    private static final int CODE_SELECT_IMG = 12;
    public static final int SHOW_NOTES_CODE = 14;

    private EditText inputTaskTitle, inputCheckListTitle;
    private TextView textDateTime;
    private View viewSubTitleIndicator;
    private SharedPreferences prefTimesEdited, preferencesSettings, prefChoice;
    private SharedPreferences.Editor editorTimes, editorChoice;
    private ImageView imageNote;
    private String selectedImgPath = "",
            selectedNoteColor = "#333333";
    private LinearLayout layoutMisc;
    private AlertDialog dialogRefUrl, dialogDeleteNote, dialogExitSave,
            dialogInfoNote, dialogRemind, dialogAddDraw, shareDialog;
    private CheckList alreadyAvailableCheckList;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private InterstitialAd mInterstitialAd;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private TimePicker timePicker;
    private ArrayList<Task> tasks;
    private RecyclerView recyclerCheckItems;
    private CheckAdapter adapter;
    private final int clickedNotePosition = -1;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_check_list);

        tasks = new ArrayList<>();
        adapter = new CheckAdapter(tasks);
        prefTimesEdited = getSharedPreferences("timesEditedPref", 0);
        preferencesSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefChoice = getSharedPreferences("choice", 0);
        editorChoice = prefChoice.edit();
        editorTimes = prefTimesEdited.edit();
        ImageView imageBack = findViewById(R.id.image_back);
        inputTaskTitle = findViewById(R.id.input_item_title);
        inputCheckListTitle = findViewById(R.id.input_task_title);
        textDateTime = findViewById(R.id.check_text_date_time);
        viewSubTitleIndicator = findViewById(R.id.check_view_sub_title_indicator);
        ImageView imgInfo = findViewById(R.id.image_info_note);
        ImageView imgShare = findViewById(R.id.image_share);
        imageNote = findViewById(R.id.check_list_image);
        textDateTime.setText(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));

        layoutMisc = findViewById(R.id.layout_miscellaneous);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);
        recyclerCheckItems = findViewById(R.id.checklist_recycler);
        recyclerCheckItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerCheckItems.setAdapter(adapter);
        recyclerCheckItems.setHasFixedSize(true);


        if (getIntent().getBooleanExtra("setViewOrUpdate", false)) {
            alreadyAvailableCheckList = (CheckList) getIntent().getSerializableExtra("checklist");
            setViewOrUpdateNote();
        }

        if (alreadyAvailableCheckList != null) getTasks(SHOW_NOTES_CODE, false);

        AdView mAdView = findViewById(R.id.banner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if (SettingsFragment.getIsPurchased(CreateCheckListActivity.this)) {
            mAdView.setVisibility(View.GONE);
        }

        ImageView addItemBtn = findViewById(R.id.add_item_btn);
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alreadyAvailableCheckList != null) {
                    saveCheck();
                    inputTaskTitle.setText("");
                } else {
                    Toast.makeText(CreateCheckListActivity.this, "Cначала сохраните чек-лист!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(CreateCheckListActivity.this);
                onBackPressed();
            }
        });

        imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alreadyAvailableCheckList != null) {
                    if (imageNote.getVisibility() == View.VISIBLE) {
                        shareDialog();
                    } else {
                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableCheckList.getTitle());
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableCheckList.getTitle());
                        startActivity(Intent.createChooser(intent, getString(R.string.share_label)));
                    }
                } else {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                            CreateCheckListActivity.this, getString(R.string.error_toast),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView imgSave = findViewById(R.id.image_save);
        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SettingsFragment.getIsPurchased(CreateCheckListActivity.this))
                    if (mInterstitialAd.isLoaded()) mInterstitialAd.show();
                hideKeyboard(CreateCheckListActivity.this);
                saveCheckList();
            }
        });

        imgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alreadyAvailableCheckList != null) {
                    showInfoNoteDialog(alreadyAvailableCheckList.getDateTime(),
                            alreadyAvailableCheckList.getDateTimeEdit(), prefTimesEdited.getInt("timesEdited", 0),
                            alreadyAvailableCheckList.getTitle().length());
                } else {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                            CreateCheckListActivity.this, getString(R.string.info_img_error_toast),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.img_remove_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                view.findViewById(R.id.img_remove_image).setVisibility(View.GONE);
                selectedImgPath = "";
            }
        });

        initMisc();
        setSubTitleIndicator();
    }

    private void saveCheck() {
        if (inputTaskTitle.getText().toString().trim().isEmpty()) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(this,
                    getString(R.string.toast_error_title_empty),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final Task task = new Task();
        task.setTitle(inputTaskTitle.getText().toString());
        task.setDateTime(textDateTime.getText().toString());
        task.setParentId(alreadyAvailableCheckList.getCheckListId());
        task.setCompleted(alreadyAvailableCheckList.isCompleted());

        @SuppressLint("StaticFieldLeak")
        class SaveNoteAsyncTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                CheckListDatabase.getCheckListDatabase(getApplicationContext())
                        .getCheckDAO().insertTask(task);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                getTasks(SHOW_NOTES_CODE, false);
            }
        }

        new SaveNoteAsyncTask().execute();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ADD_CHECK_LIST_CODE) {
            getTasks(ADD_CHECK_LIST_CODE, false);
        } else if (resultCode == RESULT_OK && requestCode == UPDATE_CHECK_LIST_CODE) {
            if (data != null) {
                getTasks(UPDATE_CHECK_LIST_CODE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if (requestCode == REQUEST_CODE_ENABLE && resultCode == RESULT_OK) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                    this, "Пин-код успешно задан!",
                    Toast.LENGTH_SHORT).show();
        } else if (resultCode == RESULT_OK && requestCode == CODE_SELECT_IMG) {
            if (data != null) {
                Uri selectedImgUri = data.getData();
                if (selectedImgUri != null) {
                    try {
                        InputStream is = getContentResolver().openInputStream(selectedImgUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.img_remove_image).setVisibility(View.VISIBLE);
                        selectedImgPath = getPathFromUri(selectedImgUri);
                    } catch (Exception e) {
                        if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                                this, getString(R.string.error_add_img_toast),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SPEECH) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            StringBuilder builder = new StringBuilder();
            for (String item : result) {
                String finalItem = item.replace("[]", " ");
                builder.append(finalItem);
            }
            inputTaskTitle.setText(inputTaskTitle.getText() + " " + builder.toString());
        }
    }

    private void getTasks(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetAllNotesAsyncTask extends AsyncTask<Void, Void, List<Task>> {
            @Override
            protected List<Task> doInBackground(Void... voids) {
                return CheckListDatabase.getCheckListDatabase(getApplicationContext())
                        .getCheckDAO().getAllTasks(alreadyAvailableCheckList.getCheckListId());
            }

            @Override
            protected void onPostExecute(List<Task> checkList) {
                super.onPostExecute(checkList);
                if (requestCode == SHOW_NOTES_CODE) {
                    tasks.addAll(checkList);
                    adapter.notifyDataSetChanged();
                } else if (requestCode == ADD_CHECK_LIST_CODE) {
                    tasks.add(0, checkList.get(0));
                    adapter.notifyItemInserted(0);
                    recyclerCheckItems.smoothScrollToPosition(0);
                } else if (requestCode == UPDATE_CHECK_LIST_CODE) {
                    tasks.remove(clickedNotePosition);
                    if (isNoteDeleted) {
                        adapter.notifyItemRemoved(clickedNotePosition);
                    } else {
                        tasks.add(clickedNotePosition, checkList.get(clickedNotePosition));
                        adapter.notifyItemChanged(clickedNotePosition);
                    }
                }
            }
        }
        new GetAllNotesAsyncTask().execute();
    }

    private void showInfoNoteDialog(String dateTimeCreated, String dateTimeEdited,
                                    int timesEdited, int lengthOfCymbals) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.layout_info_about_note,
                        (ViewGroup) findViewById(R.id.layout_info_about_note_container));
        builder.setView(view);
        dialogInfoNote = builder.create();
        if (dialogInfoNote.getWindow() != null) {
            dialogInfoNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        TextView created = view.findViewById(R.id.date_note_created);
        TextView edited = view.findViewById(R.id.date_note_edit);
        TextView times = view.findViewById(R.id.date_times_edit);
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

    private void setViewOrUpdateNote() {
        inputCheckListTitle.setText(alreadyAvailableCheckList.getTitle());
        textDateTime.setText(alreadyAvailableCheckList.getDateTime());
        editorTimes.putInt("timesEdited", prefTimesEdited.getInt("timesEdited", 0) + 1);
        editorTimes.apply();

        if (alreadyAvailableCheckList.getImagePath() != null && !alreadyAvailableCheckList.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableCheckList.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.img_remove_image).setVisibility(View.VISIBLE);
            selectedImgPath = alreadyAvailableCheckList.getImagePath();
        }
    }

    private void saveCheckList() {
        if (inputCheckListTitle.getText().toString().trim().isEmpty()) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(this,
                    getString(R.string.toast_error_title_empty),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final CheckList checkList = new CheckList();
        checkList.setTitle(inputCheckListTitle.getText().toString());
        checkList.setDateTime(textDateTime.getText().toString());
        checkList.setCheckListColor(selectedNoteColor);
        checkList.setImagePath(selectedImgPath);

        if (alreadyAvailableCheckList != null) {
            checkList.setCheckListId(alreadyAvailableCheckList.getCheckListId());
            checkList.setDateTimeEdit(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                    .format(new Date()));
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteAsyncTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                CheckListDatabase
                        .getCheckListDatabase(getApplicationContext())
                        .getCheckDAO().insertCheckList(checkList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideKeyboard(CreateCheckListActivity.this);
                CheckListFragment checkListFragment = new CheckListFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.replaced_container, checkListFragment)
                        .commit();
            }
        }
        new SaveNoteAsyncTask().execute();
    }

    private void initMisc() {
        final ImageView attach = findViewById(R.id.attach);
        layoutMisc.findViewById(R.id.attach
        ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    attach.setImageResource(R.drawable.ic_attach);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    attach.setImageResource(R.drawable.ic_attachment);
                }
            }
        });

        final ImageView imageColor1 = layoutMisc.findViewById(R.id.image_color_1);
        final ImageView imageColor2 = layoutMisc.findViewById(R.id.image_color_2);
        final ImageView imageColor3 = layoutMisc.findViewById(R.id.image_color_3);
        final ImageView imageColor4 = layoutMisc.findViewById(R.id.image_color_4);
        final ImageView imageColor5 = layoutMisc.findViewById(R.id.image_color_5);
        final ImageView imageColor6 = layoutMisc.findViewById(R.id.image_color_6);
        final ImageView imageColor7 = layoutMisc.findViewById(R.id.image_color_7);
        final ImageView imageColor8 = layoutMisc.findViewById(R.id.image_color_8);
        final ImageView imageColor9 = layoutMisc.findViewById(R.id.image_color_9);
        final ImageView imageColor10 = layoutMisc.findViewById(R.id.image_color_10);

        layoutMisc.findViewById(R.id.view_color_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#333333";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                imageColor7.setImageResource(0);
                imageColor8.setImageResource(0);
                imageColor9.setImageResource(0);
                imageColor10.setImageResource(0);
                setSubTitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.view_color_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#FDBE3B";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                imageColor7.setImageResource(0);
                imageColor8.setImageResource(0);
                imageColor9.setImageResource(0);
                imageColor10.setImageResource(0);
                setSubTitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.view_color_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#FF4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                imageColor7.setImageResource(0);
                imageColor8.setImageResource(0);
                imageColor9.setImageResource(0);
                imageColor10.setImageResource(0);
                setSubTitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.view_color_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#3A52FC";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                imageColor7.setImageResource(0);
                imageColor8.setImageResource(0);
                imageColor9.setImageResource(0);
                imageColor10.setImageResource(0);
                setSubTitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.view_color_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                imageColor6.setImageResource(0);
                imageColor7.setImageResource(0);
                imageColor8.setImageResource(0);
                imageColor9.setImageResource(0);
                imageColor10.setImageResource(0);
                setSubTitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.view_color_6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#00FF00";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(R.drawable.ic_done);
                imageColor7.setImageResource(0);
                imageColor8.setImageResource(0);
                imageColor9.setImageResource(0);
                imageColor10.setImageResource(0);
                setSubTitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.view_color_7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#551A8B";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                imageColor7.setImageResource(R.drawable.ic_done);
                imageColor8.setImageResource(0);
                imageColor9.setImageResource(0);
                imageColor10.setImageResource(0);
                setSubTitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.view_color_8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#006400";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                imageColor7.setImageResource(0);
                imageColor8.setImageResource(R.drawable.ic_done);
                imageColor9.setImageResource(0);
                imageColor10.setImageResource(0);
                setSubTitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.view_color_9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#00FFFF";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                imageColor7.setImageResource(0);
                imageColor8.setImageResource(0);
                imageColor9.setImageResource(R.drawable.ic_done);
                imageColor10.setImageResource(0);
                setSubTitleIndicator();
            }
        });

        layoutMisc.findViewById(R.id.view_color_10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#FFA500";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                imageColor6.setImageResource(0);
                imageColor7.setImageResource(0);
                imageColor8.setImageResource(0);
                imageColor9.setImageResource(0);
                imageColor10.setImageResource(R.drawable.ic_done);
                setSubTitleIndicator();
            }
        });

        if (alreadyAvailableCheckList != null && alreadyAvailableCheckList.getCheckListColor() != null
                && !alreadyAvailableCheckList.getCheckListColor().trim().isEmpty()) {
            switch (alreadyAvailableCheckList.getCheckListColor()) {
                case "#FDBE3B":
                    layoutMisc.findViewById(R.id.view_color_2).performClick();
                    break;
                case "#FF4842":
                    layoutMisc.findViewById(R.id.view_color_3).performClick();
                    break;
                case "#3A52FC":
                    layoutMisc.findViewById(R.id.view_color_4).performClick();
                    break;
                case "#000000":
                    layoutMisc.findViewById(R.id.view_color_5).performClick();
                    break;
                case "#00FF00":
                    layoutMisc.findViewById(R.id.view_color_6).performClick();
                    break;
                case "#551A8B":
                    layoutMisc.findViewById(R.id.view_color_7).performClick();
                    break;
                case "#006400":
                    layoutMisc.findViewById(R.id.view_color_8).performClick();
                    break;
                case "#00FFFF":
                    layoutMisc.findViewById(R.id.view_color_9).performClick();
                    break;
                case "#FFA500":
                    layoutMisc.findViewById(R.id.view_color_10).performClick();
                    break;
            }
        }

        layoutMisc.findViewById(R.id.layout_add_note_voice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateCheckListActivity.this, new String[]
                                    {Manifest.permission.RECORD_AUDIO},
                            PERMISSION_RECORD_CODE);
                } else {
                    enterVoice();
                }
            }
        });

        layoutMisc.findViewById(R.id.layout_add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateCheckListActivity.this, new String[]
                                    {Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_STORAGE_CODE);
                } else {
                    selectImg();
                }
            }
        });

        if (alreadyAvailableCheckList != null) {
            layoutMisc.findViewById(R.id.layout_add_reminder).setVisibility(View.VISIBLE);
            layoutMisc.findViewById(R.id.layout_add_reminder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    openRemindDialog();
                }
            });
        }

        if (alreadyAvailableCheckList != null) {
            layoutMisc.findViewById(R.id.layout_delete_note).setVisibility(View.VISIBLE);
            layoutMisc.findViewById(R.id.layout_delete_note).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }
    }

    private void enterVoice() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.add_voice_speak_label));
        startActivityForResult(intent, REQUEST_CODE_SPEECH);
    }

    private void openRemindDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_picker,
                        (ViewGroup) findViewById(R.id.layout_dialog_picker_container));
        builder.setView(view);
        dialogRemind = builder.create();
        if (dialogRemind.getWindow() != null) {
            dialogRemind.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        timePicker = view.findViewById(R.id.timePicker);
        TextView btnOk = view.findViewById(R.id.btn_ok);
        view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogRemind.dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                final Calendar c = Calendar.getInstance();
                Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
                intent.putExtra("nameOfNote", alreadyAvailableCheckList.getTitle());
                c.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                c.set(Calendar.MINUTE, timePicker.getMinute());
                pendingIntent = PendingIntent.getBroadcast(CreateCheckListActivity.this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                dialogRemind.dismiss();
            }
        });
        dialogRemind.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (preferencesSettings.getBoolean("save_note_on_exit", false)) {
                event.startTracking();
                saveCheckList();
                return true;
            } else {
                event.startTracking();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            if (preferencesSettings.getBoolean("save_note_on_exit", false)) {
                event.startTracking();
                saveCheckList();
                return true;
            } else {
                event.startTracking();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (preferencesSettings.getBoolean("clear_settings", false)) {
            SharedPreferences.Editor editor = preferencesSettings.edit();
            editor.putBoolean("clear_settings", false);
            editor.apply();
            openQuitDialog();
        } else {
            if (prefChoice.getBoolean("choice_is_check", false)) {
                String save = prefChoice.getString("choice_is_save", "");
                if (save.equals("save")) {
                    saveCheckList();
                    Intent intent = new Intent(CreateCheckListActivity.this, CheckListFragment.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CreateCheckListActivity.this, CheckListFragment.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                }
            } else {
                openQuitDialog();
            }
        }
    }

    private void openQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.layout_exit_save_note,
                        (ViewGroup) findViewById(R.id.layout_exit_save_note_container));
        builder.setView(view);
        dialogExitSave = builder.create();
        if (dialogExitSave.getWindow() != null) {
            dialogExitSave.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        final CheckBox saveChoice = view.findViewById(R.id.save_choice).findViewById(R.id.choice_save);
        saveChoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    editorChoice.putBoolean("choice_is_check", true);
                    editorChoice.apply();
                }
            }
        });
        view.findViewById(R.id.text_save_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCheckList();
                if (saveChoice.isChecked()) {
                    editorChoice.putString("choice_is_save", "save");
                    editorChoice.apply();
                    Intent intent = new Intent(CreateCheckListActivity.this, CheckListFragment.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                } else {
                    editorChoice.putString("choice_is_save", "");
                    editorChoice.apply();
                    Intent intent = new Intent(CreateCheckListActivity.this, CheckListFragment.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                }
            }
        });
        view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogExitSave.dismiss();
                Intent intent = new Intent(CreateCheckListActivity.this, CheckListFragment.class);
                intent.putExtra("isFromBackKey", true);
                startActivity(intent);
            }
        });
        dialogExitSave.show();
    }

    private void shareDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.layout_share_note,
                        (ViewGroup) findViewById(R.id.layout_share_note_container));
        builder.setView(view);
        shareDialog = builder.create();
        if (shareDialog.getWindow() != null) {
            shareDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        view.findViewById(R.id.text_with_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageNote.getVisibility() == View.VISIBLE) {
                    imageNote.buildDrawingCache();
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableCheckList.getTitle());
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableCheckList.getTitle());
                    Bitmap bitmap = ((BitmapDrawable) imageNote.getDrawable()).getBitmap();
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(),
                            bitmap, "image", null);
                    Uri imageDrawUri = Uri.parse(path);
                    intent.putExtra(Intent.EXTRA_STREAM, imageDrawUri);
                    shareDialog.dismiss();
                    startActivity(Intent.createChooser(intent, getString(R.string.share_label)));
                } else {
                    Toast.makeText(CreateCheckListActivity.this, getString(R.string.no_img_in_note_share_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
        view.findViewById(R.id.text_with_draw).setVisibility(View.GONE);
        view.findViewById(R.id.text_only_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableCheckList.getTitle());
                intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableCheckList.getTitle());
                shareDialog.dismiss();
                startActivity(Intent.createChooser(intent, getString(R.string.share_label)));
            }
        });
        view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareDialog.dismiss();
            }
        });
        shareDialog.show();
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
                new CreateCheckListActivity.DeleteNoteAsyncTask().execute();
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

    private void setSubTitleIndicator() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubTitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void selectImg() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CODE_SELECT_IMG);
        }
    }

    public String getPathFromUri(Uri uri) {
        String pathToFile;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            pathToFile = uri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            pathToFile = cursor.getString(index);
            cursor.close();
        }
        return pathToFile;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_STORAGE_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImg();
            } else {
                if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                        this, getString(R.string.error_toast_perm_denied),
                        Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PERMISSION_RECORD_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enterVoice();
            } else {
                if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                        this, getString(R.string.toast_error_record_denied),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class DeleteNoteAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            CheckListDatabase.getCheckListDatabase(getApplicationContext())
                    .getCheckDAO().deleteCheckById(alreadyAvailableCheckList.getCheckListId());
            CheckListDatabase.getCheckListDatabase(getApplicationContext())
                    .getCheckDAO().deleteCheckListById(alreadyAvailableCheckList.getCheckListId());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideKeyboard(CreateCheckListActivity.this);
            Intent intent = new Intent(CreateCheckListActivity.this, BaseActivity.class);
            intent.putExtra("isNoteDeleted", true);
            intent.putExtra("noteTitle", alreadyAvailableCheckList.getTitle());
            startActivityForResult(intent, ADD_CHECK_LIST_CODE);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}