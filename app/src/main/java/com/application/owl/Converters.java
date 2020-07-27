package com.application.owl;

import androidx.room.TypeConverter;
import com.application.owl.models.Contact;
import com.google.gson.Gson;
import java.util.Date;

class Converters {

    @SuppressWarnings("WeakerAccess")
    @TypeConverter
    public static Long convertDateToLong(Date dateToConvert)
    {
        if (dateToConvert == null) {
            return(null);
        }

        return(dateToConvert.getTime());
    }

    @SuppressWarnings("WeakerAccess")
    @TypeConverter
    public static Date convertLongToDate(Long millisSinceEpoch)
    {
        if (millisSinceEpoch == null) {
            return(null);
        }

        return(new Date(millisSinceEpoch));
    }

    static String serializeToJson(Contact contact) {
        Gson gson = new Gson();
        return gson.toJson(contact);
    }

    static Contact deserializeFromJson(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, Contact.class);
    }
}
