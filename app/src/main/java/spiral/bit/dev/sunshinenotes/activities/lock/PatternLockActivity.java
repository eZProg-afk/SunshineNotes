package spiral.bit.dev.sunshinenotes.activities.lock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.List;
import java.util.concurrent.TimeUnit;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.other.BaseActivity;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.other.AdWorker;

public class PatternLockActivity extends AppCompatActivity {

    private PatternLockView graphicKey;
    private SharedPreferences preferencesSettings;
    private SharedPreferences.Editor editorPrefPin, editorPrefPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_lock);
        SharedPreferences prefPass = getApplicationContext().getSharedPreferences("pass", 0);
        SharedPreferences prefPassword = getSharedPreferences("password", 0);
        editorPrefPin = prefPass.edit();
        editorPrefPassword = prefPassword.edit();
        preferencesSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        graphicKey = findViewById(R.id.graphic_view);
        final AdView mAdView = findViewById(R.id.adView);
        MobileAds.initialize(PatternLockActivity.this, new OnInitializationCompleteListener() {
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
                WorkManager workManager = WorkManager.getInstance(PatternLockActivity.this);
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
                WorkManager workManager = WorkManager.getInstance(PatternLockActivity.this);
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            }
        });

        if (SettingsFragment.getIsPurchased(PatternLockActivity.this)) mAdView.setVisibility(View.GONE);
        final SharedPreferences preferences = getSharedPreferences("graphic", 0);
        final SharedPreferences.Editor editor = preferences.edit();
        final TextView addGraphicKey = findViewById(R.id.text_graphic_code);
        final Intent intent = getIntent();

        if (intent.hasExtra("type")) {
            if (intent.getStringExtra("type").equals("create")) {
                addGraphicKey.setText(R.string.add_label);
            } else if (intent.getStringExtra("type").equals("input")) {
                TextView label = findViewById(R.id.label);
                label.setText(getString(R.string.enter_graphic_key));
                addGraphicKey.setVisibility(View.GONE);
                findViewById(R.id.text_cancel).setVisibility(View.GONE);
            }

            graphicKey.addPatternLockListener(new PatternLockViewListener() {
                @Override
                public void onStarted() {
                }

                @Override
                public void onProgress(List<PatternLockView.Dot> progressPattern) {
                }

                @Override
                public void onComplete(final List<PatternLockView.Dot> pattern) {
                    if (intent.hasExtra("type")) {
                        if (intent.getStringExtra("type").equals("create")) {
                            addGraphicKey.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    editor.putString("graphic_key", PatternLockUtils.patternToString(graphicKey, pattern));
                                    editorPrefPin.clear();
                                    editorPrefPassword.clear();
                                    editorPrefPin.apply();
                                    editorPrefPassword.apply();
                                    editor.apply();
                                    Intent intent = new Intent(PatternLockActivity.this, BaseActivity.class);
                                    intent.putExtra("isFromBackKey", true);
                                    startActivity(intent);
                                    if (preferencesSettings.getBoolean("remove_toasts", false))
                                        FancyToast.makeText(PatternLockActivity.this,
                                                getString(R.string.set),
                                                FancyToast.LENGTH_LONG,
                                                FancyToast.SUCCESS,
                                                false).show();
                                    finish();
                                }
                            });
                        } else if (intent.getStringExtra("type").equals("input")) {
                            String password = preferences.getString("graphic_key", "");
                            if (password.equals(PatternLockUtils.patternToString(graphicKey, pattern))) {
                                Intent intent = new Intent(PatternLockActivity.this, BaseActivity.class);
                                intent.putExtra("isFromBackKey", true);
                                startActivity(intent);
                                finish();
                            }
                        }
                    } else onBackPressed();
                }

                @Override
                public void onCleared() {
                }
            });
        }
    }

        @Override
        public void onBackPressed () {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }
    }