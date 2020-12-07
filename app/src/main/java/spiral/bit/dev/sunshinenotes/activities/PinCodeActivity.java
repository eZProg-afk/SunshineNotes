package spiral.bit.dev.sunshinenotes.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.hanks.passcodeview.PasscodeView;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.fragments.NotesFragment;

public class PinCodeActivity extends AppCompatActivity {

    private PasscodeView passCodeView;
    private AlertDialog dialogForgetPinCode, dialogRecreatePin;
    private SharedPreferences prefSecret;
    private SharedPreferences preferenceSettings;
    private SharedPreferences.Editor editor;
    private SharedPreferences.Editor editorPrefPass;
    private EditText inputSecretWord;
    private SharedPreferences.Editor editorGraphicKey, editorPrefPin;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_code);

        SharedPreferences prefPassword = getSharedPreferences("password", 0);
        preferenceSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefSecret = getSharedPreferences("secret", 0);
        SharedPreferences prefPass = getSharedPreferences("password", 0);
        SharedPreferences graphicPref = getSharedPreferences("graphic", 0);

        editorGraphicKey = graphicPref.edit();
        editorPrefPass = prefPassword.edit();
        editor = prefSecret.edit();
        SharedPreferences.Editor editPass = prefPass.edit();
    }

    private void openForgetDialog() {
        if (dialogForgetPinCode == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PinCodeActivity.this);
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.layout_add_pin_code,
                            (ViewGroup) findViewById(R.id.layout_add_pin_code_container));
            builder.setView(view);
            dialogForgetPinCode = builder.create();
            if (dialogForgetPinCode.getWindow() != null) {
                dialogForgetPinCode.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            inputSecretWord = view.findViewById(R.id.input_secret);
            view.findViewById(R.id.text_pin_code).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editor.putString("secretWord", inputSecretWord.getText().toString());
                    editor.apply();
                    if (preferenceSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                            PinCodeActivity.this, getString(R.string.secret_word_added_toast),
                            Toast.LENGTH_SHORT).show();
                    dialogForgetPinCode.dismiss();
                }
            });
            view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogForgetPinCode.dismiss();
                }
            });
        }
        dialogForgetPinCode.show();
    }

    private void openRecreatePinDialog() {
        if (dialogRecreatePin == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PinCodeActivity.this);
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.layout_forget_dialog_code,
                            (ViewGroup) findViewById(R.id.layout_forget_dialog_code_container));
            builder.setView(view);
            dialogRecreatePin = builder.create();
            if (dialogRecreatePin.getWindow() != null) {
                dialogRecreatePin.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            inputSecretWord = view.findViewById(R.id.input_secret);
            view.findViewById(R.id.text_pin_code).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String trueSecretWord = prefSecret.getString("secretWord", "");
                    String enteredSecretWord = inputSecretWord.getText().toString();
                    if (enteredSecretWord.equals(trueSecretWord)) {
                        Intent intent = new Intent(PinCodeActivity.this, NotesFragment.class);
                        intent.putExtra("setForget", true);
                        startActivity(intent);
                    } else {
                        dialogRecreatePin.dismiss();
                        if (preferenceSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                                PinCodeActivity.this, R.string.secret_word_isnt_right,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogRecreatePin.dismiss();
                }
            });
        }
        dialogRecreatePin.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences prefPass = getApplicationContext().getSharedPreferences("pass", 0);
        final SharedPreferences.Editor editor = prefPass.edit();
        passCodeView = findViewById(R.id.passcodeView);
        passCodeView.setPasscodeLength(5);
        final String set = getIntent().getStringExtra("set");
        if ((set != null && set.equals("set")) || prefPass.getString("pin-code", "").isEmpty()) {
            findViewById(R.id.btn_forget_password).setVisibility(View.GONE);
            openForgetDialog();
            passCodeView.setPasscodeType(PasscodeView.PasscodeViewType.TYPE_SET_PASSCODE);
        } else if (!prefPass.getString("pin-code", "").isEmpty()) {
            findViewById(R.id.btn_forget_password).setVisibility(View.VISIBLE);
            passCodeView.setPasscodeType(PasscodeView.PasscodeViewType.TYPE_CHECK_PASSCODE);
            passCodeView.setLocalPasscode(prefPass.getString("pin-code", ""));
        }
        passCodeView.setListener(new PasscodeView.PasscodeViewListener() {
            @Override
            public void onFail() {
                if (preferenceSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                        PinCodeActivity.this, R.string.pin_code_isnt_right,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(String number) {
                editor.putString("pin-code", number);
                editorPrefPass.clear();
                editorGraphicKey.clear();
                editorPrefPass.apply();
                editorGraphicKey.apply();
                editor.apply();
                if ((set != null && set.equals("set")) || prefPass.getString("pin-code", "").isEmpty()) {
                    if (preferenceSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                            PinCodeActivity.this, R.string.pin_code_set,
                            Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(getApplicationContext(), NotesFragment.class);
                if (passCodeView.getPasscodeType() == PasscodeView.PasscodeViewType.TYPE_SET_PASSCODE) {
                    intent.putExtra("isFromSet", true);
                }
                startActivity(intent);
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

    public void forgetPassword(View view) {
        openForgetPinCodeDialog();
    }

    private void openForgetPinCodeDialog() {
        openRecreatePinDialog();
    }
}