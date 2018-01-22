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

            private SplashBean(String imageUrl, String url) {
                this.imageUrl = imageUrl;
                this.url = url;
            }
        }

        SplashBean bean = new SplashBean("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1486907589533&di=1a9665fbf75fa80399a07c446d72f25a&imgtype=0&src=http%3A%2F%2Fattach.bbs.miui.com%2Fforum%2F201112%2F02%2F124550nk6mz2f6kh3hfkn1.png", "http://www.baidu.com");

        final Splash<SplashBean> splash = new Splash.Builder<SplashBean>(this)
                .imageAdapter(new Splash.ImageAdapter<SplashBean>() {
                    @Override
                    public void setImage(Splash<SplashBean> splash, ImageView imageView) {
                        Picasso.with(MainActivity.this).load(splash.getTag().imageUrl).fit().into(imageView);
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
                .layoutId(R.layout.splash_layout)
                .build();
        splash.show(this);
    }

    private void openBrowser(final String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
