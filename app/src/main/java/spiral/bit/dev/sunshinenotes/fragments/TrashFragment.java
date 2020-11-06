package spiral.bit.dev.sunshinenotes.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import spiral.bit.dev.sunshinenotes.R;

public class TrashFragment extends Fragment {

    public TrashFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_trash, container, false);


        return view;
    }
}