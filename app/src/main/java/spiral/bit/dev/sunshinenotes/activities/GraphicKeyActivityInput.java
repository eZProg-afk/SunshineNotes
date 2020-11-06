package spiral.bit.dev.sunshinenotes.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

import spiral.bit.dev.sunshinenotes.R;

public class GraphicKeyActivityInput extends AppCompatActivity {

    private PatternLockView graphicKey;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphic_key_input);
        final TextView textWrongLabel = findViewById(R.id.text_graphic_code);
        SharedPreferences preferences = getSharedPreferences("graphic", 0);
        password = preferences.getString("graphic_key", "");
        graphicKey = findViewById(R.id.graphic_view);
        graphicKey.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
            }

            @Override
            public void onComplete(final List<PatternLockView.Dot> pattern) {
                if (password.equals(PatternLockUtils.patternToString(graphicKey, pattern))) {
                    Intent intent = new Intent(GraphicKeyActivityInput.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    textWrongLabel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCleared() {
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }
}