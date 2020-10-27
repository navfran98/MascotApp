package com.barkindustries.mascotapp.ui.yourPets.medicalHistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.R;
import com.barkindustries.mascotapp.ui.yourPets.medicalHistory.MedicalHistoryAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MedicalHistoryFragment extends Fragment {

    private MedicalHistoryAdapter adapter;
    private FloatingActionButton addMedicalHistoryButton;
    private List<String> historiesTitles;
    private String petId;
    private String petName;
    private List<MedicalHistoryItem> items;

    // Firebase variables
    private DatabaseReference mDatabase;
    private String userId;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_medical_history, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Medical History");
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            petId = bundle.getString("petId");
            petName = bundle.getString("petName");
        }

        addMedicalHistoryButton = view.findViewById(R.id.add_medical_history_fab);
        addMedicalHistoryButton.setOnClickListener(this::addMedicalHistoryItem);

        historiesTitles = new ArrayList<>();
        items = new ArrayList<>();

        userId = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        setupRecyclerView(view);

        getMedicalHistoryOfThisPet(view);
    }

    private void setupRecyclerView(@NonNull View view) {
        RecyclerView recyclerView = view.findViewById(R.id.medical_history_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MedicalHistoryAdapter(getContext(), items);
        recyclerView.setAdapter(adapter);
    }

    private void getMedicalHistoryOfThisPet(View view) {
        new Thread(() -> {
            mDatabase.child("Pets").child(petId).child("MedicalHistory").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot historyItem : dataSnapshot.getChildren()) {
                        obtainDataFromDatabase(historyItem);
                    }

                    if(items.size() == 0) {
                        view.findViewById(R.id.zero_medical_histories).setVisibility(View.VISIBLE);
                        TextView petNameOnScreen = view.findViewById(R.id.zero_medical_histories_petName);
                        petNameOnScreen.setText(petName);
                    } else {
                        view.findViewById(R.id.zero_medical_histories).setVisibility(View.GONE);
                    }
                    view.findViewById(R.id.loading_medical_history).setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    view.findViewById(R.id.loading_medical_history).setVisibility(View.GONE);
                    view.findViewById(R.id.medical_history_error_message).setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    private void obtainDataFromDatabase(@NonNull DataSnapshot historyItem) {

        final String title = Objects.requireNonNull(historyItem.child("Title").getValue()).toString();
        final String description = Objects.requireNonNull(historyItem.child("Description").getValue()).toString();
        final String medicineIsPresent = Objects.requireNonNull(historyItem.child("MedicineIsPresent").getValue()).toString();
        String medicine = null;
        if(medicineIsPresent.equals("true"))
            medicine = Objects.requireNonNull(historyItem.child("Medicine").getValue()).toString();
        final String isImportant = Objects.requireNonNull(historyItem.child("ItemIsImportant").getValue()).toString();
        final String timestampString = Objects.requireNonNull(historyItem.child("Timestamp").getValue()).toString();

        MedicalHistoryItem medicalHistory = new MedicalHistoryItem(title, description, timestampString, medicineIsPresent,
                medicine, isImportant);
        items.add(medicalHistory);
        adapter.notifyItemInserted(items.size() - 1);
    }

    private void addMedicalHistoryItem(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("petId", petId);
        final NavController navController =  Navigation.findNavController(view);
        navController.navigate(R.id.addMedicalHistory, bundle);
    }
}
