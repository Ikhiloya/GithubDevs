package com.loya.githubdevs.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.loya.githubdevs.R;
import com.loya.githubdevs.db.GitItem;
import com.loya.githubdevs.viewmodel.UserProfileViewModel;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {
    private UserProfileViewModel mUserViewModel;
    private int userId;
    public static final String TEXT_PLAIN = "text/plain";
    private ImageView mImageView;
    private TextView mUsernameText;
    private String mUsername;
    private String mProfileUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mImageView = findViewById(R.id.detail_image);
        mUsernameText = findViewById(R.id.detail_username);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(MainActivity.USER_ID)) {
                userId = intent.getIntExtra(MainActivity.USER_ID, 0);
            }

            mUserViewModel = ViewModelProviders.of(this).get(UserProfileViewModel.class);

            mUserViewModel.getUser(userId).observe(this, new Observer<GitItem>() {
                @Override
                public void onChanged(@Nullable GitItem user) {
                    System.out.println("id: " + user.getId() + " Login: " + user.getLogin());
                    mUsername = user.getLogin();
                    mProfileUrl = user.getHtmlUrl();
                    loadImage(user.getAvatarUrl());
                    mUsernameText.setText(mUsername);
                }
            });

        }
    }


    private void loadImage(String url) {
        Picasso.get()
                .load(url)
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .error(R.drawable.ic_error_outline_black_24dp)
                .into(mImageView);
    }

    public void shareProfile(View view) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(TEXT_PLAIN);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this awesome developer " + mUsername + "\n" + mProfileUrl);
        // check if the destination device has an app to share this content
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(shareIntent);
        }
    }

    public void viewProfile(View view) {
        // Convert the String URL into a URI object (to pass into the Intent constructor)
        Uri url = Uri.parse(mProfileUrl);
        // Create a new intent to view the earthquake URI
        Intent websiteIntent = new Intent(Intent.ACTION_VIEW, url);
        // Send the intent to launch a new activity
        startActivity(websiteIntent);
    }
}
