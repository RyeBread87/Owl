package com.application.owl;

import android.content.Context;
import android.os.Bundle;
import com.google.firebase.analytics.FirebaseAnalytics;

// logs Firebase events
class FirebaseEventLogger {

    static void logFirebaseEvent(Context context, String eventName, String type, String preference, String content, String startDate, String extra, String extraValue) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, type);
        if (!preference.equals("")) {
            bundle.putString(FirebaseAnalytics.Param.METHOD, preference);
        }
        if (!content.equals("")) {
            bundle.putString(FirebaseAnalytics.Param.CONTENT, content);
        }
        if (!startDate.equals("")) {
            bundle.putString(FirebaseAnalytics.Param.START_DATE, startDate);
        }
        if ((!extra.equals("")) && (!extraValue.equals(""))){
            bundle.putString(extra, extraValue);
        }
        FirebaseAnalytics.getInstance(context).logEvent(eventName, bundle);
    }
}
