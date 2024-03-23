package com.example.baivolley.contronller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.baivolley.R;
import com.example.baivolley.model.User;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    TextView id, userName, userEmail, gender;
    Button btnLogout;
    ImageView imageViewpprofile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (SharedPrefManager.getInstance(this)
                             .isLoggedIn()) {
            id = findViewById(R.id.tv_id);
            userName = findViewById(R.id.tv_username);
            userEmail = findViewById(R.id.tv_email);
            gender = findViewById(R.id.tv_gender);
            btnLogout = findViewById(R.id.button_logout);
            imageViewpprofile = findViewById(R.id.img_avt);

            User user = SharedPrefManager.getInstance(this)
                                         .getUser();

            id.setText(String.valueOf(user.getId()));
            userEmail.setText(user.getEmail());
            gender.setText(user.getGender());
            userName.setText(user.getName());

            Glide.with(getApplicationContext())
                 .load(user.getImages())
                 .into(imageViewpprofile);

            btnLogout.setOnClickListener(this);

        } else {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(btnLogout)) {
            SharedPrefManager.getInstance(this)
                             .logout();
        }
    }

}