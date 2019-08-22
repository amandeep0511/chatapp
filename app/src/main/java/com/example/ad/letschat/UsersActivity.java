package com.example.ad.letschat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    Toolbar mToolbar;
    RecyclerView mRecyclerView;

    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    private EditText mSearchField;
    private ImageButton mSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth=FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.users_appbar);
        mSearch = (ImageButton) findViewById(R.id.search_imagebutton);
        mSearchField = (EditText) findViewById(R.id.user_search);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.all_users);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchContent = mSearchField.getText().toString();
                loadUsers(searchContent);
            }
        });

        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchContent = mSearchField.getText().toString();
                loadUsers(searchContent);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

//        FirebaseUser curr_user = mAuth.getCurrentUser();
//        String curr_user_id= curr_user.getUid().toString();
//        if(curr_user!=null){
//            mDatabase.child(curr_user_id).child("online").setValue("true");
//        }

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,R.layout.user_single_layout,UsersViewHolder.class, mDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setThumbImage(model.getThumb_image());

                final String uid = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent  = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("curr_uid",uid);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public void loadUsers(String searchContent){

        Query firebaseSearchQuery = mDatabase.orderByChild("name").startAt(searchContent).endAt(searchContent + "\uf8ff");
        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,R.layout.user_single_layout,UsersViewHolder.class,firebaseSearchQuery
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setThumbImage(model.getThumb_image());

                final String uid = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent  = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("curr_uid",uid);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){
            TextView mName = (TextView) mView.findViewById(R.id.user_display_name);
            mName.setText(name);
        }

        public void setStatus(String status){
            TextView mStatus = (TextView) mView.findViewById(R.id.user_status);
            mStatus.setText(status);
        }

        public void setThumbImage(String thumbImage){
            CircleImageView mThumb = (CircleImageView) mView.findViewById(R.id.user_pic);
            Picasso.get().load(thumbImage).placeholder(R.drawable.dummy).into(mThumb);
        }
    }
}
