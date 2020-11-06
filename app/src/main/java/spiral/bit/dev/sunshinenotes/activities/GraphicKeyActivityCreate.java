package spiral.bit.dev.sunshinenotes.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

import spiral.bit.dev.sunshinenotes.R;

public class GraphicKeyActivityCreate extends AppCompatActivity {

    private PatternLockView graphicKey;
    private SharedPreferences preferencesSettings, prefPass, prefPassword;
    private SharedPreferences.Editor editorPrefPin, editorPrefPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphic_key_create);
        prefPass = getApplicationContext().getSharedPreferences("pass", 0);
        prefPassword = getSharedPreferences("password", 0);
        editorPrefPin = prefPass.edit();
        editorPrefPassword = prefPassword.edit();
        preferencesSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        graphicKey = findViewById(R.id.graphic_view);
        final TextView addGraphicKey = findViewById(R.id.text_graphic_code);
        graphicKey.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
            }
            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
            }
            @Override
            public void onComplete(final List<PatternLockView.Dot> pattern) {
                addGraphicKey.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences preferences = getSharedPreferences("graphic", 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("graphic_key", PatternLockUtils.patternToString(graphicKey, pattern));
                        editorPrefPin.clear();
                        editorPrefPassword.clear();
                        editorPrefPin.apply();
                        editorPrefPassword.apply();
                        editor.apply();
                        Intent intent = new Intent(GraphicKeyActivityCreate.this, MainActivity.class);
                        intent.putExtra("isFromBackKey", true);
                        startActivity(intent);
                        if (!preferencesSettings.getBoolean("remove_toasts", true)) {
                        } else {
                            Toast.makeText(GraphicKeyActivityCreate.this, R.string.set, Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                });
            }

            @Override
            public void onCleared() {
            }
        });
    }
}