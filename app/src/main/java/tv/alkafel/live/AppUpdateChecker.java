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

    /** Called from the app menu: always tells the user the result. */
    public static void checkNow(Activity activity) {
        android.widget.Toast.makeText(activity, "\u062c\u0627\u0631\u064a \u0627\u0644\u0628\u062d\u062b \u0639\u0646 \u062a\u062d\u062f\u064a\u062b\u2026", android.widget.Toast.LENGTH_SHORT).show();
        FirebaseFirestore.getInstance()
            .collection("app_settings")
            .document("update")
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(document -> {
                if (!showIfNeeded(activity, document) && !activity.isFinishing()) {
                    android.widget.Toast.makeText(activity, "\u0644\u062f\u064a\u0643 \u0623\u062d\u062f\u062b \u0625\u0635\u062f\u0627\u0631 \u0645\u0646 \u0627\u0644\u062a\u0637\u0628\u064a\u0642 \u2705", android.widget.Toast.LENGTH_LONG).show();
                }
            })
            .addOnFailureListener(e -> android.widget.Toast.makeText(activity, "\u062a\u0639\u0630\u0651\u0631 \u0627\u0644\u0627\u062a\u0635\u0627\u0644\u060c \u062a\u0623\u0643\u062f \u0645\u0646 \u0627\u0644\u0625\u0646\u062a\u0631\u0646\u062a", android.widget.Toast.LENGTH_LONG).show());
    }

    private static boolean showIfNeeded(Activity activity, DocumentSnapshot document) {
        if (!document.exists() || activity.isFinishing()) return false;
        Boolean enabled = document.getBoolean("enabled"); if (enabled != null && !enabled) return false;
        Number newest = (Number) document.get("versionCode"); if (newest == null) newest = (Number) document.get("order");
        long current; try { current = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionCode; } catch (Exception e) { return false; }
        if (newest == null || newest.longValue() <= current) return false;

        String message = document.getString("message"); if (message == null) message = document.getString("description");
        String apkUrl = document.getString("apkUrl"); if (apkUrl == null) apkUrl = document.getString("url");
        Boolean force = document.getBoolean("force");
        if (apkUrl == null || apkUrl.trim().isEmpty()) return false;
        final String downloadUrl = apkUrl;
        if (message == null || message.trim().isEmpty()) {
            message = "\u064a\u062a\u0648\u0641\u0631 \u062a\u062d\u062f\u064a\u062b \u062c\u062f\u064a\u062f \u0644\u062a\u0637\u0628\u064a\u0642 \u0642\u0646\u0627\u0629 \u0627\u0644\u0643\u0627\u0641\u0644";
        }

        AlertDialog.Builder box = new AlertDialog.Builder(activity)
            .setTitle("\u062a\u062d\u062f\u064a\u062b \u0627\u0644\u062a\u0637\u0628\u064a\u0642")
            .setMessage(message)
            .setPositiveButton("\u062a\u062d\u062f\u064a\u062b \u0627\u0644\u0622\u0646", (dialog, which) -> {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)));
            });
        if (!Boolean.TRUE.equals(force)) {
            box.setNegativeButton("\u0644\u0627\u062d\u0642\u0627\u064b", null);
        }
        AlertDialog dialog = box.create();
        dialog.setCancelable(!Boolean.TRUE.equals(force));
        dialog.setCanceledOnTouchOutside(!Boolean.TRUE.equals(force));
        dialog.show();
        android.widget.Button yes = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (yes != null) { yes.setTextColor(0xFF9C0F1F); yes.setTypeface(null, android.graphics.Typeface.BOLD); }
        android.widget.Button later = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (later != null) later.setTextColor(0xFF555555);
        return true;
    }
}