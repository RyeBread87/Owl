package com.application.owl.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.application.owl.ContactImportViewHolder;
import com.application.owl.databinding.ContactImportModelBinding;
import com.application.owl.models.ContactImportModel;
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import java.util.Comparator;

// ContactImportModel adapter - takes care of binding phone contacts (via ContactImportModel instances) to the RecyclerView in the ContactImport activity
public class ContactImportModelAdapter extends SortedListAdapter<ContactImportModel> {

    private Cursor mCursor;
    private final Listener mListener;

    public interface Listener {
        void onContactImportModelClicked(ContactImportModel model);
    }

    public ContactImportModelAdapter(Context context, Comparator<ContactImportModel> comparator, Listener listener) {
        super(context, ContactImportModel.class, comparator);
        mListener = listener;
    }

    // Create a ViewHolder for each item in the RecyclerView
    @NonNull
    @Override
    protected ViewHolder<? extends ContactImportModel> onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        final ContactImportModelBinding binding = ContactImportModelBinding.inflate(inflater, parent, false);
        return new ContactImportViewHolder(binding, mListener);
    }

    public void swapCursor(Cursor newCursor) {
/*        boolean mDataValid;
        int mRowIDColumn;*/

        if (newCursor == mCursor) {
            return;
        }

        if (newCursor != null) {
            mCursor = newCursor;
            //mDataValid = true;
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            notifyItemRangeRemoved(0, getItemCount());
            mCursor = null;
/*            mRowIDColumn = -1;
            mDataValid = false;*/
        }
    }
}