package com.ltephanysopez.emotism;

import android.app.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;


public class MainActivity extends Activity {
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.getStarted);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAnalyzeActivity();
            }
        });
    }

    public void openAnalyzeActivity() {
        Intent intent = new Intent(this, AnalyzeActivity.class);
        startActivity(intent);
    }
}
