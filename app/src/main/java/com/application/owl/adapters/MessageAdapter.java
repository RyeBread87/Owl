package com.application.owl.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.application.owl.MessageEdit;
import com.application.owl.R;
import com.application.owl.clicklisteners.CustomMessageClickListener;
import com.application.owl.databinding.MessageListRowBinding;
import com.application.owl.models.Message;
import java.util.Collections;
import java.util.List;

// message adapter - takes care of binding messages to the RecyclerView in the MessageSelect activity
// as well as updating the message list and launching the MessageEdit activity
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> implements CustomMessageClickListener {

    private static final String CONTACT = "contact";
    private List<Message> messageList;
    private Context context;
    private String contactBitmapString;

    public MessageAdapter(Context ctx, List<Message> msgList, String contactBitmapString) {
        messageList = msgList;
        context = ctx;
        this.contactBitmapString = contactBitmapString;
    }

    // sets up a message_list_row layout for each row
    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MessageListRowBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.message_list_row, parent, false);

        return new MessageViewHolder(binding);
    }

    // binds a message to each row
    @Override
    public void onBindViewHolder(MessageAdapter.MessageViewHolder holder, int position) {
        holder.messageListRowBinding.setMessage(messageList.get(position));
        holder.bind(messageList.get(position));
        holder.messageListRowBinding.setItemClickListener(this);

        if (messageList.get(position).getId() <= 50) {
            holder.itemView.setBackgroundColor(context.getColor(R.color.colorPrimaryDark));
            holder.messageListRowBinding.messageBody.setTextColor(Color.WHITE);
        }
        else {
            holder.itemView.setBackgroundColor(context.getColor(R.color.colorAccent));
            holder.messageListRowBinding.messageBody.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        if (messageList != null) {
            return messageList.size();
        } else {
            return 0;
        }
    }

    // static message view holder class with a constructor that takes a binding and a bind method
    static class MessageViewHolder extends RecyclerView.ViewHolder {

        MessageListRowBinding messageListRowBinding;

        MessageViewHolder(MessageListRowBinding messageListRowBinding) {
            super(messageListRowBinding.getRoot());
            this.messageListRowBinding = messageListRowBinding;
        }

        void bind(Object obj) {
            messageListRowBinding.setVariable(com.application.owl.BR.message, obj);
            messageListRowBinding.executePendingBindings();
        }
    }

    // launch the message edit activity when a message is selected
    public void editMessage(Message message) {
        Intent intent = new Intent(context, MessageEdit.class);
        intent.putExtra("id", message.getId());
        // if we have a contact, we'll set the contactBitmapString in this extra
        intent.putExtra(CONTACT, contactBitmapString);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // update the message list when a message is created, deleted, or updated
    public void updateMessageList(List<Message> data) {
        messageList.clear();
        messageList.addAll(data);
        Collections.sort(messageList, (m1, m2) -> m2.getId().compareTo(m1.getId()));
        notifyDataSetChanged();
    }
}