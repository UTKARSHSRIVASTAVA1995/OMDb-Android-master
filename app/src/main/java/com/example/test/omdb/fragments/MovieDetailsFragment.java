package com.example.test.omdb.fragments;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.test.omdb.R;
import com.squareup.picasso.Picasso;

public class MovieDetailsFragment extends BottomSheetDialogFragment {

    private TextView title, release, time, description;
    private ImageView poster;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_movie_details, null);

        poster = (ImageView) contentView.findViewById(R.id.sheet_poster);
        title = (TextView) contentView.findViewById(R.id.sheet_title);
        release = (TextView) contentView.findViewById(R.id.sheet_release);
        time = (TextView) contentView.findViewById(R.id.sheet_time);
        description = (TextView) contentView.findViewById(R.id.sheet_description);

        Picasso.with(getContext()).load(getArguments().getString("poster")).fit().into(poster);
        title.setText(getArguments().getString("title"));
        release.setText(getArguments().getString("release"));
        time.setText(getArguments().getString("time"));
        description.setText(getArguments().getString("description"));
        dialog.setContentView(contentView);
    }
}
