package com.loya.githubdevs.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.loya.githubdevs.util.AppExecutors;
import com.loya.githubdevs.util.NetworkBoundResource;
import com.loya.githubdevs.util.RateLimiter;
import com.loya.githubdevs.util.Resource;
import com.loya.githubdevs.db.UserDao;
import com.loya.githubdevs.db.UserRoomDatabase;
import com.loya.githubdevs.db.GitItem;
import com.loya.githubdevs.db.GithubUser;
import com.loya.githubdevs.service.ApiResponse;
import com.loya.githubdevs.service.GithubUserService;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserRepository {
    private UserDao mUserDao;
    private LiveData<List<GitItem>> mAllUsers;
    private LiveData<GitItem> mUser;
    private GithubUserService githubUserService;
    private AppExecutors appExecutors;
    private static String LOG_TAG = UserRepository.class.getSimpleName();
    private RateLimiter<String> repoListRateLimit = new RateLimiter<>(1, TimeUnit.MINUTES);


    public UserRepository(Application application, GithubUserService githubUserService, AppExecutors appExecutors) {
        UserRoomDatabase db = UserRoomDatabase.getDatabase(application);
        mUserDao = db.userDao();
//        mAllUsers = mUserDao.getAllUsers();
        this.githubUserService = githubUserService;
        this.appExecutors = appExecutors;
    }

    /**
     * @return a list of users stored in the db if there is
     */
//    public LiveData<List<GitItem>> getAllUsers() {
//        LiveData<List<GitItem>> users = mAllUsers;
//        Log.d(LOG_TAG, "retrieved list of users from database successful");
//        return users;
//    }
    public LiveData<GitItem> getUser(int userId) {
        LiveData<GitItem> user = mUserDao.loadUser(userId);
        Log.d(LOG_TAG, "retrieved user from database successful");
        return user;
    }


    /**
     * method that makes the network call in a separate thread and inserts the result into the db
     */
//    public void insertUsers() {
//        // Executor executor = Executors.newCachedThreadPool();
//
//
//        appExecutors.networkIO().execute(new Runnable() {
//            //        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                Response<GithubUser> response = null;
//                try {
//
//                    response = githubUserService.getGithubUsers("language:java location:port-harcourt").execute();
//                    if (response.isSuccessful()) {
//                        GithubUser user = response.body();
//                        List<GitItem> users = user.getItems();
//                        System.out.println("***********************************");
//                        System.out.println("***********************************");
//                        System.out.println("***********************************");
//
//                        for (GitItem a : users) {
//                            System.out.println("id: " + a.getId() + " login: " + a.getLogin());
//                        }
//                        Log.d(LOG_TAG, "Fetched from network successfully");
//
//                        mUserDao.insertUsers(users);
//                        Log.d(LOG_TAG, "Inserted to database");
//                    } else {
//                        Log.d(LOG_TAG, "Failed to fetch from network and insert to db");
//                    }
//
//                } catch (IOException e) {
//                    Log.d(LOG_TAG, "Failed to fetch from network and insert to db: " + e.getLocalizedMessage());
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

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

//
//    public LiveData<List<GitItem>> getAllUsers() {
//        LiveData<List<GitItem>> users = mAllUsers;
//        Log.d(LOG_TAG, "retrieved list of users from database successful");
//        return users;
//    }


    public LiveData<Resource<List<GitItem>>> getAllUsers() {
        //ResultType, RequestType
        /**
         * List<GitItem> is the [ResultType]
         * GithubUser is the [RequestType]
         */
        return new NetworkBoundResource<List<GitItem>, GithubUser>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull GithubUser item) {
                Log.d(LOG_TAG, "call to insert results to db");
                mUserDao.insertUsers(item.getItems());
            }

            @Override
            protected boolean shouldFetch(@Nullable List<GitItem> data) {
                Log.d(LOG_TAG, "null?" + (data == null));
                Log.d(LOG_TAG, "empty? " + (data.isEmpty()));
                Log.d(LOG_TAG, "rate? " + (repoListRateLimit.shouldFetch("owner")));
                Log.d(LOG_TAG, "should fetch? " + (data.isEmpty() || repoListRateLimit.shouldFetch("owner")));
                return data.isEmpty() || data == null;
            }

            @NonNull
            @Override
            protected LiveData<List<GitItem>> loadFromDb() {
                Log.d(LOG_TAG, " call to load from db");
                return mUserDao.getAllUsers();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<GithubUser>> createCall() {
                Log.d(LOG_TAG, "creating a call to network");
                return githubUserService.getGithubUsers("language:java location:port-harcourt");
            }

            @Override
            protected GithubUser processResponse(ApiResponse<GithubUser> response) {
                return super.processResponse(response);
            }
        }.asLiveData();
    }
}
