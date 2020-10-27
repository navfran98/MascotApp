package com.barkindustries.mascotapp.ui.chatWithVets.Holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessengerHolder extends RecyclerView.ViewHolder {

    private TextView name;
    private TextView message;
    private TextView hour;
    private CircleImageView messageProfilePic;
    private ImageView messagePicture;

    public MessengerHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.nameMessage);
        message = (TextView) itemView.findViewById(R.id.messageMessage);
        hour = (TextView) itemView.findViewById(R.id.hourMessage);
        messageProfilePic = (CircleImageView) itemView.findViewById(R.id.profilePicMessage);
        messagePicture = (ImageView) itemView.findViewById(R.id.messagePicture);
    }

    // GETTERS
    public TextView getName() {
        return name;
    }
    public TextView getMessage() {
        return message;
    }
    public TextView getHour() {
        return hour;
    }
    public CircleImageView getMessageProfilePic() {
        return messageProfilePic;
    }
    public ImageView getMessagePicture() {
        return messagePicture;
    }

    // SETTERS
    public void setName(TextView name) {
        this.name = name;
    }
    public void setMessage(TextView message) {
        this.message = message;
    }
    public void setHour(TextView hour) {
        this.hour = hour;
    }
    public void setMessageProfilePic(CircleImageView messageProfilePic) {
        this.messageProfilePic = messageProfilePic;
    }
    public void setMessagePicture(ImageView messagePicture) {
        this.messagePicture = messagePicture;
    }
}
