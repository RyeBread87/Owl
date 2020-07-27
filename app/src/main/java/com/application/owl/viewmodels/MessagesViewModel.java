package com.application.owl.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.application.owl.models.Message;
import com.application.owl.repositories.MessageRepository;
import java.util.List;

// view model for a list of messages, used by MessageSelect (primarily so we can set wrap LiveData around the list of messages)
public class MessagesViewModel extends AndroidViewModel {

    private MessageRepository messageRepository;
    private LiveData<List<Message>> messageLiveData;

    public MessagesViewModel(@NonNull Application application) {
        super(application);
        messageRepository = new MessageRepository(application);
        messageLiveData = messageRepository.getAllMessagesLiveData();
    }

    public LiveData<List<Message>> getMessagesLiveData() {
        return messageLiveData;
    }

    public void insert(Message message) {
        messageRepository.insert(message);
    }

    public void update(Message message) {
        messageRepository.update(message);
    }

    public void delete(Message message) {
        messageRepository.delete(message);
    }
}
