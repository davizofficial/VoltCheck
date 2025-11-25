package com.voltcheck.app.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.voltcheck.app.models.SessionEntity;

/**
 * Room Database untuk VoltCheck
 */
@Database(entities = {SessionEntity.class}, version = 1, exportSchema = false)
public abstract class SessionDatabase extends RoomDatabase {
    
    private static SessionDatabase instance;
    
    public abstract SessionDao sessionDao();
    
    public static synchronized SessionDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    SessionDatabase.class,
                    "voltcheck_database"
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
}
