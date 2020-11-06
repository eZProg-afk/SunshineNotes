package spiral.bit.dev.sunshinenotes.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
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
import spiral.bit.dev.sunshinenotes.activities.CreateCheckActivity;
import spiral.bit.dev.sunshinenotes.activities.SettingsActivity;
import spiral.bit.dev.sunshinenotes.adapters.CheckListAdapter;
import spiral.bit.dev.sunshinenotes.data.CheckDatabase;
import spiral.bit.dev.sunshinenotes.listeners.CheckListener;
import spiral.bit.dev.sunshinenotes.models.CheckItem;
import static android.app.Activity.RESULT_OK;
import static spiral.bit.dev.sunshinenotes.activities.CreateNoteActivity.hideKeyboard;

public class CheckListFragment extends Fragment implements CheckListener {

    public static final int ADD_FOLDER_CODE = 12;
    public static final int UPDATE_FOLDER_CODE = 13;
    public static final int SHOW_FOLDERS_CODE = 14;
    private static RecyclerView noteRecyclerView;
    private CheckListAdapter adapter;
    private List<CheckItem> checksList;
    private int clickedNotePosition = -1;
    private AdView mAdView;
    private ImageView imgClear;
    private SharedPreferences preferenceSettings;
    private LottieAnimationView nowHereEmpty;
    private TextView labelEmptyNow, labelHint;
    private boolean clicked = false;
    public ImageView imageAddNoteMain;

    public CheckListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_folders, container, false);
        imageAddNoteMain = view.findViewById(R.id.icon_add_folder_main);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getContext(), CreateCheckActivity.class), ADD_FOLDER_CODE);
            }
        });
        preferenceSettings = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        nowHereEmpty = view.findViewById(R.id.now_empty_view);
        noteRecyclerView = view.findViewById(R.id.folders_recycler_view);
        checksList = new ArrayList<>();
        adapter = new CheckListAdapter(checksList, this);
        noteRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteRecyclerView.setAdapter(adapter);
        labelEmptyNow = view.findViewById(R.id.label_empty);
        labelHint = view.findViewById(R.id.label_hint);
        getFolders(SHOW_FOLDERS_CODE, false);
        MobileAds.initialize(view.getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        if (SettingsFragment.getIsPurchased(view.getContext())) {
            mAdView.setVisibility(View.GONE);
        }
        imgClear = view.findViewById(R.id.ic_clear);
        imageAddNoteMain = view.findViewById(R.id.icon_add_note_main);
        final EditText searchEditText = view.findViewById(R.id.searc_edit_text);
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
                if (checksList.size() != 0) {
                    adapter.searchFolder(editable.toString());
                    new TextHighlighter()
                            .setBackgroundColor(Color.parseColor("#FFFF00"))
                            .addTarget(view.findViewById(R.id.text_title))
                            .highlight(searchEditText.getText().toString(), TextHighlighter.BASE_MATCHER);
                }
            }
        });
        imgClear.setVisibility(View.GONE);
        ImageView iconRemoveAds = view.findViewById(R.id.icon_remove_ads);
        if (SettingsFragment.getIsPurchased(view.getContext())) {
            iconRemoveAds.setVisibility(View.GONE);
        } else {
            iconRemoveAds.setVisibility(View.VISIBLE);
            view.findViewById(R.id.icon_remove_ads).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(view.getContext(), SettingsActivity.class));
                }
            });
        }

        final ImageView settingsImg = view.findViewById(R.id.icon_settings);
        settingsImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), SettingsActivity.class));
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

        return view;
    }

    private void getFolders(final int requestCode, final boolean isNoteDeleted) {
        class GetAllFoldersAsyncTask extends AsyncTask<Void, Void, List<CheckItem>> {
            @Override
            protected List<CheckItem> doInBackground(Void... voids) {
                return CheckDatabase.getCheckDatabase(getContext())
                        .getCheckDao().getAllCheckLists();
            }

            @Override
            protected void onPostExecute(List<CheckItem> checkItems) {
                super.onPostExecute(checkItems);
                if (requestCode == SHOW_FOLDERS_CODE) {
                    checksList.addAll(checkItems);
                    adapter.notifyDataSetChanged();
                } else if (requestCode == ADD_FOLDER_CODE) {
                    checksList.add(0, checkItems.get(0));
                    adapter.notifyItemInserted(0);
                    noteRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == UPDATE_FOLDER_CODE) {
                    checksList.remove(clickedNotePosition);
                    if (isNoteDeleted) {
                        adapter.notifyItemRemoved(clickedNotePosition);
                    } else {
                        checksList.add(clickedNotePosition, checkItems.get(clickedNotePosition));
                        adapter.notifyItemChanged(clickedNotePosition);
                    }
                }
                if (checksList.isEmpty()) {
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
        new GetAllFoldersAsyncTask().execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ADD_FOLDER_CODE) {
            getFolders(ADD_FOLDER_CODE, false);
        } else if (resultCode == RESULT_OK && requestCode == UPDATE_FOLDER_CODE) {
            if (data != null) {
                getFolders(UPDATE_FOLDER_CODE, data.getBooleanExtra("isNoteDeleted", false));
            }
        }
    }

    @Override
    public void onCheckClicked(CheckItem checkItem, int position) {
        clickedNotePosition = position;
        Intent intent = new Intent(getContext(), CreateCheckActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("checkItem", checkItem);
        startActivityForResult(intent, UPDATE_FOLDER_CODE);
    }
}