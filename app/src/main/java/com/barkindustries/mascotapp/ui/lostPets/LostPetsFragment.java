package com.barkindustries.mascotapp.ui.lostPets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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

public class LostPetsFragment extends Fragment {

    LostPetsAdapter adapter;
    private FloatingActionButton add_lost_pet;
    private TextView zero_lost_pets;

    private List<String> lostPetIds;

    private List<String> petIds;
    private List<String> petNames;

    private Boolean loaded = false;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;
    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lost_pets, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lostPetIds = new ArrayList<>();
        petIds = new ArrayList<>();
        petNames = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        setupRecyclerView(view);

        zero_lost_pets = view.findViewById(R.id.zero_lost_pets);
        add_lost_pet = view.findViewById(R.id.add_lost_pet);
        add_lost_pet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLostPet(v);
            }
        });

        add_lost_pet.setVisibility(View.INVISIBLE);

        progressBar = view.findViewById(R.id.progressBar);

        displayLostPetList(view);

        getMyPets(view);
    }

    private void setupRecyclerView(@NonNull View view) {
        RecyclerView recyclerView = view.findViewById(R.id.lost_pet_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new LostPetsAdapter(getContext(), lostPetIds, this);
        recyclerView.setAdapter(adapter);
    }

    void currentLostPetDialog(View v, int position){
        LostPetDialog dialog = new LostPetDialog(requireContext(), this, lostPetIds.get(position));    // FALTA OBTENER EL PETID
        dialog.show();
    }

    private void addLostPet(View v){
        getMyPets(v);
        if(petNames.size()==0){
            Toast.makeText(this.getContext(), "You don't have any pet to report as lost!", Toast.LENGTH_SHORT).show();
        }  else {
            PopupMenu menu = new PopupMenu(this.getContext(), v);
            for(String pet : petNames){
                menu.getMenu().add(pet);
            }
            menu.show();

            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int i = getNameIndex(item.toString());
                    showAddLostPetDialog(v, petIds.get(i));
                    return true;
                }
            });
        }
    }

    private void showAddLostPetDialog(View v, String petId) {
        // Create and show the dialog.
        AddLostPetDialog dialog = new AddLostPetDialog(requireContext(), this, petId);
        dialog.show(getChildFragmentManager(), "hola");
    }

    private int getNameIndex(String name) {
        int i;
        for(i = 0; i < petNames.size(); i++) {
            if(name.equals(petNames.get(i)))
                return i;
        }
        return -1;
    }

    private int getIdIndex(String id) {
        int i;
        for(i = 0; i < petIds.size(); i++) {
            if(id == petIds.get(i))
                return i;
        }
        return -1;
    }

    void notifyNewLostPetAdded(String lostPetId){
        if(zero_lost_pets.getVisibility() == View.VISIBLE){
            zero_lost_pets.setVisibility(View.INVISIBLE);
        }
        lostPetIds.add(lostPetId);
        adapter.notifyItemInserted(lostPetIds.size()-1);
        int auxIndex = getIdIndex(lostPetId);
        petIds.remove(lostPetId);
        petNames.remove(auxIndex);

    }

    private void getMyPets(@NonNull View view){
        new Thread(() -> {
            petIds = new ArrayList<>();
            petNames = new ArrayList<>();
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
                                    // we go inside the Pets node, and retrieve the petName
                                    mDatabase.child("Pets").child(petId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.child("IsLost").getValue().toString().equals("false")) {
                                                petIds.add(petId);
                                                Object petNameObject = dataSnapshot.child("Name").getValue();
                                                if(petNameObject != null) {
                                                    String petNameString = petNameObject.toString();
                                                    petNames.add(petNameString);
                                                }

                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Nothing, we just ignore that there was an error retrieving 1 pet
                                            // and we just don't add it to the list
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // todo: agregar boton de try again
                            }
                        });
                    } else {

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }).start();
    }

    private void displayLostPetList(@NonNull View view) {
        new Thread(() -> {
            mDatabase.child("LostPets").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    loaded = true;
                    if(dataSnapshot.exists()){
                        for(DataSnapshot lostPet : dataSnapshot.getChildren()) {
                            final String petId = lostPet.getKey();
                            lostPetIds.add(petId);
                            adapter.notifyItemInserted(lostPetIds.size() - 1);
                        }
                    }else{
                        zero_lost_pets.setVisibility(View.VISIBLE);
                    }
                    add_lost_pet.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    view.findViewById(R.id.get_lost_pet_list_failed).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.button_get_lost_pets_list_again).setOnClickListener(LostPetsFragment.this::getLostPetsAgain);
                }
            });
        }).start();
    }

    private void getLostPetsAgain(@NonNull View view) {
        view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        view.findViewById(R.id.get_lost_pet_list_failed).setVisibility(View.GONE);
        displayLostPetList(view);
    }
}
