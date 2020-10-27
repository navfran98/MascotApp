package com.barkindustries.mascotapp.ui.lostPets;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.barkindustries.mascotapp.R;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.Mapbox;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class AddLostPetDialog extends DialogFragment implements android.view.View.OnClickListener {

    public Activity activity;
    public Dialog d;

    private String petId;
    private LostPetsFragment lostPetsFragment;

    private DatabaseReference mDatabase;

    private ImageView image;
    private TextView nameTextView, breedTextView, genderTextView, ageTextView, sizeTextView, invalidDescriptionTextView, invalidAddressTextView;
    private EditText descriptionEditText;
    private Button lostItButton, closeButton;
    private RadioButton lostItHereRadioButton, lostItElsewhereRadioButton;
    private FrameLayout mapFrameLayout;

    public AddLostPetDialog(@NonNull Context context, LostPetsFragment lostPetsFragment, String petId) {
        super();
        this.petId = petId;
        this.lostPetsFragment = lostPetsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_lost_pet_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Mapbox.getInstance(getContext(), getContext().getString(R.string.access_token));

        mDatabase = FirebaseDatabase.getInstance().getReference();

        image = view.findViewById(R.id.lost_pet_imageView);
        nameTextView = view.findViewById(R.id.lost_pet_name_textView);
        breedTextView = view.findViewById(R.id.lost_pet_breed_textView);
        genderTextView = view.findViewById(R.id.lost_pet_gender_textView);
        ageTextView = view.findViewById(R.id.lost_pet_age_textView);
        sizeTextView = view.findViewById(R.id.lost_pet_size_textView);
        descriptionEditText = view.findViewById(R.id.description_editText);
        lostItButton = view.findViewById(R.id.seen_it_button);
        closeButton = view.findViewById(R.id.close_button);
        lostItHereRadioButton = view.findViewById(R.id.lost_here_radioButton);
        lostItElsewhereRadioButton = view.findViewById(R.id.lost_elsewhere_radioButton);
        invalidDescriptionTextView = view.findViewById(R.id.invalid_description_textView);
        invalidAddressTextView = view.findViewById(R.id.invalid_address_textView);
        mapFrameLayout = view.findViewById(R.id.map_frameLayout);

        lostItHereRadioButton.setOnClickListener(v -> lostItHerePressed());
        lostItElsewhereRadioButton.setOnClickListener(v -> lostItElsewherePressed());
        lostItButton.setOnClickListener(v -> addToDatabase());
        closeButton.setOnClickListener(v -> dismiss());

        lostItHereRadioButton.setChecked(true);
        lostItElsewhereRadioButton.setChecked(false);
        invalidDescriptionTextView.setVisibility(View.INVISIBLE);
        invalidAddressTextView.setVisibility(View.INVISIBLE);
        mapFrameLayout.setVisibility(View.GONE);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Fragment childFragment = new LocationPickerFragment();
        transaction.replace(R.id.map_frameLayout, childFragment).commit();

        getPetInfo();
    }


    @Override
    public void onClick(View v) {
        dismiss();
    }

    private void lostItElsewherePressed() {
        mapFrameLayout.setVisibility(View.VISIBLE);
        lostItHereRadioButton.setChecked(false);
    }

    private void lostItHerePressed() {
        mapFrameLayout.setVisibility(View.GONE);
        lostItElsewhereRadioButton.setChecked(false);
    }

    private void addToDatabase() {
        invalidDescriptionTextView.setVisibility(View.INVISIBLE);

        if (descriptionEditText.getText().toString().length() == 0)
            invalidDescriptionTextView.setVisibility(View.VISIBLE);
        else {
            mDatabase.child("LostPets").child(petId).setValue(petId);
            Map<String, Object> map = new HashMap<>();
            map.put("Description", descriptionEditText.getText().toString());

            Double auxLat = null, auxLong = null;

            if (lostItHereRadioButton.isChecked()) {
                auxLat = LocationCommunicator.getLocationCommunicator().location.getLatitude();
                auxLong = LocationCommunicator.getLocationCommunicator().location.getLongitude();
            } else if(lostItElsewhereRadioButton.isChecked()) {
                auxLat = LocationPickerFragment.lat;
                auxLong = LocationPickerFragment.lon;
                if(auxLat == null) {
                    invalidAddressTextView.setVisibility(View.INVISIBLE);
                    return;
                }
            }


            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(getContext(), Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(auxLat, auxLong, 1);
                String address = addresses.get(0).getAddressLine(0);
                map.put("Address",address);
            } catch (IOException e) {
                e.printStackTrace();
            }

            map.put("Latitude", auxLat);
            map.put("Longitude", auxLong);


            mDatabase.child("LostPets").child(petId).setValue(map).addOnCompleteListener(result -> {
                if (result.isSuccessful()) {
                    mDatabase.child("Pets").child(petId).child("IsLost").setValue("true").addOnCompleteListener(result2 -> {
                        if (result2.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Lost pet published!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error publishing lost pet!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Error publishing lost pet!", Toast.LENGTH_SHORT).show();
                }
            });
            if(lostPetsFragment != null)
                lostPetsFragment.notifyNewLostPetAdded(petId);
            dismiss();
        }
    }

  private void getPetInfo(){
      new Thread(() -> {
            mDatabase.child("Pets").child(petId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    nameTextView.setText(dataSnapshot.child("Name").getValue().toString());
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
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    
                }
            });
        }).start();
  }
}
