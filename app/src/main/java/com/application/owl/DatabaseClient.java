package com.application.owl;

import android.content.Context;
import androidx.room.Room;

public class DatabaseClient {

    private static DatabaseClient databaseClient;
    private OwlDatabase appDatabase;

    private DatabaseClient(Context context) {
        appDatabase = Room.databaseBuilder(context, OwlDatabase.class, "owl_database")
                .createFromAsset("database/owl_database")
                .build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if (databaseClient == null) {
            databaseClient = new DatabaseClient(context);
        }
        return databaseClient;
    }

    public OwlDatabase getAppDatabase() {
        return appDatabase;
    }
}
