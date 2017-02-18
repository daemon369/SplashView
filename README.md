# SplashView
Android闪屏页面

# 使用方法

## 1. 引入项目依赖

项目根目录下build.gradle中加入：
```
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```
添加依赖：

```
dependencies {
    compile 'com.github.daemon369:SplashView:v0.0.1@aar'
}
```

## 2. 示例

在Activity中调用：

```
class SplashBean {
    final String imageUrl;
    final String url;

    SplashBean(String imageUrl, String url) {
        this.imageUrl = imageUrl;
        this.url = url;
    }
}

final SplashBean bean = new SplashBean("http://pic6.huitu.com/res/20130116/84481_20130116142820494200_1.jpg",
          "http://daemon369.github.io/");

final Splash<SplashBean> splash = new Splash.Builder<SplashBean>(MainActivity.this)
    .imageAdapter(new Splash.ImageAdapter<SplashBean>() {
        @Override
        public void setImage(Splash<SplashBean> splash, ImageView imageView) {
            Picasso.with(MainActivity.this).load(splash.getTag().imageUrl).into(imageView);
        }
    })
    .tag(bean)
    .duration(0)
    .callback(new Splash.Callback<SplashBean>() {
        @Override
        public void onSplashImageClicked(Splash<SplashBean> splash) {
            Log.e(TAG, "onSplashImageClicked");
            // 处理 Splash 的 ImageView 点击事件
        }

        @Override
        public void onSplashSkipClicked(Splash<SplashBean> splash) {
            Log.e(TAG, "onSplashSkipClicked");
            // 处理跳过 Splash 的处理
        }

        @Override
        public void onSplashFinish(Splash<SplashBean> splash) {
            Log.e(TAG, "onSplashFinish");
            // 指定 Splash 显示时间(秒)时，处理倒计时结束
        }
     })
     .build();

splash.show(this);

```

# 效果

<img src="demo.png" width = "540" height = "960" alt="Demo效果" align=center />

# Thanks:
https://github.com/jkyeo/Android-SplashView
