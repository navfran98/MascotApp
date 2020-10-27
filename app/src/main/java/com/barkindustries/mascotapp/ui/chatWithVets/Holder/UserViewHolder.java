package com.barkindustries.mascotapp.ui.chatWithVets.Holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import com.barkindustries.mascotapp.R;

public class UserViewHolder extends RecyclerView.ViewHolder {

    private CircleImageView civProfilePic;
    private TextView txtUserName;
    private LinearLayout principalLayout;
    private TextView circle;
    private TextView lastMessage;
    private ImageView originSymbol;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
        civProfilePic = itemView.findViewById(R.id.civProfilePic);
        txtUserName =itemView.findViewById(R.id.txtUsername);
        principalLayout = itemView.findViewById(R.id.principalLayout);
        circle = itemView.findViewById(R.id.dotNotification);
        originSymbol = itemView.findViewById(R.id.tinderFire);
    }

    public ImageView getOriginSymbol() {
        return originSymbol;
    }

    public void setOriginSymbol(ImageView originSymbol) {
        this.originSymbol = originSymbol;
    }

    // GETTERS
    public CircleImageView getCivProfilePic() {
        return civProfilePic;
    }
    public TextView getTxtUserName() {
        return txtUserName;
    }
    public LinearLayout getPrincipalLayout() {
        return principalLayout;
    }
    public TextView getCircle() {
        return circle;
    }
    public TextView getLastMessage() {
        return lastMessage;
    }

    // SETTERS
    public void setCivProfilePic(CircleImageView civProfilePic) {
        this.civProfilePic = civProfilePic;
    }
    public void setTxtUserName(TextView txtUserName) {
        this.txtUserName = txtUserName;
    }
    public void setPrincipalLayout(LinearLayout principalLayout) {
        this.principalLayout = principalLayout;
    }
    public void setCircle(TextView circle) {
        this.circle = circle;
    }
    public void setLastMessage(TextView lastMessage) {
        this.lastMessage = lastMessage;
    }
}
