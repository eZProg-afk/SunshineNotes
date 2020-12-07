package spiral.bit.dev.sunshinenotes.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.fragments.CheckListFragment;
import spiral.bit.dev.sunshinenotes.fragments.CreateNoteFragment;
import spiral.bit.dev.sunshinenotes.fragments.FoldersFragment;
import spiral.bit.dev.sunshinenotes.fragments.NotesFragment;

public class CreateActivity extends AppCompatActivity {

    private Fragment fragment;
    private SmoothBottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            CreateNoteFragment createNoteFragment = new CreateNoteFragment();
            createNoteFragment.myOnKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }
}