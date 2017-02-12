package me.daemon.splashview;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * @author daemon369
 * @since 2017/2/11
 */
public class Splash<T> {

    private final static String TAG = Splash.class.getSimpleName();

    private final Integer duration;
    private final String imageUrl;
    private final T tag;
    private final View splashView;
    private final ImageView imageView;
    private final TextView timeView;
    private final View skipView;
    private final Callback<T> callback;
    private WeakReference<Activity> activityRef;

    private Splash(final Context context, final Integer layoutId, final View view, final Integer duration, final String imageUrl, final T tag, Callback<T> callback) {
        this.duration = duration;
        this.imageUrl = imageUrl;
        this.tag = tag;
        this.callback = callback;

        if (null != layoutId) {
            splashView = LayoutInflater.from(context).inflate(layoutId, null);
        } else if (null != view) {
            splashView = view;
        } else {
            splashView = new SplashView(context);
        }

        imageView = (ImageView) splashView.findViewById(R.id.splash_view_image);
        if (null == imageView) {
            throw new IllegalStateException("can't find an image view with id R.id.splash_view_image");
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                if (null != Splash.this.callback) {
                    Splash.this.callback.onSplashImageClicked(Splash.this, tag);
                }
            }
        });

        timeView = (TextView) splashView.findViewById(R.id.splash_view_time);
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
                    Splash.this.callback.onSplashSkipClicked(Splash.this, tag);
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

    private void showInternal(final Activity activity) {
        if (isActivityAlive(activity)) {
            final ViewGroup content = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
            final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

            content.addView(splashView, lp);

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
                                callback.onSplashFinish(Splash.this, tag);
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
                    final ViewGroup content = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
                    content.removeView(splashView);
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

        void onSplashImageClicked(final Splash<T> splash, final T tag);

        void onSplashSkipClicked(final Splash<T> splash, final T tag);

        void onSplashFinish(final Splash<T> splash, final T tag);
    }

    public static class Builder<T> {
        private final Context context;
        private Integer layoutId;
        private View view;
        private Integer duration;
        private String imageUrl;
        private T tag;
        private Callback<T> callback;

        public Builder(Context context) {
            if (null == context) {
                throw new IllegalArgumentException("Context must not be null");
            }
            this.context = context;
        }

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

        public Builder<T> imageUrl(final String imgUrl) {
            if (TextUtils.isEmpty(imgUrl)) {
                throw new IllegalArgumentException("imageUrl must not be empty");
            }
            if (null != this.imageUrl) {
                throw new IllegalStateException("imageUrl already set");
            }
            this.imageUrl = imgUrl;
            return this;
        }

        public Builder<T> tag(final T tag) {
            if (null == tag) {
                throw new IllegalStateException("tag already set");
            }
            this.tag = tag;
            return this;
        }

        public Builder<T> callback(final Callback<T> callback) {
            if (null != this.callback && null != callback) {
                throw new IllegalStateException("callback already set");
            }
            this.callback = callback;
            return this;
        }

        public Splash<T> build() {
            return new Splash<T>(context, layoutId, view, duration, imageUrl, tag, callback);
        }
    }
}
