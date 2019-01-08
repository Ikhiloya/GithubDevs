package com.loya.githubdevs;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loya.githubdevs.adapter.LiveDataCallAdapterFactory;
import com.loya.githubdevs.service.GithubUserService;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class GithubApplication extends Application {
    public static final String BASE_URL = "https://api.github.com";

    private GithubUserService githubUserService;
    private Picasso picasso;


    public static GithubApplication get(Activity activity) {
        return (GithubApplication) activity.getApplication();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        //Gson Builder
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Timber.plant(new Timber.DebugTree());

        // HttpLoggingInterceptor
        HttpLoggingInterceptor httpLoggingInterceptor = new
                HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(@NonNull String message) {
                Timber.i(message);
            }
        });

        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


        // OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .addInterceptor(httpLoggingInterceptor)
                .build();


        /**
         *  Used if you want to use the OkHttp3Downloader
         *
         picasso instance using an OkHttp3Downloader
         picasso = new Picasso.Builder(this)
         .downloader(new OkHttp3Downloader(okHttpClient))
         .build();
         */

        picasso = Picasso.get();

        //Retrofit
        Retrofit mRetrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // GithubUserService
        githubUserService = mRetrofit.create(GithubUserService.class);

    }

    public GithubUserService getGithubUserService() {
        return githubUserService;
    }

    public Picasso getPicasso() {
        return picasso;
    }
}
