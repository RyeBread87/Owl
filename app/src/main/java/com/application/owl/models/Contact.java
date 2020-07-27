package com.application.owl.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.application.owl.OwlDatabase;
import java.io.Serializable;

// model class for contacts with properties, Room annotations, and getters & setters
@Entity(tableName = OwlDatabase.TABLE_NAME_CONTACTS)
public class Contact implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int contact_id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "country_code")
    public String countryCode;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "frequency")
    public int frequency;

    @ColumnInfo(name = "start_date")
    public long startDate;

    @ColumnInfo(name = "post_snooze_date")
    public long postSnoozeDate;

    @ColumnInfo(name = "preference")
    public String preference;

    @ColumnInfo(name = "facebook_uri")
    public String facebookURI;

    @ColumnInfo(name = "skype_name")
    public String skypeName;

    @ColumnInfo(name = "status")
    public int status;

    @ColumnInfo(name = "owl_count")
    public int owlCount;

    public Contact() {
    }

    // getters and setters
    public Integer getId() {
        return contact_id;
    }

    public void setId(int id) {
        this.contact_id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFrequency(int frequency) {this.frequency = frequency; }

    public Integer getFrequency() { return this.frequency; }

    public void setStartDate(long startDate) {this.startDate = startDate; }

    public long getStartDate() {return this.startDate; }

    public void setPostSnoozeDate(long postSnoozeDate) {this.postSnoozeDate = postSnoozeDate; }

    //public long getPostSnoozeDate() {return this.postSnoozeDate; }

    public void setPreference(String preference) {this.preference = preference; }

    public String  getPreference() { return this.preference; }

    public void setFacebookURI(String facebookURI) {this.facebookURI = facebookURI; }

    public String getFacebookURI() { return this.facebookURI; }

    public void setSkypeName(String skypeName) {this.skypeName = skypeName; }

    public String getSkypeName() { return this.skypeName; }

    public void setStatus(int status) {this.status = status; }

    public Integer getStatus() { return this.status; }

    public void setCountryCode(String countryCode) {this.countryCode = countryCode; }

    public String getCountryCode() { return this.countryCode; }

    public void setOwlCount(int owlCount) {this.owlCount = owlCount; }

    public int getOwlCount() { return this.owlCount; }
}
