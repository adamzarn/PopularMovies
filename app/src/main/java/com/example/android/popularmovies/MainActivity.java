package com.example.android.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.res.AssetManager;
import android.content.Context;
import java.io.InputStream;
import java.io.IOException;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = getApplicationContext();
        AssetManager am = context.getAssets();
        try {
            InputStream is = am.open("keys.txt");
            Scanner s = new Scanner(is).useDelimiter("\\A");
            String apiKey = s.hasNext() ? s.next() : "";
            System.out.println(apiKey);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
