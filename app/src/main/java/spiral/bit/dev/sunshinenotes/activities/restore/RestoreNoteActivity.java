package spiral.bit.dev.sunshinenotes.activities.restore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.other.BaseActivity;
import spiral.bit.dev.sunshinenotes.data.NoteDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashDatabase;
import spiral.bit.dev.sunshinenotes.databinding.ActivityRestoreNoteBinding;
import spiral.bit.dev.sunshinenotes.fragments.NotesFragment;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.models.Folder;
import spiral.bit.dev.sunshinenotes.models.SimpleNote;
import spiral.bit.dev.sunshinenotes.models.trash.TrashNote;
import spiral.bit.dev.sunshinenotes.other.AdWorker;

import static spiral.bit.dev.sunshinenotes.other.Utils.hideKeyboard;

public class RestoreNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle, inputNoteSubTitle, inputNoteText;
    private ActivityRestoreNoteBinding restoreNoteBinding;
    private TextView textDateTime, textWebUrlRef;
    private View viewSubTitleIndicator;
    private AdView mAdView;
    private SharedPreferences prefTimesEdited, preferencesSettings, prefChoice;
    private SharedPreferences.Editor editorTimes, editorChoice;
    private ImageView imageNote, imgDraw;
    private String selectedImgPath = "", textSize, textColor,
            selectedNoteColor = "#333333", fontStyle = "", path = "";
    private LinearLayout layoutMisc, layoutWebRef;
    private AlertDialog dialogDeleteNote, dialogInfoNote;
    private SimpleNote alreadyAvailableNote;
    private TrashNote alreadyDeletedNote;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private InterstitialAd mInterstitialAd;
    private Folder folder;
    private String type = "";

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreNoteBinding = ActivityRestoreNoteBinding.inflate(getLayoutInflater());
        View view = restoreNoteBinding.getRoot();
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
                WorkManager workManager = WorkManager.getInstance(RestoreNoteActivity.this);
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
                WorkManager workManager = WorkManager.getInstance(RestoreNoteActivity.this);
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
        layoutMisc = findViewById(R.id.layout_misc_restore);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);
        inputNoteTitle = restoreNoteBinding.inputNoteTitle;
        inputNoteSubTitle = restoreNoteBinding.inputNoteSubTitle;
        inputNoteText = restoreNoteBinding.inputNote;
        textDateTime = restoreNoteBinding.textDateTime;
        viewSubTitleIndicator = restoreNoteBinding.viewSubTitleIndicator;
        imageNote = restoreNoteBinding.imageNote;
        textWebUrlRef = restoreNoteBinding.textWebRef;
        layoutWebRef = restoreNoteBinding.layoutWebRef;
        imgDraw = restoreNoteBinding.imageDraw;
        mAdView = restoreNoteBinding.banner;

        //Listeners
        restoreNoteBinding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(RestoreNoteActivity.this);
                onBackPressed();
            }
        });

        restoreNoteBinding.imageInfoNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alreadyAvailableNote != null) {
                    showInfoNoteDialog(alreadyAvailableNote.getDateTime(),
                            alreadyAvailableNote.getDateTimeEdit(), prefTimesEdited.getInt("timesEdited", 0),
                            alreadyAvailableNote.getNoteText().length() +
                                    alreadyAvailableNote.getTitle().length() +
                                    alreadyAvailableNote.getSubTitle().length());
                } else {
                    if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(
                            RestoreNoteActivity.this,
                            getString(R.string.info_img_error_toast),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            false).show();
                }
            }
        });

        restoreNoteBinding.imgRemoveLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textWebUrlRef.setText("");
                layoutWebRef.setVisibility(View.GONE);
            }
        });

        restoreNoteBinding.imgRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.img_remove_image).setVisibility(View.GONE);
                selectedImgPath = "";
            }
        });

        restoreNoteBinding.imgRemoveDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alreadyDeletedNote != null) {
                    imgDraw.setImageBitmap(null);
                    imgDraw.setVisibility(View.GONE);
                    restoreNoteBinding.imgRemoveDraw.setVisibility(View.GONE);
                    alreadyDeletedNote.setDrawPath("");
                    path = "";
                } else {
                    imgDraw.setImageBitmap(null);
                    imgDraw.setVisibility(View.GONE);
                    restoreNoteBinding.imgRemoveDraw.setVisibility(View.GONE);
                }
            }
        });

        MobileAds.initialize(RestoreNoteActivity.this, new OnInitializationCompleteListener() {
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
                WorkManager workManager = WorkManager.getInstance(RestoreNoteActivity.this);
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
                WorkManager workManager = WorkManager.getInstance(RestoreNoteActivity.this);
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            }
        });
        if (SettingsFragment.getIsPurchased(RestoreNoteActivity.this))
            mAdView.setVisibility(View.GONE);

        if (getIntent().hasExtra("isViewOrUpdate")) {
            alreadyDeletedNote = (TrashNote) getIntent().getSerializableExtra("trash_note");
            setViewOrUpdateNote();
        }

        textDateTime.setText(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date()));

        initMisc();
        setSubTitleIndicator();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobileAds.initialize(RestoreNoteActivity.this, new OnInitializationCompleteListener() {
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
                restoreNote();
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
    class DeleteNoteAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            TrashDatabase.getNoteDatabase(getApplicationContext())
                    .getTrashDAO().deleteTrashNote(alreadyDeletedNote);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent(RestoreNoteActivity.this, BaseActivity.class);
            intent.putExtra("isNoteDeleted", true);
            intent.putExtra("noteTitle", alreadyDeletedNote.getTitle());
            intent.putExtra("noteSubtitle", alreadyDeletedNote.getSubTitle());
            intent.putExtra("noteText", alreadyDeletedNote.getNoteText());
            hideKeyboard(RestoreNoteActivity.this);
            if (!SettingsFragment.getIsPurchased(RestoreNoteActivity.this)) {
                if (mInterstitialAd.isLoaded() && !preferencesSettings.getBoolean("time_block_ads", false)) {
                    mInterstitialAd.show();
                }
            }
            hideKeyboard(RestoreNoteActivity.this);
            startActivity(intent);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RestoreNoteActivity.this, BaseActivity.class);
        intent.putExtra("isFromBackKey", true);
        startActivity(intent);
    }

    private void setViewOrUpdateNote() {
        inputNoteTitle.setText(alreadyDeletedNote.getTitle());
        inputNoteSubTitle.setText(alreadyDeletedNote.getSubTitle());
        inputNoteText.setText(alreadyDeletedNote.getNoteText());
        textDateTime.setText(alreadyDeletedNote.getDateTime());
        editorTimes.putInt("timesEdited", prefTimesEdited.getInt("timesEdited", 0) + 1);
        editorTimes.apply();

        if (alreadyDeletedNote != null && alreadyDeletedNote.getNoteColor() != null)
            setTextColor(alreadyDeletedNote.getNoteColor());

        if (alreadyDeletedNote != null && alreadyDeletedNote.getFontStyle() != null)
            setFont(alreadyDeletedNote.getFontStyle());

        if (alreadyDeletedNote != null && alreadyDeletedNote.getTextSize() != null)
            setSize(alreadyDeletedNote.getTextSize());

        if (alreadyDeletedNote.getImagePath() != null && !alreadyDeletedNote.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyDeletedNote.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            restoreNoteBinding.imgRemoveImage.setVisibility(View.VISIBLE);
            selectedImgPath = alreadyDeletedNote.getImagePath();
        }

        if (alreadyDeletedNote.getDrawPath() != null && !alreadyDeletedNote.getDrawPath().trim().isEmpty()) {
            try {
                InputStream is = getContentResolver().openInputStream(Uri.parse(alreadyDeletedNote.getDrawPath()));
                imgDraw.setImageBitmap(BitmapFactory.decodeStream(is));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            imgDraw.setVisibility(View.VISIBLE);
            findViewById(R.id.img_remove_draw).setVisibility(View.VISIBLE);
            path = alreadyDeletedNote.getDrawPath();
        }

        if (alreadyDeletedNote.getWebLink() != null && !alreadyDeletedNote.getWebLink().trim().isEmpty()) {
            textWebUrlRef.setText(alreadyDeletedNote.getWebLink());
            layoutWebRef.setVisibility(View.VISIBLE);
        }
    }

    private void restoreNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(
                    RestoreNoteActivity.this,
                    getString(R.string.toast_error_title_empty),
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false).show();
            return;
        } else if (inputNoteSubTitle.getText().toString().trim().isEmpty()
                && inputNoteText.getText().toString().trim().isEmpty()) {
            if (preferencesSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(
                    RestoreNoteActivity.this,
                    getString(R.string.toast_error_title_empty),
                    FancyToast.LENGTH_LONG,
                    FancyToast.ERROR,
                    false).show();
            return;
        }

        final SimpleNote simpleNote = new SimpleNote();
        simpleNote.setTitle(inputNoteTitle.getText().toString());
        simpleNote.setSubTitle(inputNoteSubTitle.getText().toString());
        simpleNote.setNoteText(inputNoteText.getText().toString());
        simpleNote.setDateTime(textDateTime.getText().toString());
        simpleNote.setColor(alreadyDeletedNote.getColor());
        simpleNote.setImagePath(selectedImgPath);
        simpleNote.setDrawPath(path);
        simpleNote.setFontStyle(fontStyle);
        simpleNote.setTextSize(textSize);
        simpleNote.setNoteColor(textColor);

        if (layoutWebRef.getVisibility() == View.VISIBLE) {
            simpleNote.setWebLink(textWebUrlRef.getText().toString());
        }

        if (alreadyDeletedNote != null) {
            simpleNote.setId(alreadyDeletedNote.getId());
            simpleNote.setDateTimeEdit(new SimpleDateFormat("EEEE, dd, MMMM yyyy HH:mm a", Locale.getDefault())
                    .format(new Date()));
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteAsyncTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                NoteDatabase.getNoteDatabase(getApplicationContext()).getNoteDAO().insertNote(simpleNote);
                TrashDatabase.getNoteDatabase(getApplicationContext()).getTrashDAO()
                        .deleteTrashNote(alreadyDeletedNote);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideKeyboard(RestoreNoteActivity.this);
                Intent intent = new Intent(RestoreNoteActivity.this, BaseActivity.class);
                if (!SettingsFragment.getIsPurchased(RestoreNoteActivity.this)) {
                    if (mInterstitialAd.isLoaded() && !preferencesSettings.getBoolean("time_block_ads", false)) {
                        mInterstitialAd.show();
                    }
                }
                hideKeyboard(RestoreNoteActivity.this);
                startActivity(intent);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveNoteAsyncTask().execute();
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
                new RestoreNoteActivity.DeleteNoteAsyncTask().execute();
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
}