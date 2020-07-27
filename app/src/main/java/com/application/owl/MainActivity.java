package com.application.owl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.FrameLayout;
import com.application.owl.fragments.ContactsFragment;
import com.application.owl.fragments.MessagesFragment;
import com.application.owl.fragments.OptionsFragment;
import com.application.owl.models.Contact;
import com.application.owl.repositories.ContactRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ContactsFragment.OnFragmentInteractionListener
        , MessagesFragment.OnFragmentInteractionListener, OptionsFragment.OnFragmentInteractionListener {

    FrameLayout frameLayout;
    TabLayout tabLayout;
    private ContactRepository contactRepository;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        contactRepository = new ContactRepository(getApplication());
        FirebaseEventLogger.logFirebaseEvent(context, FirebaseAnalytics.Event.APP_OPEN, "", "", "", "", "", "");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.owl);

        // get the reference of FrameLayout and TabLayout
        frameLayout =  findViewById(R.id.simpleFrameLayout);
        tabLayout = findViewById(R.id.simpleTabLayout);
        // create a new Tab named "Contacts"
        TabLayout.Tab firstTab = tabLayout.newTab();
        firstTab.setText(getString(R.string.contacts)); // set the Text for the first Tab (Contacts)
        firstTab.setIcon(R.drawable.ic_person_black_48dp); // set an icon for the
        // first tab
        tabLayout.addTab(firstTab); // add  the tab at in the TabLayout
        // create a new Tab named "Messages"
        TabLayout.Tab secondTab = tabLayout.newTab();
        secondTab.setText(getString(R.string.messages)); // set the Text for the second Tab (Messages)
        secondTab.setIcon(R.drawable.ic_message_black_18dp); // set an icon for the second tab
        tabLayout.addTab(secondTab); // add  the tab  in the TabLayout
        // create a new Tab named "Options"
        TabLayout.Tab thirdTab = tabLayout.newTab();
        thirdTab.setText(getString(R.string.options)); // set the Text for the first Tab (Options)
        thirdTab.setIcon(R.drawable.options_icon); // set an icon for the first tab
        tabLayout.addTab(thirdTab); // add  the tab at in the TabLayout


        // perform setOnTabSelectedListener event on TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // get the current selected tab's position and replace the fragment accordingly
                Fragment fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new ContactsFragment();
                        break;
                    case 1:
                        fragment = new MessagesFragment();
                        break;
                    case 2:
                        fragment = new OptionsFragment();
                        break;
                }
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                assert fragment != null;
                ft.replace(R.id.simpleFrameLayout, fragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
        });

        // if the app is being explicitly started, refresh notification tasks just in case
        startNotifications();

        // start on the contacts fragment
        Fragment fragment = new ContactsFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.simpleFrameLayout, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @SuppressLint("StaticFieldLeak")
    private void startNotifications() {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                List<Contact> contactList = contactRepository.getAllContacts();

                for (Contact contact : contactList) {
                    MessageCreator.setMessageTask(context, contact);

                    // If the post-snooze date is in the future, this contact had their last owl
                    // snoozed and we need to handle schedule the snoozed notification
                    Calendar cal = Calendar.getInstance();
                    long now =  cal.getTimeInMillis();
                    if (contact.postSnoozeDate > now) {
                        MessageCreator.createSnoozeMessage(context, contact);
                    }
                }
                return null;
            }
        }.execute();
    }
}
