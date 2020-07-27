package com.application.owl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.application.owl.models.Contact;
import java.util.List;
import com.application.owl.models.Message;
import com.application.owl.repositories.ContactRepository;
import com.application.owl.repositories.MessageRepository;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class MessageEdit extends AppCompatActivity {

    private Message editMessage;
    private EditText editTextMessage;
    private TextView textViewMessage;
    private MessageRepository messageRepository;
    private ContactRepository contactRepository;
    private Context context;
    private boolean isNewMessage = false;
    private boolean isPrepackagedMessage = false;
    private int editMessageID;
    public static final String CONTACT = "contact";
    private String sendMessageBody = "";
    public Contact messageContact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_edit);

        // initialize basic variables
        context = getApplicationContext();
        messageRepository = new MessageRepository(getApplication());
        contactRepository = new ContactRepository(getApplication());

        // get ui components
        editTextMessage = findViewById(R.id.message_edit_text);
        textViewMessage = findViewById(R.id.message_textview);
        textViewMessage.setVisibility(View.INVISIBLE);
        Button deleteButton = findViewById(R.id.delete_message_button);
        Button saveButton = findViewById(R.id.save_message_button);

        // get the contact from the intent, if there is one (i.e. if we came from an Owl notification)
        Intent messageIntent = this.getIntent();
        String bitmapString = messageIntent.getStringExtra(CONTACT);       // if this is set, we're about to send a message rather than edit one
        if (bitmapString != null) {
            messageContact = Converters.deserializeFromJson(bitmapString);
        }

        editMessageID = messageIntent.getIntExtra("id", -100);

        if (editMessageID != -100) {           // is this not a new message?
            handleMessage(editMessageID);
            if (messageContact != null) {      // if we have a contact, we don't need the message edit activity itself &
                finish();                      // we should exit as soon as we've called handleMessage
                return;
            }
            if (editMessageID <= 50) {          // it's not a new message and we don't have a message contact - is the message a released one?
                Toast.makeText(context, getResources().getString(R.string.prepackaged_message_warning), Toast.LENGTH_LONG).show();
                editTextMessage.setVisibility(View.INVISIBLE);
                textViewMessage.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.INVISIBLE);
                isPrepackagedMessage = true;
            }
        }
        else {
            if (messageContact != null) {      // we shouldn't have a contact here, but if we do for whatever reason, just exit
                finish();
                return;
            }
            isNewMessage = true;
        }

        // if it's not a new or pre-packaged message, we should be able to delete it
        if ((!isNewMessage) && (!isPrepackagedMessage)) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener((View v) -> deleteMessage(editMessage));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void deleteMessage(final Message message) {
        // delete the message
        messageRepository.delete(message);

        // we've deleted the message! notify the user and finish up here, kicking them over to the message selection activity
        Toast.makeText(context, getResources().getString(R.string.message_deleted_toast), Toast.LENGTH_LONG).show();
        Intent messageSelectIntent = new Intent(context, MessageSelect.class);
        messageSelectIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        context.startActivity(messageSelectIntent);
    }

    public void saveMessage(View v) {
        // get and validate the message body
        final String messageText = editTextMessage.getText().toString().trim();
        if (!validateMessageBody(messageText)) {
            return;
        }

        // if it checks out, create and set up the message object
        Message message = new Message();
        message.setBody(messageText);

        // if it's not a new message, set the ID to match the message we're editing & update it
        if (!isNewMessage) {
            message.message_id = editMessageID;
            messageRepository.update(message);
        }
        // if it's a new message log creation of a new message and insert it
        if (isNewMessage) {
            FirebaseEventLogger.logFirebaseEvent(context, "create_message", "message created", "", "", "", "", "");
            messageRepository.insert(message);
        }

        // we've saved the message! notify the user and finish up here, kicking them over to the message selection activity
        Toast.makeText(context, getResources().getString(R.string.message_saved_toast), Toast.LENGTH_LONG).show();
        Intent messageSelectIntent = new Intent(context, MessageSelect.class);
        messageSelectIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(messageSelectIntent);
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    public void handleMessage(final int messageID) {
        new HandleMessageAsyncTask(context).execute(messageID);
    }

    @SuppressLint("StaticFieldLeak")
    public class HandleMessageAsyncTask extends AsyncTask<Integer, Void, Message> {

        Context context;

        private HandleMessageAsyncTask(@NonNull Context context) {
            this.context = context;
        }

        @Override
        protected Message doInBackground(@NonNull Integer... params) {
            return messageRepository.messageDao.fetchMessageById(params[0]);
        }

        @Override
        protected void onPostExecute(Message message) {
            super.onPostExecute(message);

            if (message != null) {
                if (!isPrepackagedMessage) {                    // if it's a message the user owns, set the edittext up with the message body
                    sendMessageBody = message.getBody();
                    editTextMessage.setText(message.getBody());
                    editMessage = message;
                    editMessageID = message.getId();
                }
                else {
                    sendMessageBody = message.getBody();        // if it's a pre-packaged message, do the same but for the textview
                    textViewMessage.setText(message.getBody());
                }
            }

            if (messageContact != null) {                       // if we have a contact, that means we're here via Owl notification
                                                                // and we should process an Owl message according to preferences
                String preference = messageContact.getPreference();
                Intent messageIntent = new Intent(Intent.ACTION_VIEW);          // this intent is going to be whatever app is associated with the contact's preference
                String phoneNumber = "";

                // we only use the phone number if the preference is sms or WhatsApp
                if ((preference.equals(getResources().getString(R.string.sms))) || (preference.equals(getResources().getString(R.string.whatsapp)))) {      // phone number needy preferences
                    phoneNumber = getFormattedPhoneNumber(messageContact);
                }

                // set the intent according to the contact's preference
                if (preference.equals(getResources().getString(R.string.sms))) {
                    messageIntent = setupSMSIntent(phoneNumber, sendMessageBody);
                }
                if (preference.equals(getResources().getString(R.string.email))) {
                    messageIntent = setupEmailIntent(messageContact.getEmail(), sendMessageBody);
                }
                if (preference.equals(getResources().getString(R.string.whatsapp))) {
                    messageIntent = setupWhatsAppIntent(phoneNumber, sendMessageBody);
                }
                if (preference.equals(getResources().getString(R.string.facebook_messenger))) {
                    messageIntent = setupFacebookIntent(messageContact.getFacebookURI(), sendMessageBody);
                }
                if (preference.equals(getResources().getString(R.string.skype))) {
                    messageIntent = setupSkypeIntent(messageContact.getSkypeName(), sendMessageBody);
                }

                //messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    if (messageIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(messageIntent);
                    }
                }
                catch (ActivityNotFoundException ex) {
                    Toast.makeText(context, preference + getResources().getString(R.string.not_installed_toast), Toast.LENGTH_SHORT).show();
                }

                // if we're going to Facebook Messenger or Skype, we can't set the text in a message,
                // so we show a toast to let the user know to paste the message from their clipboard
                if ((preference.equals(getResources().getString(R.string.facebook_messenger))) || (preference.equals(getResources().getString(R.string.skype)))) {
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.owl_toast_view, findViewById(R.id.owl_toast_container));
                    TextView text = layout.findViewById(R.id.text);
                    text.setText(String.format("%s %s", getResources().getString(R.string.paste_message_from_clipboard_toast), preference));
                    Toast toast = new Toast(context);
                    toast.setGravity(Gravity.CENTER, 0, 40);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }

                // log that we've tapped an Owl notification to send a message & update the Owl count for this contact
                FirebaseEventLogger.logFirebaseEvent(context, "tap_owl", "owl notification tapped", preference, sendMessageBody, "", "", "");
                messageContact.setOwlCount(messageContact.owlCount + 1);
                contactRepository.update(messageContact);
                finish();
            }
        }
    }

    // helper for getting formatted phone number (edits here should also go to equivalent function in Call)
    private String getFormattedPhoneNumber(Contact contact) {
        int countryCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(contact.getCountryCode());
        String countryCodeString = ((Integer) countryCode).toString();
        return "+" + countryCodeString + contact.getPhone();
    }

    // build SMS intent
    private Intent setupSMSIntent(String phoneNumber, String message) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            intent.setData(Uri.parse("sms" + "to:" + Uri.encode(phoneNumber)));
            intent.putExtra("address", phoneNumber);
            intent.putExtra("sms_body", message);

            PackageManager pm = this.getPackageManager();
            List<ResolveInfo> resInfo = pm.queryIntentActivities(intent, 0);

            for (int i = 0; i < resInfo.size(); i++) {
                ResolveInfo ri = resInfo.get(i);
                String packageName = ri.activityInfo.packageName;

                if (packageName.contains("sms")) {
                    //Log.d("TAG", packageName + " : " + ri.activityInfo.name);
                    intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                }
            }
        } catch (Exception e) {
            System.out.println("Error in setupSMSIntent with phoneNumber - " + phoneNumber + " and message - " + message);
        }
        return intent;
    }

    // build email intent
    private Intent setupEmailIntent(String email, String message) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        String uriText ="mailto:" + Uri.encode(email) + "?body=" + Uri.encode(message);
        emailIntent.setData(Uri.parse(uriText)); // only email apps should handle this

        try {
            startActivity(emailIntent);
        }
        catch (ActivityNotFoundException ex) {
            Toast.makeText(this, getResources().getString(R.string.email_app_toast) + getResources().getString(R.string.not_installed_toast), Toast.LENGTH_SHORT)
                    .show();
        }
        return emailIntent;
    }

    // build WhatsApp intent
    private Intent setupWhatsAppIntent(String phoneNumber, String message) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String whatsAppPackage = "com.whatsapp";
        String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + message;

/*        PackageManager pm = this.getPackageManager();
        pm.getPackageInfo(whatsAppPackage, PackageManager.GET_ACTIVITIES);
        intent.setData(Uri.parse(url));
        intent.setPackage(whatsAppPackage);*/

        try {
            PackageManager pm = this.getPackageManager();
            pm.getPackageInfo(whatsAppPackage, PackageManager.GET_ACTIVITIES);
            intent.setData(Uri.parse(url));
            intent.setPackage(whatsAppPackage);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, getResources().getString(R.string.whatsapp) + getResources().getString(R.string.not_installed_toast), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            intent = new Intent();
        }
        return intent;
    }

    // build Facebook Messenger intent
    private Intent setupFacebookIntent(String facebookURI, String message) {
        String fbUser = facebookURI.substring(facebookURI.lastIndexOf("/") + 1);
        Uri FbURI = Uri.parse("http://m.me/" + fbUser);         // + "&text=" + message
/*        Intent fbIntent = new Intent(Intent.ACTION_VIEW, FbURI);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("message", message);
        clipboard.setPrimaryClip(clip);*/
        Intent fbIntent = new Intent(Intent.ACTION_VIEW, FbURI);

        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("message", message);
            assert clipboard != null;
            clipboard.setPrimaryClip(clip);
            //startActivity(fbIntent);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(this,getResources().getString(R.string.facebook_messenger) + getResources().getString(R.string.not_installed_toast), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return fbIntent;
    }

    // build Skype intent
    @NonNull
    private Intent setupSkypeIntent(String skypeName, String message) {
        Uri skypeUri = Uri.parse("skype:" + skypeName + "?chat&topic=mytopic");         //"skype:desired_skype_username?chat&topic=mytopic"
        Intent skypeMessengerIntent = new Intent(Intent.ACTION_VIEW, skypeUri);
        skypeMessengerIntent.setComponent(new ComponentName("com.skype.raider", "com.skype.raider.Main"));
        skypeMessengerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("message", message);
            assert clipboard != null;
            clipboard.setPrimaryClip(clip);
            //startActivity(skypeMessengerIntent);
        }
        catch (ActivityNotFoundException ex) {
            Toast.makeText(this,getResources().getString(R.string.skype) + getResources().getString(R.string.not_installed_toast), Toast.LENGTH_SHORT)
                    .show();
        }
        return skypeMessengerIntent;
    }


    // message body validation

    public boolean validateMessageBody(String messageBody) {
        if (isMessageBodyEmpty(messageBody)) {
            editTextMessage.setError(getResources().getString(R.string.empty_message_toast));
            editTextMessage.requestFocus();
            return false;
        }
        if (isMessageBodyTooLong(messageBody)) {
            editTextMessage.setError(getResources().getString(R.string.message_character_limit));
            editTextMessage.requestFocus();
            return false;
        }
        return true;
    }

    // static message body validation helpers

    public static boolean isMessageBodyEmpty(String messageBody) {
        return messageBody.isEmpty();
    }

    public static boolean isMessageBodyTooLong(String messageBody) { return (messageBody.length() > 280);}
}