package spiral.bit.dev.sunshinenotes.activities.other;

import android.annotation.SuppressLint;
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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.create.CreateNoteActivity;
import spiral.bit.dev.sunshinenotes.adapter.StatisticAdapter;
import spiral.bit.dev.sunshinenotes.data.StatisticDatabase;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.listeners.StatisticListener;
import spiral.bit.dev.sunshinenotes.models.other.Statistic;
import spiral.bit.dev.sunshinenotes.other.AdWorker;
import static spiral.bit.dev.sunshinenotes.fragments.CheckListFragment.REQUEST_CODE_ENABLE;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_NOTE_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.CHANGE_BACK_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.DELETE_NOTES_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.SHOW_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_NOTE_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.hideKeyboard;
import static spiral.bit.dev.sunshinenotes.other.Utils.returnBitmap;

public class StatisticsActivity extends AppCompatActivity implements StatisticListener {

    private static RecyclerView noteRecyclerView;
    private List<Statistic> tempList;
    private StatisticAdapter adapter;
    private List<Statistic> notesList;
    private int clickedNotePosition = -1;
    private List<Integer> clickedNotePositions;
    private ImageView imgClear;
    private SharedPreferences prefPass, checkedPref, prefPassword, graphicPref, preferenceSettings;
    private SharedPreferences.Editor editorIsShowed;
    private LottieAnimationView nowHereEmpty;
    private EditText searchEditText;
    private TextView labelEmptyNow, labelHint;
    public ImageView imageAddNoteMain, icStateDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        tempList = new ArrayList<>();
        notesList = new ArrayList<>();
        clickedNotePositions = new ArrayList<>();
        preferenceSettings = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
//        if (preferenceSettings.getBoolean("dark", false)) AppCompatDelegate
//                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        icStateDelete = findViewById(R.id.ic_state_delete);
        icStateDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DeleteWholeStatisticAsyncTask().execute();
            }
        });
        checkedPref = getSharedPreferences("check", 0);
        editorIsShowed = checkedPref.edit();
        prefPass = getSharedPreferences("pass", 0);
        prefPassword = getSharedPreferences("password", 0);
        graphicPref = getSharedPreferences("graphic", 0);
        nowHereEmpty = findViewById(R.id.now_empty_view);
        getNotes(SHOW_CODE, false);
        noteRecyclerView = findViewById(R.id.statistics_recycler_view);
        adapter = new StatisticAdapter(notesList, this);
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteRecyclerView.setAdapter(adapter);
        labelEmptyNow = findViewById(R.id.label_empty);
        labelHint = findViewById(R.id.label_hint);
        final AdView mAdView = findViewById(R.id.adView);
        MobileAds.initialize(StatisticsActivity.this, new OnInitializationCompleteListener() {
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
                WorkManager workManager = WorkManager.getInstance(StatisticsActivity.this);
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
                WorkManager workManager = WorkManager.getInstance(StatisticsActivity.this);
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            }
        });

        if (SettingsFragment.getIsPurchased(StatisticsActivity.this))
            mAdView.setVisibility(View.GONE);

        imgClear = findViewById(R.id.ic_clear);
        imageAddNoteMain = findViewById(R.id.icon_add_note_main);
        searchEditText = findViewById(R.id.search_edit_text);
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
                        hideKeyboard(StatisticsActivity.this);
                    }
                });
                adapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                if (notesList.size() != 0) {
                    adapter.searchStatistics(editable.toString());
                    new TextHighlighter()
                            .setBackgroundColor(Color.parseColor("#FFFF00"))
                            .addTarget(findViewById(R.id.text_title))
                            .highlight(searchEditText.getText().toString(), TextHighlighter.BASE_MATCHER);
                }
            }
        });
        imgClear.setVisibility(View.GONE);

        final ImageView settingsImg = findViewById(R.id.icon_settings);
        settingsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StatisticsActivity.this, SettingsActivity.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (preferenceSettings.getBoolean("dark", false))
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
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
                    StatisticsActivity.this,
                    "Пин-код успешно задан!",
                    FancyToast.LENGTH_LONG,
                    FancyToast.SUCCESS,
                    false).show();
        }
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetAllStatisticsAsyncTask extends AsyncTask<Void, Void, List<Statistic>> {
            @Override
            protected List<Statistic> doInBackground(Void... voids) {
                return StatisticDatabase.getStatisticDatabase(getApplicationContext())
                        .getStatisticDAO().getAllStatistic();
            }

            @Override
            protected void onPostExecute(List<Statistic> notes) {
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
                    for (Statistic statistic : tempList) {
                        notesList.remove(statistic);
                        adapter.notifyDataSetChanged();
                    }
                    tempList = new ArrayList<>();
                }
                if (notesList.isEmpty()) {
                    if (preferenceSettings.getInt("back", 0) == 0
                            || preferenceSettings.getInt("picture", 0) == 0) {
                        nowHereEmpty.setVisibility(View.VISIBLE);
                        labelEmptyNow.setVisibility(View.VISIBLE);
                        labelHint.setVisibility(View.VISIBLE);
                        noteRecyclerView.setVisibility(View.GONE);
                        nowHereEmpty.playAnimation();
                    }
                } else {
                    nowHereEmpty.setVisibility(View.GONE);
                    labelHint.setVisibility(View.GONE);
                    labelEmptyNow.setVisibility(View.GONE);
                    noteRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        }
        new GetAllStatisticsAsyncTask().execute();
    }

    @Override
    public void onStatisticsClicked(Statistic statistic, int position) {
        clickedNotePosition = position;
        Intent intent = new Intent(StatisticsActivity.this, CreateNoteActivity.class);
        intent.putExtra("note", statistic);
        intent.putExtra("setViewOrUpdate", true);
        intent.putExtra("isNoteDeleted", false);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(intent, UPDATE_NOTE_CODE);
    }

    @Override
    public void onLongStatisticsClicked(Statistic statistic, int position) {
        //isDeletedModeEnabled = true;
    }

    @SuppressLint("StaticFieldLeak")
    class DeleteWholeStatisticAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
                StatisticDatabase.getStatisticDatabase(getApplicationContext())
                        .getStatisticDAO().deleteStatistic();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getNotes(DELETE_NOTES_CODE, true);
        }
    }
}