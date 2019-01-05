package com.loya.githubdevs.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.loya.githubdevs.R;
import com.loya.githubdevs.adapter.GithubAdapter;
import com.loya.githubdevs.db.GitItem;
import com.loya.githubdevs.util.Resource;
import com.loya.githubdevs.viewmodel.UserProfileViewModel;

import java.util.List;


public class MainActivity extends AppCompatActivity implements GithubAdapter.ListItemClickListener {

    private RecyclerView mRecyclerView;
    private UserProfileViewModel mUserViewModel;
    public static final String USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUserViewModel = ViewModelProviders.of(this).get(UserProfileViewModel.class);

        //  initViews();
        mRecyclerView = findViewById(R.id.users_recycler);
        final GithubAdapter mAdapter = new GithubAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // call to perform fetch users from the network and save it to the db
        //mUserViewModel.insertUsers();


//        mUserViewModel.getmAllUsers().observe(this, new Observer<List<GitItem>>() {
//            @Override
//            public void onChanged(@Nullable List<GitItem> users) {
//                Toast.makeText(MainActivity.this, "@success", Toast.LENGTH_LONG).show();
//                mAdapter.setUsers(users);
//            }
//        });

        mUserViewModel.getmAllUsers().observe(this, new Observer<Resource<List<GitItem>>>() {
            @Override
            public void onChanged(@Nullable Resource<List<GitItem>> listResource) {
          //   Toast.makeText(MainActivity.this, listResource.message + "here msg", Toast.LENGTH_LONG).show();

              Toast.makeText(MainActivity.this, "" + listResource.status , Toast.LENGTH_LONG).show();
//                for(int i = 0; i< listResource.data.size(); i++){
//                    System.out.println("******************************");
//                    System.out.println("******************************");
//                    System.out.println("******************************");
//                    System.out.println(listResource.data.get(i).getLogin());
//                }
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
}

