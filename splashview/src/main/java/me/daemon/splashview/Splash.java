package me.daemon.splashview;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.support.annotation.UiThread;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.concurrent.TimeUnit;

/**
 * @author daemon369
 * @since 2017/2/11
 */
public class Splash<T> {

    private final static String TAG = Splash.class.getSimpleName();

    private final Integer duration;
    private final T tag;
    private final View splashView;
    private final ImageView imageView;
    private final TextView timeView;
    private final View skipView;
    private final Callback<T> callback;
    private final ImageAdapter<T> imageAdapter;
    private WeakReference<Activity> activityRef;
    private boolean isActionBarShowing;

    private Splash(final Context context, final Integer layoutId, final View view, final Integer duration, final T tag, Callback<T> callback, ImageAdapter<T> imageAdapter) {
        this.duration = duration;
        this.tag = tag;
        this.callback = callback;
        this.imageAdapter = imageAdapter;

        if (null != layoutId) {
            splashView = LayoutInflater.from(context).inflate(layoutId, null);
        } else if (null != view) {
            splashView = view;
        } else {
            splashView = new SplashView(context);
        }

        imageView = splashView.findViewById(R.id.splash_view_image);
        if (null == imageView) {
            throw new IllegalStateException("can't find an image view with id R.id.splash_view_image");
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                if (null != Splash.this.callback) {
                    Splash.this.callback.onSplashImageClicked(Splash.this);
                }
            }
        });

        timeView = splashView.findViewById(R.id.splash_view_time);
        if (null == timeView) {
            throw new IllegalStateException("can't find a text view with id R.id.splash_view_time");
        }

        skipView = splashView.findViewById(R.id.splash_view_skip);
        if (null == skipView) {
            throw new IllegalStateException("can't find a view with id R.id.splash_view_skip");
        }

        skipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                if (null != Splash.this.callback) {
                    Splash.this.callback.onSplashSkipClicked(Splash.this);
                }
            }
        });
    }

    public View getSplashView() {
        return splashView;
    }

    public T getTag() {
        return tag;
    }

    private boolean isActivityAlive(final Activity activity) {
        return null != activity && !activity.isFinishing();
    }

    protected void hideSystemUi(final Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (activity instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (null != supportActionBar) {
                supportActionBar.setShowHideAnimationEnabled(false);
                isActionBarShowing = supportActionBar.isShowing();
                supportActionBar.hide();
            }
        } else {
            android.app.ActionBar actionBar = activity.getActionBar();
            if (null != actionBar) {
                isActionBarShowing = actionBar.isShowing();
                actionBar.hide();
            }
        }
    }

    protected void showSystemUi(final Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (activity instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (null != supportActionBar) {
                if (isActionBarShowing) supportActionBar.show();
            }
        } else {
            android.app.ActionBar actionBar = activity.getActionBar();
            if (null != actionBar) {
                if (isActionBarShowing) actionBar.show();
            }
        }
    }

    private void showInternal(final Activity activity) {
        if (isActivityAlive(activity)) {
            final ViewGroup content = activity.getWindow().getDecorView().findViewById(android.R.id.content);
            final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

            content.addView(splashView, lp);

            if (null != imageAdapter) {
                imageAdapter.setImage(this, imageView);
            }

            hideSystemUi(activity);

            if (null != duration && duration > 0) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setText(String.valueOf(duration));
                final Runnable countDown = new Runnable() {

                    int leftOver = duration;

                    @Override
                    public void run() {
                        leftOver--;
                        if (leftOver > 0) {
                            timeView.setText(String.valueOf(leftOver));
                            splashView.postDelayed(this, TimeUnit.SECONDS.toMillis(1));
                        } else {
                            hide();
                            if (null != callback) {
                                callback.onSplashFinish(Splash.this);
                            }
                        }
                    }
                };

                splashView.postDelayed(countDown, TimeUnit.SECONDS.toMillis(1));
            } else {
                timeView.setVisibility(View.GONE);
            }
        }
    }

    public void show(final Activity activity) {
        if (null != splashView.getParent()) {
            Log.i(TAG, "SplashView is showing already");
            return;
        }

        activityRef = new WeakReference<>(activity);

        if (isActivityAlive(activity)) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                showInternal(activity);
            } else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showInternal(activity);
                    }
                });
            }
        } else {
            Log.i(TAG, "activity is not alive: " + activity);
        }
    }

    public void hide() {
        if (null == splashView.getParent()) {
            Log.i(TAG, "SplashView is not showing yet");
            return;
        }

        final Activity activity = null != activityRef ? activityRef.get() : null;

        final Runnable showTask = new Runnable() {
            @UiThread
            @Override
            public void run() {
                if (isActivityAlive(activity)) {
                    final ViewGroup content = activity.getWindow().getDecorView().findViewById(android.R.id.content);
                    content.removeView(splashView);

                    showSystemUi(activity);
                }
            }
        };

        if (isActivityAlive(activity)) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                showTask.run();
            } else {
                activity.runOnUiThread(showTask);
            }
        } else {
            Log.i(TAG, "activity is not alive: " + activity);
        }
    }

    public interface Callback<T> {

        void onSplashImageClicked(final Splash<T> splash);

        void onSplashSkipClicked(final Splash<T> splash);

        void onSplashFinish(final Splash<T> splash);
    }

    public interface ImageAdapter<T> {

        void setImage(final Splash<T> splash, final ImageView imageView);
    }

    public static class Builder<T> {
        private final Context context;
        private Class<? extends Splash> clz;
        private Integer layoutId;
        private View view;
        private Integer duration;
        private T tag;
        private Callback<T> callback;
        private ImageAdapter<T> imageAdapter;

        public Builder(Context context) {
            if (null == context) {
                throw new IllegalArgumentException("Context must not be null");
            }
            this.context = context;
        }

        /**
         * 设置自定义布局，
         * 自定义布局需要包含id为R.id.splash_view_image的ImageView、
         * id为R.id.splash_view_time的TextView
         * 以及id为R.id.splash_view_skip的View
         *
         * @param layoutId 布局资源ID
         */
        public Builder<T> layoutId(final int layoutId) {
            if (layoutId <= 0) {
                throw new IllegalArgumentException("layoutId must be positive");
            }
            if (null != this.layoutId) {
                throw new IllegalStateException("layoutId already set");
            }
            this.layoutId = layoutId;
            return this;
        }

        /**
         * 设置自定义布局，
         * 自定义布局需要包含id为R.id.splash_view_image的ImageView、
         * id为R.id.splash_view_time的TextView
         * 以及id为R.id.splash_view_skip的View
         *
         * @param view 布局View
         */
        public Builder<T> view(final View view) {
            if (null == view) {
                throw new IllegalArgumentException("view must not be null");
            }
            if (null != this.view) {
                throw new IllegalStateException("view already set");
            }
            this.view = view;
            return this;
        }

        /**
         * 设置倒计时时间，不设置或设置为0，{@link SplashView}不自动退出
         */
        public Builder<T> duration(final int duration) {
            if (duration < 0) {
                throw new IllegalArgumentException("duration must not be negative");
            }
            if (null != this.duration) {
                throw new IllegalStateException("duration already set");
            }
            this.duration = duration;
            return this;
        }

        /**
         * 设置tag
         */
        public Builder<T> tag(final T tag) {
            if (null == tag) {
                throw new IllegalStateException("tag already set");
            }
            this.tag = tag;
            return this;
        }

        /**
         * 设置回调
         */
        public Builder<T> callback(final Callback<T> callback) {
            if (null != this.callback && null != callback) {
                throw new IllegalStateException("callback already set");
            }
            this.callback = callback;
            return this;
        }

        /**
         * 设置图片适配器
         */
        public Builder<T> imageAdapter(final ImageAdapter<T> imageAdapter) {
            if (null != this.imageAdapter) {
                throw new IllegalStateException("imageAdapter already set");
            }
            this.imageAdapter = imageAdapter;
            return this;
        }

        /**
         * 设置自定义实现
         */
        public Builder<T> implementation(final Class<? extends Splash> clz) {
            if (this.clz != null) {
                throw new IllegalStateException("custom implementation already set");
            }
            this.clz = clz;
            return this;
        }

        public Splash<T> build() {
            Class<? extends Splash> c = Splash.class;
            if (this.clz != null) {
                c = this.clz;
            }

            try {
                final Constructor<? extends Splash> constructor =
                        c.getDeclaredConstructor(Context.class, Integer.class, View.class,
                                Integer.class, Object.class, Callback.class, ImageAdapter.class);
                constructor.setAccessible(true);
                return constructor.newInstance(context, layoutId, view, duration, tag, callback, imageAdapter);
            } catch (Exception e) {
                return new Splash<>(context, layoutId, view, duration, tag, callback, imageAdapter);
            }
        }
    }
}
