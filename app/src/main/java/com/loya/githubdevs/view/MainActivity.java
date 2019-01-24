package com.loya.githubdevs.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.loya.githubdevs.GithubApplication;
import com.loya.githubdevs.R;
import com.loya.githubdevs.adapter.GithubAdapter;
import com.loya.githubdevs.db.GitItem;
import com.loya.githubdevs.repository.UserRepository;
import com.loya.githubdevs.service.GithubUserService;
import com.loya.githubdevs.util.AppExecutors;

import com.loya.githubdevs.util.Resource;
import com.loya.githubdevs.viewmodel.UserProfileViewModel;
import com.loya.githubdevs.viewmodel.ViewModelFactory;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements GithubAdapter.ListItemClickListener,
        LoaderManager.LoaderCallbacks<Boolean> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private UserProfileViewModel mUserViewModel;
    public static final String USER_ID = "userId";
    private ConnectivityManager cm;
    private boolean isConnected;
    private UserRepository mRepository;
    private GithubUserService mGithubUserService;

    private NetworkInfo activeNetwork;
    private Picasso mPicasso;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private static final int LOADER_ID = 88;
    private LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout = findViewById(R.id.swiperefresh_layout);
        mGithubUserService = GithubApplication.get(MainActivity.this).getGithubUserService();
        mPicasso = GithubApplication.get(MainActivity.this).getPicasso();

        mRepository = new UserRepository(getApplication(), mGithubUserService, new AppExecutors());
        loaderManager = getSupportLoaderManager();

        // the factory and its dependencies instead should be injected with DI framework like Dagger
        ViewModelFactory factory = new ViewModelFactory(mRepository);

        mUserViewModel = ViewModelProviders.of(this, factory).get(UserProfileViewModel.class);

        //  initViews;
        mRecyclerView = findViewById(R.id.users_recycler);
        final GithubAdapter mAdapter = new GithubAdapter(this, this, mPicasso);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        getUsers(mAdapter, false); // refactor boolean


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //check if there is a network connection
                activeNetwork = cm.getActiveNetworkInfo();
                isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                    // This method performs the actual data-refresh operation.
                    // The method calls setRefreshing(false) when it's finished.
                    refreshData();
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(Objects.requireNonNull(getCurrentFocus()), "no connection", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

    }

    private void getUsers(GithubAdapter mAdapter, boolean isRefresh) {
        mUserViewModel.getAllUsers(isRefresh).observe(this, new Observer<Resource<List<GitItem>>>() {
            @Override
            public void onChanged(@Nullable Resource<List<GitItem>> listResource) {
                Log.i(LOG_TAG, "LiveData called from ViewModel");
                Toast.makeText(MainActivity.this, "" + listResource.status, Toast.LENGTH_LONG).show();
                mAdapter.setUsers(listResource.data);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(int userId) {
        Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
        detailIntent.putExtra(USER_ID, userId);
        startActivity(detailIntent);
    }

    private void refreshData() {
        Log.i(LOG_TAG, "call to refresh data");
        loaderManager.restartLoader(LOADER_ID, null, MainActivity.this);
    }


    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        return new UserRepository.GithubLoader(MainActivity.this);
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean isSuccess) {
        if (isSuccess) {
            Log.i(LOG_TAG, "call to destroy Loader");
            loaderManager.destroyLoader(LOADER_ID);
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(MainActivity.this, "Failed to fetch data, try again!!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }
}

