package com.loya.githubdevs;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.loya.githubdevs.model.GitItem;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * from user_table order by login asc")
    LiveData<List<GitItem>> getAllUsers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsers(List<GitItem> users);
}
