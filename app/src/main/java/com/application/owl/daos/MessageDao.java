package com.application.owl.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.application.owl.models.Message;
import com.application.owl.OwlDatabase;

import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM " + OwlDatabase.TABLE_NAME_MESSAGES)
    LiveData<List<Message>> getAllMessagesLiveData();

    @Query("SELECT * FROM " + OwlDatabase.TABLE_NAME_MESSAGES)
    List<Message> getAllMessages();

    @Query("SELECT COUNT(*) FROM " + OwlDatabase.TABLE_NAME_MESSAGES)
    Integer getMessageCount();

    @Insert
    void insert(Message... message);

    @Delete
    int delete(Message message);

    @SuppressWarnings("UnusedReturnValue")
    @Update
    int update(Message message);

    @Insert
    long insert(Message message);

    @Query("SELECT * FROM " + OwlDatabase.TABLE_NAME_MESSAGES + " WHERE message_id = :messageID")
    Message fetchMessageById(int messageID);

/*    @Query("DELETE FROM " + OwlDatabase.TABLE_NAME_MESSAGES)
    void deleteAllMessages();*/

/*    @Query("SELECT * FROM " + OwlDatabase.TABLE_NAME_MESSAGES + " WHERE body LIKE :messageText")
    LiveData<List<Message>> findMessageList(String messageText);*/
}