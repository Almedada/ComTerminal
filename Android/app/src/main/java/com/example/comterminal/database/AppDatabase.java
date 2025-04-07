package com.example.comterminal.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {TerminalMessage.class, Device.class, LogEntry.class, DropboxUpload.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // Абстрактные методы для получения DAO
    public abstract TerminalMessageDao terminalMessageDao();
    public abstract DeviceDao deviceDao();
    public abstract LogEntryDao logEntryDao();
    public abstract DropboxUploadDao dropboxUploadDao();

    // Метод для получения экземпляра базы данных с применением паттерна Singleton
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {

                    // Удаление старой базы данных перед созданием новой
                    // context.deleteDatabase("terminal_database");

                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "device_database")
                            .fallbackToDestructiveMigration()  // Очистка старой базы данных
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
