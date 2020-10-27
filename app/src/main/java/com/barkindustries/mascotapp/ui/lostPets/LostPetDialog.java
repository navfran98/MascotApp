package com.barkindustries.mascotapp.ui.lostPets;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.barkindustries.mascotapp.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LostPetDialog extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;

    private String petId;
    private String petName;
    private String ownerId;
    private LostPetsFragment lostPetsFragment;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ImageView image;
    private TextView nameTextView, breedTextView, genderTextView, ageTextView, sizeTextView, descriptionTextView, locationTextView;
    private Button seenItButton, closeButton;
    private ProgressBar progressBar;

    public LostPetDialog(@NonNull Context context, LostPetsFragment lostPetsFragment, String petId) {
        super(context);
        this.petId = petId;
        this.lostPetsFragment = lostPetsFragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.lost_pet_dialog);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        image = findViewById(R.id.lost_pet_imageView);
        nameTextView = findViewById(R.id.lost_pet_name_textView);
        breedTextView = findViewById(R.id.lost_pet_breed_textView);
        genderTextView = findViewById(R.id.lost_pet_gender_textView);
        ageTextView = findViewById(R.id.lost_pet_age_textView);
        sizeTextView = findViewById(R.id.lost_pet_size_textView);
        descriptionTextView = findViewById(R.id.lost_pet_description_textView);
        seenItButton = findViewById(R.id.seen_it_button);
        closeButton = findViewById(R.id.close_button);
        progressBar = findViewById(R.id.progressBar);
        locationTextView = findViewById(R.id.lost_pet_location_textView);
        
        image.setVisibility(View.INVISIBLE);
        locationTextView.setVisibility(View.INVISIBLE);
        nameTextView.setVisibility(View.INVISIBLE);
        breedTextView.setVisibility(View.INVISIBLE);
        genderTextView.setVisibility(View.INVISIBLE);
        ageTextView.setVisibility(View.INVISIBLE);
        sizeTextView.setVisibility(View.INVISIBLE);
        descriptionTextView.setVisibility(View.INVISIBLE);
        

        descriptionTextView.setMovementMethod(new ScrollingMovementMethod());

        seenItButton.setOnClickListener(v -> seenIt());
        closeButton.setOnClickListener(v -> dismiss());

        getPetInfo();
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }


    private void seenIt() {
        LostPetOwnerDialog dialog = new LostPetOwnerDialog(getContext(), this, ownerId, petName);
        dialog.show();
    }

    private void getPetInfo(){
        new Thread(() -> {
            mDatabase.child("Pets").child(petId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ownerId = dataSnapshot.child("Owner").getValue().toString();
                    petName = dataSnapshot.child("Name").getValue().toString();
                    nameTextView.setText(petName);
                    breedTextView.setText(dataSnapshot.child("Breed").getValue().toString());
                    genderTextView.setText(dataSnapshot.child("Gender").getValue().toString());
                    ageTextView.setText(dataSnapshot.child("Age").getValue().toString() + " years old");
                    int weight = Integer.parseInt(dataSnapshot.child("Weight").getValue().toString());
                    String size;
                    if(weight < 10){
                        size = "Small-sized";
                    }
                    else if(weight > 10 && weight < 20){
                        size = "Medium-sized";
                    }
                    else{
                        size = "Big-sized";
                    }
                    sizeTextView.setText(size);

                    Object photoUrlObject = dataSnapshot.child("PhotoURL").getValue();
                    if(photoUrlObject != null) {
                        String photoURL = photoUrlObject.toString();
                        Glide.with(getContext()).load(photoURL).into(image);
                    }

                    mDatabase.child("LostPets").child(petId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String aux = "Last seen near: ";
                            descriptionTextView.setText(dataSnapshot.child("Description").getValue().toString());
                            locationTextView.setText(aux + dataSnapshot.child("Address").getValue().toString());
                            locationTextView.setVisibility(View.VISIBLE);
                            image.setVisibility(View.VISIBLE);
                            nameTextView.setVisibility(View.VISIBLE);
                            breedTextView.setVisibility(View.VISIBLE);
                            genderTextView.setVisibility(View.VISIBLE);
                            ageTextView.setVisibility(View.VISIBLE);
                            sizeTextView.setVisibility(View.VISIBLE);
                            descriptionTextView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }).start();
    }

}
 