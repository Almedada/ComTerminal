package com.example.comterminal.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface DeviceDao {

    @Insert
    void insert(Device device);

    @Delete
    void delete(Device device);

    @Query("SELECT * FROM devices")
    List<Device> getAllDevices();

    @Query("DELETE FROM devices")  // Метод для удаления всех устройств
    void deleteAll();  // Удаление всех устройств
}
