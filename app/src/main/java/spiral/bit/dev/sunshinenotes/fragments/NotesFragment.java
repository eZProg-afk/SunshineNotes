package spiral.bit.dev.sunshinenotes.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.xeoh.android.texthighlighter.TextHighlighter;

import java.util.ArrayList;
import java.util.List;

import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.BaseActivity;
import spiral.bit.dev.sunshinenotes.activities.PasswordActivity;
import spiral.bit.dev.sunshinenotes.activities.PatternLockActivity;
import spiral.bit.dev.sunshinenotes.activities.PinCodeActivity;
import spiral.bit.dev.sunshinenotes.activities.SettingsActivity;
import spiral.bit.dev.sunshinenotes.adapter.NoteAdapter;
import spiral.bit.dev.sunshinenotes.data.NoteDatabase;
import spiral.bit.dev.sunshinenotes.listeners.NotesListener;
import spiral.bit.dev.sunshinenotes.models.SimpleNote;

import static android.app.Activity.RESULT_OK;

public class NotesFragment extends Fragment implements NotesListener {

    public static final int ADD_NOTE_CODE = 12;
    public static final int UPDATE_NOTE_CODE = 13;
    public static final int SHOW_NOTES_CODE = 14;
    public static final int REQUEST_CODE_ENABLE = 1803;

    private static RecyclerView noteRecyclerView;
    private NoteAdapter adapter;
    private List<SimpleNote> notesList;
    private int clickedNotePosition = -1;
    private ImageView imgClear;
    private SharedPreferences prefPass, checkedPref, prefPassword, graphicPref, preferenceSettings;
    private SharedPreferences.Editor editorIsShowed;
    private LottieAnimationView nowHereEmpty;
    private TextView labelEmptyNow, labelHint;
    private Animation rotateOpen, rotateClose;
    private boolean clicked = false;
    public ImageView imageAddNoteMain;

    @SuppressLint("CommitPrefEdits")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_notes, container, false);
//        BaseActivity.toggleBottomBar(true);
        getParentFragmentManager().setFragmentResultListener("requestKey",
                this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                if (requestKey.equals(String.valueOf(ADD_NOTE_CODE))) {
                    getNotes(ADD_NOTE_CODE, false);
                } else if (requestKey.equals(String.valueOf(UPDATE_NOTE_CODE))) {
                    getNotes(UPDATE_NOTE_CODE, false);
                }
            }
        });
        preferenceSettings = PreferenceManager
                .getDefaultSharedPreferences(getContext().getApplicationContext());
        if (preferenceSettings.getBoolean("dark", false)) AppCompatDelegate
                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        checkedPref = getContext().getSharedPreferences("check", 0);
        editorIsShowed = checkedPref.edit();
        prefPass = getContext().getSharedPreferences("pass", 0);
        prefPassword = getContext().getSharedPreferences("password", 0);
        graphicPref = getContext().getSharedPreferences("graphic", 0);
        checkLock();
        getNotes(SHOW_NOTES_CODE, false);
        rotateOpen = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_close_anim);
        nowHereEmpty = view.findViewById(R.id.now_empty_view);
        noteRecyclerView = view.findViewById(R.id.notes_recycler_view);
        notesList = new ArrayList<>();
        adapter = new NoteAdapter(notesList, this);
        noteRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteRecyclerView.setAdapter(adapter);
        labelEmptyNow = view.findViewById(R.id.label_empty);
        labelHint = view.findViewById(R.id.label_hint);
        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if (SettingsFragment.getIsPurchased(getContext())) mAdView.setVisibility(View.GONE);
        imgClear = view.findViewById(R.id.ic_clear);
        imageAddNoteMain = view.findViewById(R.id.icon_add_note_main);
        final EditText searchEditText = view.findViewById(R.id.search_edit_text);
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
                        hideKeyboard(getActivity());
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
                            .addTarget(view.findViewById(R.id.text_title))
                            .highlight(searchEditText.getText().toString(), TextHighlighter.BASE_MATCHER);
                }
            }
        });
        imgClear.setVisibility(View.GONE);

        final ImageView settingsImg = view.findViewById(R.id.icon_settings);
        settingsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SettingsActivity.class));
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (preferenceSettings.getBoolean("dark", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void checkLock() { // FIXME: 04.12.2020
        if (!getActivity().getIntent().getBooleanExtra("fromSwitch", true)) {
            if (getActivity().getIntent().getBooleanExtra("isFromSet", false)) {
            } else if (getActivity().getIntent().getBooleanExtra("setForget", false)) {
            } else if (!getActivity().getIntent().getBooleanExtra("isFromBackKey", false)) {
                if (!prefPass.getString("pin-code", "").isEmpty() && !checkedPref.getBoolean("isShowed", false)) {
                    startActivity(new Intent(getContext(), PinCodeActivity.class));
                    editorIsShowed.putBoolean("isShowed", true);
                    editorIsShowed.apply();
                } else if (!prefPass.getString("pin-code", "").isEmpty() && checkedPref.getBoolean("isShowed", false)) {
                    editorIsShowed.remove("isShowed");
                    editorIsShowed.apply();
                }
            }
            if (getActivity().getIntent().getBooleanExtra("isFromBackKey", false)) {
            } else if (!prefPassword.getString("passwordCode", "").isEmpty()) {
                Intent intent = new Intent(getContext(), PasswordActivity.class);
                intent.putExtra("inputPassword", true);
                startActivity(intent);
            }
            if (getActivity().getIntent().getBooleanExtra("isFromBackKey", false)) {
            } else if (!graphicPref.getString("graphic_key", "").isEmpty() && !checkedPref.getBoolean("isShowed", false)) {
                editorIsShowed.putBoolean("isShowed", true);
                editorIsShowed.apply();
                Intent intent = new Intent(getContext(), PatternLockActivity.class);
                intent.putExtra("type", "input");
                startActivity(intent);
            } else if (!graphicPref.getString("graphic_key", "").isEmpty() && checkedPref.getBoolean("isShowed", false)) {
                editorIsShowed.remove("isShowed");
                editorIsShowed.apply();
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
            if (preferenceSettings.getBoolean("remove_toasts", false)) Toast.makeText(
                    getContext() , "Пин-код успешно задан!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetAllNotesAsyncTask extends AsyncTask<Void, Void, List<SimpleNote>> {
            @Override
            protected List<SimpleNote> doInBackground(Void... voids) {
                return NoteDatabase.getNoteDatabase(getContext().getApplicationContext())
                        .getNoteDAO().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<SimpleNote> notes) {
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

    @Override
    public void onNoteClicked(SimpleNote simpleNote, int position) {
        clickedNotePosition = position;
        CreateNoteFragment createNoteFragment = new CreateNoteFragment();
        Bundle result = new Bundle();
        result.putSerializable("note", simpleNote);
        getParentFragmentManager().setFragmentResult(String.valueOf(UPDATE_NOTE_CODE), result);
        createNoteFragment.setArguments(result);
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.replaced_container, createNoteFragment)
                .commit();
    }

    @Override
    public void onLongNoteClicked(SimpleNote simpleNote, int position) {

    }
}