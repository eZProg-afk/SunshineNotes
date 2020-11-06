package spiral.bit.dev.sunshinenotes.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.xeoh.android.texthighlighter.TextHighlighter;

import java.util.ArrayList;
import java.util.List;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.SettingsFragment;
import spiral.bit.dev.sunshinenotes.adapters.NoteAdapter;
import spiral.bit.dev.sunshinenotes.data.NoteDatabase;
import spiral.bit.dev.sunshinenotes.listeners.NotesListener;
import spiral.bit.dev.sunshinenotes.models.Note;

public class MainActivity extends AppCompatActivity implements NotesListener {

    public static final int ADD_NOTE_CODE = 12;
    public static final int UPDATE_NOTE_CODE = 13;
    public static final int SHOW_NOTES_CODE = 14;
    public static final int REQUEST_CODE_ENABLE = 1803;
    private static RecyclerView noteRecyclerView;
    private NoteAdapter adapter;
    private List<Note> notesList;
    private int clickedNotePosition = -1;
    private AlertDialog dialogTypeSecurity;
    private AdView mAdView;
    private ImageView imgClear;
    private SharedPreferences prefPass;
    private SharedPreferences checkedPref;
    private SharedPreferences prefPassword;
    private SharedPreferences graphicPref;
    private SharedPreferences preferenceSettings;
    private SharedPreferences.Editor editorIsShowed, editorPrefPin, editorPrefPassword, editorGraphicKey;
    private LottieAnimationView nowHereEmpty;
    private TextView labelEmptyNow, labelHint;
    private Animation rotateOpen, rotateClose;
    private boolean clicked = false;
    public ImageView imageAddNoteMain;

    @Override
    protected void onResume() {
        super.onResume();
        if (preferenceSettings.getBoolean("dark", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (preferenceSettings.getBoolean("dark", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        imageAddNoteMain = findViewById(R.id.icon_add_note_main);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddBtnClicked();
            }
        });
        nowHereEmpty = findViewById(R.id.now_empty_view);
        noteRecyclerView = findViewById(R.id.notes_recycler_view);
        notesList = new ArrayList<>();
        adapter = new NoteAdapter(notesList, this);
        noteRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteRecyclerView.setAdapter(adapter);
        labelEmptyNow = findViewById(R.id.label_empty);
        labelHint = findViewById(R.id.label_hint);
        getNotes(SHOW_NOTES_CODE, false);
        checkedPref = getSharedPreferences("check", 0);
        editorIsShowed = checkedPref.edit();

        prefPass = getSharedPreferences("pass", 0);
        prefPassword = getSharedPreferences("password", 0);
        graphicPref = getSharedPreferences("graphic", 0);
        editorGraphicKey = graphicPref.edit();
        editorPrefPin = prefPass.edit();
        editorPrefPassword = prefPassword.edit();
        checkLock();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if (SettingsFragment.getIsPurchased(this)) {
            mAdView.setVisibility(View.GONE);
        }
        imgClear = findViewById(R.id.ic_clear);
        imageAddNoteMain = findViewById(R.id.icon_add_note_main);
        final EditText searchEditText = findViewById(R.id.searc_edit_text);
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
                        hideKeyboard(MainActivity.this);
                    }
                });
                adapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                if (notesList.size() != 0) {
                    adapter.searchNote(editable.toString());
                    new TextHighlighter()
                            .setBackgroundColor(Color.parseColor("#FFFF00"))
                            .addTarget(findViewById(R.id.text_title))
                            .highlight(searchEditText.getText().toString(), TextHighlighter.BASE_MATCHER);
                }
            }
        });
        imgClear.setVisibility(View.GONE);
        ImageView iconRemoveAds = findViewById(R.id.icon_remove_ads);
        if (SettingsFragment.getIsPurchased(this)) {
            iconRemoveAds.setVisibility(View.GONE);
        } else {
            iconRemoveAds.setVisibility(View.VISIBLE);
            findViewById(R.id.icon_remove_ads).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                }
            });
        }

        findViewById(R.id.icon_pin_code).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                showNewSecureDialog();
            }
        });

        findViewById(R.id.icon_music).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        final ImageView settingsImg = findViewById(R.id.icon_settings);
        settingsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
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
    }

    private void onAddBtnClicked() {
        setAnim(clicked);
        if (!clicked) clicked = true;
        else clicked = false;
        startActivityForResult(new Intent(MainActivity.this, CreateNoteActivity.class), ADD_NOTE_CODE);
    }

    private void setAnim(boolean clicked) {
        if (!clicked) imageAddNoteMain.startAnimation(rotateOpen);
        else imageAddNoteMain.startAnimation(rotateClose);
    }

    private void checkLock() {
        if (getIntent().getBooleanExtra("fromSwitch", false)) {
        } else {
            if (getIntent().getBooleanExtra("fromWidget", false)) {
            } else {
                if (getIntent().getBooleanExtra("isFromSet", false)) {
                } else if (getIntent().getBooleanExtra("setForget", false)) {
                } else if (!getIntent().getBooleanExtra("isFromBackKey", false)) {
                    if (!prefPass.getString("pin-code", "").isEmpty() && !checkedPref.getBoolean("isShowed", false)) {
                        startActivity(new Intent(MainActivity.this, PinCodeActivity.class));
                        editorIsShowed.putBoolean("isShowed", true);
                        editorIsShowed.apply();
                    } else if (!prefPass.getString("pin-code", "").isEmpty() && checkedPref.getBoolean("isShowed", false)) {
                        editorIsShowed.remove("isShowed");
                        editorIsShowed.apply();
                    }
                }
                if (getIntent().getBooleanExtra("isFromBackKey", false)) {
                } else if (!prefPassword.getString("passwordCode", "").isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, PasswordActivity.class);
                    intent.putExtra("inputPassword", true);
                    startActivity(intent);
                }
                if (getIntent().getBooleanExtra("isFromBackKey", false)) {
                } else if (!graphicPref.getString("graphic_key", "").isEmpty() && !checkedPref.getBoolean("isShowed", false)) {
                    editorIsShowed.putBoolean("isShowed", true);
                    editorIsShowed.apply();
                    startActivity(new Intent(MainActivity.this, GraphicKeyActivityInput.class));
                } else if (!graphicPref.getString("graphic_key", "").isEmpty() && checkedPref.getBoolean("isShowed", false)) {
                    editorIsShowed.remove("isShowed");
                    editorIsShowed.apply();
                }
            }
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showNewSecureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_type_of_security, (
                ViewGroup) findViewById(R.id.layout_type_of_security_container));
        builder.setView(view);
        dialogTypeSecurity = builder.create();
        if (dialogTypeSecurity.getWindow() != null) {
            dialogTypeSecurity.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        view.findViewById(R.id.text_pin_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PinCodeActivity.class);
                intent.putExtra("set", "set");
                startActivityForResult(intent, REQUEST_CODE_ENABLE);
                dialogTypeSecurity.dismiss();
            }
        });
        view.findViewById(R.id.text_graphic_key).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GraphicKeyActivityCreate.class);
                startActivity(intent);
                dialogTypeSecurity.dismiss();
            }
        });
        view.findViewById(R.id.text_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PasswordActivity.class);
                startActivity(intent);
                dialogTypeSecurity.dismiss();
            }
        });
        view.findViewById(R.id.text_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorGraphicKey.clear();
                editorPrefPin.clear();
                editorPrefPassword.clear();
                editorGraphicKey.apply();
                editorPrefPin.apply();
                editorPrefPassword.apply();
                if (!preferenceSettings.getBoolean("remove_toasts", true)) {
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.lock_removed_toast), Toast.LENGTH_SHORT).show();
                }
                dialogTypeSecurity.dismiss();
            }
        });
        view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogTypeSecurity.dismiss();
            }
        });
        dialogTypeSecurity.show();
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        clickedNotePosition = position;
        Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, UPDATE_NOTE_CODE);
    }

    @Override
    public void onLongNoteClicked(Note note, int position) {

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
            if (!preferenceSettings.getBoolean("remove_toasts", true)) {
            } else {
                Toast.makeText(this, "Пин-код успешно задан!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        class GetAllNotesAsyncTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDatabase.getNoteDatabase(getApplicationContext())
                        .getNoteDAO().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requestCode == SHOW_NOTES_CODE) {
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
                }
                if (notesList.isEmpty()) {
                    nowHereEmpty.setVisibility(View.VISIBLE);
                    labelEmptyNow.setVisibility(View.VISIBLE);
                    labelHint.setVisibility(View.VISIBLE);
                    noteRecyclerView.setVisibility(View.GONE);
                    nowHereEmpty.playAnimation();
                } else {
                    nowHereEmpty.setVisibility(View.GONE);
                    labelHint.setVisibility(View.GONE);
                    labelEmptyNow.setVisibility(View.GONE);
                    noteRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        }
        new GetAllNotesAsyncTask().execute();
    }
}