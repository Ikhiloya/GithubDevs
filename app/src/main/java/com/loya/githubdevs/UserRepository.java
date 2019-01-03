package com.loya.githubdevs;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.loya.githubdevs.adapter.GithubAdapter;
import com.loya.githubdevs.model.GitItem;
import com.loya.githubdevs.model.GithubUser;
import com.loya.githubdevs.service.GithubUserService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class UserRepository {
    private UserDao mUserDao;
    private LiveData<List<GitItem>> mAllUsers;
    private GithubUserService githubUserService;
    private static String LOG_TAG = UserRepository.class.getSimpleName();


    UserRepository(Application application, GithubUserService githubUserService) {
        UserRoomDatabase db = UserRoomDatabase.getDatabase(application);
        mUserDao = db.userDao();
        mAllUsers = mUserDao.getAllUsers();
        this.githubUserService = githubUserService;
    }

    /**
     *
     * @return a list of users stored in the db if there is
     */
    public LiveData<List<GitItem>> getAllUsers() {
        LiveData<List<GitItem>> users = mAllUsers;
        Log.d(LOG_TAG, "retrieved from database successful");
        return users;
    }

    /**
     * method that makes the network call in a separate thread and inserts the result into the db
     */
    public void insertUsers() {
        Executor executor = Executors.newCachedThreadPool();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                Response<GithubUser> response = null;
                try {

                    response = githubUserService.getGithubUsers("language:java location:port-harcourt").execute();
                    if (response.isSuccessful()) {
                        GithubUser user = response.body();
                        List<GitItem> users = user.getItems();
                        Log.d(LOG_TAG, "Fetched from network successfully");

                        mUserDao.insertUsers(users);
                        Log.d(LOG_TAG, "Inserted to database");
                    } else {
                        Log.d(LOG_TAG, "Failed to fetch from network and insert to db");
                    }

                } catch (IOException e) {
                    Log.d(LOG_TAG, "Failed to fetch from network and insert to db: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // [REMOVE]-- ASYNCTASK used initially to insert the result of
    // the network call to the db in a separate thread
    private static class insertAsyncTask extends AsyncTask<List<GitItem>, Void, Void> {

        private UserDao mAsyncTaskDao;

        insertAsyncTask(UserDao dao) {
            mAsyncTaskDao = dao;
        }


        @Override
        protected Void doInBackground(List<GitItem>... lists) {

            mAsyncTaskDao.insertUsers(lists[0]);
            return null;
        }
    }


}
