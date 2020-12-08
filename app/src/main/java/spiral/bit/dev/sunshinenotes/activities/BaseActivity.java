package spiral.bit.dev.sunshinenotes.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.activities.create.CreateCheckListActivity;
import spiral.bit.dev.sunshinenotes.activities.create.CreateFolderActivity;
import spiral.bit.dev.sunshinenotes.activities.create.CreateNoteActivity;
import spiral.bit.dev.sunshinenotes.fragments.CheckListFragment;
import spiral.bit.dev.sunshinenotes.fragments.FoldersFragment;
import spiral.bit.dev.sunshinenotes.fragments.NotesFragment;
import spiral.bit.dev.sunshinenotes.models.PaintView;

import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_CHECK_LIST_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_FOLDER_CODE;
import static spiral.bit.dev.sunshinenotes.other.Utils.ADD_NOTE_CODE;

public class BaseActivity extends FragmentActivity {

    private Fragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        setUpQuickActions(this);
    }

    @SuppressLint("CommitPrefEdits")
    public void setUpQuickActions(final Context context) {
        final SmoothBottomBar bottomBar = findViewById(R.id.bottomBar);
        ImageView imageAddNoteMain = findViewById(R.id.icon_add_note_main);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment = getSupportFragmentManager().findFragmentById(R.id.replaced_container);
                if (fragment instanceof NotesFragment) {
                    Intent intent = new Intent(context, CreateNoteActivity.class);
                    startActivityForResult(intent, ADD_NOTE_CODE);
                } else if (fragment instanceof FoldersFragment) {
                    Intent intent = new Intent(context, CreateFolderActivity.class);
                    startActivityForResult(intent, ADD_FOLDER_CODE);
                } else if (fragment instanceof CheckListFragment) {
                    Intent intent = new Intent(context, CreateCheckListActivity.class);
                    startActivityForResult(intent, ADD_CHECK_LIST_CODE);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

}