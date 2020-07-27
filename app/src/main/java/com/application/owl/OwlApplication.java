package com.application.owl;

import android.app.Application;
import android.content.Context;
import org.acra.*;
import org.acra.annotation.*;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.ToastConfigurationBuilder;
import org.acra.data.StringFormat;

@AcraCore(buildConfigClass = BuildConfig.class,
        reportSenderFactoryClasses = OwlSenderFactory.class,
        reportFormat=StringFormat.JSON)
@AcraMailSender(mailTo = "OwlKeepInTouchAssistant@gmail.com")
public class OwlApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this)
            .setBuildConfigClass(BuildConfig.class)
            .setReportFormat(StringFormat.JSON);

        builder.getPluginConfigurationBuilder(ToastConfigurationBuilder.class)
                .setResText(R.string.acra_toast_text)
                .setEnabled(true);

        builder.setReportField(ReportField.DEVICE_ID, true);
        builder.setReportField(ReportField.ANDROID_VERSION, true);
        builder.setReportField(ReportField.APP_VERSION_CODE, true);
        builder.setReportField(ReportField.BRAND, true);
        builder.setReportField(ReportField.PHONE_MODEL, true);
        builder.setReportField(ReportField.STACK_TRACE, true);
        ACRA.init(this, builder);
    }
}