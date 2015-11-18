package lecho.lib.hellocharts.samples;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MILLS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goHome();
            }
        }, SPLASH_DELAY_MILLS);
    }

    private void goHome(){
        Intent intent = new Intent(SplashActivity.this, Main2Activity.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }
}
