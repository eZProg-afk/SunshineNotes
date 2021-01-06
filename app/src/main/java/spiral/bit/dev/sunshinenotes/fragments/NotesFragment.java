package spiral.bit.dev.sunshinenotes.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.airbnb.lottie.LottieAnimationView;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;
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
import spiral.bit.dev.sunshinenotes.activities.create.CreateNoteActivity;
import spiral.bit.dev.sunshinenotes.activities.other.ChangeBackActivity;
import spiral.bit.dev.sunshinenotes.activities.other.SettingsActivity;
import spiral.bit.dev.sunshinenotes.adapter.NoteAdapter;
import spiral.bit.dev.sunshinenotes.data.NoteDatabase;
import spiral.bit.dev.sunshinenotes.data.StatisticDatabase;
import spiral.bit.dev.sunshinenotes.data.trash.TrashDatabase;
import spiral.bit.dev.sunshinenotes.fragments.other.SettingsFragment;
import spiral.bit.dev.sunshinenotes.listeners.NotesListener;
import spiral.bit.dev.sunshinenotes.listeners.SelectListener;
import spiral.bit.dev.sunshinenotes.models.SimpleNote;
import spiral.bit.dev.sunshinenotes.models.other.Statistic;
import spiral.bit.dev.sunshinenotes.models.trash.TrashNote;
import spiral.bit.dev.sunshinenotes.other.AdWorker;

import static android.app.Activity.RESULT_OK;
import static spiral.bit.dev.sunshinenotes.fragments.CheckListFragment.REQUEST_CODE_ENABLE;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_NOTE_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.CHANGE_BACK_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.DELETE_NOTES_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.SHOW_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.UPDATE_NOTE_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.hideKeyboard;
import static spiral.bit.dev.sunshinenotes.other.Utils.returnBitmap;

public class NotesFragment extends Fragment implements NotesListener, SelectListener {

    private static RecyclerView noteRecyclerView;
    private List<SimpleNote> tempList;
    private NoteAdapter adapter;
    private List<SimpleNote> notesList;
    private int clickedNotePosition = -1;
    private List<Integer> clickedNotePositions;
    private ImageView imgClear;
    private SharedPreferences prefPass, checkedPref, prefPassword, graphicPref, preferenceSettings;
    private SharedPreferences.Editor editorIsShowed;
    private LottieAnimationView nowHereEmpty;
    private EditText searchEditText;
    private TextView labelEmptyNow, labelHint;
    public ImageView imageAddNoteMain;
    private LinearLayout menuDelete;
    private ConstraintLayout mainBack;
    private boolean isDeletedModeEnabled = false;

    @SuppressLint("CommitPrefEdits")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_notes, container, false);
        tempList = new ArrayList<>();
        notesList = new ArrayList<>();
        clickedNotePositions = new ArrayList<>();
        preferenceSettings = PreferenceManager
                .getDefaultSharedPreferences(getContext().getApplicationContext());
//        if (preferenceSettings.getBoolean("dark", false)) AppCompatDelegate
//                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        mainBack = view.findViewById(R.id.main_back);
        setBackImg();
        checkedPref = getContext().getSharedPreferences("check", 0);
        editorIsShowed = checkedPref.edit();
        prefPass = getContext().getSharedPreferences("pass", 0);
        prefPassword = getContext().getSharedPreferences("password", 0);
        graphicPref = getContext().getSharedPreferences("graphic", 0);
        nowHereEmpty = view.findViewById(R.id.now_empty_view);
        getNotes(SHOW_CODE, false);
        noteRecyclerView = view.findViewById(R.id.notes_recycler_view);
        adapter = new NoteAdapter(notesList, this, this);
        noteRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteRecyclerView.setAdapter(adapter);
        menuDelete = view.findViewById(R.id.menu_delete_note);
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
        imageAddNoteMain = view.findViewById(R.id.icon_add_note_main);
        searchEditText = view.findViewById(R.id.search_edit_text);
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

        ImageView imageChangeBack = view.findViewById(R.id.icon_change_back);
        if (SettingsFragment.getIsPurchased(getContext()))
            imageChangeBack.setVisibility(View.VISIBLE);
        else imageChangeBack.setVisibility(View.GONE);
        imageChangeBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChangeBackActivity.class);
                intent.putExtra("from", "notes");
                startActivityForResult(intent, CHANGE_BACK_CODE);
            }
        });
        //initSpotLight(view);
        return view;
    }

    private void initSpotLight(View view) {
//        TapTargetView.showFor(getActivity(),
//                TapTarget.forView(view.findViewById(R.id.search_edit_text),
//                        "Поиск по заметкам", "Быстро находите нужные вам заметки по описанию, тексту и заголовку")
//                        .outerCircleColor(R.color.red_color_picker)
//                        .outerCircleAlpha(0.96f)
//                        .targetCircleColor(R.color.white)
//                        .titleTextSize(25)
//                        .titleTextColor(R.color.white)
//                        .descriptionTextSize(20)
//                        .descriptionTextColor(R.color.colorNoteColor5)
//                        .textColor(R.color.colorNoteColor5)
//                        .textTypeface(Typeface.DEFAULT_BOLD)
//                        .dimColor(R.color.black)
//                        .drawShadow(true)
//                        .cancelable(true)
//                        .tintTarget(true)
//                        .transparentTarget(false)
//                        .icon(ContextCompat.getDrawable(getContext(),
//                                R.drawable.ic_search))
//                        .targetRadius(60),
//                new TapTargetView.Listener() {
//                    @Override
//                    public void onTargetClick(TapTargetView view) {
//                        super.onTargetClick(view);
//                        view.dismiss(true);
//                    }
//                });
    }

    private void setBackImg() {
        if (preferenceSettings.contains("back")) {
            int imageId = preferenceSettings.getInt("back", 0);
            mainBack.setBackgroundResource(imageId);
        } else if (preferenceSettings.contains("color")) {
            int color = preferenceSettings.getInt("color", 0);
            mainBack.setBackgroundColor(color);
        } else if (preferenceSettings.contains("picture")) {
            String selectedImgUri = preferenceSettings.getString("picture", "");
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

//    @Override
//    public void onResume() {
//        super.onResume();
//        if (preferenceSettings.getBoolean("dark", false))
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//    }

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
            if (preferenceSettings.getBoolean("remove_toasts", false))  FancyToast.makeText(
                    getContext(),
                    "Пин-код успешно задан!",
                    FancyToast.LENGTH_LONG,
                    FancyToast.SUCCESS,
                    false).show();
        } else if (requestCode == CHANGE_BACK_CODE && resultCode == RESULT_OK) {
            if (preferenceSettings.contains("back")) {
                int imageId = preferenceSettings.getInt("back", 0);
                mainBack.setBackgroundResource(imageId);
            } else if (preferenceSettings.contains("color")) {
                int color = preferenceSettings.getInt("color", 0);
                mainBack.setBackgroundColor(color);
            } else if (preferenceSettings.contains("picture")) {
                String selectedImgUri = preferenceSettings.getString("picture", "");
                InputStream is;
                try {
                    is = getContext().getContentResolver().openInputStream(Uri.parse(selectedImgUri));
                    Bitmap bitmap = returnBitmap(BitmapFactory.decodeStream(is), 300, 300);
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
        class GetAllNotesAsyncTask extends AsyncTask<Void, Void, List<SimpleNote>> {
            @Override
            protected List<SimpleNote> doInBackground(Void... voids) {
                return NoteDatabase.getNoteDatabase(getContext().getApplicationContext())
                        .getNoteDAO().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<SimpleNote> notes) {
                super.onPostExecute(notes);
                if (requestCode == SHOW_CODE) {
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
                } else if (requestCode == DELETE_NOTES_CODE) {
                    for (SimpleNote simpleNote : tempList) {
                        notesList.remove(simpleNote);
                        adapter.notifyDataSetChanged();
                    }
                    tempList = new ArrayList<>();
                }
                if (notesList.isEmpty()) {
                    if (preferenceSettings.getInt("back", 0) == 0
                            || preferenceSettings.getInt("picture", 0) == 0) {
                        nowHereEmpty.setVisibility(View.VISIBLE);
                        labelEmptyNow.setVisibility(View.VISIBLE);
                        labelHint.setVisibility(View.VISIBLE);
                        noteRecyclerView.setVisibility(View.GONE);
                        nowHereEmpty.playAnimation();
                    }
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
        Intent intent = new Intent(getContext(), CreateNoteActivity.class);
        intent.putExtra("note", simpleNote);
        intent.putExtra("setViewOrUpdate", true);
        intent.putExtra("isNoteDeleted", false);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(intent, UPDATE_NOTE_CODE);
    }

    @Override
    public void onLongNoteClicked(SimpleNote simpleNote, int position) {
        isDeletedModeEnabled = true;
    }

    @Override
    public void onItemSelected(boolean isSelected) {
        menuDelete.findViewById(R.id.image_accept_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuDelete.setVisibility(View.GONE);
                for (SimpleNote simpleNote : notesList) {
                    if (simpleNote.isDelete()) {
                        tempList.add(simpleNote);
                    }
                }
                new MultipleDeleteAsyncTask().execute();
                adapter.notifyDataSetChanged();
                menuDelete.setVisibility(View.GONE);
            }
        });
        menuDelete.findViewById(R.id.image_close_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (SimpleNote simpleNote : notesList) {
                    simpleNote.setDelete(false);
                    adapter.notifyDataSetChanged();
                }
                menuDelete.setVisibility(View.GONE);
            }
        });
        if (isSelected) {
            menuDelete.setVisibility(View.VISIBLE);
        } else {
            menuDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemSelectedByOneTap(boolean isSelected) {
        menuDelete.findViewById(R.id.image_accept_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MultipleDeleteAsyncTask().execute();
                menuDelete.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }
        });
        menuDelete.findViewById(R.id.image_close_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (SimpleNote simpleNote : notesList) {
                    simpleNote.setDelete(false);
                    adapter.notifyDataSetChanged();
                }
                menuDelete.setVisibility(View.GONE);
            }
        });
        if (isSelected) {
            for (SimpleNote simpleNote : notesList) {
                if (simpleNote.isDelete()) {
                    simpleNote.setDelete(false);
                } else {
                    simpleNote.setDelete(true);
                }
            }
            menuDelete.setVisibility(View.VISIBLE);
        } else {
            menuDelete.setVisibility(View.GONE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MultipleDeleteAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < tempList.size(); i++) {
                TrashNote trashNote = new TrashNote();
                trashNote.setTitle(tempList.get(i).getTitle());
                trashNote.setSubTitle(tempList.get(i).getSubTitle());
                trashNote.setNoteText(tempList.get(i).getNoteText());
                trashNote.setWebLink(tempList.get(i).getWebLink());
                trashNote.setTextSize(tempList.get(i).getTextSize());
                trashNote.setNoteColor(tempList.get(i).getNoteColor());
                trashNote.setImagePath(tempList.get(i).getImagePath());
                trashNote.setId(tempList.get(i).getId());
                trashNote.setFontStyle(tempList.get(i).getFontStyle());
                trashNote.setDrawPath(tempList.get(i).getDrawPath());
                trashNote.setDateTimeEdit(tempList.get(i).getDateTimeEdit());
                trashNote.setDateTimeRemind(tempList.get(i).getDateTimeRemind());
                trashNote.setColor(tempList.get(i).getColor());

                Statistic statistic = new Statistic();
                statistic.setTypeText(getString(R.string.note_label));
                SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy");
                String date = sdf.format(Calendar.getInstance().getTime());
                statistic.setDateText(date);
                statistic.setActionText(getString(R.string.deleted_action));
                statistic.setItemText(tempList.get(i).getTitle());
                statistic.setItemSubText(tempList.get(i).getSubTitle());

                TrashDatabase.getNoteDatabase(getContext().getApplicationContext())
                        .getTrashDAO().insertTrashNote(trashNote);
                StatisticDatabase.getStatisticDatabase(getContext().getApplicationContext())
                        .getStatisticDAO().insertStatistic(statistic);
            }
            NoteDatabase.getNoteDatabase(getContext().getApplicationContext())
                    .getNoteDAO().deleteMultiplyNotes(tempList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            getNotes(DELETE_NOTES_CODE, true);
        }
    }
}