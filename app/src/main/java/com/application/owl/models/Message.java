package com.application.owl.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// model class for messages with properties, Room annotations, and getters & setters
@Entity(tableName = "Message")
public class Message {

    @PrimaryKey(autoGenerate = true)
    public int message_id;

    @ColumnInfo(name = "body")
    public String body;

    public Message() {
    }

    // getters and setters
    public Integer getId() {
        return message_id;
    }

    public void setId(int id) {
        this.message_id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String name) {
        this.body = name;
    }
}