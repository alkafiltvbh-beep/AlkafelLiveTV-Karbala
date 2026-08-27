package tv.alkafel.live;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ZiyaratActivity extends Activity {
    private MediaPlayer player;
    private TextView status;
    private static final int BG = Color.rgb(5, 5, 5);
    private static final int CARD = Color.rgb(18, 18, 18);
    private static final int RED = Color.rgb(185, 15, 15);
    private static final int GOLD = Color.rgb(210, 170, 75);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        page.setPadding(dp(16), dp(18), dp(16), dp(28));
        scroll.addView(page);

        TextView back = text("‹  الرجوع", 17, true, GOLD);
        back.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        back.setOnClickListener(v -> finish());
        page.addView(back, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));

        TextView title = text("الزيارات والصوتيات", 27, true, Color.WHITE);
        title.setGravity(Gravity.CENTER);
        page.addView(title, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)));

        TextView note = text("اختر الزيارة أو الدعاء للاستماع", 14, false, Color.LTGRAY);
        note.setGravity(Gravity.CENTER);
        page.addView(note, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        status = text("لا يوجد صوت قيد التشغيل", 14, true, GOLD);
        status.setGravity(Gravity.CENTER);
        status.setBackground(background(CARD, 14, GOLD));
        page.addView(status, marginParams(dp(58), 0, dp(14)));

        String[] names = {"دعاء الفرج", "دعاء الصباح", "دعاء التوسل", "زيارة أمين الله", "زيارة عاشوراء", "زيارة وارث"};
        int[] sounds = {R.raw.dua_faraj, R.raw.dua_sabah, R.raw.dua_tawassul, R.raw.ziyarat_amin_allah, R.raw.ziyarat_ashura, R.raw.ziyarat_warith};
        for (int i = 0; i < names.length; i++) {
            final int sound = sounds[i];
            final String name = names[i];
            TextView button = text("▶   " + name, 18, true, Color.WHITE);
            button.setGravity(Gravity.CENTER);
            button.setBackground(background(CARD, 16, RED));
            button.setOnClickListener(v -> play(sound, name));
            page.addView(button, marginParams(dp(66), dp(5), dp(5)));
        }

        TextView stop = text("■  إيقاف الصوت", 17, true, Color.WHITE);
        stop.setGravity(Gravity.CENTER);
        stop.setBackground(background(Color.rgb(90, 10, 10), 16, RED));
        stop.setOnClickListener(v -> stopAudio());
        page.addView(stop, marginParams(dp(62), dp(12), 0));
        setContentView(scroll);
    }

    private void play(int sound, String name) {
        stopAudio();
        player = MediaPlayer.create(this, sound);
        if (player == null) {
            status.setText("تعذر تشغيل الصوت");
            return;
        }
        status.setText("يعمل الآن: " + name);
        player.setOnCompletionListener(m -> {
            m.release();
            if (player == m) player = null;
            status.setText("انتهى التشغيل");
        });
        player.start();
    }

    private void stopAudio() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        if (status != null) status.setText("تم إيقاف الصوت");
    }

    private TextView text(String value, int size, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(dp(12), dp(8), dp(12), dp(8));
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private GradientDrawable background(int color, int radius, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private LinearLayout.LayoutParams size(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private LinearLayout.LayoutParams marginParams(int height, int top, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        params.setMargins(0, top, 0, bottom);
        return params;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAudio();
    }
}
