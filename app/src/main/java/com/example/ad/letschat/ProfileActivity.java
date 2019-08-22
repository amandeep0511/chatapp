package com.example.ad.letschat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    TextView mDisplayName, mStatus, mFriendsCount;
    ImageView mProfilePhoto;
    Button mFriendReqBtn;
    DatabaseReference mDatabase;
    DatabaseReference mFriendReqDatabase;
    DatabaseReference mFriends;
    DatabaseReference mNotificatonDatabase;
    DatabaseReference mRootRef;
    FirebaseUser mCurrUser;
    FirebaseAuth mAuth;
    String mCurr_State ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String curr_id = getIntent().getStringExtra("curr_uid");

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(curr_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Req");
        mFriends = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificatonDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mDisplayName = (TextView) findViewById(R.id.profile_display_name);
        mStatus = (TextView) findViewById(R.id.profile_status);
        mFriendsCount = (TextView) findViewById(R.id.profile_total_friends);
        mProfilePhoto = (ImageView) findViewById(R.id.profile_image);
        mFriendReqBtn = (Button) findViewById(R.id.profile_request_btn);

        mCurr_State = "not_friends";

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String dispName = dataSnapshot.child("name").getValue().toString();
                String profStatus = dataSnapshot.child("status").getValue().toString();
                String profImage = dataSnapshot.child("image").getValue().toString();

                mDisplayName.setText(dispName);
                mStatus.setText(profStatus);
                Picasso.get().load(profImage).placeholder(R.drawable.dummy).into(mProfilePhoto);

                //FriendList----- RequestFeature ----- //

                mFriendReqDatabase.child(mCurrUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(curr_id)){
                            String req_type = dataSnapshot.child(curr_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){
                                mCurr_State = "req_received";
                                mFriendReqBtn.setText("Accept Friend Request");
                            }else if(req_type.equals("sent")){
                                mCurr_State = "req_sent";
                                mFriendReqBtn.setText("Cancel Friend Request");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mFriends.child(mCurrUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(curr_id)){
                            mCurr_State = "friends";
                            mFriendReqBtn.setText("Unfriend this person");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFriendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFriendReqBtn.setEnabled(false);

                if(curr_id.equals(mCurrUser.getUid()))
                {
                    Toast.makeText(ProfileActivity.this, "You are trying to send a request to yourself", Toast.LENGTH_SHORT).show();
                    return;
                }

                //------------NOT FRIEND STATE---------------//

                if(mCurr_State.equals("not_friends")){

                    DatabaseReference mNotificationRef= mRootRef.child("notifications").child(curr_id).push();
                    String newNotificationId = mNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from",mCurrUser.getUid());
                    notificationData.put("type","request");

                    Map requestMap = new HashMap<>();
                    requestMap.put("Friend_Req/" + mCurrUser.getUid() + "/" + curr_id + "/request_type", "sent");
                    requestMap.put("Friend_Req/" + curr_id + "/" + mCurrUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + curr_id + "/" + newNotificationId,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Toast.makeText(ProfileActivity.this, "Some error occured", Toast.LENGTH_SHORT).show();
                            }
                            mFriendReqBtn.setEnabled(true);
                            mCurr_State = "req_sent";
                            mFriendReqBtn.setText("Cancel Friend Request");
                            Toast.makeText(ProfileActivity.this, "Request Sent successfully", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                //-------------CANCEL REQUEST STATE-------------//
                if(mCurr_State == "req_sent"){
                    mFriendReqDatabase.child(mCurrUser.getUid()).child(curr_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendReqDatabase.child(curr_id).child(mCurrUser.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mFriendReqBtn.setEnabled(true);
                                                    mCurr_State = "not_friends";
                                                    mFriendReqBtn.setText("Send Friend Request");
                                                    Toast.makeText(ProfileActivity.this, "Friend Request Canceled123", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                }
                            });
                }

                //---------RECEIVE REQUEST ------------//
                if(mCurr_State == "req_received"){
                    String currDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap<>();
                    friendsMap.put("Friends/"+ mCurrUser.getUid() +"/" + curr_id + "/date",currDate);
                    friendsMap.put("Friends/"+ curr_id +"/" + mCurrUser.getUid() + "/date",currDate);

                    friendsMap.put("Friend_Req/" + mCurrUser.getUid() + "/" + curr_id , null);
                    friendsMap.put("Friend_Req/"  + curr_id + "/" + mCurrUser.getUid() ,null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError==null){
                                    mFriendReqBtn.setEnabled(true);
                                    mCurr_State = "friends";
                                    mFriendReqBtn.setText("Unfriend this Person");

                                }else{
                                    String error = databaseError.getMessage();
                                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                                }
                        }
                    });
                }

                //------------ UNFRIEND-------------------//

                if(mCurr_State.equals("friends")){
                    mFriends.child(mCurrUser.getUid()).child(curr_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriends.child(curr_id).child(mCurrUser.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mFriendReqBtn.setEnabled(true);
                                                    mCurr_State = "not_friends";
                                                    mFriendReqBtn.setText("Send Friend Request");
                                                    Toast.makeText(ProfileActivity.this, "Not Friends", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                }
                            });
                }
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue(false);
    }
}
