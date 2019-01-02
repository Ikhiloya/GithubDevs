package com.loya.githudevs.service;

import com.loya.githudevs.model.GithubUser;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GithubUserService {


//    @GET("users?q=location:port-harcourt+language:java")
//    Call<GithubUser> getGithubUsers();


    @GET("search/users")
    Call<GithubUser> getGithubUsers(@Query("q") String params);
}
