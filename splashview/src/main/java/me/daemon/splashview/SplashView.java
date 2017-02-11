package me.daemon.splashview;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @author daemon369
 * @since 2017/2/11
 */
public class SplashView extends FrameLayout {
    public SplashView(Context context) {
        super(context);

        init();
    }

    public SplashView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public SplashView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SplashView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_splash_view, this);
    }
}
