package com.application.owl.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.application.owl.AppInfo;
import com.application.owl.MessageCreator;
import com.application.owl.PrivacyInfo;
import com.application.owl.R;
import com.application.owl.SendFeedback;
import com.application.owl.models.Contact;
import com.application.owl.models.Message;
import com.application.owl.repositories.ContactRepository;
import java.util.List;
import java.util.Objects;

// fragment for options & informational activities, including:
//      -overview of the Owl application
//      -privacy info
//      -switch to turn all notifications on or off
//      -triggering a one-time broadcast of a single notification for all active contacts
//      -an activity to compose feedback & submit via email
// the "Turn All Notifications On/Off" switch is set up to initialize in the "on position" if at least one contact
// is active, and to initialize in the off position if all contacts are inactive
public class OptionsFragment extends Fragment {

    private SwitchCompat toggleNotificationSwitch;
    private ContactRepository contactRepository;
    private boolean doNotToggle = true;         // doNotToggle starts as true while we initialize the switch to on or off depending on whether any contacts are currently active

    public OptionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactRepository = new ContactRepository(Objects.requireNonNull(getActivity()).getApplication());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_options, container, false);

        SwitchParams switchParams = new SwitchParams(this.getContext(), false, true);       // in our first call we're not toggling
        new InitContactDataAsyncTask(switchParams).execute(switchParams);

        // set a listener on the switch which (1) sets onOrOff to the on / off status of the switch and
        // (2) sets the initial position of the switch based on whether any contacts are currently active
        toggleNotificationSwitch = view.findViewById(R.id.notification_switch);
        toggleNotificationSwitch.setOnClickListener((buttonView) -> {
            switchParams.onOrOff = toggleNotificationSwitch.isChecked();
            switchParams.toggleNotifications = true;
            new InitContactDataAsyncTask(switchParams).execute(switchParams);
        });

        // set the listener so that when the switch is flipped we toggle all statuses on or off
        final Button triggerAllNotificationsButton = view.findViewById(R.id.all_notification_button);
        triggerAllNotificationsButton.setOnClickListener(v -> triggerAllNotifications());

        final Button aboutTheAppButton = view.findViewById(R.id.about_the_app_button);
        aboutTheAppButton.setOnClickListener(this::aboutTheApp);

        final Button sendFeedbackButton = view.findViewById(R.id.send_feedback_button);
        sendFeedbackButton.setOnClickListener(this::sendFeedback);

        final Button privacyInfoButton = view.findViewById(R.id.privacy_info_button);
        privacyInfoButton.setOnClickListener(this::privacyInfo);

        return view;
    }

    // triggers a single notification for all active contacts
    private void triggerAllNotifications() {
        new TriggerAllNotificationsAsyncTask(Objects.requireNonNull(Objects.requireNonNull(this.getActivity()).getApplicationContext())).execute();
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

    // launches the About Owl activity
    private void aboutTheApp(View view)
    {
        Intent intent = new Intent(this.getContext(), AppInfo.class);
        startActivity(intent);
    }

    // launches the Send Feedback (via Email) activity
    private void sendFeedback(View view)
    {
        Intent intent = new Intent(this.getContext(), SendFeedback.class);
        startActivity(intent);
    }

    // launches the Privacy Info activity
    private void privacyInfo(View view)
    {
        Intent intent = new Intent(this.getContext(), PrivacyInfo.class);
        startActivity(intent);
    }

    // async class to handle triggering all notifications
    @SuppressLint("StaticFieldLeak")
    public class TriggerAllNotificationsAsyncTask extends AsyncTask<Integer, Void, Integer> {

        Context context;

        private TriggerAllNotificationsAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            List<Contact> contactList = contactRepository.getAllContacts();
            int owlNotificationsTriggered = 0;
            for (Contact contact : contactList) {
                owlNotificationsTriggered = owlNotificationsTriggered + MessageCreator.createMessage(context, contact);         // createMessage creates a one-time message
            }
            return owlNotificationsTriggered;
        }

        @Override
        protected void onPostExecute(Integer owlNotificationsTriggered) {
            if (owlNotificationsTriggered == 0) { Toast.makeText(context.getApplicationContext(), getResources().getString(R.string.no_active_contacts_toast), Toast.LENGTH_LONG).show(); }
        }
    }

    private static class SwitchParams {
        Context context;
        Boolean toggleNotifications;
        Boolean onOrOff;

        SwitchParams(Context context, Boolean toggleNotifications, Boolean onOrOff) {
            this.context = context;
            this.toggleNotifications = toggleNotifications;
            this.onOrOff = onOrOff;
        }
    }

    // async class to handle (1) initializing of the switch state and (2) toggling of all statuses
    @SuppressLint("StaticFieldLeak")
    public class InitContactDataAsyncTask extends AsyncTask<SwitchParams, Void, List<Contact>> {

        Context context;
        Boolean toggleNotifications;
        Boolean onOrOff;

        private InitContactDataAsyncTask(SwitchParams switchParams) {
            this.context = switchParams.context;
            this.toggleNotifications = switchParams.toggleNotifications;
            this.onOrOff = switchParams.onOrOff;
        }

        @Override
        protected List<Contact> doInBackground(SwitchParams... params) {
            Context context = this.context;
            Boolean toggleNotifications = this.toggleNotifications;
            Boolean onOrOff = this.onOrOff;

            List<Contact> contactList = contactRepository.getAllContacts();

            if ((toggleNotifications) && (!doNotToggle)) {          // we're here to toggle, and the doNotToggle flag is off
                if (onOrOff) {                                      // so we toggle - if we're turning on all notifications (onOrOff == true)
                    for (Contact contact : contactList) {           // then we set the status to 1 (active) for all contacts where
                        if (contact.getStatus() != 1) {             // this isn't already the case
                            contact.setStatus(1);
                        }
                        contactRepository.update(contact);
                        MessageCreator.setMessageTask(context, contact);
                    }
                }
                else {
                    for (Contact contact : contactList) {           // if we're turning off all notifications (onOrOff == false)
                        if (contact.getStatus() != 0) {             // then we set the status to 0 (inactive) for all active contacts
                            contact.setStatus(0);
                        }
                        contactRepository.update(contact);
                        MessageCreator.setMessageTask(context, contact);
                    }
                }
            }
            return contactList;
        }

        @Override
        protected void onPostExecute(List<Contact> contactList) {
            if ((toggleNotifications) && (!doNotToggle)) { return; }        // if we're here to toggle, we just did that, so return
            boolean atLeastOneActive = false;                               // if we're not here to toggle, we're here to initialize
            for (Contact contact : contactList) {                           // the switch, so proceed to do that

                if (contact.getStatus() == 1) {
                    atLeastOneActive = true;
                    break;
                }
            }
            if (atLeastOneActive) {
                toggleNotificationSwitch.setChecked(true);
            }
            doNotToggle = false;                    // doNotToggle only needs to be true on the first pass through while we're initializing the switch position
        }
    }
}
