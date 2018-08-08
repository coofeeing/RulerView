package com.example.hudson.rulerproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private RulerView rulerView;
    private TextView tvValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvValue = findViewById(R.id.tv_value);
        rulerView = findViewById(R.id.ruler_view);
        rulerView.setOnValueChangeListener(new OnValueChangeListener() {
            @Override
            public void onChange(float val) {
                tvValue.setText("" + val);
            }
        });
    }
}
