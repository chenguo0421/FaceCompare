package com.cg.facecompare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cg.face.comparison.view.FaceCompareActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_face).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FaceCompareActivity.class)));
    }
}