package com.example.ad.letschat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    Toolbar mToolbar;
    DatabaseReference mRootRef;
    String curr_id;
    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfilePic;
    FirebaseAuth mAuth;
    String currentUserId;

    private EditText mChatText;
    private ImageButton mChatSend,mChatAdd;
    private RecyclerView mMessageList;
    private List<Messages> messagesList = new ArrayList<>();
    private MessageAdapter mAdapter;
    private LinearLayoutManager mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
         curr_id = getIntent().getStringExtra("curr_uid");
        mToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        mRootRef = FirebaseDatabase.getInstance().getReference();
       // Toast.makeText(this, curr_id, Toast.LENGTH_SHORT).show();

        String userName = getIntent().getStringExtra("curr_name");
        //Toast.makeText(this, userName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(userName);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        mTitleView = (TextView) findViewById(R.id.chat_display_name);
        mLastSeenView = (TextView) findViewById(R.id.chat_last_seen);
        mProfilePic = (CircleImageView) findViewById(R.id.chat_profile_pic);

        mChatAdd = (ImageButton) findViewById(R.id.chat_add);
        mChatSend = (ImageButton) findViewById(R.id.chat_send);
        mChatText = (EditText) findViewById(R.id.chat_text);
        mMessageList = (RecyclerView) findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);
        mAdapter = new MessageAdapter(messagesList);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setAdapter(mAdapter);

        loadMessages();

        mRootRef.child("Users").child(curr_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String onlineStatus = dataSnapshot.child("online").getValue().toString();
               // Toast.makeText(ChatActivity.this, onlineStatus, Toast.LENGTH_SHORT).show();
                String image = dataSnapshot.child("image").getValue().toString();

                if(onlineStatus.equals("true")){
                    mLastSeenView.setText("Online");

                }
                else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                   Long onlineSince = Long.parseLong(onlineStatus);
                    String lastTimeSeen = getTimeAgo.getTimeAgo(onlineSince,getApplicationContext());
                    mLastSeenView.setText(lastTimeSeen);
                }
                Picasso.get().load(image).placeholder(R.drawable.dummy).into(mProfilePic);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       mTitleView.setText(userName);


       mRootRef.child("Chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               if(!dataSnapshot.hasChild(curr_id)){
                   Map chatAddMap = new HashMap();
                   chatAddMap.put("seen",false);
                   chatAddMap.put("timestamp",ServerValue.TIMESTAMP);

                   Map chatUserMap = new HashMap();
                   chatUserMap.put("Chat/"+ currentUserId + "/" + curr_id, chatAddMap);
                   chatUserMap.put("Chat/"+ currentUserId  + "/" + currentUserId, chatAddMap);

                   mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                       @Override
                       public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                           if(databaseError!=null){
                               Log.e("Chat_Log",databaseError.getMessage().toString());
                           }
                       }
                   });
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });


       mChatSend.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               sendMessage();


           }
       });

    }

    private void loadMessages() {
        mRootRef.child("messages").child(currentUserId).child(curr_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                mAdapter.notifyDataSetChanged();
                mMessageList.scrollToPosition(messagesList.size()-1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        //Toast.makeText(ChatActivity.this, "jhgjg", Toast.LENGTH_SHORT).show();
        final String message = mChatText.getText().toString();

        if(!TextUtils.isEmpty(message)){
            String current_user_ref = "messages/" + currentUserId + "/" + curr_id;
            String chat_user_ref = "messages/" + curr_id + "/" + currentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(currentUserId).child(curr_id).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",currentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
            mChatText.setText("");
            //Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                    }else{
                        //Toast.makeText(ChatActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser curr_user = mAuth.getCurrentUser();
        String currentUserId = curr_user.getUid().toString();
            if(curr_user!=null)
            mRootRef.child("Users").child(currentUserId).child("online").setValue("true");



    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser curr_user = mAuth.getCurrentUser();
        String currentUserId = curr_user.getUid().toString();
        if(curr_user!=null)
            mRootRef.child("Users").child(currentUserId).child("online").setValue(ServerValue.TIMESTAMP);
    }
}
