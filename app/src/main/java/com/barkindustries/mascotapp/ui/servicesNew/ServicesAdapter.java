package com.barkindustries.mascotapp.ui.servicesNew;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.R;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.List;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ServicesViewHolder> {

    private Context context;
    private List<String> servicesIds;
    private List<String> servicesTypes;
    private Services currentFragment;
    private DatabaseReference mDatabase;


    public ServicesAdapter(Context context, List<String> ids, List<String> types, Services currentFragment) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        this.context = context;
        this.servicesIds = ids;
        this.servicesTypes = types;
        this.currentFragment = currentFragment;

    }

    public Context getContext() {
        return currentFragment.getContext();
    }


    @NonNull
    @Override
    public ServicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.service_provider_item, parent, false);
        return new ServicesAdapter.ServicesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicesViewHolder holder, final int position) {
        String currentServiceId = servicesIds.get(position);
        String currentServiceType = servicesTypes.get(position);

        getInfo(currentServiceId, currentServiceType, holder);

        holder.cardView.setOnClickListener(v -> currentFragment.currentServiceProviderDialog(v, position));

    }

    private void getInfo(String serviceId, String serviceType, ServicesViewHolder holder) {
        mDatabase.child("Services").child(serviceType).child(serviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("BusinessName").getValue() != null)
                    holder.nameTextView.setText(dataSnapshot.child("BusinessName").getValue().toString());
                else
                    holder.nameTextView.setText(dataSnapshot.child("UsernameText").getValue().toString());
                holder.nameTextView.setVisibility(View.VISIBLE);

                holder.typeTextView.setText(serviceType);
                holder.typeTextView.setVisibility(View.VISIBLE);

                double starsAvg = 0.0;
                long stars = (long) dataSnapshot.child("Stars").getValue();
                long starsCounter = (long) dataSnapshot.child("starCounter").getValue();
                if(starsCounter != 0)
                    starsAvg = (double) stars / starsCounter;
                holder.ratingTextView.setText(new DecimalFormat("#.#").format(starsAvg) + " out of 5");
                holder.ratingTextView.setVisibility(View.VISIBLE);

                Object photoUrlObject = dataSnapshot.child("profilePicURL").getValue();
                if(photoUrlObject != null) {
                    String photoURL = photoUrlObject.toString();
                    Glide.with(getContext()).load(photoURL).into(holder.photoImageView);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return servicesTypes.size();
    }

    public static class ServicesViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nameTextView, ratingTextView, typeTextView;
        ImageView photoImageView;

        public ServicesViewHolder(View v) {
            super(v);
            cardView = v.findViewById(R.id.cardView);

            nameTextView = v.findViewById(R.id.name_textView);
            nameTextView.setVisibility(View.GONE);

            ratingTextView = v.findViewById(R.id.rating_textView);
            ratingTextView.setVisibility(View.GONE);

            typeTextView = v.findViewById(R.id.type_textView);
            typeTextView.setVisibility(View.GONE);

            photoImageView = v.findViewById(R.id.photo_imageView);
        }
    }

}


