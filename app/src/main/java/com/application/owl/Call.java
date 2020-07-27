package com.application.owl;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.application.owl.models.Contact;
import com.application.owl.repositories.ContactRepository;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

// activity to handle call intents
public class Call extends AppCompatActivity {

    public static final String CONTACT = "contact";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Contact contact = Converters.deserializeFromJson(getIntent().getStringExtra(CONTACT));
        if (contact == null) { return; }        // if we don't have a contact, something's wrong & we should not continue
        handleCallIntent(contact);
    }

    public void handleCallIntent(Contact contact) {
        Context context = getApplicationContext();
        ContactRepository contactRepository = new ContactRepository(getApplication());

        // log a firebase event for the tapped notification that got us here
        FirebaseEventLogger.logFirebaseEvent(context, "tap_owl", "owl notification tapped", getResources().getString(R.string.call), "", "","", "");

        // update the owl count for this contact
        contact.setOwlCount(contact.owlCount + 1);
        @SuppressLint("StaticFieldLeak")
        class UpdateOwlCount extends AsyncTask<Contact, Void, Void> {

            @Override
            protected Void doInBackground(Contact... params) {
                contactRepository.update(params[0]);
                return null;
            }
        }
        UpdateOwlCount uoc = new UpdateOwlCount();
        uoc.execute(contact);

        // set up intent & launch activity for call
        String phoneNumber = getFormattedPhoneNumber(contact);
        Intent callIntent = setupCallIntent(phoneNumber);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(callIntent);
        finish();
    }

    // helper for getting formatted phone number (edits here should also go to equivalent function in MessageEdit)
    private String getFormattedPhoneNumber(Contact contact) {
        int countryCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(contact.getCountryCode());
        String countryCodeString = ((Integer) countryCode).toString();
        return "+" + countryCodeString + contact.getPhone();
    }

    // create & return intent for phone call
    private Intent setupCallIntent(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        try {
            intent.setData(Uri.parse("tel:" + Uri.encode(phoneNumber)));
        } catch (Exception e) {
            System.out.println("Error in setupCallIntent with phoneNumber " + phoneNumber);
        }
        return intent;
    }
}
