package com.example.comterminal.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface DeviceDao {

    // Добавление устройства в базу данных
    @Insert
    void insert(Device device);

    // Получение всех устройств
    @Query("SELECT * FROM device")
    List<Device> getAllDevices();

    // Получение устройства по адресу
    @Query("SELECT * FROM device WHERE address = :address LIMIT 1")
    Device getDeviceByAddress(String address);

    // Удаление устройства
    @Delete
    void delete(Device device);

    // Очистка всей таблицы устройств
    @Query("DELETE FROM device")
    void deleteAll();
}
