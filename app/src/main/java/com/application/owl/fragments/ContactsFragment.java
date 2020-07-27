package com.application.owl.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.application.owl.ContactEdit;
import com.application.owl.ContactImport;
import com.application.owl.ContactSelect;
import com.application.owl.R;
import com.application.owl.repositories.ContactRepository;
import java.util.Objects;

// fragment for contact-related activities, i.e. ContactEdit, ContactSelect, and ContactImport
public class ContactsFragment extends Fragment {

    private ContactRepository contactRepository;
    private Integer contactCount;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactRepository = new ContactRepository(Objects.requireNonNull(getActivity()).getApplication());
        setContactCount();
    }

    // get the current contact count so we can check if we're at the maximum when a user tries to create a new one
    @SuppressLint("StaticFieldLeak")
    private void setContactCount() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
            contactCount = contactRepository.getContactCount();
            return null;
            }
        }.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        final Button createContactButton = view.findViewById(R.id.create_contact_button);
        createContactButton.setOnClickListener(this::createContact);

        final Button selectContactButton = view.findViewById(R.id.select_contact_button);
        selectContactButton.setOnClickListener(this::selectContact);

        final Button importContactButton = view.findViewById(R.id.import_contact_button);
        importContactButton.setOnClickListener(this::importContact);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
    }

    private void createContact(View view)
    {
        int contactLimit = 1000;                // the limit of 1000 is arbitrary, but seems reasonable
        if (contactCount > contactLimit) {
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), getResources().getString(R.string.contact_limit_toast, contactLimit), Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this.getContext(), ContactEdit.class);
        startActivity(intent);
    }

    private void selectContact(View view)
    {
        Intent intent = new Intent(this.getContext(), ContactSelect.class);
        startActivity(intent);
    }

    private void importContact(View view)
    {
        Intent intent = new Intent(this.getContext(), ContactImport.class);
        startActivity(intent);
    }
}
