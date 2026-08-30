package tv.alkafel.live;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class FirebaseHomeContent {
    private static final List<DocumentSnapshot> posters = new ArrayList<>();
    private static final List<DocumentSnapshot> announcements = new ArrayList<>();

    private FirebaseHomeContent() {}

    public static void bind(Activity activity, TextView marquee, LinearLayout ads) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("news_ticker").whereEqualTo("enabled", true)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;
                    List<DocumentSnapshot> docs = new ArrayList<>(snapshot.getDocuments());
                    sort(docs);
                    StringBuilder news = new StringBuilder();
                    for (DocumentSnapshot d : docs) {
                        String title = text(d, "title");
                        if (title.isEmpty()) continue;
                        if (news.length() > 0) news.append("     \u2022     ");
                        news.append(title);
                    }
                    if (news.length() > 0) {
                        marquee.setText(news.toString());
                        marquee.setSelected(true);
                    }
                });

        db.collection("posters").whereEqualTo("enabled", true)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;
                    posters.clear();
                    posters.addAll(snapshot.getDocuments());
                    render(activity, ads);
                });

        db.collection("announcements").whereEqualTo("enabled", true)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;
                    announcements.clear();
                    announcements.addAll(snapshot.getDocuments());
                    render(activity, ads);
                });
    }

    private static void render(Activity activity, LinearLayout ads) {
        List<DocumentSnapshot> all = new ArrayList<>();
        all.addAll(posters);
        all.addAll(announcements);
        sort(all);
        if (all.isEmpty()) return;
        ads.removeAllViews();
        for (DocumentSnapshot d : all) ads.addView(card(activity, d));
    }

    private static View card(Activity activity, DocumentSnapshot d) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(activity, 10), dp(activity, 10), dp(activity, 10), dp(activity, 10));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dp(activity, 270), ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(dp(activity, 6), 0, dp(activity, 6), 0);
        card.setLayoutParams(cardParams);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(24, 30, 40));
        bg.setCornerRadius(dp(activity, 14));
        bg.setStroke(dp(activity, 1), Color.rgb(92, 76, 31));
        card.setBackground(bg);

        String imageUrl = text(d, "imageUrl");
        if (!imageUrl.isEmpty()) {
            ImageView image = new ImageView(activity);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            card.addView(image, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 125)));
            Glide.with(activity).load(imageUrl).into(image);
        }

        TextView title = new TextView(activity);
        title.setText(text(d, "title"));
        title.setTextColor(Color.rgb(240, 204, 98));
        title.setTextSize(17);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(activity, 9), 0, dp(activity, 3));
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        card.addView(title);

        String description = text(d, "description");
        if (!description.isEmpty()) {
            TextView body = new TextView(activity);
            body.setText(description);
            body.setTextColor(Color.rgb(220, 224, 232));
            body.setTextSize(13);
            body.setGravity(Gravity.CENTER);
            card.addView(body);
        }

        String url = text(d, "url");
        if (!url.isEmpty()) {
            card.setClickable(true);
            card.setOnClickListener(v -> {
                try { activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
                catch (Exception ignored) {}
            });
        }
        return card;
    }

    private static void sort(List<DocumentSnapshot> docs) {
        Collections.sort(docs, Comparator.comparingLong(d -> {
            Long value = d.getLong("order");
            return value == null ? 0L : value;
        }));
    }

    private static String text(DocumentSnapshot d, String key) {
        String value = d.getString(key);
        return value == null ? "" : value.trim();
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}