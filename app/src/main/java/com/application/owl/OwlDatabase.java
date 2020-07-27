package com.application.owl;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.application.owl.daos.ContactDao;
import com.application.owl.daos.MessageDao;
import com.application.owl.models.Contact;
import com.application.owl.models.Message;

@TypeConverters({Converters.class})
@Database(entities = {Contact.class, Message.class}, version = 1, exportSchema = false)
public abstract class OwlDatabase extends RoomDatabase {

    public static final String TABLE_NAME_CONTACTS = "Contact";
    public static final String TABLE_NAME_MESSAGES = "Message";
    public abstract ContactDao contactDao();
    public abstract MessageDao messageDao();
}
