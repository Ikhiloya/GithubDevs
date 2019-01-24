package com.loya.githubdevs.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.loya.githubdevs.R;
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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;
import timber.log.Timber;

public class UserRepository {
    private static UserDao mUserDao;
    private LiveData<List<GitItem>> mAllUsers;
    private LiveData<GitItem> mUser;
    private static GithubUserService githubUserService;
    private final AppExecutors appExecutors;
    private final Application application;
    private static String LOG_TAG = UserRepository.class.getSimpleName();
    private RateLimiter<String> repoListRateLimit = new RateLimiter<>(1, TimeUnit.MINUTES);
    private static final String REQUEST_URL = "language:java location:port-harcourt";


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

    public LiveData<Resource<List<GitItem>>> getAllUsers(boolean isRefresh) {
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
                Log.d(LOG_TAG, "isRefresh?" + isRefresh);
                Log.d(LOG_TAG, "empty? " + (data.isEmpty()));
                Log.d(LOG_TAG, "rate? " + (repoListRateLimit.shouldFetch("owner")));
                Timber.d("should fetch? " + (data.isEmpty() || isRefresh || data == null));
                return data.isEmpty() || isRefresh || data == null;

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

    /**
     * method that makes the network call in a separate thread and inserts the result into the db
     */

//    public int insertUsers() {
//      new AppExecutors().networkIO().execute(new Runnable() {
//            @Override
//            public void run() {
//                Response<GithubUser> response = null;
//                try {
//
//                    response = githubUserService.getUsers("language:java location:port-harcourt").execute();
//                    if (response.isSuccessful()) {
//                        GithubUser user = response.body();
//                        List<GitItem> users = user.getItems();
//                        Log.d(LOG_TAG, "Fetched from network successfully");
//
//                        mUserDao.insertUsers(users);
//                        Log.d(LOG_TAG, "Inserted to database");
//                        System.out.println("*************resp code before "+ respCode);
//                       respCode= 1;
//                        System.out.println("*************resp code after "+ respCode);
//
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
//      return respCode;
//    }
//    public int insertUsers() {
//        int x = 0;
//
//        ExecutorService executor = Executors.newCachedThreadPool();
//        Callable<Integer> callable = new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                Response<GithubUser> response = null;
//                try {
//
//                    response = githubUserService.getUsers("language:java location:port-harcourt").execute();
//                    if (response.isSuccessful()) {
//                        GithubUser user = response.body();
//                        List<GitItem> users = user.getItems();
//                        Log.d(LOG_TAG, "Fetched from network successfully");
//
//                        mUserDao.insertUsers(users);
//                        Log.d(LOG_TAG, "Inserted to database");
//                        System.out.println("*************resp code before " + respCode);
//                        return 1;
//
//                    } else {
//                        Log.d(LOG_TAG, "Failed to fetch from network and insert to db");
//                        return 0;
//                    }
//
//                } catch (IOException e) {
//                    Log.d(LOG_TAG, "Failed to fetch from network and insert to db: " + e.getLocalizedMessage());
//                    e.printStackTrace();
//                }
//                return 0;
//            }
//        };
//
//        Future<Integer> future = executor.submit(callable);
//        try {
//            x = future.get(); //returns 2 or raises an exception if the thread dies, so safer
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        executor.shutdown();
//        return x;
//    }
//    public GithubLoader insertUsers(Context context) {
//        return  GithubLoader(context);
//    }


    public static class GithubLoader extends android.support.v4.content.AsyncTaskLoader<Boolean> {
        Boolean status;

        public GithubLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            Log.v(LOG_TAG, this.getContext().getString(R.string.in_onStartLoading));
            Log.v(LOG_TAG, "status: " + status);
            if (status != null) {
                //To skip loadInBackground call
                deliverResult(status);
            } else {
                forceLoad();
            }
        }


        @Nullable
        @Override
        public Boolean loadInBackground() {
            Boolean isSuccess = false;
            Log.v(LOG_TAG, this.getContext().getString(R.string.in_LoadInBackground));

            Response<GithubUser> response = null;
            try {

                response = githubUserService.getUsers(REQUEST_URL).execute();
                isSuccess = response.isSuccessful();

                if (isSuccess) {
                    GithubUser user = response.body();
                    List<GitItem> users = user.getItems();
                    Log.d(LOG_TAG, this.getContext().getString(R.string.network_success_string));

                    mUserDao.insertUsers(users);
                    Log.d(LOG_TAG, "Inserted to database");
                    Log.d(LOG_TAG, "isSuccess: " + isSuccess);
                    Log.v(LOG_TAG, "status: " + status);

                    status = isSuccess;
                    Log.v(LOG_TAG, "status: " + status);

                    return status;

                } else {
                    Log.d(LOG_TAG, "Failed to fetch from network and insert to db");
                    Log.d(LOG_TAG, "isSuccess: " + isSuccess);
                    status = isSuccess;
                    return status;
                }

            } catch (IOException e) {
                Log.d(LOG_TAG, "Failed to fetch from network and insert to db: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
            Log.d(LOG_TAG, "isSuccess: " + isSuccess);
            status = isSuccess;
            return status;
        }
    }


}
