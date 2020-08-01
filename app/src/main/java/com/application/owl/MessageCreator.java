package com.application.owl;

import android.content.Context;
import com.application.owl.models.Contact;
import java.util.Calendar;
import static com.application.owl.Converters.convertLongToDate;

// class with static methods to handle creating recurring notification tasks and one-off notifications
public class MessageCreator {

    // create (or cancel, if they're inactive) a recurring Owl notification task for a contact
    public static void setMessageTask(Context context, Contact contact) {
        NotificationScheduler notificationScheduler = new NotificationScheduler();
        if (contact.getStatus() != 1) {
            notificationScheduler.cancelAlarm(context, contact);
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(convertLongToDate(contact.startDate));
        int mRepeatNo = contact.frequency;
        long milDay = 86400000L;     //60000L is a minute //86400000L is a day
        long mRepeatTime = mRepeatNo * milDay;

        long nextTimeStamp = getNextAlarmTime(contact.startDate, mRepeatTime);
        notificationScheduler.cancelAlarm(context, contact);
        notificationScheduler.setRepeatAlarm(context, nextTimeStamp, mRepeatTime, contact);
    }

    // create a single one-off Owl notification
    public static int createMessage(Context context, Contact contact) {
        if (contact.getStatus() != 1) {
            return 0;
        }

        Calendar cal = Calendar.getInstance();
        long selectedTimestamp = cal.getTimeInMillis();
        new NotificationScheduler().setAlarm(context, selectedTimestamp, contact);
        return 1;
    }

    // create a single one-off snoozed Owl notification
    static void createSnoozeMessage(Context context, Contact contact) {
        if (contact.getStatus() != 1) {
            return;
        }
        new NotificationScheduler().setAlarm(context, contact.postSnoozeDate, contact);
    }

    // gets the next scheduled notification time (excluding snoozed notifications) to display in setMessageTask & in ContactEdit
    static long getNextAlarmTime(long startTime, long repeatTime) {
        Calendar cal = Calendar.getInstance();
        long now =  cal.getTimeInMillis();
        long nextTime = startTime;

        while (nextTime < now) {
            nextTime = nextTime + repeatTime;
        }

        return nextTime;
    }
}
