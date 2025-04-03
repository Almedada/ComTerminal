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

    // Удаление устройства
    @Delete
    void delete(Device device);

    // Очистка всей таблицы устройств
    @Query("DELETE FROM device")
    void deleteAll();
}
