package com.barkindustries.mascotapp.ui.yourPets;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.R;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class YourPetsAdapter extends RecyclerView.Adapter<YourPetsAdapter.YourPetsViewHolder> {

    private YourPetsFragment currentFragment;
    private Context context;
    private List<String> petIds;

    private DatabaseReference mDatabase;

    private String name, age, breed, gender, color, photoURL;



    public YourPetsAdapter(Context context, List<String> petIds, @NonNull YourPetsFragment currentFragment) {
        this.context = context;
        this.petIds = petIds;
        this.currentFragment = currentFragment;
    }

    @NonNull
    @Override
    public YourPetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.pet_item_layout, parent, false);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        return new YourPetsAdapter.YourPetsViewHolder(v);
    }

    private void getInfo(@NonNull YourPetsAdapter.YourPetsViewHolder holder, String id){
        mDatabase.child("Pets").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.nameTextView.setText(dataSnapshot.child("Name").getValue().toString());
                holder.ageTextView.setText(dataSnapshot.child("Age").getValue().toString() + " years old");
                holder.breedTextView.setText(dataSnapshot.child("Breed").getValue().toString());
                holder.genderTextView.setText(dataSnapshot.child("Gender").getValue().toString());

                Object photoUrlObject = dataSnapshot.child("PhotoURL").getValue();
                if(photoUrlObject != null) {
                    photoURL = photoUrlObject.toString();
                    Glide.with(currentFragment.getContext()).load(photoURL).into(holder.photoImageView);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBindViewHolder(@NonNull YourPetsAdapter.YourPetsViewHolder holder, final int position) {
        String petId = petIds.get(position);
        getInfo(holder, petId);

        holder.petConstraintLayout.setOnClickListener(v -> {
            List<String> petIds = currentFragment.getPetIds();
            String clickedPetId = petIds.get(position);

            Bundle bundle = new Bundle();
            bundle.putString("petId", clickedPetId);

            final NavController navController =  Navigation.findNavController(v);
            navController.navigate(R.id.petProfileFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return petIds.size();
    }

    public static class YourPetsViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView, ageTextView, breedTextView, genderTextView;
        private ImageView photoImageView;
        private ConstraintLayout petConstraintLayout;

        public YourPetsViewHolder(View v) {
            super(v);
            nameTextView = v.findViewById(R.id.name_textView);
            ageTextView = v.findViewById(R.id.age_textView);
            breedTextView = v.findViewById(R.id.breed_textView);
            genderTextView = v.findViewById(R.id.gender_textView);
            photoImageView = v.findViewById(R.id.photo_imageView);

            petConstraintLayout = v.findViewById(R.id.pet_item_layout);
        }
    }
}
