package com.example.comterminal.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DropboxUploadDao {

    @Insert
    void insert(DropboxUpload upload);

    @Query("SELECT * FROM dropbox_uploads")
    List<DropboxUpload> getAllUploads();

    @Query("DELETE FROM dropbox_uploads")
    void deleteAllUploads();

    @Delete
    void delete(DropboxUpload upload);
}

