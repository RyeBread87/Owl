package com.application.owl;

import android.content.Context;
import androidx.annotation.NonNull;
import org.acra.config.CoreConfiguration;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;

public class OwlSenderFactory implements ReportSenderFactory {

    public OwlSenderFactory() {}

    @NonNull
    @Override
    public ReportSender create(@NonNull Context context, @NonNull CoreConfiguration config) {
        return new OwlCrashSender(context, config);
    }

    @Override
    public boolean enabled(@NonNull CoreConfiguration coreConfig) {
        return true;
    }
}
