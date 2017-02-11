package me.daemon.splashview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.daemon.splashview.demo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Splash<String> splash = new Splash.Builder<String>(this)
                .imageUrl("http://pic6.huitu.com/res/20130116/84481_20130116142820494200_1.jpg")
                .tag("http://www.baidu.com")
                .build();
        splash.show(this);
    }
}
