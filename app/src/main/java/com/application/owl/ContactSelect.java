package com.application.owl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.application.owl.adapters.ContactAdapter;
import com.application.owl.models.Contact;
import com.application.owl.databinding.ActivityContactSelectBinding;
import com.application.owl.viewmodels.ContactsViewModel;
import java.util.ArrayList;
import java.util.List;

// activity for selecting existing contacts to edit via a RecyclerView
public class ContactSelect extends AppCompatActivity {

    private List<Contact> contactList = new ArrayList<>();
    private ContactAdapter mAdapter;
    private ActivityContactSelectBinding binding;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_select);

        context = getApplicationContext();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact_select);
        initRecyclerView();
        // set ui pieces to handle case where there are no contacts
        TextView noContactsMessage = findViewById(R.id.no_contacts_message);
        noContactsMessage.setVisibility(View.INVISIBLE);
        Button createContactButton = findViewById(R.id.create_contact_from_contacts);
        createContactButton.setVisibility(View.INVISIBLE);

        ContactsViewModel contactsViewModel = ViewModelProviders.of(this).get(ContactsViewModel.class);
        contactsViewModel.getContactsLiveData().observe(this, contacts -> {
            mAdapter.updateContactList(contacts);
            if ((contacts == null) || (contacts.size() == 0)) {
                noContactsMessage.setVisibility(View.VISIBLE);
                createContactButton.setVisibility(View.VISIBLE);
            }
        });
    }

    // initialize the RecyclerView and set the adapter
    private void initRecyclerView() {
        RecyclerView recyclerView = binding.contactSelectRecyclerView;
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ContactAdapter(contactList, this);
        binding.setContactAdapter(mAdapter);
    }

    // if there are no contacts created yet, the user will see a button that takes them here & onward to ContactEdit to create their first contact
    public void createContact(View view)
    {
        Intent intent = new Intent(this, ContactEdit.class);
        startActivity(intent);
    }
}
