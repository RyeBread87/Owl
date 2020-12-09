package com.application.owl;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.application.owl.models.Contact;
import com.application.owl.repositories.ContactRepository;
import java.util.Calendar;

// JobIntentService to handle creating Owl notifications & handling snoozed Owl notifications
public class NotificationService extends JobIntentService {

    private ContactRepository contactRepository;
    public static final String CHANNEL_ID = "OwlNotificationChannel";
    public static final String CONTACT = "contact";
    public static final String ACTION_SNOOZE = "com.application.owl.action.SNOOZE";
    static final int JOB_ID = 1000;
    final private Handler mHandler = new Handler();
    private Context context;
    Contact contact;

    // called from NotificationBroadcastReceiver to queue up work which is picked up in onHandleWork
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, NotificationService.class, JOB_ID, work);
    }

    public NotificationService() {
        super();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        context = getApplicationContext();
        String bitmapString = intent.getStringExtra(CONTACT);
        contactRepository = new ContactRepository((Application) context);
        Contact contact = Converters.deserializeFromJson(bitmapString);

        if (contact == null) { return; }     // there's nothing to do here if we don't have a contact

        final String action = intent.getAction();
        if (ACTION_SNOOZE.equals(action)) {                 // if the user snoozes a notification we go here
            handleActionSnooze(context, contact);
            return;
        }

        createNotification(context, contact);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toast();
    }

    // helper for showing toasts
    void toast() {
        mHandler.post(() -> {
            if (contact != null) {
                Toast.makeText(context, getResources().getString(R.string.owl_time_toast), Toast.LENGTH_LONG).show();
            }
        });
    }

    // creates an Owl notification for a contact with a pending intent to go to Call or MessageSelect based on their preference, as well as a snooze action
    private void createNotification(Context context, Contact contact) {
        if (!isNotificationChannelEnabled(context, CHANNEL_ID)) {
            createNotificationChannel(context);     // if the notification channel wasn't enabled, we need to create it
        }

        NotificationCompat.Action snoozeAction = snoozeActionBuilder(context, contact);     // create the snooze action to add to the notification
        NotificationCompat.Builder builder = owlNotificationBuilder(context, contact, snoozeAction);
        Notification notification = builder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(contact.getId(), notification);

        // log firebase event for creation of owl notification
        FirebaseEventLogger.logFirebaseEvent(context, "launch_owl_notification", "owl notification generated", "", "", "", "owl_count", ((Integer) contact.getOwlCount()).toString());
    }

    // this builds an Owl notification for a contact based on their preference
    public NotificationCompat.Builder owlNotificationBuilder(Context context, Contact contact, NotificationCompat.Action action) {

        if (contact.preference.equals(getResources().getString(R.string.call))) {           // the call preference gets its own intent, which takes us to the Call class (since we don't need messages for this preference)
            Intent callIntent = new Intent(context, Call.class);
            String bitmapString = Converters.serializeToJson(contact);
            callIntent.putExtra(CONTACT, bitmapString);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent callPendingIntent = PendingIntent.getActivity(
                    context,
                    contact.getId(),
                    callIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            );

            return new NotificationCompat.Builder(this.context, CHANNEL_ID)
                    .setContentTitle(getString(R.string.owl_time_for) + " " + contact.getName() + "!")
                    .setContentText(getString(R.string.connect_by) + " " + contact.getPreference())
                    .setSmallIcon(R.drawable.ic_stat_owl)
                    .setColor(getColor(R.color.primary))
                    .setContentIntent(callPendingIntent)
                    .addAction(action)
                    .setAutoCancel(true);
        }

        else {                  // non-call preferences all get an intent that takes them to MessageSelect with an extra storing the contact - this is what routes us to a messaging app rather than MessageEdit
            Intent messageSelectIntent = new Intent(context, MessageSelect.class);
            String bitmapString = Converters.serializeToJson(contact);
            System.out.println("bitmapString: " + bitmapString);
            messageSelectIntent.putExtra(CONTACT, bitmapString);
            //messageSelectIntent.setData(new Uri.Builder().scheme("data").appendQueryParameter(CONTACT, bitmapString).appendQueryParameter("time", String.valueOf(System.currentTimeMillis())).build());
            messageSelectIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent messageSelectPendingIntent = PendingIntent.getActivity(
                    context,
                    contact.getId(),
                    messageSelectIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT           //FLAG_UPDATE_CURRENT
            );

            return new NotificationCompat.Builder(this.context, CHANNEL_ID)
                    .setContentTitle(getString(R.string.owl_time_for) + " " + contact.getName() + "!")
                    .setContentText(getString(R.string.connect_by) + " " + contact.getPreference())
                    .setSmallIcon(R.drawable.ic_stat_owl)
                    .setColor(getColor(R.color.primary))
                    .setContentIntent(messageSelectPendingIntent)
                    .addAction(action)
                    .setAutoCancel(true);
        }
    }

    // handles the user snoozing an Owl notification
    private void handleActionSnooze(Context context, Contact contact) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(contact.getId());
        Calendar cal = Calendar.getInstance();
        long currentTimestamp = cal.getTimeInMillis();
        long milDay = 86400000L;     //60000L is a minute //86400000L is a day
        long postSnoozeTimestamp = currentTimestamp + milDay;       // snoozing defers the message by one day
        contact.setPostSnoozeDate(postSnoozeTimestamp);         // we set and update the postSnoozeTimestamp in case we need to
        contactRepository.update(contact);                      // reschedule it after a restart
        MessageCreator.createSnoozeMessage(context, contact);       // create one-off message
    }

    // builds a snooze action for a contact
    public NotificationCompat.Action snoozeActionBuilder(Context context, Contact contact) {
        Intent snoozeIntent = new Intent(context, NotificationBroadcastReceiver.class);
        String bitmapString = Converters.serializeToJson(contact);
        snoozeIntent.putExtra(CONTACT, bitmapString);
        snoozeIntent.setAction(NotificationService.ACTION_SNOOZE);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, -contact.getId(), snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);       // notification ID is negative for one-off notifications
                                                                                                                                                          // to keep them separate from recurring notifications
        return new NotificationCompat.Action.Builder(
                R.drawable.ic_alarm_white_48dp,
                getString(R.string.snooze),
                snoozePendingIntent)
                .build();
    }

    // checks if the notification channel with the passed in channelId is enabled - we'll only create the channel if this returns false
    public boolean isNotificationChannelEnabled(Context context, @Nullable String channelId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!TextUtils.isEmpty(channelId)) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                assert manager != null;
                NotificationChannel channel = manager.getNotificationChannel(channelId);
                if (channel != null) {
                    return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
                }
            }
            return false;
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
    }

    // creates a notification channel with ID CHANNEL_ID
    private void createNotificationChannel(Context context) {
        // create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getResources().getString(R.string.notification_channel_name);
            String description = context.getResources().getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }
}
