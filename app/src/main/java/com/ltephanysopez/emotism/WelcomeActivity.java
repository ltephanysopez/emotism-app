package com.ltephanysopez.emotism;

import android.app.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;


public class WelcomeActivity extends Activity {
    private Button getStartedBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getStartedBtn = (Button) findViewById(R.id.getStarted);
        getStartedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLoginActivity();
            }
        });
    }

    public void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
