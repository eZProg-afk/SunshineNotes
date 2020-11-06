package spiral.bit.dev.sunshinenotes.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.TimeUnit;
import spiral.bit.dev.sunshinenotes.R;

public class PasswordActivity extends AppCompatActivity {

    private TextInputEditText inputPassword;
    private SharedPreferences prefPass, preferenceSettings, graphicPref;
    private SharedPreferences.Editor editPass;
    private LottieAnimationView animationView;
    private SharedPreferences.Editor editorGraphicKey, editorPrefPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        prefPass = getApplicationContext().getSharedPreferences("pass", 0);
        graphicPref = getSharedPreferences("graphic", 0);
        editorGraphicKey = graphicPref.edit();
        editorPrefPin = prefPass.edit();
        preferenceSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefPass = getSharedPreferences("password", 0);
        editPass = prefPass.edit();
        inputPassword = findViewById(R.id.input_password);
        animationView = findViewById(R.id.animation_unlock);
        inputPassword.requestFocus();
        Intent intent = getIntent();
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
                        if (!preferenceSettings.getBoolean("remove_toasts", true)) {
                        } else {
                            Toast.makeText(PasswordActivity.this, R.string.password_isnt_right, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        final Intent intent = new Intent(PasswordActivity
                                .this, MainActivity.class);
                        animationView.setVisibility(View.VISIBLE);
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
                    Intent intent = new Intent(PasswordActivity
                            .this, MainActivity.class);
                    intent.putExtra("isFromBackKey", true);
                    startActivity(intent);
                    if (!preferenceSettings.getBoolean("remove_toasts", true)) {
                    } else {
                        Toast.makeText(PasswordActivity.this, R.string.password_set_up, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(PasswordActivity.this, MainActivity.class);
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