package com.loya.githubdevs.service;

import com.loya.githubdevs.model.GithubUser;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GithubUserService {
    @GET("search/users")
    Call<GithubUser> getGithubUsers(@Query("q") String params);
}
