package spiral.bit.dev.sunshinenotes.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.fragments.CheckListFragment;
import spiral.bit.dev.sunshinenotes.fragments.CreateCheckListFragment;
import spiral.bit.dev.sunshinenotes.fragments.CreateFolderFragment;
import spiral.bit.dev.sunshinenotes.fragments.CreateNoteFragment;
import spiral.bit.dev.sunshinenotes.fragments.FoldersFragment;
import spiral.bit.dev.sunshinenotes.fragments.NotesFragment;
//
public class BaseActivity extends FragmentActivity {

    private Fragment fragment;
    private static ConstraintLayout bottomMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        bottomMenu = findViewById(R.id.bottom_menu);
        setUpQuickActions();
    }

    public static void switchBottomMenu(boolean isEnabled) {
        if (isEnabled) bottomMenu.setVisibility(View.VISIBLE);
        else bottomMenu.setVisibility(View.GONE);
    }

    @SuppressLint("CommitPrefEdits")
    public void setUpQuickActions() {
        SmoothBottomBar bottomBar = findViewById(R.id.bottomBar);
        ImageView imageAddNoteMain = findViewById(R.id.icon_add_note_main);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment = getSupportFragmentManager().findFragmentById(R.id.replaced_container);
                if (fragment instanceof NotesFragment) {
                    CreateNoteFragment createNoteFragment = new CreateNoteFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.replaced_container, createNoteFragment)
                            .commit();
                } else if (fragment instanceof FoldersFragment) {
                    CreateFolderFragment createFolderFragment = new CreateFolderFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.replaced_container, createFolderFragment)
                            .commit();
                } else if (fragment instanceof CheckListFragment) {
                    CreateCheckListFragment createCheckListFragment = new CreateCheckListFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.replaced_container, createCheckListFragment)
                            .commit();
                }
            }
        });
        bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                fragment = getSupportFragmentManager().findFragmentById(R.id.replaced_container);
                if (i == 0) {
                    NotesFragment notesFragment = new NotesFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.replaced_container, notesFragment)
                            .commit();
                } else if (i == 1) {
                    CheckListFragment checkListFragment = new CheckListFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.replaced_container, checkListFragment)
                            .commit();
                } else if (i == 2) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    FoldersFragment foldersFragment = new FoldersFragment();
                    fragmentTransaction.replace(R.id.replaced_container, foldersFragment)
                            .commit();
                }
                return true;
            }
        });
    }
}