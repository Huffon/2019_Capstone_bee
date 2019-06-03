package com.example.huffon.bee_and;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectActivity extends AppCompatActivity {

    Button normal;
    Button barrier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        normal = (Button) findViewById(R.id.norm);
        barrier = (Button) findViewById(R.id.barrier);

        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectActivity.this, STTActivity.class);
                startActivity(intent);
                finish();
            }
        });

        barrier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectActivity.this, TabActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}