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
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.textfield.TextInputEditText;
import com.shashank.sony.fancytoastlib.FancyToast;
import java.util.concurrent.TimeUnit;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.other.BaseActivity;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.other.AdWorker;
import static spiral.bit.dev.sunshinenotes.other.Utils.hideKeyboard;

public class PasswordActivity extends AppCompatActivity {

    private TextInputEditText inputPassword;
    private SharedPreferences prefPass;
    private SharedPreferences preferenceSettings;
    private SharedPreferences.Editor editPass;
    private LottieAnimationView animationView;
    private SharedPreferences.Editor editorGraphicKey, editorPrefPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        prefPass = getApplicationContext().getSharedPreferences("pass", 0);
        SharedPreferences graphicPref = getSharedPreferences("graphic", 0);
        editorGraphicKey = graphicPref.edit();
        editorPrefPin = prefPass.edit();
        preferenceSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefPass = getSharedPreferences("password", 0);
        editPass = prefPass.edit();
        inputPassword = findViewById(R.id.input_password);
        animationView = findViewById(R.id.animation_unlock);
        inputPassword.requestFocus();
        Intent intent = getIntent();
        final AdView mAdView = findViewById(R.id.adView);
        MobileAds.initialize(PasswordActivity.this, new OnInitializationCompleteListener() {
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
                WorkManager workManager = WorkManager.getInstance(PasswordActivity.this);
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
                WorkManager workManager = WorkManager.getInstance(PasswordActivity.this);
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            }
        });
        if (SettingsFragment.getIsPurchased(PasswordActivity.this)) mAdView.setVisibility(View.GONE);
        if (intent.hasExtra("inputPassword")) {
            TextView textSubmitPassword = findViewById(R.id.text_pin_code);
            textSubmitPassword.setText(R.string.submit_label);
            findViewById(R.id.text_cancel).setVisibility(View.GONE);
            findViewById(R.id.label_password_desc).setVisibility(View.GONE);
            TextView label = findViewById(R.id.label_set_up);
            label.setText(R.string.enter_your_password);
            inputPassword.setHint(R.string.hint_pass);
            inputPassword.setHintTextColor(getResources().getColor(R.color.colorTextHint));
            findViewById(R.id.text_pin_code).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!prefPass.getString("passwordCode", "").equals(inputPassword.getText().toString())) {
                        hideKeyboard(PasswordActivity.this);
                        if (preferenceSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(PasswordActivity.this,
                                getString(R.string.password_isnt_right),
                                FancyToast.LENGTH_LONG,
                                FancyToast.ERROR,
                                false).show();
                    } else {
                        final Intent intent = new Intent(PasswordActivity
                                .this, BaseActivity.class);
                        animationView.setVisibility(View.VISIBLE);
                        hideKeyboard(PasswordActivity.this);
                        animationView.playAnimation();
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    TimeUnit.MILLISECONDS.sleep(2100);
                                    intent.putExtra("isFromBackKey", true);
                                    startActivity(intent);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                    }
                }
            });
        } else {
            inputPassword.setHint(R.string.password_add_label);
            inputPassword.setHintTextColor(getResources().getColor(R.color.colorTextHint));
            findViewById(R.id.text_pin_code).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editPass.putString("passwordCode", inputPassword.getText().toString());
                    editorPrefPin.clear();
                    editorGraphicKey.clear();
                    editorPrefPin.apply();
                    editorGraphicKey.apply();
                    editPass.apply();
                    hideKeyboard(PasswordActivity.this);
                    Intent intent = new Intent(PasswordActivity
                            .this, BaseActivity.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                    if (preferenceSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(PasswordActivity.this,
                            getString(R.string.password_set_up),
                            FancyToast.LENGTH_LONG,
                            FancyToast.SUCCESS,
                            false).show();
                }
            });
            findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideKeyboard(PasswordActivity.this);
                    Intent intent = new Intent(PasswordActivity.this, BaseActivity.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }
}