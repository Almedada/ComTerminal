package com.example.comterminal.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(tableName = "logs",
        foreignKeys = @ForeignKey(entity = Device.class, parentColumns = "id", childColumns = "device_id"))
public class LogEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int device_id;
    public long timestamp;
    public String status; // "подключено", "отключено", "ошибка"

    public LogEntry(int device_id, long timestamp, String status) {
        this.device_id = device_id;
        this.timestamp = timestamp;
        this.status = status;
    }
}
