package com.loya.githubdevs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loya.githubdevs.model.GitItem;
import com.loya.githubdevs.model.GithubUser;
import com.loya.githubdevs.service.GithubUserService;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;


public class UserProfileViewModel extends AndroidViewModel {
    public static final String BASE_URL = "https://api.github.com";

    private UserRepository mRepository;


    private LiveData<List<GitItem>> mAllUsers;


    public UserProfileViewModel(@NonNull Application application) {
        super(application);
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Timber.plant(new Timber.DebugTree());

        HttpLoggingInterceptor httpLoggingInterceptor = new
                HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(@NonNull String message) {
                Timber.i(message);
            }
        });

        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .addInterceptor(httpLoggingInterceptor)
                .build();

       Retrofit mRetrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

       GithubUserService githubUserService =  mRetrofit.create(GithubUserService.class);

        mRepository = new UserRepository(application, githubUserService);
        mAllUsers = mRepository.getAllUsers();
    }

    LiveData<List<GitItem>> getmAllUsers() {
        return mAllUsers;
    }

    public void insertUsers() {
        mRepository.insertUsers();
    }
}