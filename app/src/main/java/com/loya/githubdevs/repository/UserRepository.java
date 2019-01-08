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
    private final UserDao mUserDao;
    private LiveData<List<GitItem>> mAllUsers;
    private LiveData<GitItem> mUser;
    private final GithubUserService githubUserService;
    private final AppExecutors appExecutors;
    private final Application application;
    private static String LOG_TAG = UserRepository.class.getSimpleName();
    private RateLimiter<String> repoListRateLimit = new RateLimiter<>(1, TimeUnit.MINUTES);


    public UserRepository(Application application, GithubUserService githubUserService, AppExecutors appExecutors) {
        this.application = application;
        UserRoomDatabase db = UserRoomDatabase.getDatabase(application);
        mUserDao = db.userDao();
        this.githubUserService = githubUserService;
        this.appExecutors = appExecutors;
    }

    public LiveData<GitItem> getUser(int userId) {
        LiveData<GitItem> user = mUserDao.loadUser(userId);
        Log.d(LOG_TAG, "retrieved user from database successful");
        return user;
    }

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


    public LiveData<Resource<List<GitItem>>> refreshUserData() {
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
                Log.d(LOG_TAG, "refreshUserData");
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<GitItem>> loadFromDb() {
                Log.d(LOG_TAG, "refreshUserData");
                Log.d(LOG_TAG, " call to load from db");
                return mUserDao.getAllUsers();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<GithubUser>> createCall() {
                Log.d(LOG_TAG, "refreshUserData");
                Log.d(LOG_TAG, "creating a call to network");
                return githubUserService.getGithubUsers("language:java location:port-harcourt");
            }

            @Override
            protected GithubUser processResponse(ApiResponse<GithubUser> response) {
                return super.processResponse(response);
            }
        }.asLiveData();
    }

    public Application getApplication() {
        return application;
    }
}
