package spiral.bit.dev.sunshinenotes.fragments;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

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
import spiral.bit.dev.sunshinenotes.activities.BaseActivity;
import spiral.bit.dev.sunshinenotes.data.NoteDatabase;
import spiral.bit.dev.sunshinenotes.data.NoteInFolderDatabase;
import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.NoteInFolder;
import spiral.bit.dev.sunshinenotes.models.PaintView;
import spiral.bit.dev.sunshinenotes.models.SimpleNote;
import spiral.bit.dev.sunshinenotes.other.AlarmReceiver;
import spiral.bit.dev.sunshinenotes.other.RoomWorker;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.ALARM_SERVICE;

public class CreateNoteFragment extends Fragment {

    public static final int ADD_NOTE_CODE = 12;
    public static final int UPDATE_NOTE_CODE = 13;
    public static final int SHOW_NOTES_CODE = 14;
    private static final int REQUEST_CODE_SPEECH = 125;
    private static final int PERMISSION_STORAGE_CODE = 11;
    public static final int PERMISSION_RECORD_CODE = 111;
    private static final int CODE_SELECT_IMG = 12;

    private EditText inputNoteTitle, inputNoteSubTitle, inputNoteText;
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
    private SimpleNote alreadyAvailableNoteInFolder;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_create_note, container, false);

        getParentFragmentManager().setFragmentResultListener(String.valueOf(UPDATE_NOTE_CODE), this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                if (requestKey.equals(String.valueOf(UPDATE_NOTE_CODE))) {
                    alreadyAvailableNoteInFolder = (SimpleNote) bundle.getSerializable("note");
                    setViewOrUpdateNote(view);
                } else if (requestKey.equals(REQUEST_CODE_SPEECH)) {
                    ArrayList<String> result = bundle.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
                    StringBuilder builder = new StringBuilder();
                    for (String item : result) {
                        String finalItem = item.replace("[]", " ");
                        builder.append(finalItem);
                    }
                    inputNoteText.setText(inputNoteText.getText() + " " + builder.toString());
//                } else if (requestKey.equals(String.valueOf(CODE_SELECT_IMG))) {
//                    if (bundle != null) {
//                        Uri selectedImgUri = getActivity().getIntent().getData();
//                        if (selectedImgUri != null) {
//                            try {
//                                InputStream is = getContext().getContentResolver().openInputStream(selectedImgUri);
//                                Bitmap bitmap = BitmapFactory.decodeStream(is);
//                                imageNote.setImageBitmap(bitmap);
//                                imageNote.setVisibility(View.VISIBLE);
//                                getView().findViewById(R.id.img_remove_image).setVisibility(View.VISIBLE);
//                                selectedImgPath = getPathFromUri(selectedImgUri);
//                            } catch (Exception e) {
//                                if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
//                                        getContext(), getString(R.string.error_add_img_toast),
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }
//                }
                }
            }
        });
        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId(getString(R.string.admob_interstitial_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        //prefs
        prefTimesEdited = getContext().getSharedPreferences("timesEditedPref", 0);
        preferencesSettings = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        prefChoice = getContext().getSharedPreferences("choice", 0);
        editorChoice = prefChoice.edit();
        editorTimes = prefTimesEdited.edit();

        layoutMisc = view.findViewById(R.id.layout_miscellaneous);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);
        ImageView imageBack = view.findViewById(R.id.image_back);
        ImageView imgTextStyle = view.findViewById(R.id.image_note_text_style);
        imgTextStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStylePopupMenu(v);
            }
        });
        inputNoteTitle = view.findViewById(R.id.input_note_title);
        inputNoteSubTitle = view.findViewById(R.id.input_note_sub_title);
        inputNoteText = view.findViewById(R.id.input_note);
        textDateTime = view.findViewById(R.id.text_date_time);
        viewSubTitleIndicator = view.findViewById(R.id.view_sub_title_indicator);
        ImageView imgInfo = view.findViewById(R.id.image_info_note);
        ImageView imgShare = view.findViewById(R.id.image_share);
        imageNote = view.findViewById(R.id.image_note);
        textWebUrlRef = view.findViewById(R.id.text_web_ref);
        layoutWebRef = view.findViewById(R.id.layout_web_ref);
        textDateTime.setText(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));
        imgDraw = view.findViewById(R.id.image_draw);

        AdView mAdView = view.findViewById(R.id.banner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if (SettingsFragment.getIsPurchased(getContext())) {
            mAdView.setVisibility(View.GONE);
        }

        if (getActivity().getIntent().hasExtra("folder")) {
            folder = (Folder) getActivity().getIntent().getSerializableExtra("folder");
        }

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(getActivity());
                //onBackPressed();
            }
        });

        imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alreadyAvailableNoteInFolder != null) {
                    if (imageNote.getVisibility() == View.VISIBLE || imgDraw.getVisibility() == View.VISIBLE) {
                        shareDialog(view);
                    } else {
                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableNoteInFolder.getTitle());
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, alreadyAvailableNoteInFolder.getSubTitle());
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableNoteInFolder.getTitle() + "\n" +
                                alreadyAvailableNoteInFolder.getSubTitle() + "\n" +
                                alreadyAvailableNoteInFolder.getNoteText());
                        startActivity(Intent.createChooser(intent, getString(R.string.share_label)));
                    }
                } else {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                            getContext(), getString(R.string.error_toast),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView imgSave = view.findViewById(R.id.image_save);
        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!SettingsFragment.getIsPurchased(getContext())) {
                    if (mInterstitialAd.isLoaded()) mInterstitialAd.show();
                }
                hideKeyboard(getActivity());
                saveNote();
            }
        });

        imgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alreadyAvailableNoteInFolder != null) {
                    showInfoNoteDialog(alreadyAvailableNoteInFolder.getDateTime(),
                            alreadyAvailableNoteInFolder.getDateTimeEdit(), prefTimesEdited.getInt("timesEdited", 0),
                            alreadyAvailableNoteInFolder.getNoteText().length() +
                                    alreadyAvailableNoteInFolder.getTitle().length() +
                                    alreadyAvailableNoteInFolder.getSubTitle().length(), view);
                } else {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                            getContext(), getString(R.string.info_img_error_toast),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        final ImageView imgReadMode = view.findViewById(R.id.image_read_mode);
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
                        imgReadMode.setBackground(ContextCompat.getDrawable(getContext(),
                                R.drawable.background_add_btn));
                    }
                } else {
                    isReadModeOn = true;
                    inputNoteTitle.setEnabled(true);
                    inputNoteSubTitle.setEnabled(true);
                    inputNoteText.setEnabled(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        imgReadMode.setBackground(ContextCompat.getDrawable(getContext(),
                                R.drawable.background_simple));
                    }
                }
            }
        });

        view.findViewById(R.id.img_remove_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textWebUrlRef.setText("");
                layoutWebRef.setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.img_remove_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                view.findViewById(R.id.img_remove_image).setVisibility(View.GONE);
                selectedImgPath = "";
            }
        });

        view.findViewById(R.id.img_remove_draw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alreadyAvailableNoteInFolder != null) {
                    imgDraw.setImageBitmap(null);
                    imgDraw.setVisibility(View.GONE);
                    view.findViewById(R.id.img_remove_draw).setVisibility(View.GONE);
                    alreadyAvailableNoteInFolder.setDrawPath("");
                    path = "";
                } else {
                    imgDraw.setImageBitmap(null);
                    imgDraw.setVisibility(View.GONE);
                    view.findViewById(R.id.img_remove_draw).setVisibility(View.GONE);
                }
            }
        });

        initMisc(view);
        setSubTitleIndicator();
        return view;
    }

    private void showInfoNoteDialog(String dateTimeCreated, String dateTimeEdited, int timesEdited, int lengthOfCymbals, final View viewContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_info_about_note,
                        (ViewGroup) viewContext.findViewById(R.id.layout_info_about_note_container));
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

    private void setViewOrUpdateNote(final View viewContext) {
        inputNoteTitle.setText(alreadyAvailableNoteInFolder.getTitle());
        inputNoteSubTitle.setText(alreadyAvailableNoteInFolder.getSubTitle());
        inputNoteText.setText(alreadyAvailableNoteInFolder.getNoteText());
        textDateTime.setText(alreadyAvailableNoteInFolder.getDateTime());
        editorTimes.putInt("timesEdited", prefTimesEdited.getInt("timesEdited", 0) + 1);
        editorTimes.apply();

        if (alreadyAvailableNoteInFolder != null && alreadyAvailableNoteInFolder.getNoteColor() != null) {
            setTextColor(alreadyAvailableNoteInFolder.getNoteColor());
        }

        if (alreadyAvailableNoteInFolder != null && alreadyAvailableNoteInFolder.getFontStyle() != null) {
            setFont(alreadyAvailableNoteInFolder.getFontStyle());
        }

        if (alreadyAvailableNoteInFolder != null && alreadyAvailableNoteInFolder.getTextSize() != null) {
            setSize(alreadyAvailableNoteInFolder.getTextSize());
        }

        if (alreadyAvailableNoteInFolder.getImagePath() != null && !alreadyAvailableNoteInFolder.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNoteInFolder.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            viewContext.findViewById(R.id.img_remove_image).setVisibility(View.VISIBLE);
            selectedImgPath = alreadyAvailableNoteInFolder.getImagePath();
        }

        if (alreadyAvailableNoteInFolder.getDrawPath() != null && !alreadyAvailableNoteInFolder.getDrawPath().trim().isEmpty()) {
            try {
                InputStream is = getContext().getContentResolver().openInputStream(Uri.parse(alreadyAvailableNoteInFolder.getDrawPath()));
                imgDraw.setImageBitmap(BitmapFactory.decodeStream(is));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            imgDraw.setVisibility(View.VISIBLE);
            viewContext.findViewById(R.id.img_remove_draw).setVisibility(View.VISIBLE);
            path = alreadyAvailableNoteInFolder.getDrawPath();
        }

        if (alreadyAvailableNoteInFolder.getWebLink() != null && !alreadyAvailableNoteInFolder.getWebLink().trim().isEmpty()) {
            textWebUrlRef.setText(alreadyAvailableNoteInFolder.getWebLink());
            layoutWebRef.setVisibility(View.VISIBLE);
        }
    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(getContext(),
                    getString(R.string.toast_error_title_empty),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (inputNoteSubTitle.getText().toString().trim().isEmpty()
                && inputNoteText.getText().toString().trim().isEmpty()) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(getContext(),
                    getString(R.string.toast_error_empty_note),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        final NoteInFolder noteInFolder = new NoteInFolder();
        final SimpleNote simpleNote = new SimpleNote();

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

            if (layoutWebRef.getVisibility() == View.VISIBLE) {
                noteInFolder.setWebLink(textWebUrlRef.getText().toString());
            }

            if (alreadyAvailableNoteInFolder != null) {
                noteInFolder.setId(alreadyAvailableNoteInFolder.getId());
                noteInFolder.setDateTimeEdit(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date()));
            }

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

            if (layoutWebRef.getVisibility() == View.VISIBLE) {
                simpleNote.setWebLink(textWebUrlRef.getText().toString());
            }

            if (alreadyAvailableNoteInFolder != null) {
                simpleNote.setId(alreadyAvailableNoteInFolder.getId());
                simpleNote.setDateTimeEdit(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date()));
            }

        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteAsyncTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                if (folder != null) {
                    NoteInFolderDatabase.getNoteDatabase(getContext().getApplicationContext()).getNoteDAO().insertNote(noteInFolder);
                } else {
                    NoteDatabase.getNoteDatabase(getContext().getApplicationContext()).getNoteDAO().insertNote(simpleNote);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Bundle result = new Bundle();
                result.putString(String.valueOf(ADD_NOTE_CODE), "result");
                hideKeyboard(getActivity());
                getParentFragmentManager().setFragmentResult(String.valueOf(ADD_NOTE_CODE), result);
                NotesFragment notesFragment = new NotesFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.replaced_container, notesFragment)
                        .commit();
            }
        }
        new SaveNoteAsyncTask().execute();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) view = new View(activity);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_SELECT_IMG && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImgUri = data.getData();
                if (selectedImgUri != null) {
                    try {
                        InputStream is = getContext().getContentResolver().openInputStream(selectedImgUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        getView().findViewById(R.id.img_remove_image).setVisibility(View.VISIBLE);
                        selectedImgPath = getPathFromUri(selectedImgUri);
                    } catch (Exception e) {
                        if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                                getContext(), getString(R.string.error_add_img_toast),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void initMisc(final View view) {
        final ImageView attach = view.findViewById(R.id.attach);
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

        if (alreadyAvailableNoteInFolder != null && alreadyAvailableNoteInFolder.getColor() != null && !alreadyAvailableNoteInFolder.getColor()
                .trim().isEmpty()) {
            switch (alreadyAvailableNoteInFolder.getColor()) {
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
                if (ContextCompat.checkSelfPermission(getContext().getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]
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
                if (ContextCompat.checkSelfPermission(getContext().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]
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
                showAddDrawDialog(view);
            }
        });

        layoutMisc.findViewById(R.id.layout_add_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddRefDialog(view);
            }
        });


        if (alreadyAvailableNoteInFolder != null) {
            layoutMisc.findViewById(R.id.layout_add_reminder).setVisibility(View.VISIBLE);
            layoutMisc.findViewById(R.id.layout_add_reminder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    openRemindDialog(view);
                }
            });
        }

        if (alreadyAvailableNoteInFolder != null) {
            layoutMisc.findViewById(R.id.layout_delete_note).setVisibility(View.VISIBLE);
            layoutMisc.findViewById(R.id.layout_delete_note).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog(view);
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

    private void showAddDrawDialog(final View viewContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final View view = LayoutInflater.from(getContext())
                .inflate(R.layout.content_draw,
                        (ViewGroup) viewContext.findViewById(R.id.content_draw_container));
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
                        getContext().getContentResolver(), paintView.getDrawingCache(),
                        UUID.randomUUID().toString() + ".png",
                        "drawing"
                );
                if (imgSaved != null) {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                            getContext(), R.string.saved,
                            Toast.LENGTH_SHORT).show();
                    if (alreadyAvailableNoteInFolder != null) {
                        alreadyAvailableNoteInFolder.setDrawPath(imgSaved);
                        path = alreadyAvailableNoteInFolder.getDrawPath();
                    }
                    path = imgSaved;
                    imgDraw.setVisibility(View.VISIBLE);
                    viewContext.findViewById(R.id.img_remove_draw).setVisibility(View.VISIBLE);
                    InputStream is = null;
                    try {
                        is = getContext().getContentResolver().openInputStream(Uri.parse(imgSaved));
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
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
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
        PopupMenu textStyle = new PopupMenu(getContext(), v);
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
                Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/ubuntu_bold.ttf");
                Typeface typeface2 = Typeface.createFromAsset(getContext().getAssets(), "fonts/ubuntu_medium.ttf");
                Typeface typeface3 = Typeface.createFromAsset(getContext().getAssets(), "fonts/ubuntu_regular.ttf");
                inputNoteTitle.setTypeface(typeface);
                inputNoteSubTitle.setTypeface(typeface2);
                inputNoteText.setTypeface(typeface3);
                fontStyle = type;
                break;
            }
            case "comissioner": {
                Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/comm_black.ttf");
                Typeface typeface2 = Typeface.createFromAsset(getContext().getAssets(), "fonts/comm_medium.ttf");
                Typeface typeface3 = Typeface.createFromAsset(getContext().getAssets(), "fonts/comm_thin.ttf");
                inputNoteTitle.setTypeface(typeface);
                inputNoteSubTitle.setTypeface(typeface2);
                inputNoteText.setTypeface(typeface3);
                fontStyle = type;
                break;
            }
            case "roboto": {
                Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/robotoslab_black.ttf");
                Typeface typeface2 = Typeface.createFromAsset(getContext().getAssets(), "fonts/robotoslab_regular.ttf");
                Typeface typeface3 = Typeface.createFromAsset(getContext().getAssets(), "fonts/robotoslab_thin.ttf");
                inputNoteTitle.setTypeface(typeface);
                inputNoteSubTitle.setTypeface(typeface2);
                inputNoteText.setTypeface(typeface3);
                fontStyle = type;
                break;
            }
            case "sourcecode": {
                Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/sourcecode_black.ttf");
                Typeface typeface2 = Typeface.createFromAsset(getContext().getAssets(), "fonts/source_regular.ttf");
                Typeface typeface3 = Typeface.createFromAsset(getContext().getAssets(), "fonts/source_light.ttf");
                inputNoteTitle.setTypeface(typeface);
                inputNoteSubTitle.setTypeface(typeface2);
                inputNoteText.setTypeface(typeface3);
                fontStyle = type;
                break;
            }
        }
    }


    private void openRemindDialog(final View viewContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_picker,
                        (ViewGroup) viewContext.findViewById(R.id.layout_dialog_picker_container));
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
                alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                final Calendar c = Calendar.getInstance();
                Intent intent = new Intent(getContext().getApplicationContext(), AlarmReceiver.class);
                intent.putExtra("nameOfNote", alreadyAvailableNoteInFolder.getTitle());
                c.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                c.set(Calendar.MINUTE, timePicker.getMinute());
                pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                dialogRemind.dismiss();
            }
        });
        dialogRemind.show();
    }

    public boolean myOnKeyDown(int keyCode, KeyEvent keyEvent){
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
        return true;
    }

//    @Override
//    public void onBackPressed() {
//        if (preferencesSettings.getBoolean("clear_settings", false)) {
//            SharedPreferences.Editor editor = preferencesSettings.edit();
//            editor.putBoolean("clear_settings", false);
//            editor.apply();
//            openQuitDialog(getView());
//        } else {
//            if (prefChoice.getBoolean("choice_is_check", false)) {
//                String save = prefChoice.getString("choice_is_save", "");
//                if (save.equals("save")) {
//                    saveNote();
//                    Intent intent = new Intent(getContext(), NotesFragment.class);
//                    intent.putExtra("isFromBackKey", true);
//                    startActivity(intent);
//                } else {
//                    Intent intent = new Intent(getContext(), NotesFragment.class);
//                    intent.putExtra("isFromBackKey", true);
//                    startActivity(intent);
//                }
//            } else {
//                openQuitDialog();
//            }
//        }
//    }

    private void openQuitDialog(final View viewContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_exit_save_note,
                        (ViewGroup) viewContext.findViewById(R.id.layout_exit_save_note_container));
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
                    Intent intent = new Intent(getContext(), NotesFragment.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                } else {
                    editorChoice.putString("choice_is_save", "");
                    editorChoice.apply();
                    Intent intent = new Intent(getContext(), NotesFragment.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                }
            }
        });
        view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogExitSave.dismiss();
                Intent intent = new Intent(getContext(), NotesFragment.class);
                intent.putExtra("isFromBackKey", true);
                startActivity(intent);
            }
        });
        dialogExitSave.show();
    }

    private void shareDialog(final View viewContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_share_note,
                        (ViewGroup) viewContext.findViewById(R.id.layout_share_note_container));
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
                    intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableNoteInFolder.getTitle());
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, alreadyAvailableNoteInFolder.getSubTitle());
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableNoteInFolder.getTitle() + "\n" +
                            alreadyAvailableNoteInFolder.getSubTitle() + "\n" +
                            alreadyAvailableNoteInFolder.getNoteText());
                    Bitmap bitmap = ((BitmapDrawable) imageNote.getDrawable()).getBitmap();
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                            bitmap, "image", null);
                    Uri imageDrawUri = Uri.parse(path);
                    intent.putExtra(Intent.EXTRA_STREAM, imageDrawUri);
                    shareDialog.dismiss();
                    startActivity(Intent.createChooser(intent, getString(R.string.share_label)));
                } else {
                    Toast.makeText(getContext(), getString(R.string.no_img_in_note_share_error), Toast.LENGTH_SHORT).show();
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
                    intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableNoteInFolder.getTitle());
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, alreadyAvailableNoteInFolder.getSubTitle());
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableNoteInFolder.getTitle() + "\n" +
                            alreadyAvailableNoteInFolder.getSubTitle() + "\n" +
                            alreadyAvailableNoteInFolder.getNoteText());
                    Bitmap drawBitmap = ((BitmapDrawable) imgDraw.getDrawable()).getBitmap();
                    ByteArrayOutputStream bytesDraw = new ByteArrayOutputStream();
                    drawBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytesDraw);
                    String drawPath = MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                            drawBitmap, "image_draw", null);
                    Uri imageDrawUri = Uri.parse(drawPath);
                    intent.putExtra(Intent.EXTRA_STREAM, imageDrawUri);
                    shareDialog.dismiss();
                    startActivity(Intent.createChooser(intent, getString(R.string.share_label)));
                } else {
                    Toast.makeText(getContext(), getString(R.string.no_draw_in_note_share_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
        view.findViewById(R.id.text_only_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, alreadyAvailableNoteInFolder.getTitle());
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, alreadyAvailableNoteInFolder.getSubTitle());
                intent.putExtra(android.content.Intent.EXTRA_TEXT, alreadyAvailableNoteInFolder.getTitle() + "\n" +
                        alreadyAvailableNoteInFolder.getSubTitle() + "\n" +
                        alreadyAvailableNoteInFolder.getNoteText());
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

    private void showDeleteNoteDialog(final View viewContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_delete_note,
                        (ViewGroup) viewContext.findViewById(R.id.layout_delete_note_container));
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

    private void setSubTitleIndicator() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubTitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void selectImg() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, CODE_SELECT_IMG);
        }
    }

    public String getPathFromUri(Uri uri) {
        String pathToFile;
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
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
                        getContext(), getString(R.string.error_toast_perm_denied),
                        Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PERMISSION_RECORD_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enterVoice();
            } else {
                if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                        getContext(), getString(R.string.toast_error_record_denied),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showAddRefDialog(final View viewContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_add_url,
                        (ViewGroup) viewContext
                                .findViewById(R.id.layout_add_url_container));
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
                    if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                            getContext(), getString(R.string.error_toast_ref_empty),
                            Toast.LENGTH_SHORT).show();
                } else if (!Patterns.WEB_URL.matcher(inputRef.getText().toString().trim()).matches()) {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                            getContext(), getString(R.string.error_toast_not_valid_ref),
                            Toast.LENGTH_SHORT).show();
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

    @SuppressLint("StaticFieldLeak")
    class DeleteNoteAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            NoteDatabase.getNoteDatabase(getContext().getApplicationContext()).getNoteDAO().deleteNote(alreadyAvailableNoteInFolder);
            PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(RoomWorker.class,
                    23,
                    TimeUnit.HOURS,
                    23,
                    TimeUnit.HOURS)
                    .build();
            WorkManager.getInstance().enqueue(workRequest);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Bundle result = new Bundle();
            result.putString("delete", "result");
            result.putBoolean("isNoteDeleted", true);
            result.putString("noteTitle", alreadyAvailableNoteInFolder.getTitle());
            result.putString("noteSubtitle", alreadyAvailableNoteInFolder.getSubTitle());
            result.putString("noteText", alreadyAvailableNoteInFolder.getNoteText());
            hideKeyboard(getActivity());
            getParentFragmentManager().setFragmentResult("requestKey", result);
        }
    }
}