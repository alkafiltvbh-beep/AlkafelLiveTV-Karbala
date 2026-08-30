package tv.alkafel.live;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private static final String STREAM_URL =
            "https://ch14.tv/live/5net/abbassia.m3u8";

    private static final int BG = Color.rgb(5, 5, 5);
    private static final int CARD = Color.rgb(18, 18, 18);
    private static final int RED = Color.rgb(170, 0, 0);
    private static final int RED2 = Color.rgb(235, 25, 25);
    private static final int GOLD = Color.rgb(210, 170, 75);
    private static final int MUTED = Color.rgb(180, 180, 180);

    private ExoPlayer player;
    private PlayerView playerView;
    private LinearLayout prayerRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(14), dp(14), dp(14), dp(30));
        page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scroll.addView(page);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(8), dp(4), dp(8), dp(8));

        TextView menu = iconText("☰", 24);
        header.addView(menu, fixed(dp(42), dp(42)));

        LinearLayout brand = new LinearLayout(this);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        brand.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams brandLp = new LinearLayout.LayoutParams(0, dp(74), 1f);

        ImageView logo = new ImageView(this);
        logo.setImageResource(getResources().getIdentifier("channel_logo", "drawable", getPackageName()));
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        brand.addView(logo, new LinearLayout.LayoutParams(dp(70), dp(70)));

        LinearLayout brandText = new LinearLayout(this);
        brandText.setOrientation(LinearLayout.VERTICAL);
        brandText.setGravity(Gravity.CENTER_VERTICAL);
        brandText.addView(txt("الكافل الفضائية", 24, true, Color.WHITE));
        brandText.addView(txt("AL-KAFEL TV", 11, true, GOLD));
        brand.addView(brandText);

        header.addView(brand, brandLp);

        TextView bell = iconText("●", 18);
        bell.setTextColor(RED2);
        header.addView(bell, fixed(dp(42), dp(42)));
        page.addView(header);

        LinearLayout liveCard = cardContainer(16, RED);
        liveCard.setPadding(dp(8), dp(8), dp(8), dp(10));

        LinearLayout liveTop = new LinearLayout(this);
        liveTop.setOrientation(LinearLayout.HORIZONTAL);
        liveTop.setGravity(Gravity.CENTER_VERTICAL);
        TextView liveText = txt("●  مباشر الآن", 15, true, RED2);
        liveTop.addView(liveText, new LinearLayout.LayoutParams(0, dp(36), 1f));
        liveTop.addView(txt("البث المباشر", 17, true, Color.WHITE));
        liveCard.addView(liveTop);

        playerView = new PlayerView(this);
        playerView.setUseController(true);
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        playerView.setBackgroundColor(Color.BLACK);
        LinearLayout.LayoutParams videoLp =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(220));
        videoLp.setMargins(0, dp(4), 0, dp(6));
        liveCard.addView(playerView, videoLp);

        TextView tapHint = txt("البث يبدأ تلقائيًا عند فتح التطبيق", 12, false, MUTED);
        tapHint.setGravity(Gravity.CENTER);
        liveCard.addView(tapHint);

        page.addView(liveCard, matchWrapWithMargin(0, 0, 0, 12));

        GridLayout shortcuts = new GridLayout(this);
        shortcuts.setColumnCount(4);
        shortcuts.setAlignmentMode(GridLayout.ALIGN_MARGINS);
        String[][] quick = {
                {"الخطباء", "♟"},
                {"المجالس", "♨"},
                {"جدول البث", "▣"},
                {"المفضلة", "♡"}
        };
        for (String[] q : quick) shortcuts.addView(shortcut(q[0], q[1]), gridCell());
        page.addView(shortcuts, matchWrapWithMargin(0, 0, 0, 12));

        LinearLayout ticker = cardContainer(14, Color.rgb(80,0,0));
        ticker.setOrientation(LinearLayout.HORIZONTAL);
        ticker.setGravity(Gravity.CENTER_VERTICAL);
        TextView badge = txt("آخر الأخبار", 13, true, Color.WHITE);
        badge.setBackground(roundBg(RED2, 12, 0, Color.TRANSPARENT));
        badge.setPadding(dp(10), dp(7), dp(10), dp(7));
        ticker.addView(badge);

        TextView marquee = txt("  قناة الكافل الفضائية • تابعوا البث المباشر والمجالس والزيارات والإعلانات الجديدة  ",
                14, false, Color.WHITE);
        marquee.setSingleLine(true);
        marquee.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
        marquee.setMarqueeRepeatLimit(-1);
        marquee.setSelected(true);
        marquee.setGravity(Gravity.CENTER_VERTICAL);
        ticker.addView(marquee, new LinearLayout.LayoutParams(0, dp(48), 1f));
        page.addView(ticker, matchWrapWithMargin(0, 0, 0, 14));

        sectionTitle(page, "المحتوى المميز");
        GridLayout features = new GridLayout(this);
        features.setColumnCount(3);
        features.setAlignmentMode(GridLayout.ALIGN_MARGINS);
        features.addView(featureCard("لبيك يا حسين", "يا حسين", 0), gridFeature());
        features.addView(featureCard("الزيارات", "زيارة", 1), gridFeature());
        features.addView(studioCard(), gridFeature());
        features.addView(featureCard("\u0627\u0644\u0625\u0639\u0644\u0627\u0646\u0627\u062a \u0648\u0627\u0644\u0628\u0648\u0633\u062a\u0631\u0627\u062a", "\u0639\u0631\u0636 \u0627\u0644\u0645\u062d\u062a\u0648\u0649", 2), gridFeature());
        page.addView(features, matchWrapWithMargin(0, 0, 0, 14));

        sectionTitle(page, "الإعلانات والبوسترات");
        HorizontalScrollView adsScroll = new HorizontalScrollView(this);
        adsScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout ads = new LinearLayout(this);
        ads.setOrientation(LinearLayout.HORIZONTAL);
        ads.addView(poster("إعلان القناة", "البث على مدار الساعة"));
        ads.addView(poster("برامج ومجالس", "تابع جديد الكافل"));
        ads.addView(poster("تنويه", "تحديثات القناة"));
        adsScroll.addView(ads);
        page.addView(adsScroll, matchWrapWithMargin(0, 0, 0, 14));
        FirebaseHomeContent.bind(this, marquee, ads);

        sectionTitle(page, "مواقيت الصلاة");
        LinearLayout prayerCard = cardContainer(16, Color.rgb(95,65,15));
        prayerCard.setPadding(dp(14), dp(12), dp(14), dp(12));
        prayerCard.addView(txt("العراق - كربلاء المقدسة", 18, true, GOLD));
        prayerCard.addView(txt("تتحدث المواقيت تلقائيًا عند توفر الإنترنت", 12, false, MUTED));
        prayerRows = new LinearLayout(this);
        prayerRows.setOrientation(LinearLayout.VERTICAL);
        prayerCard.addView(prayerRows);
        setPrayerPlaceholder();
        page.addView(prayerCard, matchWrapWithMargin(0, 0, 0, 14));

        sectionTitle(page, "تابعنا على");
        LinearLayout social = new LinearLayout(this);
        social.setOrientation(LinearLayout.HORIZONTAL);
        social.setGravity(Gravity.CENTER);
        String[] socialNames = {"يوتيوب", "فيسبوك", "إنستغرام", "تيليجرام", "واتساب"};
        String[] socialIcons = {"▶", "f", "◎", "✈", "☎"};
        for (int i=0;i<socialNames.length;i++) {
            social.addView(socialButton(socialNames[i], socialIcons[i]),
                    new LinearLayout.LayoutParams(0, dp(78), 1f));
        }
        page.addView(social, matchWrapWithMargin(0, 0, 0, 12));

        LinearLayout nav = cardContainer(18, RED);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.addView(navItem("الرئيسية", "⌂", true), new LinearLayout.LayoutParams(0, dp(72), 1f));
        nav.addView(navItem("البث", "▶", false), new LinearLayout.LayoutParams(0, dp(72), 1f));
        nav.addView(navItem("المجالس", "♨", false), new LinearLayout.LayoutParams(0, dp(72), 1f));
        nav.addView(navItem("الخطباء", "♟", false), new LinearLayout.LayoutParams(0, dp(72), 1f));
        nav.addView(navItem("المزيد", "•••", false), new LinearLayout.LayoutParams(0, dp(72), 1f));
        page.addView(nav);

        setContentView(scroll);
        startPlayer();
        fetchPrayerTimes();
    }

    private void startPlayer() {
        if (player != null) return;
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setMediaItem(MediaItem.fromUri(Uri.parse(STREAM_URL)));
        player.setPlayWhenReady(true);
        player.prepare();
        player.play();
    }

    private void fetchPrayerTimes() {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.aladhan.com/v1/timingsByCity?city=Karbala&country=Iraq&method=4");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setConnectTimeout(7000);
                c.setReadTimeout(7000);
                BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder b = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) b.append(line);
                r.close();
                JSONObject t = new JSONObject(b.toString())
                        .getJSONObject("data").getJSONObject("timings");
                String[][] vals = {
                        {"الفجر", t.optString("Fajr", "--:--")},
                        {"الشروق", t.optString("Sunrise", "--:--")},
                        {"الظهر", t.optString("Dhuhr", "--:--")},
                        {"العصر", t.optString("Asr", "--:--")},
                        {"المغرب", t.optString("Maghrib", "--:--")},
                        {"العشاء", t.optString("Isha", "--:--")}
                };
                new Handler(Looper.getMainLooper()).post(() -> setPrayerRows(vals));
            } catch (Exception ignored) { }
        }).start();
    }

    private void setPrayerPlaceholder() {
        setPrayerRows(new String[][]{
                {"الفجر","--:--"},{"الشروق","--:--"},{"الظهر","--:--"},
                {"العصر","--:--"},{"المغرب","--:--"},{"العشاء","--:--"}
        });
    }

    private void setPrayerRows(String[][] vals) {
        prayerRows.removeAllViews();
        for (String[] p : vals) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(4), dp(8), dp(4), dp(8));
            TextView name = txt(p[0], 15, true, Color.WHITE);
            TextView time = txt(p[1], 16, true, GOLD);
            row.addView(name, new LinearLayout.LayoutParams(0, dp(34), 1f));
            row.addView(time);
            prayerRows.addView(row);
        }
    }

    private TextView shortcut(String title, String icon) {
        TextView t = txt(icon + "\n" + title, 14, true, Color.WHITE);
        t.setGravity(Gravity.CENTER);
        t.setBackground(roundBg(CARD, 16, 1, Color.rgb(65,65,65)));
        t.setOnClickListener(v -> { if ("المجالس".equals(title)) openUrl("https://www.youtube.com/@AlkafilTVBHUK"); else Toast.makeText(this, title + " — قريبًا", Toast.LENGTH_SHORT).show(); });
        return t;
    }
    private void openRemotePage(String title, String collections) {
        Intent i = new Intent(this, RemoteContentActivity.class);
        i.putExtra("title", title);
        i.putExtra("collections", collections);
        startActivity(i);
    }

    private View featureCard(String title, String sub, int type) {
        LinearLayout c = cardContainer(16, type == 0 ? RED : Color.rgb(100,70,20));
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(8), dp(14), dp(8), dp(14));
        TextView big = txt(type == 0 ? "يا حسين" : "✦", 24, true, type == 0 ? Color.WHITE : GOLD);
        big.setGravity(Gravity.CENTER);
        c.addView(big);
        TextView t = txt(title, 14, true, Color.WHITE);
        t.setGravity(Gravity.CENTER);
        c.addView(t);
        if (type == 0) c.setOnClickListener(v -> openRemotePage("\u0644\u0628\u064a\u0643 \u064a\u0627 \u062d\u0633\u064a\u0646", "labbaik"));
        if (type == 1) c.setOnClickListener(v -> startActivity(new Intent(this, ZiyaratActivity.class)));
        if (type == 2) c.setOnClickListener(v -> openRemotePage("\u0627\u0644\u0625\u0639\u0644\u0627\u0646\u0627\u062a \u0648\u0627\u0644\u0628\u0648\u0633\u062a\u0631\u0627\u062a", "announcements,posters"));
        return c;
    }

    private View studioCard() {
        LinearLayout c = cardContainer(16, RED);
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(6), dp(8), dp(6), dp(8));
        ImageView image = new ImageView(this);
        image.setImageResource(getResources().getIdentifier("studio_logo", "drawable", getPackageName()));
        image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        c.addView(image, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(88)));
        TextView t = txt("استديو الكافل", 13, true, Color.WHITE);
        t.setGravity(Gravity.CENTER);
        c.addView(t);
        c.setOnClickListener(v -> openRemotePage("\u0627\u0633\u062a\u062f\u064a\u0648 \u0627\u0644\u0643\u0627\u0641\u0644", "studio"));
        return c;
    }

    private View poster(String title, String sub) {
        LinearLayout p = cardContainer(18, RED);
        p.setGravity(Gravity.CENTER);
        p.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(220), dp(120));
        lp.setMargins(0, 0, dp(10), 0);
        p.setLayoutParams(lp);
        TextView t = txt(title, 18, true, Color.WHITE);
        t.setGravity(Gravity.CENTER);
        TextView s = txt(sub, 13, false, MUTED);
        s.setGravity(Gravity.CENTER);
        p.addView(t);
        p.addView(s);
        return p;
    }

    private View socialButton(String name, String icon) {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER);
        b.setBackground(roundBg(CARD, 16, 1, Color.rgb(55,55,55)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(78), 1f);
        lp.setMargins(dp(3), dp(3), dp(3), dp(3));
        b.setLayoutParams(lp);
        TextView i = txt(icon, 20, true, Color.WHITE);
        TextView n = txt(name, 10, false, MUTED);
        i.setGravity(Gravity.CENTER);
        n.setGravity(Gravity.CENTER);
        b.addView(i);
        b.addView(n);
        b.setOnClickListener(v -> { if ("يوتيوب".equals(name)) openUrl("https://www.youtube.com/@AlkafilTVBHUK"); else if ("فيسبوك".equals(name)) openUrl("https://www.facebook.com/profile.php?id=61592112443888"); else if ("إنستغرام".equals(name)) openUrl("https://www.instagram.com/alkafil.tv/"); else if ("تيليجرام".equals(name)) openUrl("https://t.me/ALKAFELTV"); else if ("واتساب".equals(name)) openUrl("https://wa.me/9647762409447"); });
        return b;
    }

    private void openUrl(String url) { try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) { Toast.makeText(this, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show(); } }

    private View navItem(String name, String icon, boolean active) {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setGravity(Gravity.CENTER);
        TextView i = txt(icon, 18, true, active ? RED2 : MUTED);
        TextView n = txt(name, 11, true, active ? Color.WHITE : MUTED);
        i.setGravity(Gravity.CENTER);
        n.setGravity(Gravity.CENTER);
        b.addView(i);
        b.addView(n);
        return b;
    }

    private void sectionTitle(LinearLayout page, String text) {
        TextView s = txt(text, 20, true, Color.WHITE);
        s.setGravity(Gravity.RIGHT);
        s.setPadding(dp(4), dp(8), dp(4), dp(8));
        page.addView(s);
    }

    private LinearLayout cardContainer(int radius, int strokeColor) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackground(roundBg(CARD, radius, 1, strokeColor));
        return l;
    }

    private GradientDrawable roundBg(int color, int radiusDp, int strokeDp, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) d.setStroke(dp(strokeDp), strokeColor);
        return d;
    }

    private TextView txt(String s, int size, boolean bold, int color) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        v.setPadding(dp(8), dp(5), dp(8), dp(5));
        v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    private TextView iconText(String s, int size) {
        TextView v = txt(s, size, true, Color.WHITE);
        v.setGravity(Gravity.CENTER);
        return v;
    }

    private LinearLayout.LayoutParams fixed(int w, int h) {
        return new LinearLayout.LayoutParams(w, h);
    }

    private GridLayout.LayoutParams gridCell() {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = dp(86);
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        return lp;
    }

    private GridLayout.LayoutParams gridFeature() {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = dp(126);
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        return lp;
    }

    private LinearLayout.LayoutParams matchWrapWithMargin(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(l), dp(t), dp(r), dp(b));
        return lp;
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (player == null && playerView != null) startPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
