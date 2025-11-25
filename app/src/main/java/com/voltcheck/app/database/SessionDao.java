package com.voltcheck.app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.voltcheck.app.models.SessionEntity;

import java.util.List;

/**
 * Data Access Object untuk operasi database Session
 */
@Dao
public interface SessionDao {
    
    @Insert
    long insert(SessionEntity session);
    
    @Update
    void update(SessionEntity session);
    
    @Delete
    void delete(SessionEntity session);
    
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    List<SessionEntity> getAllSessions();
    
    @Query("SELECT * FROM sessions WHERE id = :id")
    SessionEntity getSessionById(long id);
    
    @Query("DELETE FROM sessions")
    void deleteAll();
}
