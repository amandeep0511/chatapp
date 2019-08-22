package com.example.ad.letschat;

import android.graphics.Color;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mCurrentUser = mAuth.getCurrentUser();
        String currentUserId = mCurrentUser.getUid().toString();



        Messages c = mMessageList.get(position);
        if(currentUserId.equals(c.getFrom())){
            holder.messageText.setBackgroundResource(R.drawable.background_chat_admin);

            holder.messageText.setTextColor(Color.BLACK);

            holder.messageContainer.setGravity(Gravity.RIGHT);
            holder.messageContainer.setPadding(0,0,5,0);
            holder.profileImage.setVisibility(View.INVISIBLE);
        }else{
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);
        }
        holder.messageText.setText(c.getMessage());

    }



    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public CircleImageView profileImage;
        public RelativeLayout messageContainer;
        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.message_text_layout);
           profileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_layout);
           messageContainer = (RelativeLayout) itemView.findViewById(R.id.message_container);
        }
    }
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
