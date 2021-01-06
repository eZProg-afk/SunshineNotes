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
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import com.google.android.gms.ads.AdView;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.other.BaseActivity;
import spiral.bit.dev.sunshinenotes.data.NoteDatabase;
import spiral.bit.dev.sunshinenotes.data.NoteInFolderDatabase;
import spiral.bit.dev.sunshinenotes.data.StatisticDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashDatabase;
import spiral.bit.dev.sunshinenotes.databinding.ActivityCreateNoteBinding;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.NoteInFolder;
import spiral.bit.dev.sunshinenotes.models.SimpleNote;
import spiral.bit.dev.sunshinenotes.models.other.PaintView;
import spiral.bit.dev.sunshinenotes.models.other.Statistic;
import spiral.bit.dev.sunshinenotes.models.trash.TrashNote;
import spiral.bit.dev.sunshinenotes.other.AdWorker;
import spiral.bit.dev.sunshinenotes.other.AlarmReceiver;

import static spiral.bit.dev.sunshinenotes.other.Utils.CODE_SELECT_IMG;
import static spiral.bit.dev.sunshinenotes.other.Utils.PERMISSION_RECORD_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.PERMISSION_STORAGE_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.REQUEST_CODE_SPEECH;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_NOTE_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.hideKeyboard;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle, inputNoteSubTitle, inputNoteText;
    private ActivityCreateNoteBinding noteBinding;
    private TextView textDateTime, textWebUrlRef;
    private View viewSubTitleIndicator;
    private SharedPreferences prefTimesEdited, preferencesSettings, prefChoice;
    private SharedPreferences.Editor editorTimes, editorChoice;
    private ImageView imageNote, imgDraw, currPaint;
    private String selectedImgPath = "", textSize, textColor,
            selectedNoteColor = "#333333", fontStyle = "", path = "";
    private LinearLayout layoutWebRef, layoutMisc;
    private AlertDialog dialogRefUrl, dialogDeleteNote, dialogExitSave,
            dialogInfoNote, dialogRemind, dialogAddDraw, shareDialog;
    private SimpleNote alreadyAvailableNote;
    private NoteInFolder alreadyAvailableNoteFolder;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private InterstitialAd mInterstitialAd;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private TimePicker timePicker;
    private boolean isReadModeOn = true;
    private PaintView paintView;
    private Folder folder;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteBinding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
        View view = noteBinding.getRoot();
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
                WorkManager workManager = WorkManager.getInstance(CreateNoteActivity.this);
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
                WorkManager workManager = WorkManager.getInstance(CreateNoteActivity.this);
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
        editorChoice = prefChoice.edit();
        editorTimes = prefTimesEdited.edit();

        //Vars
        layoutMisc = findViewById(R.id.layout_miscellaneous);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);
        inputNoteTitle = noteBinding.inputNoteTitle;
        inputNoteSubTitle = noteBinding.inputNoteSubTitle;
        inputNoteText = noteBinding.inputNote;
        textDateTime = noteBinding.textDateTime;
        viewSubTitleIndicator = noteBinding.viewSubTitleIndicator;
        imageNote = noteBinding.imageNote;
        textWebUrlRef = noteBinding.textWebRef;
        layoutWebRef = noteBinding.layoutWebRef;
        imgDraw = noteBinding.imageDraw;
        final AdView mAdView = noteBinding.banner;
        MobileAds.initialize(CreateNoteActivity.this, new OnInitializationCompleteListener() {
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
                WorkManager workManager = WorkManager.getInstance(CreateNoteActivity.this);
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
                WorkManager workManager = WorkManager.getInstance(CreateNoteActivity.this);
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            }
        });

        textDateTime.setText(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));
        mAdView.loadAd(new AdRequest.Builder().build());
        if (SettingsFragment.getIsPurchased(CreateNoteActivity.this))
            mAdView.setVisibility(View.GONE);

        if (getIntent().hasExtra("folder"))
            folder = (Folder) getIntent().getSerializableExtra("folder");

        if (getIntent().hasExtra("setViewOrUpdate")) {
            alreadyAvailableNote = (SimpleNote) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        } else if (getIntent().hasExtra("isViewOrUpdate")) {
            alreadyAvailableNoteFolder = (NoteInFolder) getIntent().getSerializableExtra("note_in_folder");
            setViewOrUpdateNote();
        }

        initMisc();
        setSubTitleIndicator();

        //Listeners
        noteBinding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(CreateNoteActivity.this);
                onBackPressed();
            }
        });

        noteBinding.imageNoteTextStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStylePopupMenu(v);
            }
        });

        noteBinding.imageInfoNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alreadyAvailableNote != null) {
                    showInfoNoteDialog(alreadyAvailableNote.getDateTime(),
                            alreadyAvailableNote.getDateTimeEdit(), prefTimesEdited.getInt("timesEdited", 0),
                            alreadyAvailableNote.getNoteText().length() +
                                    alreadyAvailableNote.getTitle().length() +
                                    alreadyAvailableNote.getSubTitle().length());
                } else {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateNoteActivity.this,
                            getString(R.string.info_img_error_toast),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false).show();
                }
            }
        });

        noteBinding.imageShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alreadyAvailableNote != null) {
                    if (imageNote.getVisibility() == View.VISIBLE || imgDraw.getVisibility() == View.VISIBLE) {
                        shareDialog();
                    } else {
                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableNote.getTitle());
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, alreadyAvailableNote.getSubTitle());
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableNote.getTitle() + "\n" +
                                alreadyAvailableNote.getSubTitle() + "\n" +
                                alreadyAvailableNote.getNoteText());
                        startActivity(Intent.createChooser(intent, getString(R.string.share_label)));
                    }
                } else {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateNoteActivity.this,
                            getString(R.string.error_toast),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false).show();
                }
            }
        });

        noteBinding.imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SettingsFragment.getIsPurchased(CreateNoteActivity.this)) {
                    if (mInterstitialAd.isLoaded() && !preferencesSettings.getBoolean("time_block_ads", false)) {
                        mInterstitialAd.show();
                    }
                }
                hideKeyboard(CreateNoteActivity.this);
                saveNote();
            }
        });

        noteBinding.imageReadMode.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (isReadModeOn) {
                    isReadModeOn = false;
                    inputNoteTitle.setEnabled(false);
                    inputNoteSubTitle.setEnabled(false);
                    inputNoteText.setEnabled(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        noteBinding.imageReadMode.setBackground(ContextCompat.getDrawable(CreateNoteActivity.this,
                                R.drawable.background_add_btn));
                    }
                } else {
                    isReadModeOn = true;
                    inputNoteTitle.setEnabled(true);
                    inputNoteSubTitle.setEnabled(true);
                    inputNoteText.setEnabled(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        noteBinding.imageReadMode.setBackground(ContextCompat.getDrawable(CreateNoteActivity.this,
                                R.drawable.background_simple));
                    }
                }
            }
        });

        noteBinding.imgRemoveLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textWebUrlRef.setText("");
                layoutWebRef.setVisibility(View.GONE);
            }
        });

        noteBinding.imgRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.img_remove_image).setVisibility(View.GONE);
                selectedImgPath = "";
            }
        });

        noteBinding.imgRemoveDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alreadyAvailableNote != null) {
                    imgDraw.setImageBitmap(null);
                    imgDraw.setVisibility(View.GONE);
                    noteBinding.imgRemoveDraw.setVisibility(View.GONE);
                    alreadyAvailableNote.setDrawPath("");
                    path = "";
                } else {
                    imgDraw.setImageBitmap(null);
                    imgDraw.setVisibility(View.GONE);
                    noteBinding.imgRemoveDraw.setVisibility(View.GONE);
                }
            }
        });
    }

    //Dialogs

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

    private void showAddDrawDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = LayoutInflater.from(this)
                .inflate(R.layout.content_draw,
                        (ViewGroup) findViewById(R.id.content_draw_container));
        builder.setView(view);
        dialogAddDraw = builder.create();
        if (dialogAddDraw.getWindow() != null) {
            dialogAddDraw.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        final ImageView drawBtn, erase, clearAllCanvasBtn, sizeSelectBtn;
        TextView save;
        paintView = view.findViewById(R.id.drawing);
        drawBtn = view.findViewById(R.id.brush_btn);
        sizeSelectBtn = view.findViewById(R.id.size_select_btn);
        erase = view.findViewById(R.id.erase_btn);
        save = view.findViewById(R.id.save_btn);
        clearAllCanvasBtn = view.findViewById(R.id.clear_all_canvas_btn);
        LinearLayout paintLayout = view.findViewById(R.id.paint_colors);
        currPaint = (ImageView) paintLayout.getChildAt(0);
        currPaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null) paintClicked(v);
            }
        });
        drawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.setUpDrawing();
            }
        });
        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.setErase(true);
                paintView.setBrushSize(paintView.getBrushSize());
            }
        });
        clearAllCanvasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.startNew();
            }
        });
        sizeSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintView.setDrawingCacheEnabled(true);
                String imgSaved = MediaStore.Images.Media.insertImage(
                        getContentResolver(), paintView.getDrawingCache(),
                        UUID.randomUUID().toString() + ".png",
                        "drawing"
                );
                if (imgSaved != null) {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateNoteActivity.this,
                            getString(R.string.saved),
                            FancyToast.LENGTH_LONG,
                            FancyToast.SUCCESS,
                            false).show();
                    if (alreadyAvailableNote != null) {
                        alreadyAvailableNote.setDrawPath(imgSaved);
                        path = alreadyAvailableNote.getDrawPath();
                    } else if (alreadyAvailableNoteFolder != null) {
                        alreadyAvailableNoteFolder.setDrawPath(imgSaved);
                        path = alreadyAvailableNoteFolder.getDrawPath();
                    }
                    path = imgSaved;
                    imgDraw.setVisibility(View.VISIBLE);
                    findViewById(R.id.img_remove_draw).setVisibility(View.VISIBLE);
                    InputStream is = null;
                    try {
                        is = getContentResolver().openInputStream(Uri.parse(imgSaved));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    imgDraw.setImageBitmap(bitmap);
                    dialogAddDraw.dismiss();
                }
                paintView.destroyDrawingCache();
            }
        });
        dialogAddDraw.show();
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
                intent.putExtra("nameOfNote", alreadyAvailableNote.getTitle());
                c.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                c.set(Calendar.MINUTE, timePicker.getMinute());
                pendingIntent = PendingIntent.getBroadcast(CreateNoteActivity.this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                dialogRemind.dismiss();
            }
        });
        dialogRemind.show();
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
                saveNote();
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
                    intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableNote.getTitle());
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, alreadyAvailableNote.getSubTitle());
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableNote.getTitle() + "\n" +
                            alreadyAvailableNote.getSubTitle() + "\n" +
                            alreadyAvailableNote.getNoteText());
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
                    FancyToast.makeText(CreateNoteActivity.this,
                            getString(R.string.no_img_in_note_share_error),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false).show();
                }
            }
        });
        view.findViewById(R.id.text_with_draw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imgDraw.getVisibility() == View.VISIBLE) {
                    imgDraw.buildDrawingCache();
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableNote.getTitle());
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, alreadyAvailableNote.getSubTitle());
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableNote.getTitle() + "\n" +
                            alreadyAvailableNote.getSubTitle() + "\n" +
                            alreadyAvailableNote.getNoteText());
                    Bitmap drawBitmap = ((BitmapDrawable) imgDraw.getDrawable()).getBitmap();
                    ByteArrayOutputStream bytesDraw = new ByteArrayOutputStream();
                    drawBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytesDraw);
                    String drawPath = MediaStore.Images.Media.insertImage(getContentResolver(),
                            drawBitmap, "image_draw", null);
                    Uri imageDrawUri = Uri.parse(drawPath);
                    intent.putExtra(Intent.EXTRA_STREAM, imageDrawUri);
                    shareDialog.dismiss();
                    startActivity(Intent.createChooser(intent, getString(R.string.share_label)));
                } else {
                    FancyToast.makeText(CreateNoteActivity.this,
                            getString(R.string.no_draw_in_note_share_error),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false).show();
                }
            }
        });
        view.findViewById(R.id.text_only_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableNote.getTitle());
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, alreadyAvailableNote.getSubTitle());
                intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableNote.getTitle() + "\n" +
                        alreadyAvailableNote.getSubTitle() + "\n" +
                        alreadyAvailableNote.getNoteText());
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

    private void showAddRefDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.layout_add_url,
                        (ViewGroup) findViewById(R.id.layout_add_url_container));
        builder.setView(view);
        dialogRefUrl = builder.create();
        if (dialogRefUrl.getWindow() != null) {
            dialogRefUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        final EditText inputRef = view.findViewById(R.id.input_url);
        inputRef.requestFocus();

        view.findViewById(R.id.text_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputRef.getText().toString().trim().isEmpty()) {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateNoteActivity.this,
                            getString(R.string.error_toast_ref_empty),
                            FancyToast.LENGTH_LONG,
                            FancyToast.WARNING,
                            false).show();
                } else if (!Patterns.WEB_URL.matcher(inputRef.getText().toString().trim()).matches()) {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateNoteActivity.this,
                            getString(R.string.error_toast_not_valid_ref),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false).show();
                } else {
                    textWebUrlRef.setText(inputRef.getText().toString().trim());
                    layoutWebRef.setVisibility(View.VISIBLE);
                    dialogRefUrl.dismiss();
                }
            }
        });
        view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogRefUrl.dismiss();
            }
        });
        dialogRefUrl.show();
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
                new DeleteNoteAsyncTask().execute();
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

    private void setViewOrUpdateNote() {
        if (alreadyAvailableNote != null) {
            inputNoteTitle.setText(alreadyAvailableNote.getTitle());
            inputNoteSubTitle.setText(alreadyAvailableNote.getSubTitle());
            inputNoteText.setText(alreadyAvailableNote.getNoteText());
            textDateTime.setText(alreadyAvailableNote.getDateTime());
            editorTimes.putInt("timesEdited", prefTimesEdited.getInt("timesEdited", 0) + 1);
            editorTimes.apply();

            if (alreadyAvailableNote != null && alreadyAvailableNote.getNoteColor() != null)
                setTextColor(alreadyAvailableNote.getNoteColor());

            if (alreadyAvailableNote != null && alreadyAvailableNote.getFontStyle() != null)
                setFont(alreadyAvailableNote.getFontStyle());

            if (alreadyAvailableNote != null && alreadyAvailableNote.getTextSize() != null)
                setSize(alreadyAvailableNote.getTextSize());

            if (alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
                noteBinding.imgRemoveImage.setVisibility(View.VISIBLE);
                selectedImgPath = alreadyAvailableNote.getImagePath();
            }

            if (alreadyAvailableNote.getDrawPath() != null && !alreadyAvailableNote.getDrawPath().trim().isEmpty()) {
                try {
                    InputStream is = getContentResolver().openInputStream(Uri.parse(alreadyAvailableNote.getDrawPath()));
                    imgDraw.setImageBitmap(BitmapFactory.decodeStream(is));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                imgDraw.setVisibility(View.VISIBLE);
                findViewById(R.id.img_remove_draw).setVisibility(View.VISIBLE);
                path = alreadyAvailableNote.getDrawPath();
            }

            if (alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()) {
                textWebUrlRef.setText(alreadyAvailableNote.getWebLink());
                layoutWebRef.setVisibility(View.VISIBLE);
            }
        } else if (alreadyAvailableNoteFolder != null) {
            inputNoteTitle.setText(alreadyAvailableNoteFolder.getTitle());
            inputNoteSubTitle.setText(alreadyAvailableNoteFolder.getSubTitle());
            inputNoteText.setText(alreadyAvailableNoteFolder.getNoteText());
            textDateTime.setText(alreadyAvailableNoteFolder.getDateTime());
            editorTimes.putInt("timesEdited", prefTimesEdited.getInt("timesEdited", 0) + 1);
            editorTimes.apply();

            if (alreadyAvailableNoteFolder != null && alreadyAvailableNoteFolder.getNoteColor() != null)
                setTextColor(alreadyAvailableNoteFolder.getNoteColor());

            if (alreadyAvailableNoteFolder != null && alreadyAvailableNoteFolder.getFontStyle() != null)
                setFont(alreadyAvailableNoteFolder.getFontStyle());

            if (alreadyAvailableNoteFolder != null && alreadyAvailableNoteFolder.getTextSize() != null)
                setSize(alreadyAvailableNoteFolder.getTextSize());

            if (alreadyAvailableNoteFolder.getImagePath() != null && !alreadyAvailableNoteFolder.getImagePath().trim().isEmpty()) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNoteFolder.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
                noteBinding.imgRemoveImage.setVisibility(View.VISIBLE);
                selectedImgPath = alreadyAvailableNoteFolder.getImagePath();
            }

            if (alreadyAvailableNoteFolder.getDrawPath() != null && !alreadyAvailableNoteFolder.getDrawPath().trim().isEmpty()) {
                try {
                    InputStream is = getContentResolver().openInputStream(Uri.parse(alreadyAvailableNoteFolder.getDrawPath()));
                    imgDraw.setImageBitmap(BitmapFactory.decodeStream(is));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                imgDraw.setVisibility(View.VISIBLE);
                findViewById(R.id.img_remove_draw).setVisibility(View.VISIBLE);
                path = alreadyAvailableNoteFolder.getDrawPath();
            }

            if (alreadyAvailableNoteFolder.getWebLink() != null && !alreadyAvailableNoteFolder.getWebLink().trim().isEmpty()) {
                textWebUrlRef.setText(alreadyAvailableNoteFolder.getWebLink());
                layoutWebRef.setVisibility(View.VISIBLE);
            }
        }
    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateNoteActivity.this,
                    getString(R.string.toast_error_title_empty),
                    FancyToast.LENGTH_LONG,
                    FancyToast.WARNING,
                    false).show();
            return;
        } else if (inputNoteSubTitle.getText().toString().trim().isEmpty()
                && inputNoteText.getText().toString().trim().isEmpty()) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateNoteActivity.this,
                    getString(R.string.toast_error_empty_note),
                    FancyToast.LENGTH_LONG,
                    FancyToast.WARNING,
                    false).show();
            return;
        }

        final NoteInFolder noteInFolder = new NoteInFolder();
        final SimpleNote simpleNote = new SimpleNote();
        final Statistic statistic = new Statistic();

        if (folder != null) {
            int folderID = folder.getId();
            noteInFolder.setTitle(inputNoteTitle.getText().toString());
            noteInFolder.setSubTitle(inputNoteSubTitle.getText().toString());
            noteInFolder.setNoteText(inputNoteText.getText().toString());
            noteInFolder.setDateTime(textDateTime.getText().toString());
            noteInFolder.setColor(selectedNoteColor);
            noteInFolder.setImagePath(selectedImgPath);
            noteInFolder.setDrawPath(path);
            noteInFolder.setFontStyle(fontStyle);
            noteInFolder.setTextSize(textSize);
            noteInFolder.setNoteColor(textColor);
            noteInFolder.setChildId(folderID);
            noteInFolder.setDelete(false);

            if (layoutWebRef.getVisibility() == View.VISIBLE) {
                noteInFolder.setWebLink(textWebUrlRef.getText().toString());
            }

            if (alreadyAvailableNoteFolder != null) {
                noteInFolder.setId(alreadyAvailableNoteFolder.getId());
                noteInFolder.setDateTimeEdit(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date()));
                statistic.setDateText(new SimpleDateFormat("yyyy dd HH:mm a", Locale.getDefault())
                        .format(new Date()));
            }

            statistic.setDateText(new SimpleDateFormat("yyyy dd HH:mm a", Locale.getDefault())
                    .format(new Date()));

            statistic.setTypeText(getString(R.string.note_in_folder_label));
            statistic.setItemSubText(noteInFolder.getSubTitle());
            statistic.setItemText(noteInFolder.getNoteText());
            statistic.setActionText(getString(R.string.added_label));
        } else {
            simpleNote.setTitle(inputNoteTitle.getText().toString());
            simpleNote.setSubTitle(inputNoteSubTitle.getText().toString());
            simpleNote.setNoteText(inputNoteText.getText().toString());
            simpleNote.setDateTime(textDateTime.getText().toString());
            simpleNote.setColor(selectedNoteColor);
            simpleNote.setImagePath(selectedImgPath);
            simpleNote.setDrawPath(path);
            simpleNote.setFontStyle(fontStyle);
            simpleNote.setTextSize(textSize);
            simpleNote.setNoteColor(textColor);
            simpleNote.setDelete(false);

            if (layoutWebRef.getVisibility() == View.VISIBLE) {
                simpleNote.setWebLink(textWebUrlRef.getText().toString());
            }

            if (alreadyAvailableNote != null) {
                simpleNote.setId(alreadyAvailableNote.getId());
                simpleNote.setDateTimeEdit(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date()));
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
            String date = sdf.format(Calendar.getInstance().getTime());
            statistic.setDateText(date);

            statistic.setTypeText(getString(R.string.note_label));
            statistic.setItemSubText(simpleNote.getSubTitle());
            statistic.setItemText(simpleNote.getTitle());
            statistic.setActionText(getString(R.string.added_label));
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteAsyncTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                if (folder != null) {
                    NoteInFolderDatabase.getNoteDatabase(getApplicationContext()).getNoteDAO().insertNote(noteInFolder);
                    StatisticDatabase.getStatisticDatabase(getApplicationContext()).getStatisticDAO()
                            .insertStatistic(statistic);
                } else {
                    NoteDatabase.getNoteDatabase(getApplicationContext()).getNoteDAO().insertNote(simpleNote);
                    StatisticDatabase.getStatisticDatabase(getApplicationContext()).getStatisticDAO()
                            .insertStatistic(statistic);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideKeyboard(CreateNoteActivity.this);
                Intent intent = new Intent();
                if (folder != null) {
                    intent.putExtra("folder", folder);
                    intent.putExtra("note", alreadyAvailableNoteFolder);
                    intent.putExtra("isViewOrUpdate", true);
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveNoteAsyncTask().execute();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
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
        }
        if (requestCode == CODE_SELECT_IMG && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImgUri = data.getData();
                if (selectedImgUri != null) {
                    CropImage.activity(selectedImgUri)
                            .start(this);
                }
            }
        } else if (requestCode == UPDATE_NOTE_CODE && resultCode == RESULT_OK) {
            alreadyAvailableNote = (SimpleNote) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        } else if (requestCode == REQUEST_CODE_SPEECH && resultCode == RESULT_OK) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            StringBuilder builder = new StringBuilder();
            for (String item : result) {
                String finalItem = item.replace("[]", " ");
                builder.append(finalItem);
            }
            inputNoteText.setText(inputNoteText.getText() + " " + builder.toString());
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

        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor()
                .trim().isEmpty()) {
            switch (alreadyAvailableNote.getColor()) {
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
        } else if (alreadyAvailableNoteFolder != null && alreadyAvailableNoteFolder.getColor() != null && !alreadyAvailableNoteFolder.getColor()
                .trim().isEmpty()) {
            switch (alreadyAvailableNoteFolder.getColor()) {
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
                    ActivityCompat.requestPermissions(CreateNoteActivity.this, new String[]
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
                    ActivityCompat.requestPermissions(CreateNoteActivity.this, new String[]
                                    {Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_STORAGE_CODE);
                } else {
                    selectImg();
                }
            }
        });

        layoutMisc.findViewById(R.id.layout_add_draw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddDrawDialog();
            }
        });

        layoutMisc.findViewById(R.id.layout_add_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddRefDialog();
            }
        });

        if (alreadyAvailableNote != null || alreadyAvailableNoteFolder != null) {
            layoutMisc.findViewById(R.id.layout_add_reminder).setVisibility(View.VISIBLE);
            layoutMisc.findViewById(R.id.layout_add_reminder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    openRemindDialog();
                }
            });
        }

        if (alreadyAvailableNote != null || alreadyAvailableNoteFolder != null) {
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

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.size_def:
                                paintView.setBrushSize(8f);
                                return true;
                            case R.id.size_small:
                                paintView.setBrushSize(16f);
                                return true;
                            case R.id.size_medium:
                                paintView.setBrushSize(25f);
                                return true;
                            case R.id.size_big:
                                paintView.setBrushSize(50f);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
        popupMenu.show();
    }

    private void showStylePopupMenu(View v) {
        PopupMenu textStyle = new PopupMenu(this, v);
        textStyle.inflate(R.menu.text_style_popup_menu);
        textStyle.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.size_def:
                        setSize("def");
                        return true;
                    case R.id.size_small:
                        setSize("small");
                        return true;
                    case R.id.size_medium:
                        setSize("medium");
                        return true;
                    case R.id.size_big:
                        setSize("big");
                        return true;
                    case R.id.color_def:
                        setTextColor("def");
                        return true;
                    case R.id.color_yellow:
                        setTextColor("yellow");
                        return true;
                    case R.id.color_green:
                        setTextColor("green");
                        return true;
                    case R.id.color_red:
                        setTextColor("red");
                        return true;
                    case R.id.style_def:
                        setFont("def");
                        return true;
                    case R.id.style_comissioner:
                        setFont("comissioner");
                        return true;
                    case R.id.style_roboto:
                        setFont("roboto");
                        return true;
                    case R.id.style_sourcecode:
                        setFont("sourcecode");
                        return true;
                    default:
                        return false;
                }
            }
        });
        textStyle.show();
    }

    private void setTextColor(String type) {
        switch (type) {
            case "def":
                inputNoteTitle.setTextColor(Color.WHITE);
                inputNoteSubTitle.setTextColor(Color.WHITE);
                inputNoteText.setTextColor(Color.WHITE);
                textColor = type;
                break;
            case "yellow":
                inputNoteTitle.setTextColor(Color.YELLOW);
                inputNoteSubTitle.setTextColor(Color.YELLOW);
                inputNoteText.setTextColor(Color.YELLOW);
                textColor = type;
                break;
            case "green":
                inputNoteTitle.setTextColor(Color.GREEN);
                inputNoteSubTitle.setTextColor(Color.GREEN);
                inputNoteText.setTextColor(Color.GREEN);
                textColor = type;
                break;
            case "red":
                inputNoteTitle.setTextColor(Color.RED);
                inputNoteSubTitle.setTextColor(Color.RED);
                inputNoteText.setTextColor(Color.RED);
                textColor = type;
                break;
        }
    }

    private void setSize(String type) {
        switch (type) {
            case "def":
                inputNoteTitle.setTextSize(16);
                inputNoteSubTitle.setTextSize(13);
                inputNoteText.setTextSize(13);
                textSize = type;
                break;
            case "small":
                inputNoteTitle.setTextSize(12);
                inputNoteSubTitle.setTextSize(10);
                inputNoteText.setTextSize(10);
                textSize = type;
                break;
            case "medium":
                inputNoteTitle.setTextSize(18);
                inputNoteSubTitle.setTextSize(16);
                inputNoteText.setTextSize(16);
                textSize = type;
                break;
            case "big":
                inputNoteTitle.setTextSize(22);
                inputNoteSubTitle.setTextSize(17);
                inputNoteText.setTextSize(17);
                textSize = type;
                break;
        }
    }

    private void setFont(String type) {
        switch (type) {
            case "def": {
                Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ubuntu_bold.ttf");
                Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/ubuntu_medium.ttf");
                Typeface typeface3 = Typeface.createFromAsset(getAssets(), "fonts/ubuntu_regular.ttf");
                inputNoteTitle.setTypeface(typeface);
                inputNoteSubTitle.setTypeface(typeface2);
                inputNoteText.setTypeface(typeface3);
                fontStyle = type;
                break;
            }
            case "comissioner": {
                Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/comm_black.ttf");
                Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/comm_medium.ttf");
                Typeface typeface3 = Typeface.createFromAsset(getAssets(), "fonts/comm_thin.ttf");
                inputNoteTitle.setTypeface(typeface);
                inputNoteSubTitle.setTypeface(typeface2);
                inputNoteText.setTypeface(typeface3);
                fontStyle = type;
                break;
            }
            case "roboto": {
                Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/robotoslab_black.ttf");
                Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/robotoslab_regular.ttf");
                Typeface typeface3 = Typeface.createFromAsset(getAssets(), "fonts/robotoslab_thin.ttf");
                inputNoteTitle.setTypeface(typeface);
                inputNoteSubTitle.setTypeface(typeface2);
                inputNoteText.setTypeface(typeface3);
                fontStyle = type;
                break;
            }
            case "sourcecode": {
                Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/sourcecode_black.ttf");
                Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/source_regular.ttf");
                Typeface typeface3 = Typeface.createFromAsset(getAssets(), "fonts/source_light.ttf");
                inputNoteTitle.setTypeface(typeface);
                inputNoteSubTitle.setTypeface(typeface2);
                inputNoteText.setTypeface(typeface3);
                fontStyle = type;
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (preferencesSettings.getBoolean("save_note_on_exit", false)) {
                keyEvent.startTracking();
                saveNote();
                return true;
            } else {
                keyEvent.startTracking();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            if (preferencesSettings.getBoolean("save_note_on_exit", false)) {
                keyEvent.startTracking();
                saveNote();
                return true;
            } else {
                keyEvent.startTracking();
                return true;
            }
        }
        return super.onKeyDown(keyCode, keyEvent);
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
                    saveNote();
                    Intent intent = new Intent();
                    intent.putExtra("isFromBackKey", true);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("isFromBackKey", true);
                }
                finish();
            } else {
                openQuitDialog();
            }
        }
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
                if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateNoteActivity.this,
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
                if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(CreateNoteActivity.this,
                        getString(R.string.toast_error_record_denied),
                        FancyToast.LENGTH_LONG,
                        FancyToast.WARNING,
                        false).show();
            }
        }
    }

    public void paintClicked(View view) {
        ImageView imageView = (ImageView) view;
        String color = imageView.getTag().toString();
        paintView.setColor(color);
        currPaint = (ImageView) view;
    }

@SuppressLint("StaticFieldLeak")
class DeleteNoteAsyncTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
        TrashNote trashNote = new TrashNote();
        Statistic statistic = new Statistic();
        if (alreadyAvailableNoteFolder != null) {
            trashNote.setColor(alreadyAvailableNoteFolder.getColor());
            trashNote.setDateTime(alreadyAvailableNoteFolder.getDateTime());
            trashNote.setDateTimeRemind(alreadyAvailableNoteFolder.getDateTimeRemind());
            trashNote.setDateTimeEdit(alreadyAvailableNoteFolder.getDateTimeEdit());
            trashNote.setDrawPath(alreadyAvailableNoteFolder.getDrawPath());
            trashNote.setFontStyle(alreadyAvailableNoteFolder.getFontStyle());
            trashNote.setId(alreadyAvailableNoteFolder.getId());
            trashNote.setImagePath(alreadyAvailableNoteFolder.getImagePath());
            trashNote.setNoteColor(alreadyAvailableNoteFolder.getNoteColor());
            trashNote.setNoteText(alreadyAvailableNoteFolder.getNoteText());
            trashNote.setSubTitle(alreadyAvailableNoteFolder.getSubTitle());
            trashNote.setTextSize(alreadyAvailableNoteFolder.getTextSize());
            trashNote.setWebLink(alreadyAvailableNoteFolder.getWebLink());
            trashNote.setTitle(alreadyAvailableNoteFolder.getTitle());

            statistic.setItemText(alreadyAvailableNoteFolder.getTitle());
            statistic.setItemSubText(alreadyAvailableNoteFolder.getSubTitle());
            statistic.setTypeText(getString(R.string.note_in_folder_label));
            statistic.setActionText(getString(R.string.deleted_action));
            SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
            String date = sdf.format(Calendar.getInstance().getTime());
            statistic.setDateText(date);
            TrashDatabase.getNoteDatabase(getApplicationContext()).getTrashDAO()
                    .insertTrashNote(trashNote);
            NoteInFolderDatabase.getNoteDatabase(getApplicationContext()).getNoteDAO()
                    .deleteNote(alreadyAvailableNoteFolder);
            StatisticDatabase.getStatisticDatabase(getApplicationContext()).getStatisticDAO()
                    .insertStatistic(statistic);
        } else {
            trashNote.setColor(alreadyAvailableNote.getColor());
            trashNote.setDateTime(alreadyAvailableNote.getDateTime());
            trashNote.setDateTimeRemind(alreadyAvailableNote.getDateTimeRemind());
            trashNote.setDateTimeEdit(alreadyAvailableNote.getDateTimeEdit());
            trashNote.setDrawPath(alreadyAvailableNote.getDrawPath());
            trashNote.setFontStyle(alreadyAvailableNote.getFontStyle());
            trashNote.setId(alreadyAvailableNote.getId());
            trashNote.setImagePath(alreadyAvailableNote.getImagePath());
            trashNote.setNoteColor(alreadyAvailableNote.getNoteColor());
            trashNote.setNoteText(alreadyAvailableNote.getNoteText());
            trashNote.setSubTitle(alreadyAvailableNote.getSubTitle());
            trashNote.setTextSize(alreadyAvailableNote.getTextSize());
            trashNote.setWebLink(alreadyAvailableNote.getWebLink());
            trashNote.setTitle(alreadyAvailableNote.getTitle());

            statistic.setItemText(alreadyAvailableNote.getTitle());
            statistic.setItemSubText(alreadyAvailableNote.getSubTitle());
            statistic.setTypeText(getString(R.string.note_label));
            statistic.setActionText(getString(R.string.deleted_action));
            SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
            String date = sdf.format(Calendar.getInstance().getTime());
            statistic.setDateText(date);
            StatisticDatabase.getStatisticDatabase(getApplicationContext())
                    .getStatisticDAO().insertStatistic(statistic);
            TrashDatabase.getNoteDatabase(getApplicationContext()).getTrashDAO()
                    .insertTrashNote(trashNote);
            NoteDatabase.getNoteDatabase(getApplicationContext()).getNoteDAO().deleteNote(alreadyAvailableNote);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Intent intent = new Intent(CreateNoteActivity.this, BaseActivity.class);
        intent.putExtra("isNoteDeleted", true);
        if (alreadyAvailableNoteFolder != null) {
            intent.putExtra("noteTitle", alreadyAvailableNoteFolder.getTitle());
            intent.putExtra("noteSubtitle", alreadyAvailableNoteFolder.getSubTitle());
            intent.putExtra("noteText", alreadyAvailableNoteFolder.getNoteText());
        } else {
            intent.putExtra("noteTitle", alreadyAvailableNote.getTitle());
            intent.putExtra("noteSubtitle", alreadyAvailableNote.getSubTitle());
            intent.putExtra("noteText", alreadyAvailableNote.getNoteText());
        }
        hideKeyboard(CreateNoteActivity.this);
        startActivity(intent);
        setResult(RESULT_OK, intent);
        finish();
    }
}
}