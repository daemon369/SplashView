package me.daemon.splashview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import me.daemon.splashview.demo.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        class SplashBean {
            final String imageUrl;
            final String url;

            SplashBean(String imageUrl, String url) {
                this.imageUrl = imageUrl;
                this.url = url;
            }
        }

        SplashBean bean = new SplashBean("http://pic6.huitu.com/res/20130116/84481_20130116142820494200_1.jpg", "http://www.baidu.com");

        final Splash<SplashBean> splash = new Splash.Builder<SplashBean>(this)
                .imageAdapter(new Splash.ImageAdapter<SplashBean>() {
                    @Override
                    public void setImage(Splash<SplashBean> splash, ImageView imageView) {
                        Picasso.with(MainActivity.this).load(splash.getTag().imageUrl).into(imageView);
                    }
                })
                .tag(bean)
                .duration(5)
                .callback(new Splash.Callback<SplashBean>() {
                    @Override
                    public void onSplashImageClicked(Splash<SplashBean> splash) {
                        Log.e(TAG, "onSplashImageClicked");
                        openBrowser(splash.getTag().url);
                    }

                    @Override
                    public void onSplashSkipClicked(Splash<SplashBean> splash) {
                        Log.e(TAG, "onSplashSkipClicked");
                    }

                    @Override
                    public void onSplashFinish(Splash<SplashBean> splash) {
                        Log.e(TAG, "onSplashFinish");
                    }
                })
                .build();
        splash.show(this);
    }

    private void openBrowser(final String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
