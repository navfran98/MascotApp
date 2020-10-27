package com.barkindustries.mascotapp.ui.yourPets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class YourPetsFragment extends Fragment {

    // screen controls
    private FloatingActionButton add_pet;

    private YourPetsAdapter adapter;

    private List<String> petIds;
    private List<String> petNames;

    // Firebase variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;


    // First method called. It is short because, in case of a crash, it ends quickly
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_your_pets, container, false);
    }

    // Second method called. This is our main method
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        petNames = new ArrayList<>();
        petIds = new ArrayList<>();

        setupRecyclerView(view);

        add_pet = view.findViewById(R.id.yourpets_add_fab);
        add_pet.setOnClickListener(v -> {
            // we navigate to AddPetFragment
            final NavController navController =  Navigation.findNavController(view);
            navController.navigate(R.id.addPetFragment);
        });


        displayPetList(view);
    }



    private void displayPetList(@NonNull View view) {
        new Thread(() -> {
            mDatabase.child("Users").child(userId).child("PetsAmount").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Object petAmount = dataSnapshot.getValue();
                    if(petAmount != null && (Long)petAmount > 0) {
                        mDatabase.child("Users").child(userId).child("Pets").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot pet : dataSnapshot.getChildren()) {
                                    final String petId = pet.getValue().toString();
                                    petIds.add(petId);
                                    adapter.notifyItemInserted(petIds.size() - 1);
                                }
                                YourPetsFragment.this.requireView().findViewById(R.id.loading_pets_list).setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                view.findViewById(R.id.loading_pets_list).setVisibility(View.GONE);
                                view.findViewById(R.id.get_pet_list_failed).setVisibility(View.VISIBLE);
                                view.findViewById(R.id.button_get_pets_list_again).setOnClickListener(
                                        YourPetsFragment.this::getPetsAgain);
                            }
                        });
                    } else {
                        view.findViewById(R.id.zero_pets).setVisibility(View.VISIBLE);
                        YourPetsFragment.this.requireView().findViewById(R.id.loading_pets_list).setVisibility(View.GONE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    view.findViewById(R.id.loading_pets_list).setVisibility(View.GONE);
                    view.findViewById(R.id.get_pet_list_failed).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.button_get_pets_list_again).setOnClickListener(YourPetsFragment.this::getPetsAgain);
                }
            });
        }).start();
    }

    private void getPetsAgain(@NonNull View view) {
        view.findViewById(R.id.loading_pets_list).setVisibility(View.VISIBLE);
        view.findViewById(R.id.get_pet_list_failed).setVisibility(View.GONE);
        displayPetList(view);
    }

    List<String> getPetIds() {
        return petIds;
    }

    private void setupRecyclerView(@NonNull View view) {
        // variables dealing with the RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.my_pets_recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
//        adapter = new YourPetsAdapter(getContext(), petNames, this);
        adapter = new YourPetsAdapter(getContext(), petIds, this);
        recyclerView.setAdapter(adapter);
    }
}


