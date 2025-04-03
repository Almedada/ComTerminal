package com.example.comterminal.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "devices")
public class Device {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;        // Имя устройства
    public String macAddress;  // MAC-адрес устройства

    // Конструкторы, геттеры и сеттеры, если нужно
    public Device(String name, String macAddress) {
        this.name = name;
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }
}
