package spiral.bit.dev.sunshinenotes.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import spiral.bit.dev.sunshinenotes.AlarmReceiver;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.SettingsFragment;
import spiral.bit.dev.sunshinenotes.data.NoteDatabase;
import spiral.bit.dev.sunshinenotes.models.Note;
import spiral.bit.dev.sunshinenotes.models.PaintView;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle, inputNoteSubTitle, inputNoteText;
    private TextView textDateTime, textWebUrlRef;
    private View viewSubTitleIndicator;
    private static final int REQUEST_CODE_SPEECH = 125;
    private static final int PERMISSION_STORAGE_CODE = 11;
    public static final int PERMISSION_RECORD_CODE = 111;
    private static final int CODE_SELECT_IMG = 12;
    private SharedPreferences prefTimesEdited, preferencesSettings, prefChoice;
    private SharedPreferences.Editor editorTimes, editorChoice;
    private ImageView imageNote, imgInfo, imgDraw, imgShare, imgTextSyle;
    private String selectedImgPath, textSize, textColor, selectedNoteColor, fontStyle;
    private LinearLayout layoutWebRef, layoutMisc;
    private AlertDialog dialogRefUrl, dialogDeleteNote, dialogExitSave,
            dialogInfoNote, dialogRemind, dialogAddDraw, shareDialog;
    private Note alreadyAvailableNote;
    private String dateTimeEdit = null;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private InterstitialAd mInterstitialAd;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private TimePicker timePicker;
    private String path;
    private boolean isReadModeOn = false;
    private PaintView paintView;
    private ImageView currPaint;
    private List<Note> notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        path = "";
        fontStyle = "";
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.admob_interstitial_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        prefTimesEdited = getSharedPreferences("timesEditedPref", 0);
        preferencesSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefChoice = getSharedPreferences("choice", 0);
        editorChoice = prefChoice.edit();
        notes = new ArrayList<>();
        editorTimes = prefTimesEdited.edit();
        layoutMisc = findViewById(R.id.layout_miscellaneous);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);
        ImageView imageBack = findViewById(R.id.image_back);
        imgTextSyle = findViewById(R.id.image_note_text_style);
        imgTextSyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStylePopupMenu(v);
            }
        });
        inputNoteTitle = findViewById(R.id.input_note_title);
        inputNoteSubTitle = findViewById(R.id.input_note_sub_title);
        inputNoteText = findViewById(R.id.input_note);
        textDateTime = findViewById(R.id.text_date_time);
        viewSubTitleIndicator = findViewById(R.id.view_sub_title_indicator);
        imgInfo = findViewById(R.id.image_info_note);
        imgShare = findViewById(R.id.image_share);
        imageNote = findViewById(R.id.image_note);
        textWebUrlRef = findViewById(R.id.text_web_ref);
        layoutWebRef = findViewById(R.id.layout_web_ref);
        textDateTime.setText(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));
        imgDraw = findViewById(R.id.image_draw);

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(CreateNoteActivity.this);
                onBackPressed();
            }
        });

        imgShare.setOnClickListener(new View.OnClickListener() {
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
                    if (!preferencesSettings.getBoolean("remove_toasts", false)) {
                    } else {
                        Toast.makeText(CreateNoteActivity.this, getString(R.string.error_toast), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        ImageView imgSave = findViewById(R.id.image_save);
        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SettingsFragment.getIsPurchased(CreateNoteActivity.this)) {
                    hideKeyboard(CreateNoteActivity.this);
                    saveNote();
                } else {
                    if (mInterstitialAd.isLoaded()) mInterstitialAd.show();
                    hideKeyboard(CreateNoteActivity.this);
                    saveNote();
                }
            }
        });

        imgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alreadyAvailableNote != null) {
                    showInfoNoteDialog(alreadyAvailableNote.getDateTime(),
                            alreadyAvailableNote.getDateTimeEdit(), prefTimesEdited.getInt("timesEdited", 0),
                            alreadyAvailableNote.getNoteText().length() +
                                    alreadyAvailableNote.getTitle().length() +
                                    alreadyAvailableNote.getSubTitle().length());
                } else {
                    if (!preferencesSettings.getBoolean("remove_toasts", true)) {
                    } else {
                        Toast.makeText(CreateNoteActivity.this, getString(R.string.info_img_error_toast), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        final ImageView imgReadMode = findViewById(R.id.image_read_mode);
        imgReadMode.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (isReadModeOn) {
                    isReadModeOn = false;
                    inputNoteTitle.setEnabled(false);
                    inputNoteSubTitle.setEnabled(false);
                    inputNoteText.setEnabled(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imgReadMode.setBackground(getDrawable(R.drawable.background_add_btn));
                    }
                } else {
                    isReadModeOn = true;
                    inputNoteTitle.setEnabled(true);
                    inputNoteSubTitle.setEnabled(true);
                    inputNoteText.setEnabled(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imgReadMode.setBackground(getDrawable(R.drawable.background_simple));
                    }
                }
            }
        });

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }
            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
            }
            @Override
            public void onAdOpened() {
            }
            @Override
            public void onAdClicked() {
            }
            @Override
            public void onAdLeftApplication() {
            }
            @Override
            public void onAdClosed() {
            }
        });

        selectedNoteColor = "#333333";
        selectedImgPath = "";
        path = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        findViewById(R.id.img_remove_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textWebUrlRef.setText("");
                layoutWebRef.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.img_remove_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.img_remove_image).setVisibility(View.GONE);
                selectedImgPath = "";
            }
        });

        findViewById(R.id.img_remove_draw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alreadyAvailableNote != null) {
                    imgDraw.setImageBitmap(null);
                    imgDraw.setVisibility(View.GONE);
                    findViewById(R.id.img_remove_draw).setVisibility(View.GONE);
                    alreadyAvailableNote.setDrawPath("");
                    path = "";
                } else {
                    imgDraw.setImageBitmap(null);
                    imgDraw.setVisibility(View.GONE);
                    findViewById(R.id.img_remove_draw).setVisibility(View.GONE);
                }
            }
        });

        initMisc();
        setSubTitleIndicator();
    }

    private void showInfoNoteDialog(String dateTimeCreated, String dateTimeEdited, int timesEdited, int lengthOfCymbals) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setViewOrUpdateNote() {
        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteSubTitle.setText(alreadyAvailableNote.getSubTitle());
        inputNoteText.setText(alreadyAvailableNote.getNoteText());
        textDateTime.setText(alreadyAvailableNote.getDateTime());
        editorTimes.putInt("timesEdited", prefTimesEdited.getInt("timesEdited", 0) + 1);
        editorTimes.apply();

        if (alreadyAvailableNote != null && alreadyAvailableNote.getNoteColor() != null) {
            setTextColor(alreadyAvailableNote.getNoteColor());
        }

        if (alreadyAvailableNote != null && alreadyAvailableNote.getFontStyle() != null) {
            setFont(alreadyAvailableNote.getFontStyle());
        }

        if (alreadyAvailableNote != null && alreadyAvailableNote.getTextSize() != null) {
            setSize(alreadyAvailableNote.getTextSize());
        }

        if (alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.img_remove_image).setVisibility(View.VISIBLE);
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
    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            if (!preferencesSettings.getBoolean("remove_toasts", true)) {
            } else {
                Toast.makeText(this, getString(R.string.toast_error_title_empty), Toast.LENGTH_SHORT).show();
            }
            return;
        } else if (inputNoteSubTitle.getText().toString().trim().isEmpty()
                && inputNoteText.getText().toString().trim().isEmpty()) {
            if (!preferencesSettings.getBoolean("remove_toasts", true)) {
            } else {
                Toast.makeText(this, getString(R.string.toast_error_empty_note), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubTitle(inputNoteSubTitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImgPath);
        note.setDrawPath(path);
        note.setFontStyle(fontStyle);
        note.setTextSize(textSize);
        note.setNoteColor(textColor);

        if (layoutWebRef.getVisibility() == View.VISIBLE) {
            note.setWebLink(textWebUrlRef.getText().toString());
        }

        if (alreadyAvailableNote != null) {
            note.setId(alreadyAvailableNote.getId());
            note.setDateTimeEdit(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                    .format(new Date()));
            dateTimeEdit = note.getDateTimeEdit();
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteAsyncTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                NoteDatabase.getNoteDatabase(getApplicationContext()).getNoteDAO().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveNoteAsyncTask().execute();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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


        if (alreadyAvailableNote != null) {
            layoutMisc.findViewById(R.id.layout_add_reminder).setVisibility(View.VISIBLE);
            layoutMisc.findViewById(R.id.layout_add_reminder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    openRemindDialog();
                }
            });
        }

        if (alreadyAvailableNote != null) {
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
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddDrawDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
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
                if (v != null) {
                    paintClicked(v);
                }
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
                    if (!preferencesSettings.getBoolean("remove_toasts", true)) {
                    } else {
                        Toast.makeText(CreateNoteActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                    }
                    if (alreadyAvailableNote != null) {
                        alreadyAvailableNote.setDrawPath(imgSaved);
                        path = alreadyAvailableNote.getDrawPath();
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
        if (type.equals("def")) {
            inputNoteTitle.setTextColor(Color.WHITE);
            inputNoteSubTitle.setTextColor(Color.WHITE);
            inputNoteText.setTextColor(Color.WHITE);
            textColor = type;
        } else if (type.equals("yellow")) {
            inputNoteTitle.setTextColor(Color.YELLOW);
            inputNoteSubTitle.setTextColor(Color.YELLOW);
            inputNoteText.setTextColor(Color.YELLOW);
            textColor = type;
        } else if (type.equals("green")) {
            inputNoteTitle.setTextColor(Color.GREEN);
            inputNoteSubTitle.setTextColor(Color.GREEN);
            inputNoteText.setTextColor(Color.GREEN);
            textColor = type;
        } else if (type.equals("red")) {
            inputNoteTitle.setTextColor(Color.RED);
            inputNoteSubTitle.setTextColor(Color.RED);
            inputNoteText.setTextColor(Color.RED);
            textColor = type;
        }
    }

    private void setSize(String type) {
        if (type.equals("def")) {
            inputNoteTitle.setTextSize(16);
            inputNoteSubTitle.setTextSize(13);
            inputNoteText.setTextSize(13);
            textSize = type;
        } else if (type.equals("small")) {
            inputNoteTitle.setTextSize(12);
            inputNoteSubTitle.setTextSize(10);
            inputNoteText.setTextSize(10);
            textSize = type;
        } else if (type.equals("medium")) {
            inputNoteTitle.setTextSize(18);
            inputNoteSubTitle.setTextSize(16);
            inputNoteText.setTextSize(16);
            textSize = type;
        } else if (type.equals("big")) {
            inputNoteTitle.setTextSize(22);
            inputNoteSubTitle.setTextSize(17);
            inputNoteText.setTextSize(17);
            textSize = type;
        }
    }

    private void setFont(String type) {
        if (type.equals("def")) {
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ubuntu_bold.ttf");
            Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/ubuntu_medium.ttf");
            Typeface typeface3 = Typeface.createFromAsset(getAssets(), "fonts/ubuntu_regular.ttf");
            inputNoteTitle.setTypeface(typeface);
            inputNoteSubTitle.setTypeface(typeface2);
            inputNoteText.setTypeface(typeface3);
            fontStyle = type;
        } else if (type.equals("comissioner")) {
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/comm_black.ttf");
            Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/comm_medium.ttf");
            Typeface typeface3 = Typeface.createFromAsset(getAssets(), "fonts/comm_thin.ttf");
            inputNoteTitle.setTypeface(typeface);
            inputNoteSubTitle.setTypeface(typeface2);
            inputNoteText.setTypeface(typeface3);
            fontStyle = type;
        } else if (type.equals("roboto")) {
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/robotoslab_black.ttf");
            Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/robotoslab_regular.ttf");
            Typeface typeface3 = Typeface.createFromAsset(getAssets(), "fonts/robotoslab_thin.ttf");
            inputNoteTitle.setTypeface(typeface);
            inputNoteSubTitle.setTypeface(typeface2);
            inputNoteText.setTypeface(typeface3);
            fontStyle = type;
        } else if (type.equals("sourcecode")) {
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/sourcecode_black.ttf");
            Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/source_regular.ttf");
            Typeface typeface3 = Typeface.createFromAsset(getAssets(), "fonts/source_light.ttf");
            inputNoteTitle.setTypeface(typeface);
            inputNoteSubTitle.setTypeface(typeface2);
            inputNoteText.setTypeface(typeface3);
            fontStyle = type;
        }
    }


    private void openRemindDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
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
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String hourString = String.valueOf(hour);
                String minuteString = String.valueOf(minute);
                if (hour > 12) {
                    hourString = String.valueOf(hour - 12);
                }
                if (minute < 10) {
                    minuteString = "0" + minute;
                }
                pendingIntent = PendingIntent.getBroadcast(CreateNoteActivity.this, 0, intent,
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
                saveNote();
                return true;
            } else {
                event.startTracking();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            if (preferencesSettings.getBoolean("save_note_on_exit", false)) {
                event.startTracking();
                saveNote();
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
            openQuitDialog();
        } else {
            if (prefChoice.getBoolean("choice_is_check", false)) {
                String save = prefChoice.getString("choice_is_save", "");
                if (save.equals("save")) {
                    saveNote();
                    Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                }
            } else {
                openQuitDialog();
            }
        }
    }

    private void openQuitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
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
                    Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                } else {
                    editorChoice.putString("choice_is_save", "");
                    editorChoice.apply();
                    Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                }
            }
        });
        view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogExitSave.dismiss();
                Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class);
                intent.putExtra("isFromBackKey", true);
                startActivity(intent);
            }
        });
        dialogExitSave.show();
    }

    private void shareDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
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
                    Toast.makeText(CreateNoteActivity.this, getString(R.string.no_img_in_note_share_error), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CreateNoteActivity.this, getString(R.string.no_draw_in_note_share_error), Toast.LENGTH_SHORT).show();
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

    private void showDeleteNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
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
                class DeleteNoteAsyncTask extends AsyncTask<Void, Void, Void> {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        NoteDatabase.getNoteDatabase(getApplicationContext()).getNoteDAO().deleteNote(alreadyAvailableNote);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class);
                        intent.putExtra("isNoteDeleted", true);
                        intent.putExtra("noteTitle", alreadyAvailableNote.getTitle());
                        intent.putExtra("noteSubtitle", alreadyAvailableNote.getSubTitle());
                        intent.putExtra("noteText", alreadyAvailableNote.getNoteText());
                        setResult(RESULT_OK, intent);
                        hideKeyboard(CreateNoteActivity.this);
                        startActivity(intent);
                        finish();
                    }
                }
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CODE_SELECT_IMG) {
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
                        if (!preferencesSettings.getBoolean("remove_toasts", true)) {
                        } else {
                            Toast.makeText(this, getString(R.string.error_add_img_toast), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SPEECH) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            StringBuilder builder = new StringBuilder();
            for (String item : result) {
                item.replace("[]", " ");
                builder.append(item);
            }
            inputNoteText.setText(inputNoteText.getText() + " " + builder.toString());
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
                if (!preferencesSettings.getBoolean("remove_toasts", true)) {
                } else {
                    Toast.makeText(this, getString(R.string.error_toast_perm_denied), Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (requestCode == PERMISSION_RECORD_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enterVoice();
            } else {
                if (!preferencesSettings.getBoolean("remove_toasts", true)) {
                } else {
                    Toast.makeText(this, getString(R.string.toast_error_record_denied), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showAddRefDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url, (ViewGroup) findViewById(R.id.layout_add_url_container));
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
                    if (!preferencesSettings.getBoolean("remove_toasts", true)) {
                    } else {
                        Toast.makeText(CreateNoteActivity.this, getString(R.string.error_toast_ref_empty), Toast.LENGTH_SHORT).show();
                    }
                } else if (!Patterns.WEB_URL.matcher(inputRef.getText().toString().trim()).matches()) {
                    if (!preferencesSettings.getBoolean("remove_toasts", true)) {
                    } else {
                        Toast.makeText(CreateNoteActivity.this, getString(R.string.error_toast_not_valid_ref), Toast.LENGTH_SHORT).show();
                    }
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

    public void paintClicked(View view) {
        ImageView imageView = (ImageView) view;
        String color = imageView.getTag().toString();
        paintView.setColor(color);
        currPaint = (ImageView) view;
    }
}