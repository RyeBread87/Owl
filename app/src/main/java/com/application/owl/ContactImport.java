package com.application.owl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import com.application.owl.adapters.ContactImportModelAdapter;
import com.application.owl.models.Contact;
import com.application.owl.databinding.ActivityContactImportBinding;
import com.application.owl.models.ContactImportModel;
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static android.Manifest.permission.READ_CONTACTS;

// Activity class for importing contacts from the user's contact list
public class ContactImport extends AppCompatActivity implements SearchView.OnQueryTextListener, SortedListAdapter.Callback, LoaderManager.LoaderCallbacks<Cursor> {

    private MenuItem searchItem;
    private SearchView searchView;
    private Animator mAnimator;
    private static final Comparator<ContactImportModel> COMPARATOR = new SortedListAdapter.ComparatorBuilder<ContactImportModel>()
            .setOrderForModel(ContactImportModel.class, (a, b) -> Integer.signum(a.getRank() - b.getRank()))
            .build();
    private ContactImportModelAdapter mAdapter;
    private List<ContactImportModel> mModels = new ArrayList<>();
    private ActivityContactImportBinding mBinding;
    private static final int CONTACTS_LOADER_ID = 1;
    private int REQUEST_READ_CONTACTS = 0;
    private boolean contactsLoaded = false;
    private Context context;
    // private static final String TAG_ANDROID_CONTACTS = "ANDROID_CONTACTS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.ImportTheme);

        context = getApplicationContext();
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_contact_import);
        mAdapter = new ContactImportModelAdapter(this, COMPARATOR, model -> {         // create our ContactImportModelAdapter, giving it our comparator and a listener
            Intent intent = new Intent(this, ContactEdit.class);                // that launches ContactEdit with a ContactImportModel when one is clicked
            intent.putExtra("ContactImport", model);        // this extra is what tells ContactEdit that we're importing a contact
            startActivity(intent);
        });

        mAdapter.addCallback(this);
        mBinding.recyclerViewContact.setLayoutManager(new LinearLayoutManager(this));   // set the recyclerview layout manager
        mBinding.recyclerViewContact.setAdapter(mAdapter);                                      // set our adapter to the binding
        boolean can = mayRequestContacts();
        if (!can) {
            return;         // READ_CONTACTS permission not granted
        }

        Toast.makeText(context, getResources().getString(R.string.loading_contacts), Toast.LENGTH_LONG).show();
        FetchContacts fc = new FetchContacts(context, mModels, mAdapter);
        fc.execute();
    }

    // set up ui & set the query text listener
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchItem.setVisible(false);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    // if permission hasn't been granted, request it from the user
    private boolean mayRequestContacts() {
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            androidx.recyclerview.widget.RecyclerView recViewContact = findViewById(R.id.recyclerViewContact);
            Snackbar.make(recViewContact, getResources().getString(R.string.read_contact_permission_reason), Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, v -> requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS));
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    // once we have permission, start fetching contacts
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted, start loading contacts
                Toast.makeText(context, getResources().getString(R.string.loading_contacts), Toast.LENGTH_LONG).show();
                FetchContacts fc = new FetchContacts(context, mModels, mAdapter);
                fc.execute();
            }
        }
    }

    // listener that's called any time the search string changes to filter the contact list
    @Override
    public boolean onQueryTextChange(String query) {
        if (!contactsLoaded) {                      // we hide the search view while contacts are loading, so we shouldn't encounter this,
            searchView.setIconified(true);          // but just in case - we let the user know to wait because we're still loading
            searchView.clearFocus();
            Toast.makeText(context, getResources().getString(R.string.still_loading_contacts), Toast.LENGTH_LONG).show();
            return false;
        }

        final List<ContactImportModel> filteredModelList = filter(mModels, query, context);
        mAdapter.edit()
                .replaceAll(filteredModelList)
                .commit();
        return true;
    }

    // we don't really need this since we have onQueryTextChange filtering as we go
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    // method to filter the list of ContactImportModels any time the query text changes
    private List<ContactImportModel> filter(List<ContactImportModel> models, String query, Context context) {
        final List<ContactImportModel> filteredModelList = new ArrayList<>();
        if (models == null) {
            Toast.makeText(context, getResources().getString(R.string.still_loading_contacts), Toast.LENGTH_LONG).show();
            return filteredModelList;
        }

        final String lowerCaseQuery = query.toLowerCase();
        for (ContactImportModel model : models) {
            final String text = model.getText().toLowerCase();
            if (text.contains(lowerCaseQuery)) {            // we use a pretty inclusive "string contains" search for contacts
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public void onEditStarted() {
        if (mBinding.editProgressBar.getVisibility() != View.VISIBLE) {
            mBinding.editProgressBar.setVisibility(View.VISIBLE);
            mBinding.editProgressBar.setAlpha(0.0f);
        }

        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator = ObjectAnimator.ofFloat(mBinding.editProgressBar, View.ALPHA, 1.0f);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.start();
        mBinding.recyclerViewContact.animate().alpha(0.5f);
    }

    @Override
    public void onEditFinished() {
        mBinding.recyclerViewContact.scrollToPosition(0);
        mBinding.recyclerViewContact.animate().alpha(1.0f);

        if (mAnimator != null) {
            mAnimator.cancel();
        }

        mAnimator = ObjectAnimator.ofFloat(mBinding.editProgressBar, View.ALPHA, 0.0f);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.addListener(new AnimatorListenerAdapter() {

            private boolean mCanceled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                mCanceled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!mCanceled) {
                    mBinding.editProgressBar.setVisibility(View.GONE);
                }
            }
        });
        mAnimator.start();
    }

    // async task to gather certain data items (name, phone number & email) from a user's contact list via a cursor
    @SuppressLint("StaticFieldLeak")
    public class FetchContacts extends AsyncTask<Void, Void, List<Contact>> {

        private Context context;
        private List<ContactImportModel> mModels;
        private ContactImportModelAdapter mAdapter;

        FetchContacts(Context context, List<ContactImportModel> mModels, ContactImportModelAdapter mAdapter) {
            this.context = context;
            this.mModels = mModels;
            this.mAdapter = mAdapter;
        }

        @Override
        protected List<Contact> doInBackground(Void... params) {
            ArrayList<Contact> contacts = new ArrayList<>();
            ContentResolver contentResolver = context.getContentResolver();
            String[] projection = {ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};      // contact_id, display_name, data1
            Cursor cur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);

            try {
                while (cur != null && cur.moveToNext()) {           // use the cursor to gather contact info
                    long id = cur.getLong(0);
                    String name = cur.getString(1);
                    String number = cur.getString(2);
                    String email = "";

                    //Log.d(TAG_ANDROID_CONTACTS, "got " + id + ", " + name + ", " + number);

                    // get just the first email if there are more than one
                    Cursor emails = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id, null, null);
                    if ((emails != null) && (emails.getCount() > 0)) {
                        emails.moveToFirst();
                        email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        emails.close();
                    }

                    // put what we've found into a Contact object for addition to our list
                    Contact contact = new Contact();       //(name, number, email, 14, "", null, null, 1, null, null);
                    contact.setName(name);
                    contact.setPhone(number);
                    contact.setEmail(email);
                    contacts.add(contact);
                }
            }
            finally {
                if ((cur != null) && (cur.getCount() > 0)) {
                    cur.close();
                }
            }

            // sort contacts alphabetically
            SortContacts(contacts);

            // add our contacts to the mModels list as ContactImportModels
            for (int i = 0; i < contacts.size(); i++) {
                mModels.add(new ContactImportModel(i, i + 1, contacts.get(i).name + " (" + coalesce(contacts.get(i).phone, "no phone") + "; " + coalesce(contacts.get(i).email, "no email") + ")",
                        contacts.get(i).name, contacts.get(i).phone, contacts.get(i).email));
            }
            mAdapter.edit()                 // with mModels set up our adapter can use the list
                    .replaceAll(mModels)
                    .commit();
            contactsLoaded = true;
            return contacts;
        }

        @Override
        protected void onPostExecute(List list) {
            searchItem.setVisible(true);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == CONTACTS_LOADER_ID) {
            return contactsLoader();
        }
        return new Loader<>(context);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private Loader<Cursor> contactsLoader() {
        Uri contactsUri = ContactsContract.Contacts.CONTENT_URI; // The content URI of the phone contacts

        String[] projection = {                                  // The columns to return for each row
                ContactsContract.Contacts.DISPLAY_NAME
        } ;

        //String selection = null;                                 //Selection criteria
        String[] selectionArgs = {};                               //Selection criteria
        //String sortOrder = null;                                 //The sort order for the returned rows

        return new CursorLoader(
                context,
                contactsUri,
                projection,
                null,
                selectionArgs,
                null);
    }

    // helper method to sort contacts alphabetically
    public static void SortContacts(List<Contact> contactList) {
        Collections.sort(contactList, (c1, c2) -> {
            // don't put it in here
            return c1.getName().compareToIgnoreCase(c2.getName());
        });
        // put it here, after the sort
        //System.out.println(contactList);
    }

    public static String coalesce(String a, String b) {
        return (a.isEmpty()) ? b : a;
    }
}