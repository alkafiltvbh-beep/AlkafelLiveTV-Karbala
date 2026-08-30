package tv.alkafel.live;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public final class AppUpdateChecker {
    private AppUpdateChecker() {}

    public static void check(Activity activity) {
        FirebaseFirestore.getInstance()
            .collection("app_settings")
            .document("update")
            .get()
            .addOnSuccessListener(document -> showIfNeeded(activity, document));
    }

    private static void showIfNeeded(Activity activity, DocumentSnapshot document) {
        if (!document.exists() || activity.isFinishing()) return;
        Boolean enabled = document.getBoolean("enabled"); if (enabled != null && !enabled) return;
        Number newest = (Number) document.get("versionCode"); if (newest == null) newest = (Number) document.get("order");
        long current; try { current = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionCode; } catch (Exception e) { return; }
        if (newest == null || newest.longValue() <= current) return;

        String message = document.getString("message"); if (message == null) message = document.getString("description");
        String apkUrl = document.getString("apkUrl"); if (apkUrl == null) apkUrl = document.getString("url");
        Boolean force = document.getBoolean("force");
        if (apkUrl == null || apkUrl.trim().isEmpty()) return;
        if (message == null || message.trim().isEmpty()) {
            message = "\u064a\u062a\u0648\u0641\u0631 \u062a\u062d\u062f\u064a\u062b \u062c\u062f\u064a\u062f \u0644\u062a\u0637\u0628\u064a\u0642 \u0642\u0646\u0627\u0629 \u0627\u0644\u0643\u0627\u0641\u0644";
        }

        AlertDialog.Builder box = new AlertDialog.Builder(activity)
            .setTitle("\u062a\u062d\u062f\u064a\u062b \u0627\u0644\u062a\u0637\u0628\u064a\u0642")
            .setMessage(message)
            .setPositiveButton("\u062a\u062d\u062f\u064a\u062b \u0627\u0644\u0622\u0646", (dialog, which) -> {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)));
            });
        if (!Boolean.TRUE.equals(force)) {
            box.setNegativeButton("\u0644\u0627\u062d\u0642\u0627\u064b", null);
        }
        AlertDialog dialog = box.create();
        dialog.setCancelable(!Boolean.TRUE.equals(force));
        dialog.setCanceledOnTouchOutside(!Boolean.TRUE.equals(force));
        dialog.show();
    }
}