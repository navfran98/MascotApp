package com.barkindustries.mascotapp.uiSP.clients;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class ClientsAdapter extends RecyclerView.Adapter<ClientsAdapter.ClientsViewHolder> {

    private Context context;
    private List<String> clientUsernames;
    private List<String> clientRealNames;
    private List<String> clientProfilePicURLs;
    private List<String> clientPhones;

    public ClientsAdapter(Context context, List<String> clientUsernames, List<String> clientRealNames,
                          List<String> clientProfilePicURLs, List<String> clientPhones) {
        this.context = context;
        this.clientUsernames = clientUsernames;
        this.clientRealNames = clientRealNames;
        this.clientProfilePicURLs = clientProfilePicURLs;
        this.clientPhones = clientPhones;
    }

    @NonNull
    @Override
    public ClientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.client_item_layout, parent, false);
        return new ClientsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientsViewHolder holder, int position) {

        // Set username (always present)
        holder.clientUsername.setText(clientUsernames.get(position));

        // Set RealName
        if(!clientRealNames.get(position).equals("null"))
            holder.clientRealNameData.setText(clientRealNames.get(position));
        else
            holder.clientRealNameData.setText("<Unavailable>");

        // Set phone
        if(!clientPhones.get(position).equals("null"))
            holder.clientPhoneData.setText(clientPhones.get(position));
        else
            holder.clientPhoneData.setText("<Unavailable>");

        // Set profilePic (always present)
        Glide.with(context).load(clientProfilePicURLs.get(position)).into(holder.clientProfilePic);
    }

    @Override
    public int getItemCount() {
        return clientUsernames.size();
    }

    public static class ClientsViewHolder extends RecyclerView.ViewHolder {
        private TextView clientUsername, clientRealNameData, clientPhoneData;
        private ImageView clientProfilePic;

        public ClientsViewHolder(@NonNull View itemView) {
            super(itemView);
            clientUsername = itemView.findViewById(R.id.client_username);
            clientRealNameData = itemView.findViewById(R.id.client_realName_data);
            clientPhoneData = itemView.findViewById(R.id.client_phone_data);
            clientProfilePic = itemView.findViewById(R.id.client_profile_pic);
        }
    }
}
