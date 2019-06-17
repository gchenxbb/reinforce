package com.reinforce.apk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SecActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sec);

        TextView mTextView = findViewById(R.id.tv_text_sec);

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SecActivity.this, "SecActivty!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
