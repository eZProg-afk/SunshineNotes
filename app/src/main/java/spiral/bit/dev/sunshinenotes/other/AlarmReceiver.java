package spiral.bit.dev.sunshinenotes.other;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import spiral.bit.dev.sunshinenotes.R;
import spiral.bit.dev.sunshinenotes.fragments.NotesFragment;

public class AlarmReceiver extends BroadcastReceiver {

    private static final int NOTIFY_ID = 101;
    private static final String CHANNEL_ID = "SUNSHINE CHANNEL";

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("nameOfNote")) {
            if (!intent.getStringExtra("nameOfNote").isEmpty() && intent.getStringExtra("nameOfNote") != null) {
                String nameOfNote = intent.getStringExtra("nameOfNote");
                showNotification(nameOfNote, context);
            } else showNotification("Задача", context);
        } else showNotification("Задача", context);
    }

    public void showNotification(String nameOfNote, Context context) {
        createNotificationChannel(context);
        long[] vibrate = new long[] { 1000, 1000, 1000, 1000, 1000 };
        Intent notificationIntent = new Intent(context, NotesFragment.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(context.getString(R.string.time_to_do_complete_label))
                        .setContentText(nameOfNote)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(contentIntent)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.ava))
                        .setTicker(context.getString(R.string.ticker_label))
                        .setAutoCancel(true);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getBoolean("enable_sound", false)) {
            builder.setDefaults(Notification.DEFAULT_SOUND |
                    Notification.DEFAULT_VIBRATE)
                    .setVibrate(vibrate);
        }
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFY_ID, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SUNSHINE";
            String description = "NOTES";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
