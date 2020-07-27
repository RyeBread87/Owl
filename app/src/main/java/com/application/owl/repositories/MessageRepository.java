package com.application.owl.repositories;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import com.application.owl.DatabaseClient;
import com.application.owl.models.Message;
import com.application.owl.OwlDatabase;
import com.application.owl.daos.MessageDao;
import java.util.List;

// repository class for performing CRUD operations on & other utilities for messages
public class MessageRepository {

    public MessageDao messageDao;

    public MessageRepository(Application application) {
        OwlDatabase dataBase = DatabaseClient.getInstance(application).getAppDatabase();
        messageDao = dataBase.messageDao();
    }

    public void insert(Message message) {
        new InsertMessageAsyncTask(messageDao).execute(message);
    }

    public void update(Message message) {
        new UpdateMessageAsyncTask(messageDao).execute(message);
    }

    public Integer delete(Message message) {
        new DeleteMessageAsyncTask(messageDao).execute(message);
        return message.getId();
    }

    public LiveData<List<Message>> getAllMessagesLiveData() {
        return messageDao.getAllMessagesLiveData();
    }

    public Integer getMessageCount() {
        return messageDao.getMessageCount();
    }

    public static class InsertMessageAsyncTask extends AsyncTask<Message, Void, Void> {

        MessageDao messageDao;

        InsertMessageAsyncTask(MessageDao messageDao) {
            this.messageDao = messageDao;
        }

        @Override
        public Void doInBackground(Message... messages) {
            messageDao.insert(messages[0]);
            return null;
        }
    }

    public static class UpdateMessageAsyncTask extends AsyncTask<Message, Void, Void> {

        MessageDao messageDao;

        UpdateMessageAsyncTask(MessageDao messageDao) {
            this.messageDao = messageDao;
        }

        @Override
        public Void doInBackground(Message... messages) {
            messageDao.update(messages[0]);
            return null;
        }
    }

    public static class DeleteMessageAsyncTask extends AsyncTask<Message, Void, Integer> {

        MessageDao messageDao;

        DeleteMessageAsyncTask(MessageDao messageDao) {
            this.messageDao = messageDao;
        }

        @Override
        public Integer doInBackground(Message... messages) {
            return messageDao.delete(messages[0]);
        }
    }
}
