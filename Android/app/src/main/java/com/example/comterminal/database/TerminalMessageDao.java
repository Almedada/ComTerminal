package com.example.comterminal.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TerminalMessageDao {
    @Insert
    void insert(TerminalMessage message);

    @Query("SELECT * FROM terminal_messages ORDER BY timestamp DESC")
    List<TerminalMessage> getAllMessages();

    // Добавляем метод для очистки базы данных
    @Query("DELETE FROM terminal_messages")
    void deleteAll();

    // Метод для удаления записи по ID
    @Query("DELETE FROM terminal_messages WHERE id = :messageId")
    void deleteById(int messageId);
}
