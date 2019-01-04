package com.loya.githubdevs.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loya.githubdevs.R;
import com.loya.githubdevs.model.GitItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GithubAdapter extends RecyclerView.Adapter<GithubAdapter.ViewHolder> {
    private Context context;
    private List<GitItem> mUsers;
    private ListItemClickListener listItemClickListener;


    /**
     * an interface to handle click events on a card
     */
    public interface ListItemClickListener {
        void onListItemClick(int userId);
    }

    public GithubAdapter(Context context, ListItemClickListener listItemClickListener) {
        this.context = context;
        this.listItemClickListener = listItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mUsers != null) {
            GitItem item = mUsers.get(position);
            Picasso.get()
                    .load(item.getAvatarUrl())
                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                    .error(R.drawable.ic_error_outline_black_24dp)
                    .into(holder.mAvatar);
            holder.mUsername.setText(item.getLogin());
        } else {
            //Covers the case of data not being ready yet.
            holder.mUsername.setText("No user yet...");

        }

    }

    public void setUsers(List<GitItem> users) {
        mUsers = users;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mUsers != null ? mUsers.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mAvatar;
        private TextView mUsername;

        public ViewHolder(View itemView) {
            super(itemView);
            mAvatar = itemView.findViewById(R.id.profile_image);
            mUsername = itemView.findViewById(R.id.username_text);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            GitItem user = mUsers.get(getAdapterPosition());
            int userId = user.getId();
            listItemClickListener.onListItemClick(userId);
        }
    }


}
