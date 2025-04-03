package com.example.comterminal.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(tableName = "dropbox_uploads",
        foreignKeys = @ForeignKey(entity = TerminalMessage.class, parentColumns = "id", childColumns = "message_id"))
public class DropboxUpload {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int message_id;
    public long timestamp;
    public String status; // "Успешно", "Ошибка"

    public DropboxUpload(int message_id, long timestamp, String status) {
        this.message_id = message_id;
        this.timestamp = timestamp;
        this.status = status;
    }
}
