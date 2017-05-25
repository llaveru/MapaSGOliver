package com.example.hpasarin.mapasgoliver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by hpasarin on 24/05/2017.
 */

public class ActividadSplash extends AppCompatActivity {
Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splashlayout);




        handler.postDelayed(
                new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
                finalizarActivity();
            }


                },5000);



    }

    private void finalizarActivity() {
        this.finish();
    }


}
