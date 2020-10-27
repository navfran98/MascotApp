package com.barkindustries.mascotapp.ui.tinderForPets;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.barkindustries.mascotapp.R;
import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Firebase.User;
import com.barkindustries.mascotapp.ui.chatWithVets.SeeUsersFragment;
import com.barkindustries.mascotapp.ui.chatWithVets.Utilities.Constants;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TinderForPetsFragment extends Fragment {

    //List
    private List<Pet> candidates;

    // Screen controls
    private TextView petName, petAge, petBreed, petGender;
    private ImageView candidatePetPic, heart, cross;

    private String candidatePetOwner;
    private String candidatePetUid;
    private boolean possibleMatch;
    private int currentCandidatePosition;

    //Firebase
    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private FirebaseAuth mAuth;
    private String myUid;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tinder_for_pets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        myUid = mAuth.getUid();
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference(Constants.USERS_NODE);

        initScreenControls(view);

        possibleMatch = false;
        currentCandidatePosition = 0;
        candidates = new ArrayList<>();

        hideEverything(view);

        getCandidates(view);
    }

    private void initScreenControls(@NonNull View view) {
        candidatePetPic = (ImageView) view.findViewById(R.id.tinder_inner_picture);
        heart = (ImageView) view.findViewById(R.id.heart);
        cross = (ImageView) view.findViewById(R.id.cross);

        petAge = (TextView) view.findViewById(R.id.tinder_pet_age);
        petBreed = (TextView) view.findViewById(R.id.tinder_pet_breed);
        petName = (TextView) view.findViewById(R.id.tinder_pet_name);
        petGender = (TextView) view.findViewById(R.id.tinder_pet_gender);

        heart.setOnClickListener(this::likePet);
        cross.setOnClickListener(this::dislikePet);
    }

    private void likePet(View v) {
        if(possibleMatch){
            match(mAuth.getUid(), candidatePetOwner);
            possibleMatch = false;
        } else {
            usersReference.child(candidatePetOwner).child("Tinder").child("PossibleMatch").child(myUid).setValue(myUid);
        }
        usersReference.child(myUid).child("Tinder").child("DoNotShow").child(candidatePetUid).setValue(candidatePetUid);

        getNextCandidate();
    }

    private void dislikePet(View v) {
        if(possibleMatch){
            usersReference.child(myUid).child("Tinder").child("PossibleMatch").child(candidatePetOwner).removeValue();
            possibleMatch = false;
        }
        usersReference.child(myUid).child("Tinder").child("DoNotShow").child(candidatePetUid).setValue(candidatePetUid);
        getNextCandidate();
    }

    private void hideEverything(@NonNull View view) {
        cross.setVisibility(View.GONE);
        heart.setVisibility(View.GONE);
        candidatePetPic.setVisibility(View.GONE);
        view.findViewById(R.id.tinder_outer_picture).setVisibility(View.GONE);
        petGender.setVisibility(View.GONE);
        petAge.setVisibility(View.GONE);
        petBreed.setVisibility(View.GONE);
        petName.setVisibility(View.GONE);
        view.findViewById(R.id.tinder_age_placeholder).setVisibility(View.GONE);

    }

    public void match(String currentUid, String matchUid){

        usersReference.child(matchUid).child("Tinder").child("PossibleMatch").child(currentUid).removeValue();
        usersReference.child(currentUid).child("Tinder").child("PossibleMatch").child(matchUid).removeValue();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("New friend added!");
        builder.setMessage("There was a match! Check your friend's list.");
        builder.setPositiveButton("GOT IT", (dialog, which) -> dialog.dismiss());
        builder.show();

        //se hacen amigos

        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(Objects.equals(ds.getKey(), matchUid)){
                        User match = ds.getValue(User.class);
                        assert match != null;
                        String matchEmail = match.getEmail();

                        SeeUsersFragment addUser = new SeeUsersFragment();
                        addUser.becomeFriendWithUser(matchEmail, myUid, database, true, false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void getCandidates(@NonNull View view){
       new Thread(() ->{
           database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   for(DataSnapshot ds : dataSnapshot.child("Pets").getChildren()) {
                       Pet pet = ds.getValue(Pet.class);
                       pet.setKey(ds.getKey());
                       pet.setPossibleMatch(false);

                       if (pet.getPhotoURL() != null) {
                           //en la lista, solo agrego los que quiero mostrar
                           if (!dataSnapshot.child(Constants.USERS_NODE).child(myUid).child("Tinder").child("DoNotShow").hasChild(pet.getKey()) &&
                                   !pet.getOwner().equals(myUid)) {

                               if(dataSnapshot.child(Constants.USERS_NODE).child(myUid).child("Tinder").child("PossibleMatch").hasChild(pet.getOwner())){
                                   pet.setPossibleMatch(true);
                               }

                               pet.setKey(ds.getKey());
                               candidates.add(pet);
                           }
                       }
                   }
                   getNextCandidate();
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {
                   view.findViewById(R.id.loading_tinder).setVisibility(View.GONE);
                   view.findViewById(R.id.error_in_tinder).setVisibility(View.VISIBLE);
               }
           });
       }).start();
    }

    public void getNextCandidate(){
        if(currentCandidatePosition != candidates.size()) {
            Pet candidate = candidates.get(currentCandidatePosition++);

            if (candidate != null) {
                candidatePetOwner = candidate.getOwner();
                candidatePetUid = candidate.getKey();
                String candidateName = candidate.getName();
                String candidateAge = candidate.getAge();
                String candidateGender = candidate.getGender();
                String candidateBreed = candidate.getBreed();

                Glide.with(TinderForPetsFragment.this).load(candidate.getPhotoURL()).into(candidatePetPic);

                petName.setText(candidateName);
                petAge.setText(candidateAge);
                petBreed.setText(candidateBreed);
                petGender.setText(candidateGender);

                revealScreenControls();

                if (candidate.getPossibleMatch())
                    possibleMatch = true;
            }
        } else {
            requireView().findViewById(R.id.no_more_pets_tinder).setVisibility(View.VISIBLE);
            hideEverything(requireView());
        }
        requireView().findViewById(R.id.loading_tinder).setVisibility(View.GONE);
    }

    private void revealScreenControls() {
        cross.setVisibility(View.VISIBLE);
        heart.setVisibility(View.VISIBLE);
        candidatePetPic.setVisibility(View.VISIBLE);
        requireView().findViewById(R.id.tinder_outer_picture).setVisibility(View.VISIBLE);

        petGender.setVisibility(View.VISIBLE);
        petAge.setVisibility(View.VISIBLE);
        petBreed.setVisibility(View.VISIBLE);
        petName.setVisibility(View.VISIBLE);
        requireView().findViewById(R.id.tinder_age_placeholder).setVisibility(View.VISIBLE);
    }
}
