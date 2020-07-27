package com.application.owl;

import androidx.annotation.NonNull;
import com.application.owl.adapters.ContactImportModelAdapter;
import com.application.owl.databinding.ContactImportModelBinding;
import com.application.owl.models.ContactImportModel;
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;

// view holder for ContactImportModel objects, which we bind to the RecyclerView in the ContactImport activity
public class ContactImportViewHolder extends SortedListAdapter.ViewHolder<ContactImportModel> {

    private final ContactImportModelBinding mBinding;

    // called from onCreateViewHolder in ContactImportModelAdapter
    public ContactImportViewHolder(ContactImportModelBinding binding, ContactImportModelAdapter.Listener listener) {
        super(binding.getRoot());
        binding.setListener(listener);
        mBinding = binding;
    }

    @Override
    protected void performBind(@NonNull ContactImportModel item) {
        mBinding.setModel(item);
    }
}
