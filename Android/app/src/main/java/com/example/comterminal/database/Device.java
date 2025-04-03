package com.example.comterminal.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity(tableName = "device")
public class Device {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String address;
    private long timestamp; // Добавлено поле для времени

    // Конструктор с тремя аргументами
    public Device(String name, String address, long timestamp) {
        this.name = name;
        this.address = address;
        this.timestamp = timestamp;
    }

    // Геттеры и сеттеры для name, address, и timestamp
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timestamp);
        return sdf.format(date);
    }
}
