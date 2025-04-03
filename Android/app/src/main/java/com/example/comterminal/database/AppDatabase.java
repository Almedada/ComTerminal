package com.example.comterminal.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {TerminalMessage.class, Device.class, LogEntry.class, DropboxUpload.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract TerminalMessageDao terminalMessageDao();
    public abstract DeviceDao deviceDao();
    public abstract LogEntryDao logEntryDao();
    public abstract DropboxUploadDao dropboxUploadDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "terminal_database")
                            .fallbackToDestructiveMigration() // Очистка данных при изменении схемы
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
