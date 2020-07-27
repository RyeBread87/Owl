package com.application.owl.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.application.owl.models.Contact;
import com.application.owl.repositories.ContactRepository;
import java.util.List;

// view model for a list of contacts, used by ContactSelect (primarily so we can set wrap LiveData around the list of contacts)
public class ContactsViewModel extends AndroidViewModel {

    private ContactRepository contactRepository;
    private LiveData<List<Contact>> contactsLiveData;

    public ContactsViewModel(@NonNull Application application) {
        super(application);
        contactRepository = new ContactRepository(application);
        contactsLiveData = contactRepository.getContactsLiveData();
    }

    public LiveData<List<Contact>> getContactsLiveData() {
        return contactsLiveData;
    }

    public void insert(Contact contact) {
        contactRepository.insert(contact);
    }

    public void update(Contact contact) {
        contactRepository.update(contact);
    }

    public void delete(Contact contact) {
        contactRepository.delete(contact);
    }
}
