package me.daemon.splashview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import me.daemon.splashview.demo.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Splash<String> splash = new Splash.Builder<String>(this)
                .imageUrl("http://pic6.huitu.com/res/20130116/84481_20130116142820494200_1.jpg")
                .tag("http://www.baidu.com")
                .duration(5)
                .callback(new Splash.Callback<String>() {
                    @Override
                    public void onSplashImageClicked(Splash<String> splash, String tag) {
                        Log.e(TAG, "onSplashImageClicked");
                    }

                    @Override
                    public void onSplashSkipClicked(Splash<String> splash, String tag) {
                        Log.e(TAG, "onSplashSkipClicked");
                    }

                    @Override
                    public void onSplashFinish(Splash<String> splash, String tag) {
                        Log.e(TAG, "onSplashFinish");
                    }
                })
                .build();
        splash.show(this);
    }
}
