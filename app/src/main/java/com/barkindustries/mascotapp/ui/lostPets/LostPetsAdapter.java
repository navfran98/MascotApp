package com.barkindustries.mascotapp.ui.lostPets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.R;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class LostPetsAdapter extends RecyclerView.Adapter<LostPetsAdapter.LostPetsViewHolder> {

    protected List<String> lostPets;
    protected Context context;
    protected LostPetsFragment currentFragment;
    private DatabaseReference mDatabase;

    private String name, address;
    private String photoURL;

    public LostPetsAdapter(Context context, List<String> lostPetsIds, LostPetsFragment currentFragment) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.context = context;
        this.lostPets = lostPetsIds;
        this.currentFragment = currentFragment;
    }

    public Context getContext() {
        return currentFragment.getContext();
    }

    @NonNull
    @Override
    public LostPetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.lost_pet_item_layout, parent, false);
        return new LostPetsAdapter.LostPetsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LostPetsViewHolder holder, final int position) {
        String currentLostPet = lostPets.get(position);
        getInfo(holder, currentLostPet);
        holder.lostPetsItem.setOnClickListener(v -> currentFragment.currentLostPetDialog(v, position));
    }



    //PROBLE: EL NOMBRE E IMAGEN ESTAN EN PET, PERO LA ADDRESS ESTA EN LOSTPETS
    private void getInfo(@NonNull LostPetsViewHolder holder, String id){
        mDatabase.child("Pets").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("Name").getValue().toString();
                Object photoUrlObject = dataSnapshot.child("PhotoURL").getValue();
                if(photoUrlObject != null) {
                    photoURL = photoUrlObject.toString();
                    Glide.with(getContext()).load(photoURL).into(holder.lostPetImage);
                }
                holder.lostPetName.setText(name);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabase.child("LostPets").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                address = dataSnapshot.child("Address").getValue().toString();
                int distance = (int) Math.round(
                        distance(LocationCommunicator.getLocationCommunicator().location.getLatitude(),
                        (double) dataSnapshot.child("Latitude").getValue(),
                        LocationCommunicator.getLocationCommunicator().location.getLongitude(),
                        (double) dataSnapshot.child("Longitude").getValue(), 0, 0));

                String aux = "m away)";

                if(distance > 1000) {
                    aux = "km away)";
                    distance /= 1000;
                }

                address = "Lost at " + address.substring(0, address.indexOf(',')) + " (" + distance + aux;
                holder.lostPetAddress.setText(address);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return lostPets.size();
    }

    public static class LostPetsViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout lostPetsItem;
        TextView lostPetName, lostPetAddress;
        ImageView lostPetImage;

        public LostPetsViewHolder(View v) {
            super(v);
            lostPetImage = v.findViewById(R.id.lost_pet_image);
            lostPetAddress = v.findViewById(R.id.lost_pet_address);
            lostPetName = v.findViewById(R.id.lost_pet_name);
            lostPetsItem = v.findViewById(R.id.lost_pets_item);
        }
    }

    // distance between two points on earth
    public static double distance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}


