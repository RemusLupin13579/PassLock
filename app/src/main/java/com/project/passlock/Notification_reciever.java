package com.project.passlock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class Notification_reciever extends BroadcastReceiver {
    String[] securityTips = {
            "Use strong and unique passwords for all your accounts.",
            "Enable two-factor authentication whenever possible.",
            "Keep your operating system and software up to date.",
            "Regularly backup your important data.",
            "Avoid using public Wi-Fi for sensitive transactions.",
            "Use reputable antivirus and anti-malware software.",
            "Don't reuse passwords across multiple accounts.",
            "Be wary of phishing attempts and unknown sources.",
            "Include numbers and special characters in passwords.",
            "Make passwords at least 8 characters long.",
            "Change passwords regularly for better security.",
            "Use different passwords for different accounts.",
            "Avoid using personal information in passwords.",
            "Consider using a password manager for strong passwords.",
            "Use a combination of uppercase and lowercase letters.",
            "Avoid common and easily guessable passwords.",
            "Use a unique password for each account.",
            "Be cautious with social media privacy settings.",
            "Securely store and generate passwords with a manager.",
            "Don't share passwords with others.",
            "Avoid using sequential numbers.",
            "Mix letters, numbers, and symbols.",
            "Don't rely on common substitutions.",
            "Use a passphrase instead of a single word.",
            "Avoid using personal information as passwords.",
            "Memorize passwords instead of writing them down.",
            "Regularly check for password breaches.",
            "Be cautious of shoulder surfing.",
            "Protect your passwords from keyloggers.",
            "Consider using biometric authentication.",
            "Don't store passwords in browser autofill.",
            "Change default passwords immediately.",
            "Use a random password generator.",
            "Never save passwords in plain text files.",
            "Don't use easily guessable patterns.",
            "Use password-protected Wi-Fi networks.",
            "Avoid reusing old passwords.",
            "Don't use common keyboard patterns.",
            "Use multi-factor authentication when available."
    };
    int randomIndex = (int) (Math.random() * securityTips.length);

    @Override
    public void onReceive(Context context, Intent intent) {
        String tip = securityTips[randomIndex];
        // Retrieve the application context from intent extras
        Context appContext = intent.getParcelableExtra("context");
        // Access resources using the application context
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.dailytip_icon);
        // Convert the drawable to a Bitmap
        Bitmap bitmap = drawableToBitmap(drawable);

        Intent notification_intent = new Intent(context, MainActivity.class);
        notification_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//פותח את האקטיביטי על חשבון מה שאולי פתוח

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notification_intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Notification")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.roundlogo)
                .setLargeIcon(bitmap)
                .setContentTitle("Daily Security Tip")
                .setContentText(tip)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(tip))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        //NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        //NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        notificationManagerCompat.notify(200, builder.build());
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}