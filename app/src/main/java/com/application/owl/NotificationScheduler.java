package com.application.owl;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import com.application.owl.models.Contact;

// class to handle scheduling & cancelling of repeat & one-time alarms
class NotificationScheduler {

    void setAlarm(Context context, long alarmTime, Contact contact) {
        AlarmManager manager = getAlarmManager(context);
        PendingIntent broadcastOperation = NotificationBroadcastReceiver.getReminderPendingIntent(context, contact, true);
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, broadcastOperation);
    }

    void setRepeatAlarm(Context context, long alarmTime, long repeatTime, Contact contact) {
        AlarmManager manager = getAlarmManager(context);
        PendingIntent broadcastOperation = NotificationBroadcastReceiver.getReminderPendingIntent(context, contact, false);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, repeatTime, broadcastOperation);
    }

    private AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    void cancelAlarm(Context context, Contact contact) {
        AlarmManager manager = getAlarmManager(context);
        PendingIntent broadcastOperation = NotificationBroadcastReceiver.getReminderPendingIntent(context, contact, false);
        manager.cancel(broadcastOperation);
    }
}
