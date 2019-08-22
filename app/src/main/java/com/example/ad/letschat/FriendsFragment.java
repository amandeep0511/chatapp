package com.example.ad.letschat;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    RecyclerView mFriendsList;
    FirebaseAuth mAuth;
    DatabaseReference mFriendsDatabase;
    DatabaseReference mUserDatabase;
    String mCurrentUserId;
    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList =(RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,R.layout.user_single_layout,FriendsViewHolder.class,mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());
                final String list_user_id = getRef(position).getKey();

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName =dataSnapshot.child("name").getValue().toString();
                        final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String userOnlineStatus = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setOnlineStatusIcon(userOnlineStatus);

                        }

                        viewHolder.setName(userName);
                        viewHolder.setThumbImage(userThumb);
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                            if(i==0){
                                                Intent profileIntent  = new Intent(getContext(), ProfileActivity.class);
                                                profileIntent.putExtra("curr_uid",list_user_id);
                                                startActivity(profileIntent);
                                            }
                                            if(i==1){
                                                Intent chatIntent  = new Intent(getContext(), ChatActivity.class);
                                                chatIntent.putExtra("curr_uid",list_user_id);
                                                chatIntent.putExtra("curr_name",userName);
                                                chatIntent.putExtra("thumb_image",userThumb);
                                                startActivity(chatIntent);
                                            }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mFriendsList.setAdapter(friendsRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date) {
            TextView mStatus = (TextView) mView.findViewById(R.id.user_status);
            mStatus.setText(date);
        }

        public void setName(String name){
            TextView mName = (TextView) mView.findViewById(R.id.user_display_name);
            mName.setText(name);
        }
        public void setThumbImage(String thumbImage){
            CircleImageView mThumb = (CircleImageView) mView.findViewById(R.id.user_pic);
            Picasso.get().load(thumbImage).placeholder(R.drawable.dummy).into(mThumb);
        }

        public void setOnlineStatusIcon(String icon){
            ImageView mIcon = (ImageView) mView.findViewById(R.id.user_online_status);
            if(icon.equals("true")){
                mIcon.setVisibility(View.VISIBLE);
            }else{
                mIcon.setVisibility(View.INVISIBLE);
            }
        }
        @Override
        public void onClick(View view) {

        }
    }
}
