package com.application.owl.models;

import androidx.annotation.NonNull;
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;
import java.io.Serializable;
import java.util.Objects;

// model class for contact imports with fields used by the ContactImportModelAdapter and the ContactImport activity;
// each ContactImportModel fills out a row in the ContactImport activity's recyclerview for selection & import
public class ContactImportModel implements SortedListAdapter.ViewModel, Serializable {

    private final long mId;
    private final int mRank;
    private final String mText;
    public final String name;
    public final String phone;
    public final String email;

    public ContactImportModel(long id, int rank, String text, String name, String phone, String email) {
        mId = id;
        mRank = rank;
        mText = text;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public long getId() {
        return mId;
    }

    public int getRank() {
        return mRank;
    }

    public String getText() {
        return mText;
    }

    // method to see if two ContactImportModels are equal (i.e. have the same ID and text)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactImportModel model = (ContactImportModel) o;
        if (mId != model.mId) return false;
        return Objects.equals(mText, model.mText);
    }

    // generates a hash code for a ContactImportModel
    @Override
    public int hashCode() {
        int result = (int) (mId ^ (mId >>> 32));
        result = 31 * result + (mText != null ? mText.hashCode() : 0);
        return result;
    }

    // method to compare one ContactImportModel to another to see if they're the same model (i.e. have the same ID)
    @Override
    public <T> boolean isSameModelAs(@NonNull T item) {
        if (item instanceof ContactImportModel) {
            final ContactImportModel contactImportModel = (ContactImportModel) item;
            return contactImportModel.mId == mId;
        }
        return false;
    }

    // comparison method to see if two ContactImportModels have the same rank & text
    @Override
    public <T> boolean isContentTheSameAs(@NonNull T item) {
        if (item instanceof ContactImportModel) {
            final ContactImportModel other = (ContactImportModel) item;
            if (mRank != other.mRank) {
                return false;
            }
            return Objects.equals(mText, other.mText);
        }
        return false;
    }
}