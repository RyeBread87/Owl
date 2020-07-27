package com.application.owl.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.application.owl.ContactEdit;
import com.application.owl.clicklisteners.CustomContactClickListener;
import com.application.owl.R;
import com.application.owl.models.Contact;
import com.application.owl.databinding.ContactListRowBinding;
import java.util.List;

// contact adapter - takes care of binding contacts to the RecyclerView in the ContactSelect activity
// as well as updating the contact list and launching the ContactEdit activity
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements CustomContactClickListener {

    private List<Contact> contactList;
    private Context context;

    public ContactAdapter(List<Contact> contactList, Context ctx) {
        this.contactList = contactList;
        context = ctx;
    }

    // sets up a contact_list_row layout for each row
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ContactListRowBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.contact_list_row, parent, false);

        return new ContactViewHolder(binding);
    }

    // binds a contact to each row
    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        holder.contactListRowBinding.setContact(contactList.get(position));
        holder.bind(contactList.get(position));
        holder.contactListRowBinding.setItemClickListener(this);
    }

    @Override
    public int getItemCount() {
        if (contactList != null) {
            return contactList.size();
        } else {
            return 0;
        }
    }

    // static contact view holder class with a constructor that takes a binding and a bind method
    static class ContactViewHolder extends RecyclerView.ViewHolder {

        ContactListRowBinding contactListRowBinding;

        ContactViewHolder(ContactListRowBinding contactListRowBinding) {
            super(contactListRowBinding.getRoot());
            this.contactListRowBinding = contactListRowBinding;
        }

        void bind(Object obj) {
            contactListRowBinding.setVariable(com.application.owl.BR.contact, obj);
            contactListRowBinding.executePendingBindings();
        }
    }

    // launch the contact edit activity when a contact is selected
    public void editContact(Contact contact) {
        Intent intent = new Intent(context, ContactEdit.class);
        intent.putExtra("id", contact.getId());
        context.startActivity(intent);
    }

    // update the contact list when a contact is created, deleted, or updated
    public void updateContactList(List<Contact> data) {
        contactList.clear();
        contactList.addAll(data);
        notifyDataSetChanged();
    }
}
