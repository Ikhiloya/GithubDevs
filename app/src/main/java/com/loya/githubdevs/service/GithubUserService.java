package com.loya.githubdevs.service;

import android.arch.lifecycle.LiveData;

import com.loya.githubdevs.db.GithubUser;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GithubUserService {

//     Call<Response<GithubUser> getGithubUsers(@Query("q") String params);
    @GET("search/users")
    LiveData<ApiResponse<GithubUser>> getGithubUsers(@Query("q") String params);

    @GET("search/users")
    Call<GithubUser> getUsers(@Query("q") String params);
}
