package com.example.samsungshoeshoo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import javax.annotation.Nullable;

public class SplashActivity extends AppCompatActivity {

    ImageView ssLogo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find ImageView of Logo
        ssLogo = findViewById(R.id.ssLogo);

        // Create Animation
        Animation startAnim = AnimationUtils.loadAnimation(this,R.anim.splashtrans);

        // Start Animation on Image View of Logo
        ssLogo.startAnimation(startAnim);

        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    sleep(700);  // Delay of 0.7 second
                } catch (Exception e) {

                } finally {

                    Intent i = new Intent(SplashActivity.this,
                            MainHomePage.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        welcomeThread.start();

    }


}
