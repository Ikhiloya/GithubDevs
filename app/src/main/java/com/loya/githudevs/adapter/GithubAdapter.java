package com.loya.githudevs.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loya.githudevs.R;
import com.loya.githudevs.model.GitItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GithubAdapter extends RecyclerView.Adapter<GithubAdapter.ViewHolder> {
    private Context context;
    private List<GitItem> items;

    public GithubAdapter(Context context, List<GitItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GitItem item = items.get(position);;
        Picasso.get()
                .load(item.getAvatarUrl())
                .into(holder.mAvatar);
        holder.mUsername.setText(item.getLogin());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mAvatar;
        private TextView mUsername;

        public ViewHolder(View itemView) {
            super(itemView);

            mAvatar = itemView.findViewById(R.id.profile_image);
            mUsername = itemView.findViewById(R.id.username_text);
        }
    }

}
