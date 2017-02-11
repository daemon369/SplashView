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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * @author daemon369
 * @since 2017/2/11
 */
public class Splash<T> {

    private final static String TAG = Splash.class.getSimpleName();

    private final Integer layoutId;
    private final View view;
    private final Integer duration;
    private final String imageUrl;
    private final T tag;
    private View splashView;
    private ImageView imageView;
    private Button skip;

    private Splash(final Context context, final Integer layoutId, final View view, final Integer duration, final String imageUrl, final T tag) {
        this.layoutId = layoutId;
        this.view = view;
        this.duration = duration;
        this.imageUrl = imageUrl;
        this.tag = tag;

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

        skip = (Button) splashView.findViewById(R.id.splash_view_skip);
        if (null == skip) {
            throw new IllegalStateException("can't find a button with id R.id.splash_view_skip");
        }

    }

    public boolean isActivityAlive(final Activity activity) {
        return null != activity && !activity.isFinishing();
    }

    public void show(final Activity activity) {
        if (null != splashView.getParent()) {
            Log.i(TAG, "SplashView is showing already");
            return;
        }

        final Runnable showTask = new Runnable() {
            @UiThread
            @Override
            public void run() {
                if (isActivityAlive(activity)) {
                    final ViewGroup content = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
                    LayoutParams lp = null;
                    if (content instanceof FrameLayout) {
                        lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    } else if (content instanceof RelativeLayout) {
                        lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    } else if (content instanceof LinearLayout) {
                        lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    }

                    if (null == lp) {
                        throw new IllegalStateException("unrecognized ViewGroup: " + content.getClass().getCanonicalName());
                    }

                    content.addView(splashView, lp);
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

    public void hide(final Activity activity) {
        if (null == splashView.getParent()) {
            Log.i(TAG, "SplashView is not showing yet");
            return;
        }

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

    public static class Builder<T> {
        private final Context context;
        private Integer layoutId;
        private View view;
        private Integer duration;
        private String imageUrl;
        private T tag;

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
                throw new IllegalArgumentException("imageUrl already set");
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

        public Splash<T> build() {
            return new Splash<T>(context, layoutId, view, duration, imageUrl, tag);
        }
    }
}
