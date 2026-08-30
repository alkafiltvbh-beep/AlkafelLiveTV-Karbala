package tv.alkafel.live;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RemoteContentActivity extends AppCompatActivity {
    private static final int BG = Color.rgb(7, 12, 20);
    private static final int CARD = Color.rgb(16, 25, 39);
    private static final int GOLD = Color.rgb(244, 194, 53);
    private LinearLayout list;
    private ProgressBar progress;
    private int pending;
    private final List<DocumentSnapshot> documents = new ArrayList<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(24));
        root.setBackgroundColor(BG);

        TextView back = text("\u2190  \u0631\u062c\u0648\u0639", 16, true, GOLD);
        back.setPadding(0, dp(4), 0, dp(15));
        back.setOnClickListener(v -> finish());
        root.addView(back);

        String title = getIntent().getStringExtra("title");
        TextView heading = text(title == null ? "" : title, 25, true, Color.WHITE);
        heading.setGravity(Gravity.RIGHT);
        root.addView(heading);

        progress = new ProgressBar(this);
        root.addView(progress, new LinearLayout.LayoutParams(dp(44), dp(44)));

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, dp(14), 0, 0);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);

        String collections = getIntent().getStringExtra("collections");
        if (collections == null || collections.trim().isEmpty()) showEmpty();
        else load(collections.split(","));
    }

    private void load(String[] collections) {
        pending = collections.length;
        for (String collection : collections) {
            FirebaseFirestore.getInstance().collection(collection.trim()).get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot d : snapshot.getDocuments()) {
                        Boolean visible = d.getBoolean("visible");
                        if (visible == null || visible) documents.add(d);
                    }
                    completeOne();
                })
                .addOnFailureListener(error -> completeOne());
        }
    }

    private void completeOne() {
        pending--;
        if (pending > 0) return;
        progress.setVisibility(View.GONE);
        Collections.sort(documents, (a, b) -> Long.compare(number(b, "order"), number(a, "order")));
        if (documents.isEmpty()) showEmpty();
        else for (DocumentSnapshot d : documents) addCard(d);
    }

    private long number(DocumentSnapshot d, String key) {
        Number n = (Number) d.get(key);
        return n == null ? 0 : n.longValue();
    }

    private void showEmpty() {
        progress.setVisibility(View.GONE);
        TextView empty = text("\u0644\u0627 \u064a\u0648\u062c\u062f \u0645\u062d\u062a\u0648\u0649 \u062d\u0627\u0644\u064a\u0627\u064b", 16, false, Color.LTGRAY);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, dp(70), 0, 0);
        list.addView(empty);
    }

    private void addCard(DocumentSnapshot d) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(CARD); bg.setCornerRadius(dp(16)); bg.setStroke(1, Color.rgb(43, 58, 78));
        card.setBackground(bg);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, 0, 0, dp(14)); card.setLayoutParams(cp);

        String imageUrl = value(d, "imageUrl", "image", "image_url");
        if (!imageUrl.isEmpty()) {
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            card.addView(image, new LinearLayout.LayoutParams(-1, dp(205)));
            Glide.with(this).load(imageUrl).into(image);
        }
        String title = value(d, "title", "name");
        String description = value(d, "description", "details", "sub");
        if (!title.isEmpty()) { TextView t = text(title, 20, true, GOLD); t.setGravity(Gravity.RIGHT); card.addView(t); }
        if (!description.isEmpty()) { TextView x = text(description, 15, false, Color.WHITE); x.setGravity(Gravity.RIGHT); x.setPadding(0, dp(8), 0, 0); card.addView(x); }
        String date = value(d, "date"); String time = value(d, "time");
        if (!date.isEmpty() || !time.isEmpty()) { TextView when = text((date + "  " + time).trim(), 13, false, Color.LTGRAY); when.setGravity(Gravity.RIGHT); when.setPadding(0, dp(8), 0, 0); card.addView(when); }
        String url = value(d, "url", "link");
        if (!url.isEmpty()) card.setOnClickListener(v -> openUrl(url));
        list.addView(card);
    }

    private String value(DocumentSnapshot d, String... keys) {
        for (String key : keys) { Object v = d.get(key); if (v != null && !v.toString().trim().isEmpty()) return v.toString().trim(); }
        return "";
    }

    private void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { Toast.makeText(this, "\u0627\u0644\u0631\u0627\u0628\u0637 \u063a\u064a\u0631 \u0635\u0627\u0644\u062d", Toast.LENGTH_SHORT).show(); }
    }

    private TextView text(String value, int size, boolean bold, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}