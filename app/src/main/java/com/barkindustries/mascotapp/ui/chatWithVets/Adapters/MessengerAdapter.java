package com.barkindustries.mascotapp.ui.chatWithVets.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Logic.LMessage;
import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Logic.LUser;
import com.barkindustries.mascotapp.ui.chatWithVets.Holder.MessengerHolder;
import com.barkindustries.mascotapp.ui.chatWithVets.Persistence.UserDAO;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import com.barkindustries.mascotapp.R;

public class MessengerAdapter extends RecyclerView.Adapter<MessengerHolder> {

    private List<LMessage> messageList = new ArrayList<>();
    private Context c;

    public MessengerAdapter(Context c) {
        this.c = c;
    }

    public int addMessage(LMessage lMessage){
        messageList.add(lMessage);
        //3 mensajes
        int position = messageList.size()-1;//3
        notifyItemInserted(messageList.size());
        return position;
    }

    public void updateMessage(int position, LMessage lMessage){
        messageList.set(position, lMessage);//2
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public MessengerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == 1){
            view = LayoutInflater.from(c).inflate(R.layout.card_view_messages_emisor,parent,false);
        }else{
            view = LayoutInflater.from(c).inflate(R.layout.card_view_messages_receptor,parent,false);
        }
        return new MessengerHolder(view);
    }

    @Override
    public void onBindViewHolder(MessengerHolder holder, int position) {

        LMessage lMessage = messageList.get(position);

        LUser lUser = lMessage.getlUser();

        if(lUser!=null){
            holder.getName().setText(lUser.getUser().getUsernameText());
            Glide.with(c).load(lUser.getUser().getProfilePicURL()).into(holder.getMessageProfilePic());
        }

        holder.getMessage().setText(lMessage.getMessage().getMessage());
        if(lMessage.getMessage().isContainsPhoto()){
            holder.getMessagePicture().setVisibility(View.VISIBLE);
            holder.getMessage().setVisibility(View.VISIBLE);
            Glide.with(c).load(lMessage.getMessage().getUrlPic()).into(holder.getMessagePicture());
        }else {
            holder.getMessagePicture().setVisibility(View.GONE);
            holder.getMessage().setVisibility(View.VISIBLE);
        }

        holder.getHour().setText(lMessage.messageCreationDate());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(messageList.get(position).getlUser() != null){
            if(messageList.get(position).getlUser().getKey().equals(UserDAO.getInstance().getUserKey())){
                return 1;
            }else{
                return -1;
            }
        }else{
            return -1;
        }
        //return super.getItemViewType(position);
    }
}
