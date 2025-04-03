package com.example.comterminal.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface LogEntryDao {
    @Insert
    void insert(LogEntry logEntry);

    @Delete
    void delete(LogEntry logEntry); // Метод для удаления записи

    @Query("SELECT * FROM logs")
    List<LogEntry> getAllLogs();
}
