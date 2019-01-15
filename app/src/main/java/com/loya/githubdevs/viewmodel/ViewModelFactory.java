package com.loya.githubdevs.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.loya.githubdevs.repository.UserRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository mRepository;

    public ViewModelFactory(UserRepository mRepository) {
        this.mRepository = mRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(UserProfileViewModel.class))
            return (T) new UserProfileViewModel(mRepository);
        throw new IllegalArgumentException("Unknown ViewModel class");
    }


}

