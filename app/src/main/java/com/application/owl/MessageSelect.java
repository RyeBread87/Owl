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
import com.application.owl.adapters.MessageAdapter;
import com.application.owl.databinding.ActivityMessageSelectBinding;
import com.application.owl.models.Message;
import com.application.owl.viewmodels.MessagesViewModel;
import java.util.ArrayList;
import java.util.List;

public class MessageSelect extends AppCompatActivity {

    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter mAdapter;
    private ActivityMessageSelectBinding binding;
    public static final String CONTACT = "contact";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_select);

        context = getApplicationContext();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_message_select);
        String contactBitmapString = this.getIntent().getStringExtra(CONTACT);      // if we're here via an Owl notification, we'll have the contact it's for
        initRecyclerView(contactBitmapString);
        // set ui pieces to handle case where there are no messages (shouldn't ever happen)
        TextView noMessagesMessage = findViewById(R.id.no_messages_message);
        noMessagesMessage.setVisibility(View.INVISIBLE);
        Button createMessageButton = findViewById(R.id.create_message_from_messages);
        createMessageButton.setVisibility(View.INVISIBLE);

        MessagesViewModel messagesViewModel = ViewModelProviders.of(this).get(MessagesViewModel.class);
        messagesViewModel.getMessagesLiveData().observe(this, messages -> mAdapter.updateMessageList(messages));
    }

    // initialize the RecyclerView and set the adapter
    private void initRecyclerView(String contactBitmapString) {
        RecyclerView recyclerView = binding.messagesRecyclerView;
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new MessageAdapter(context, messageList, contactBitmapString);
        binding.setMessageAdapter(mAdapter);
    }

    // if there are no messages created yet, the user will see a button that takes them here & onward to MessageEdit to create their first message;
    // this should actually never happen since we have pre-packaged messages, but it's here just in case
    public void createMessage(View view) {
        Intent intent = new Intent(this, MessageEdit.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}