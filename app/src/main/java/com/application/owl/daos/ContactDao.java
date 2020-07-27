package com.application.owl.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.application.owl.models.Contact;
import com.application.owl.OwlDatabase;
import java.util.List;

@Dao
public interface ContactDao {
    @Query("SELECT * FROM " + OwlDatabase.TABLE_NAME_CONTACTS)
    LiveData<List<Contact>> getAllContactsLiveData();

    @Query("SELECT * FROM " + OwlDatabase.TABLE_NAME_CONTACTS)
    List<Contact> getAllContacts();

    @Query("SELECT COUNT(*) FROM " + OwlDatabase.TABLE_NAME_CONTACTS)
    Integer getContactCount();

    @Insert
    long insert(Contact contact);

    @Delete
    int delete(Contact contact);

    @SuppressWarnings("UnusedReturnValue")
    @Update
    int update(Contact contact);

    @Query("SELECT * FROM " + OwlDatabase.TABLE_NAME_CONTACTS + " WHERE contact_id = :contactID")
    Contact fetchContactById(int contactID);
}
