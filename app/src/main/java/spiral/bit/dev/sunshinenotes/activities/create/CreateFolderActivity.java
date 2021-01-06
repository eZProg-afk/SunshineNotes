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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.other.BaseActivity;
import spiral.bit.dev.sunshinenotes.data.NoteInFolderDatabase;
import spiral.bit.dev.sunshinenotes.data.StatisticDatabase;
import spiral.bit.dev.sunshinenotes.databinding.ActivityCreateFolderBinding;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.other.Statistic;
import spiral.bit.dev.sunshinenotes.other.AdWorker;
import spiral.bit.dev.sunshinenotes.other.AlarmReceiver;
import static spiral.bit.dev.sunshinenotes.other.Utils.CODE_SELECT_IMG;
import static spiral.bit.dev.sunshinenotes.other.Utils.PERMISSION_RECORD_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.PERMISSION_STORAGE_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.REQUEST_CODE_SPEECH;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_FOLDER_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.hideKeyboard;

public class CreateFolderActivity extends AppCompatActivity {

    private EditText inputFolderTitle, inputFolderSubTitle;
    private TextView textDateTime;
    private View viewSubTitleIndicator;
    private SharedPreferences prefTimesEdited, preferencesSettings, prefChoice;
    private SharedPreferences.Editor editorTimes, editorChoice;
    private ImageView imageNote;
    private ActivityCreateFolderBinding folderBinding;
    private String selectedImgPath = "",
            selectedFolderColor = "#333333";
    private LinearLayout layoutMisc;
    private AlertDialog dialogDeleteNote, dialogExitSave,
            dialogInfoNote, dialogRemind, shareDialog;
    private Folder alreadyAvailableFolder;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private InterstitialAd mInterstitialAd;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private TimePicker timePicker;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        folderBinding = ActivityCreateFolderBinding.inflate(getLayoutInflater());
        View view = folderBinding.getRoot();
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
                WorkManager workManager = WorkManager.getInstance(CreateFolderActivity.this);
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
                WorkManager workManager = WorkManager.getInstance(CreateFolderActivity.this);
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            }
        });


        //Prefs
        prefTimesEdited = getSharedPreferences("timesEditedPref", 0);
        preferencesSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefChoice = getSharedPreferences("choice", 0);
        editorChoice = prefChoice.edit();
        editorTimes = prefTimesEdited.edit();

        //Vars
        inputFolderTitle = folderBinding.inputFolderTitle;
        inputFolderSubTitle = folderBinding.inputFolderSubTitle;
        textDateTime = folderBinding.folderDateTime;
        viewSubTitleIndicator = folderBinding.folderSubTitleIndicator;
        imageNote = folderBinding.imageFolder;

        if (getIntent().hasExtra("folder")) {
            alreadyAvailableFolder = (Folder) getIntent().getSerializableExtra("folder");
            setViewOrUpdateFolder();
        }

        //Listeners

        folderBinding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(CreateFolderActivity.this);
                onBackPressed();
            }
        });

        folderBinding.imageShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alreadyAvailableFolder != null) {
                    if (imageNote.getVisibility() == View.VISIBLE) {
                        shareDialog();
                    } else {
                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableFolder.getName());
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableFolder.getSubTitle());
                        startActivity(Intent.createChooser(intent, getString(R.string.share_label)));
                    }
                } else {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateFolderActivity.this,
                            getString(R.string.error_toast),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false).show();
                }
            }
        });

        folderBinding.imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SettingsFragment.getIsPurchased(CreateFolderActivity.this))
                    if (mInterstitialAd.isLoaded() && !preferencesSettings.getBoolean("time_block_ads", false)) {
                        mInterstitialAd.show();
                    }
                hideKeyboard(CreateFolderActivity.this);
                saveCheck();
            }
        });

        folderBinding.imageInfoNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alreadyAvailableFolder != null) {
                    showInfoNoteDialog(alreadyAvailableFolder.getDateTime(),
                            alreadyAvailableFolder.getName().length());
                } else {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateFolderActivity.this,
                            getString(R.string.info_img_error_toast),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false).show();
                }
            }
        });

        folderBinding.imgRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                folderBinding.imgRemoveImage.setVisibility(View.GONE);
                selectedImgPath = "";
            }
        });

        //Initialization

        textDateTime.setText(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));

        layoutMisc = findViewById(R.id.layout_miscellaneous);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);


        MobileAds.initialize(CreateFolderActivity.this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                if (!preferencesSettings.getBoolean("time_block_ads", false)) {
                    folderBinding.banner.setVisibility(View.VISIBLE);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    folderBinding.banner.loadAd(adRequest);
                } else {
                    folderBinding.banner.setVisibility(View.GONE);
                }
            }
        });

        folderBinding.banner.setAdListener(new AdListener() {
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
                WorkManager workManager = WorkManager.getInstance(CreateFolderActivity.this);
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
                WorkManager workManager = WorkManager.getInstance(CreateFolderActivity.this);
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            }
        });

        if (SettingsFragment.getIsPurchased(CreateFolderActivity.this)) folderBinding.banner.setVisibility(View.GONE);

        initMisc();
        setSubTitleIndicator();
    }

    //Dialogs

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
                    intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableFolder.getName());
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableFolder.getName());
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
                    FancyToast.makeText(CreateFolderActivity.this,
                            getString(R.string.no_img_in_note_share_error),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false).show();
                }
            }
        });
        view.findViewById(R.id.text_with_draw).setVisibility(View.GONE);
        view.findViewById(R.id.text_only_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableFolder.getName());
                intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableFolder.getName());
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
                new CreateFolderActivity.DeleteNoteAsyncTask().execute();
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



    private void saveCheck() {
        if (inputFolderTitle.getText().toString().trim().isEmpty()) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateFolderActivity.this,
                    getString(R.string.toast_error_title_empty),
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false).show();
            return;
        } else if (inputFolderSubTitle.getText().toString().trim().isEmpty()) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateFolderActivity.this,
                    getString(R.string.toast_error_empty_note),
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false).show();
            return;
        }

        final Folder folder = new Folder();
        folder.setName(inputFolderTitle.getText().toString());
        folder.setSubTitle(inputFolderSubTitle.getText().toString());
        folder.setDateTime(textDateTime.getText().toString());
        folder.setImagePath(selectedImgPath);
        folder.setColor(selectedFolderColor);

        final Statistic statistic = new Statistic();

        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
        String date = sdf.format(Calendar.getInstance().getTime());
        statistic.setDateText(date);

        statistic.setTypeText(getString(R.string.folder_label));
        statistic.setItemSubText(folder.getSubTitle());
        statistic.setItemText(folder.getName());
        statistic.setActionText(getString(R.string.added_label));

        if (alreadyAvailableFolder != null) {
            folder.setId(alreadyAvailableFolder.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveFolderAsyncTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                NoteInFolderDatabase.getNoteDatabase(getApplicationContext())
                        .getNoteDAO().insertFolder(folder);
                StatisticDatabase.getStatisticDatabase(getApplicationContext())
                        .getStatisticDAO().insertStatistic(statistic);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideKeyboard(CreateFolderActivity.this);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveFolderAsyncTask().execute();
    }

    private void showInfoNoteDialog(String dateTimeCreated, int lengthOfCymbals) {
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
        TextView cymbalsLength = view.findViewById(R.id.cymbals_in_note);
        created.setText(dateTimeCreated);
        cymbalsLength.setText(String.valueOf(lengthOfCymbals));
        view.findViewById(R.id.text_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogInfoNote.dismiss();
            }
        });
        dialogInfoNote.show();
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
                saveCheck();
                if (saveChoice.isChecked()) {
                    editorChoice.putString("choice_is_save", "save");
                    editorChoice.apply();
                    Intent intent = new Intent();
                    intent.putExtra("isFromBackKey", true);
                    setResult(RESULT_OK, intent);
                } else {
                    editorChoice.putString("choice_is_save", "");
                    editorChoice.apply();
                    Intent intent = new Intent();
                    intent.putExtra("isFromBackKey", true);
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        });
        view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogExitSave.dismiss();
                Intent intent = new Intent();
                intent.putExtra("isFromBackKey", true);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        dialogExitSave.show();
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
                intent.putExtra("nameOfNote", alreadyAvailableFolder.getName());
                c.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                c.set(Calendar.MINUTE, timePicker.getMinute());
                pendingIntent = PendingIntent.getBroadcast(CreateFolderActivity.this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                dialogRemind.dismiss();
            }
        });
        dialogRemind.show();
    }


    //Methods

    private void setViewOrUpdateFolder() {
        inputFolderTitle.setText(alreadyAvailableFolder.getName());
        inputFolderSubTitle.setText(alreadyAvailableFolder.getSubTitle());
        textDateTime.setText(alreadyAvailableFolder.getDateTime());
        editorTimes.putInt("timesEdited", prefTimesEdited.getInt("timesEdited", 0) + 1);
        editorTimes.apply();

        if (alreadyAvailableFolder.getImagePath() != null && !alreadyAvailableFolder.getImagePath().trim().isEmpty()) {
            folderBinding.imageFolder.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableFolder.getImagePath()));
            folderBinding.imageFolder.setVisibility(View.VISIBLE);
            folderBinding.imgRemoveImage.setVisibility(View.VISIBLE);
            selectedImgPath = alreadyAvailableFolder.getImagePath();
        }
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
                selectedFolderColor = "#333333";
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
                selectedFolderColor = "#FDBE3B";
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
                selectedFolderColor = "#FF4842";
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
                selectedFolderColor = "#3A52FC";
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
                selectedFolderColor = "#000000";
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
                selectedFolderColor = "#00FF00";
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
                selectedFolderColor = "#551A8B";
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
                selectedFolderColor = "#006400";
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
                selectedFolderColor = "#00FFFF";
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
                selectedFolderColor = "#FFA500";
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

        if (alreadyAvailableFolder != null && alreadyAvailableFolder.getColor() != null
                && !alreadyAvailableFolder.getColor().trim().isEmpty()) {
            switch (alreadyAvailableFolder.getColor()) {
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
                    ActivityCompat.requestPermissions(CreateFolderActivity.this, new String[]
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
                    ActivityCompat.requestPermissions(CreateFolderActivity.this, new String[]
                                    {Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_STORAGE_CODE);
                } else {
                    selectImg();
                }
            }
        });

        if (alreadyAvailableFolder != null) {
            layoutMisc.findViewById(R.id.layout_add_reminder).setVisibility(View.VISIBLE);
            layoutMisc.findViewById(R.id.layout_add_reminder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    openRemindDialog();
                }
            });
        }

        if (alreadyAvailableFolder != null) {
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (preferencesSettings.getBoolean("save_note_on_exit", false)) {
                event.startTracking();
                saveCheck();
                return true;
            } else {
                event.startTracking();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            if (preferencesSettings.getBoolean("save_note_on_exit", false)) {
                event.startTracking();
                saveCheck();
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
                if (save.equals("save")) saveCheck();
                Intent intent = new Intent();
                intent.putExtra("isFromBackKey", true);
                finish();
            } else {
                openQuitDialog();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_FOLDER_CODE && resultCode == RESULT_OK) {
            alreadyAvailableFolder = (Folder) data.getSerializableExtra("folder");
            setViewOrUpdateFolder();
        } else if (requestCode == REQUEST_CODE_SPEECH && resultCode == RESULT_OK) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            StringBuilder builder = new StringBuilder();
            for (String item : result) {
                String finalItem = item.replace("[]", " ");
                builder.append(finalItem);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                InputStream is = null;
                try {
                    is = getContentResolver().openInputStream(resultUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                imageNote.setImageBitmap(bitmap);
                imageNote.setVisibility(View.VISIBLE);
                findViewById(R.id.img_remove_image).setVisibility(View.VISIBLE);
                selectedImgPath = getPathFromUri(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        } else if (requestCode == CODE_SELECT_IMG && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImgUri = data.getData();
                if (selectedImgUri != null) {
                    CropImage.activity(selectedImgUri)
                            .start(this);
                }
            }
        }
    }

    private void setSubTitleIndicator() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubTitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedFolderColor));
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
                if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateFolderActivity.this,
                        getString(R.string.error_toast_perm_denied),
                        FancyToast.LENGTH_LONG,
                        FancyToast.WARNING,
                        false).show();
            }
        }
        if (requestCode == PERMISSION_RECORD_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enterVoice();
            } else {
                if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateFolderActivity.this,
                        getString(R.string.toast_error_record_denied),
                        FancyToast.LENGTH_LONG,
                        FancyToast.ERROR,
                        false).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class DeleteNoteAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Statistic statistic = new Statistic();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
            String date = sdf.format(Calendar.getInstance().getTime());
            statistic.setDateText(date);
            statistic.setTypeText(getString(R.string.folder_label));
            statistic.setItemSubText(alreadyAvailableFolder.getSubTitle());
            statistic.setItemText(alreadyAvailableFolder.getName());
            statistic.setActionText(getString(R.string.deleted_action));
            NoteInFolderDatabase.getNoteDatabase(getApplicationContext())
                    .getNoteDAO().deleteFolder(alreadyAvailableFolder);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideKeyboard(CreateFolderActivity.this);
            Intent intent = new Intent(CreateFolderActivity.this, BaseActivity.class);
            intent.putExtra("isNoteDeleted", true);
            intent.putExtra("noteTitle", alreadyAvailableFolder.getName());
            startActivity(intent);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}