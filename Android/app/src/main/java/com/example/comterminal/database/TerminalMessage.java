package com.example.comterminal.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "terminal_messages")
public class TerminalMessage {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String message;
    public long timestamp;

    public TerminalMessage(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }
}
