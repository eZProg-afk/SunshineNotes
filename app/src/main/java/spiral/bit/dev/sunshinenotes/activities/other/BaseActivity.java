package spiral.bit.dev.sunshinenotes.activities.other;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.shashank.sony.fancytoastlib.FancyToast;
import java.util.concurrent.TimeUnit;
import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.create.CreateCheckListActivity;
import spiral.bit.dev.sunshinenotes.activities.create.CreateFolderActivity;
import spiral.bit.dev.sunshinenotes.activities.create.CreateNoteActivity;
import spiral.bit.dev.sunshinenotes.activities.lock.PasswordActivity;
import spiral.bit.dev.sunshinenotes.activities.lock.PatternLockActivity;
import spiral.bit.dev.sunshinenotes.activities.lock.PinCodeActivity;
import spiral.bit.dev.sunshinenotes.fragments.CheckListFragment;
import spiral.bit.dev.sunshinenotes.fragments.FoldersFragment;
import spiral.bit.dev.sunshinenotes.fragments.InFolderFragment;
import spiral.bit.dev.sunshinenotes.fragments.NotesFragment;
import spiral.bit.dev.sunshinenotes.other.RoomWorker;
import static spiral.bit.dev.sunshinenotes.fragments.CheckListFragment.REQUEST_CODE_ENABLE;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_CHECK_LIST_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_FOLDER_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_NOTE_CODE;

public class BaseActivity extends FragmentActivity {

    private Fragment fragment;
    private AlertDialog dialogTypeSecurity;
    private SharedPreferences preferenceSettings, prefPass, prefPassword, graphicPref, checkedPref;
    private SharedPreferences.Editor editorPrefPin, editorGraphicKey, editorPrefPassword, editorIsShowed, editorPrefSetings;
    private static SmoothBottomBar bottomBar;
    private AlertDialog blockAdsDialog;
    private static LinearLayout layoutQuickActions;
    private static ImageView imageAddNoteMain;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        startWorkManager();
        setUpQuickActions(this);
        editorPrefSetings = preferenceSettings.edit();
        if (!preferenceSettings.getBoolean("blockDialogForever", false)) {
            showBlockAdsDialog();
        }
    }

    private void startWorkManager() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(RoomWorker.class, 15, TimeUnit.MINUTES,
                15, TimeUnit.MINUTES)
                .build();
        WorkManager workManager = WorkManager.getInstance(BaseActivity.this);
        workManager.enqueue(workRequest);
    }

    public void showBlockAdsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.layout_block_ads,
                        (ViewGroup) findViewById(R.id.layout_block_ads_container));
        builder.setView(view);
        blockAdsDialog = builder.create();
        if (blockAdsDialog.getWindow() != null) {
            blockAdsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        view.findViewById(R.id.text_disable_ads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blockAdsDialog.dismiss();
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });
        view.findViewById(R.id.text_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blockAdsDialog.dismiss();
            }
        });
        view.findViewById(R.id.text_cancel_forever).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorPrefSetings.putBoolean("blockDialogForever", true);
                editorPrefSetings.apply();
                blockAdsDialog.dismiss();
            }
        });
        blockAdsDialog.show();
    }


    @SuppressLint("CommitPrefEdits")
    public void setUpQuickActions(final Context context) {
        if (getIntent().hasExtra("fromFolderActivity")) {
            if (getIntent().getBooleanExtra("fromFolderActivity", false))
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.replaced_container, new FoldersFragment())
                        .commit();
        }

        preferenceSettings = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        prefPass = getSharedPreferences("pass", 0);
        prefPassword = getSharedPreferences("password", 0);
        graphicPref = getSharedPreferences("graphic", 0);
        editorGraphicKey = graphicPref.edit();
        editorPrefPassword = prefPassword.edit();
        editorPrefPin = prefPass.edit();
        checkedPref = getSharedPreferences("check", 0);
        editorIsShowed = checkedPref.edit();

        checkLock();
//        if (preferenceSettings.getBoolean("dark", false)) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        bottomBar = findViewById(R.id.bottomBar);
        layoutQuickActions = findViewById(R.id.layout_quick_actions);
        imageAddNoteMain = findViewById(R.id.icon_add_note_main);
        ImageView billingImage = findViewById(R.id.icon_remove_ads);

        billingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BaseActivity.this, StatisticsActivity.class));
            }
        });
        ImageView secureImage = findViewById(R.id.icon_pin_code);
        secureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewSecureDialog();
            }
        });
        ImageView imageToTrash = findViewById(R.id.trash_icon);
        imageToTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BaseActivity.this, TrashActivity.class));
            }
        });
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment = getSupportFragmentManager().findFragmentById(R.id.replaced_container);
                if (fragment instanceof NotesFragment) {
                    startActivityForResult(new Intent(context, CreateNoteActivity.class), ADD_NOTE_CODE);
                } else if (fragment instanceof FoldersFragment) {
                    startActivityForResult(new Intent(context, CreateFolderActivity.class), ADD_FOLDER_CODE);
                } else if (fragment instanceof CheckListFragment) {
                    startActivityForResult(new Intent(context, CreateCheckListActivity.class), ADD_CHECK_LIST_CODE);
                }
            }
        });
        bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                if (i == 0) {
                    NotesFragment notesFragment = new NotesFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.replaced_container, notesFragment)
                            .commit();
                } else if (i == 1) {
                    CheckListFragment checkListFragment = new CheckListFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.replaced_container, checkListFragment)
                            .commit();
                } else if (i == 2) {
                    FoldersFragment foldersFragment = new FoldersFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.replaced_container, foldersFragment)
                            .commit();
                }
                return true;
            }
        });
    }

    public static void disableBar() {
        bottomBar.setVisibility(View.INVISIBLE);
        layoutQuickActions.setVisibility(View.INVISIBLE);
        imageAddNoteMain.setVisibility(View.INVISIBLE);
    }

    public static void enableBar() {
        bottomBar.setVisibility(View.VISIBLE);
        layoutQuickActions.setVisibility(View.VISIBLE);
        imageAddNoteMain.setVisibility(View.VISIBLE);
    }

    private void showNewSecureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_type_of_security, (
                ViewGroup) findViewById(R.id.layout_type_of_security_container));
        builder.setView(view);
        dialogTypeSecurity = builder.create();
        if (dialogTypeSecurity.getWindow() != null) {
            dialogTypeSecurity.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        view.findViewById(R.id.text_pin_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BaseActivity.this, PinCodeActivity.class);
                intent.putExtra("set", "set");
                startActivityForResult(intent, REQUEST_CODE_ENABLE);
                dialogTypeSecurity.dismiss();
            }
        });
        view.findViewById(R.id.text_graphic_key).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BaseActivity.this, PatternLockActivity.class);
                intent.putExtra("type", "create");
                startActivity(intent);
                dialogTypeSecurity.dismiss();
            }
        });
        view.findViewById(R.id.text_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BaseActivity.this, PasswordActivity.class);
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
                if (preferenceSettings.getBoolean("remove_toasts", false)) FancyToast.makeText(
                        BaseActivity.this,
                        getString(R.string.lock_removed_toast),
                        FancyToast.LENGTH_LONG,
                        FancyToast.SUCCESS,
                        false).show();
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragment = getSupportFragmentManager().findFragmentById(R.id.replaced_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferenceSettings.getBoolean("dark", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void checkLock() { // FIXME: 04.12.2020
        if (getIntent().getBooleanExtra("isFromSet", false)) {
        } else if (getIntent().getBooleanExtra("setForget", false)) {
        } else if (getIntent().getBooleanExtra("isFromBackKey", false)) {
        } else if (!prefPass.getString("pin-code", "").isEmpty() && !checkedPref.getBoolean("isShowed", false)) {
            startActivity(new Intent(BaseActivity.this, PinCodeActivity.class));
            editorIsShowed.putBoolean("isShowed", true);
            editorIsShowed.apply();
        } else if (!prefPass.getString("pin-code", "").isEmpty() && checkedPref.getBoolean("isShowed", false)) {
            editorIsShowed.remove("isShowed");
            editorIsShowed.apply();
        }
        if (getIntent().getBooleanExtra("isFromBackKey", false)) {
        } else if (!prefPassword.getString("passwordCode", "").isEmpty()) {
            Intent intent = new Intent(BaseActivity.this, PasswordActivity.class);
            intent.putExtra("inputPassword", true);
            startActivity(intent);
        }
        if (getIntent().getBooleanExtra("isFromBackKey", false)) {
        } else if (!graphicPref.getString("graphic_key", "").isEmpty() && !checkedPref.getBoolean("isShowed", false)) {
            editorIsShowed.putBoolean("isShowed", true);
            editorIsShowed.apply();
            Intent intent = new Intent(BaseActivity.this, PatternLockActivity.class);
            intent.putExtra("type", "input");
            startActivity(intent);
        } else if (!graphicPref.getString("graphic_key", "").isEmpty() && checkedPref.getBoolean("isShowed", false)) {
            editorIsShowed.remove("isShowed");
            editorIsShowed.apply();
        }
    }

    @Override
    public void onBackPressed() {
        fragment = getSupportFragmentManager().findFragmentById(R.id.replaced_container);
        if (fragment instanceof InFolderFragment) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.replaced_container, new FoldersFragment())
                    .commit();
        }
    }
}