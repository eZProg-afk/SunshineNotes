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
import spiral.bit.dev.sunshinenotes.activities.SettingsActivity;
import spiral.bit.dev.sunshinenotes.activities.create.CreateCheckListActivity;
import spiral.bit.dev.sunshinenotes.activities.create.CreateNoteActivity;
import spiral.bit.dev.sunshinenotes.adapter.CheckListAdapter;
import spiral.bit.dev.sunshinenotes.data.CheckListDatabase;
import spiral.bit.dev.sunshinenotes.listeners.CheckListsListener;
import spiral.bit.dev.sunshinenotes.models.CheckList;

import static android.app.Activity.RESULT_OK;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_CHECK_LIST_CODE;

public class CheckListFragment extends Fragment implements CheckListsListener {

    public static final int ADD_NOTE_CODE = 12;
    public static final int UPDATE_NOTE_CODE = 13;
    public static final int SHOW_NOTES_CODE = 14;
    public static final int REQUEST_CODE_ENABLE = 1803;

    private static RecyclerView checkListRecyclerView;
    private CheckListAdapter adapter;
    private List<CheckList> checkListsList;
    private int clickedCheckListPosition = -1;
    private ImageView imgClear;
    private SharedPreferences preferenceSettings;
    private LottieAnimationView nowHereEmpty;
    private TextView labelEmptyNow, labelHint;
    private Animation rotateOpen, rotateClose;
    private boolean clicked = false;
    public ImageView imageAddNoteMain;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_check_list, container, false);
        preferenceSettings = PreferenceManager
                .getDefaultSharedPreferences(getContext().getApplicationContext());

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

        SharedPreferences prefPass = getActivity().getSharedPreferences("pass", 0);
        SharedPreferences prefPassword = getActivity().getSharedPreferences("password", 0);
        SharedPreferences graphicPref = getActivity().getSharedPreferences("graphic", 0);
        if (preferenceSettings.getBoolean("dark", false)) AppCompatDelegate
                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getNotes(SHOW_NOTES_CODE, false);
        rotateOpen = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_close_anim);
        nowHereEmpty = view.findViewById(R.id.now_empty_view);
        checkListRecyclerView = view.findViewById(R.id.check_lists_recycler_view);
        checkListsList = new ArrayList<>();
        adapter = new CheckListAdapter(checkListsList, this);
        checkListRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        checkListRecyclerView.setAdapter(adapter);
        labelEmptyNow = view.findViewById(R.id.label_empty);
        labelHint = view.findViewById(R.id.label_hint);
        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if (SettingsFragment.getIsPurchased(getContext())) mAdView.setVisibility(View.GONE);
        imgClear = view.findViewById(R.id.ic_clear);
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
                if (checkListsList.size() != 0) {
                    adapter.searchCheckList(editable.toString());
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
                    getContext(), "Пин-код успешно задан!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        @SuppressLint("StaticFieldLeak")
        class GetAllNotesAsyncTask extends AsyncTask<Void, Void, List<CheckList>> {
            @Override
            protected List<CheckList> doInBackground(Void... voids) {
                return CheckListDatabase.getCheckListDatabase(getContext().getApplicationContext())
                        .getCheckDAO().getAllCheckLists();
            }

            @Override
            protected void onPostExecute(List<CheckList> checkLists) {
                super.onPostExecute(checkLists);
                if (requestCode == SHOW_NOTES_CODE) {
                    checkListsList.addAll(checkLists);
                    adapter.notifyDataSetChanged();
                } else if (requestCode == ADD_NOTE_CODE) {
                    checkListsList.add(0, checkLists.get(0));
                    adapter.notifyItemInserted(0);
                    checkListRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == UPDATE_NOTE_CODE) {
                    checkListsList.remove(clickedCheckListPosition);
                    if (isNoteDeleted) {
                        adapter.notifyItemRemoved(clickedCheckListPosition);
                    } else {
                        checkListsList.add(clickedCheckListPosition, checkLists.get(clickedCheckListPosition));
                        adapter.notifyItemChanged(clickedCheckListPosition);
                    }
                }
                if (checkLists.isEmpty()) {
                    nowHereEmpty.setVisibility(View.VISIBLE);
                    labelEmptyNow.setVisibility(View.VISIBLE);
                    labelHint.setVisibility(View.VISIBLE);
                    checkListRecyclerView.setVisibility(View.GONE);
                    nowHereEmpty.playAnimation();
                } else {
                    nowHereEmpty.setVisibility(View.GONE);
                    labelHint.setVisibility(View.GONE);
                    labelEmptyNow.setVisibility(View.GONE);
                    checkListRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        }
        new GetAllNotesAsyncTask().execute();
    }

    @Override
    public void onCheckListClicked(CheckList checkList, int position) {
        clickedCheckListPosition = position;
        Intent intent = new Intent(getContext(), CreateCheckListActivity.class);
        intent.putExtra("checklist", checkList);
        startActivityForResult(intent, UPDATE_CHECK_LIST_CODE);
    }

    @Override
    public void onLongCheckListClicked(CheckList checkList, int position) {

    }
}