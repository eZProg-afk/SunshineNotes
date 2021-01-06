package spiral.bit.dev.sunshinenotes.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
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
import com.shashank.sony.fancytoastlib.FancyToast;
import com.xeoh.android.texthighlighter.TextHighlighter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.other.ChangeBackActivity;
import spiral.bit.dev.sunshinenotes.activities.other.SettingsActivity;
import spiral.bit.dev.sunshinenotes.activities.create.CreateCheckListActivity;
import spiral.bit.dev.sunshinenotes.adapter.CheckListAdapter;
import spiral.bit.dev.sunshinenotes.data.CheckListDatabase;
import spiral.bit.dev.sunshinenotes.data.StatisticDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashCheckListDatabase;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.listeners.CheckListsListener;
import spiral.bit.dev.sunshinenotes.models.CheckList;
import spiral.bit.dev.sunshinenotes.models.Task;
import spiral.bit.dev.sunshinenotes.models.other.Statistic;
import spiral.bit.dev.sunshinenotes.models.trash.TrashCheckList;
import spiral.bit.dev.sunshinenotes.models.trash.TrashTask;
import spiral.bit.dev.sunshinenotes.other.AdWorker;
import static android.app.Activity.RESULT_OK;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_CHECK_LIST_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.CHANGE_BACK_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.DELETE_NOTES_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.SHOW_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_CHECK_LIST_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.hideKeyboard;
import static spiral.bit.dev.sunshinenotes.other.Utils.returnBitmap;

public class CheckListFragment extends Fragment implements CheckListsListener {

    public static final int REQUEST_CODE_ENABLE = 1803;

    private static RecyclerView checkListRecyclerView;
    private CheckListAdapter adapter;
    private List<CheckList> checkListsList;
    private List<Task> taskList;
    private int clickedCheckListPosition = -1;
    private ImageView imgClear;
    private SharedPreferences preferenceSettings;
    private LottieAnimationView nowHereEmpty;
    private TextView labelEmptyNow, labelHint;
    private ArrayList<CheckList> tempList;
    private ArrayList<Integer> clickedCheckListsPositions;
    private LinearLayout menuDelete;
    private ConstraintLayout mainBack;
    public ImageView imageAddNoteMain;
    private boolean isDeletedModeEnabled = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_check_list, container, false);
        preferenceSettings = PreferenceManager
                .getDefaultSharedPreferences(getContext().getApplicationContext());
        mainBack = view.findViewById(R.id.main_back);
        setBackImg();
//        if (preferenceSettings.getBoolean("dark", false)) AppCompatDelegate
//                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        tempList = new ArrayList<>();
        taskList = new ArrayList<>();
        clickedCheckListsPositions = new ArrayList<>();
        menuDelete = view.findViewById(R.id.menu_delete_note);
        nowHereEmpty = view.findViewById(R.id.now_empty_view);
        checkListRecyclerView = view.findViewById(R.id.check_lists_recycler_view);
        checkListsList = new ArrayList<>();
        adapter = new CheckListAdapter(checkListsList, this);
        checkListRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        checkListRecyclerView.setAdapter(adapter);
        labelEmptyNow = view.findViewById(R.id.label_empty);
        labelHint = view.findViewById(R.id.label_hint);
        final AdView mAdView = view.findViewById(R.id.adView);

        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
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
                WorkManager workManager = WorkManager.getInstance(getContext());
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
                WorkManager workManager = WorkManager.getInstance(getContext());
                workManager.enqueue(workRequest);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
            }
        });

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
        ImageView imageChangeBack = view.findViewById(R.id.icon_change_back);
        if (SettingsFragment.getIsPurchased(getContext())) imageChangeBack.setVisibility(View.VISIBLE);
        else imageChangeBack.setVisibility(View.GONE);
        imageChangeBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChangeBackActivity.class);
                intent.putExtra("from", "checklists");
                startActivityForResult(intent, CHANGE_BACK_CODE);
            }
        });
        getNotes(SHOW_CODE, false);
        return view;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if (preferenceSettings.getBoolean("dark", false)) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ADD_CHECK_LIST_CODE) {
            getNotes(ADD_CHECK_LIST_CODE, false);
        } else if (resultCode == RESULT_OK && requestCode == UPDATE_CHECK_LIST_CODE) {
            if (data != null) {
                getNotes(UPDATE_CHECK_LIST_CODE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if (requestCode == REQUEST_CODE_ENABLE && resultCode == RESULT_OK) {
            if (preferenceSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(
                    getContext(),
                    "Пин-код успешно задан!",
                    FancyToast.LENGTH_LONG,
                    FancyToast.SUCCESS,
                    false).show();
        } else if (requestCode == CHANGE_BACK_CODE && resultCode == RESULT_OK) {
            if (preferenceSettings.contains("back_check")) {
                int imageId = preferenceSettings.getInt("back_check", 0);
                mainBack.setBackgroundResource(imageId);
            } else if (preferenceSettings.contains("color_check")) {
                int color = preferenceSettings.getInt("color_check",0);
                mainBack.setBackgroundColor(color);
            } else if (preferenceSettings.contains("picture_check")) {
                String selectedImgUri = preferenceSettings.getString("picture_check", "");
                InputStream is;
                try {
                    is = getContext().getContentResolver().openInputStream(Uri.fromFile(new File(selectedImgUri)));
                    Bitmap bitmap = returnBitmap(BitmapFactory.decodeStream(is), 800, 1400);
                    Drawable picture = new BitmapDrawable(getResources(), bitmap);
                    mainBack.setBackground(picture);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
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
                if (requestCode == SHOW_CODE) {
                    checkListsList.addAll(checkLists);
                    adapter.notifyDataSetChanged();
                } else if (requestCode == ADD_CHECK_LIST_CODE) {
                    checkListsList.add(0, checkLists.get(0));
                    adapter.notifyItemInserted(0);
                    checkListRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == UPDATE_CHECK_LIST_CODE) {
                    checkListsList.remove(clickedCheckListPosition);
                    if (isNoteDeleted) {
                        adapter.notifyItemRemoved(clickedCheckListPosition);
                    } else {
                        checkListsList.add(clickedCheckListPosition, checkLists.get(clickedCheckListPosition));
                        adapter.notifyItemChanged(clickedCheckListPosition);
                    }
                } else if (requestCode == DELETE_NOTES_CODE) {
                    checkListsList.remove(clickedCheckListPosition);
                    clickedCheckListsPositions = new ArrayList<>();
                    tempList = new ArrayList<>();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.replaced_container, new CheckListFragment())
                            .commit();
                }
                if (checkListsList.isEmpty()) {
                    if (preferenceSettings.getInt("back_check", 0) == 0
                            || preferenceSettings.getInt("picture_check", 0) == 0) {
                        nowHereEmpty.setVisibility(View.VISIBLE);
                        labelEmptyNow.setVisibility(View.VISIBLE);
                        labelHint.setVisibility(View.VISIBLE);
                        checkListRecyclerView.setVisibility(View.GONE);
                        nowHereEmpty.playAnimation();
                    }
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

    private void setBackImg() {
        if (preferenceSettings.contains("back_check")) {
            int imageId = preferenceSettings.getInt("back_check", 0);
            mainBack.setBackgroundResource(imageId);
        } else if (preferenceSettings.contains("color_check")) {
            int color = preferenceSettings.getInt("color_check",0);
            mainBack.setBackgroundColor(color);
        } else if (preferenceSettings.contains("picture_check")) {
            String selectedImgUri = preferenceSettings.getString("picture_check", "");
            InputStream is;
            try {
                is = getContext().getContentResolver().openInputStream(Uri.fromFile(new File(selectedImgUri)));
                Bitmap bitmap = returnBitmap(BitmapFactory.decodeStream(is), 800, 1400);
                Drawable picture = new BitmapDrawable(getResources(), bitmap);
                mainBack.setBackground(picture);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCheckListClicked(CheckList checkList, int position) {
        if (isDeletedModeEnabled) {
            if (tempList.contains(checkList)) {
                adapter.disableOneClickDeleteMode();
                clickedCheckListPosition = position;
                if (tempList.size() == 0 || tempList.size() == 1) {
                    tempList = new ArrayList<>();
                    clickedCheckListsPositions = new ArrayList<>();
                    menuDelete.setVisibility(View.GONE);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.replaced_container, new CheckListFragment())
                            .commit();
                    isDeletedModeEnabled = false;
                    adapter.disableOneClickDeleteMode();
                } else {
                    tempList.remove(checkList);
                    //clickedNotePositions.remove(clickedNotePosition); хуй знает вроде и без неё работает
                }
            } else {
                adapter.setOneClickDeleteMode();
                clickedCheckListPosition = position;
                tempList.add(checkList);
                clickedCheckListsPositions.add(clickedCheckListPosition);
            }
        } else {
            clickedCheckListPosition = position;
            Intent intent = new Intent(getContext(), CreateCheckListActivity.class);
            intent.putExtra("setViewOrUpdate", true);
            intent.putExtra("checklist", checkList);
            intent.putExtra("isNoteDeleted", false);
            startActivityForResult(intent, UPDATE_CHECK_LIST_CODE);
        }
    }

    @Override
    public void onLongCheckListClicked(CheckList checkList, int position) {
        isDeletedModeEnabled = true;
        clickedCheckListPosition = position;
        tempList.add(checkList);
        clickedCheckListsPositions.add(clickedCheckListPosition);
        menuDelete.setVisibility(View.VISIBLE);
        menuDelete.findViewById(R.id.image_close_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempList = new ArrayList<>();
                clickedCheckListsPositions = new ArrayList<>();
                menuDelete.setVisibility(View.GONE);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.replaced_container, new CheckListFragment())
                        .commit();
                isDeletedModeEnabled = false;
                adapter.disableOneClickDeleteMode();
            }
        });
        menuDelete.findViewById(R.id.image_accept_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MultipleDeleteAsyncTask().execute();
                menuDelete.setVisibility(View.GONE);
                isDeletedModeEnabled = false;
                adapter.disableOneClickDeleteMode();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    class MultipleDeleteAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            for (CheckList checkList : tempList) {
                TrashCheckList trashCheckList = new TrashCheckList();
                trashCheckList.setTitle(checkList.getTitle());
                trashCheckList.setImagePath(checkList.getImagePath());
                trashCheckList.setDateTimeEdit(checkList.getDateTimeEdit());
                trashCheckList.setDateTime(checkList.getDateTime());
                trashCheckList.setCompleted(checkList.isCompleted());
                trashCheckList.setCheckListId(checkList.getCheckListId());
                trashCheckList.setCheckListColor(checkList.getCheckListColor());
                TrashCheckListDatabase.getNoteDatabase(getContext().getApplicationContext())
                        .getTrashCheckListDAO().insertTrashCheckList(trashCheckList);
                Statistic statistic = new Statistic();

                SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
                String date = sdf.format(Calendar.getInstance().getTime());
                statistic.setDateText(date);

                statistic.setTypeText(getString(R.string.check_list_label));
                statistic.setItemText(checkList.getTitle());
                statistic.setActionText(getString(R.string.deleted_action));
                StatisticDatabase.getStatisticDatabase(getContext().getApplicationContext())
                        .getStatisticDAO().insertStatistic(statistic);
                taskList = CheckListDatabase.getCheckListDatabase(getContext().getApplicationContext())
                        .getCheckDAO().getAllTasks(checkList.getCheckListId());
                for (Task task : taskList) {
                    TrashTask trashTask = new TrashTask();
                    trashTask.setCompleted(task.isCompleted());
                    trashTask.setDateTime(task.getDateTime());
                    trashTask.setId(task.getId());
                    trashTask.setParentId(task.getParentId());
                    trashTask.setTitle(task.getTitle());
                    Statistic statistic2 = new Statistic();

                    SimpleDateFormat sdf2 = new SimpleDateFormat("dd MM yyyy");
                    String date2 = sdf2.format(Calendar.getInstance().getTime());
                    statistic2.setDateText(date2);

                    statistic2.setTypeText(getString(R.string.task_label));
                    statistic2.setItemText(task.getTitle());
                    statistic2.setActionText(getString(R.string.deleted_action));
                    StatisticDatabase.getStatisticDatabase(getContext().getApplicationContext())
                            .getStatisticDAO().insertStatistic(statistic2);
                    TrashCheckListDatabase.getNoteDatabase(getContext().getApplicationContext())
                            .getTrashCheckListDAO().insertTrashTask(trashTask);
                }
            }
            CheckListDatabase.getCheckListDatabase(getContext().getApplicationContext())
                    .getCheckDAO().deleteMultipleTasks(taskList);
            CheckListDatabase.getCheckListDatabase(getContext().getApplicationContext())
                    .getCheckDAO().deleteMultipleCheckLists(tempList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getNotes(DELETE_NOTES_CODE, true);
        }
    }
}