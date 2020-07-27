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
import com.application.owl.MessageEdit;
import com.application.owl.MessageSelect;
import com.application.owl.R;
import com.application.owl.repositories.MessageRepository;
import java.util.Objects;

// fragment for message-related activities, i.e. MessageEdit and MessageSelect
public class MessagesFragment extends Fragment {

    private MessageRepository messageRepository;
    private Integer messageCount;

    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messageRepository = new MessageRepository(Objects.requireNonNull(getActivity()).getApplication());
        setMessageCount();
    }

    // get the current message count so we can check if we're at the maximum when a user tries to create a new one
    @SuppressLint("StaticFieldLeak")
    private void setMessageCount() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                messageCount = messageRepository.getMessageCount();
                return null;
            }
        }.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        final Button createMessageButton = view.findViewById(R.id.create_message_button);
        createMessageButton.setOnClickListener(this::createMessage);

        final Button selectMessageButton = view.findViewById(R.id.select_message_button);
        selectMessageButton.setOnClickListener(this::selectMessage);

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

    private void createMessage(View view)
    {
        int messageLimit = 1000;            // the limit of 1000 is arbitrary, but seems reasonable
        if (messageCount > messageLimit) {
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), getResources().getString(R.string.message_limit_toast, messageLimit), Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this.getContext(), MessageEdit.class);
        startActivity(intent);
    }

    private void selectMessage(View view)
    {
        Intent intent = new Intent(this.getContext(), MessageSelect.class);
        startActivity(intent);
    }
}
