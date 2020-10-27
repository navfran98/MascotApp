package com.barkindustries.mascotapp.ui.yourPets.medicalHistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.barkindustries.mascotapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddMedicalHistoryFragment extends Fragment {

    // Screen controls
    private EditText title;
    private EditText description;
    private Switch medicineSwitch;
    private EditText medicine;
    private ImageView notImportantImage;
    private ImageView importantImage;
    private Button cancelButton;
    private Button addButton;

    // Firebase
    private DatabaseReference mDatabase;

    private String userId;
    private String petId;
    private boolean medicinePresent = false;
    private boolean importantItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_medical_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle != null) {
            petId = bundle.getString("petId");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getUid();

        initScreenControls(view);
    }

    private void initScreenControls(@NonNull View view) {
        title = view.findViewById(R.id.new_medical_history_title);
        description = view.findViewById(R.id.new_medical_history_description);
        medicineSwitch = view.findViewById(R.id.new_medical_history_switch);
        medicine = view.findViewById(R.id.new_medicine);
        notImportantImage = view.findViewById(R.id.new_medical_history_notImportant_image);
        importantImage = view.findViewById(R.id.new_medical_history_important_image);
        cancelButton = view.findViewById(R.id.new_medical_history_cancel_button);
        addButton = view.findViewById(R.id.new_medical_history_confirm_button);

        cancelButton.setOnClickListener(v -> {
            final NavController navController =  Navigation.findNavController(v);
            navController.navigateUp();
        });

        addButton.setOnClickListener(this::addMedicalHistoryItem);

        medicineSwitch.setOnClickListener(v -> {
            if(medicineSwitch.isChecked()) {
                medicinePresent = true;
                medicine.setVisibility(View.VISIBLE);
            } else {
                medicinePresent = false;
                medicine.setVisibility(View.GONE);
            }
        });

        notImportantImage.setOnClickListener(v -> {
            notImportantImage.setVisibility(View.GONE);
            importantImage.setVisibility(View.VISIBLE);
            importantItem = true;
        });

        importantImage.setOnClickListener(v -> {
            notImportantImage.setVisibility(View.VISIBLE);
            importantImage.setVisibility(View.GONE);
            importantItem = false;
        });
    }

    private void addMedicalHistoryItem(@NonNull View view) {
        requireView().findViewById(R.id.loading_adding_medical_history).setVisibility(View.VISIBLE);
        requireView().findViewById(R.id.new_medical_history_confirm_button).setVisibility(View.GONE);
        requireView().findViewById(R.id.new_medical_history_cancel_button).setVisibility(View.GONE);

        // We add all necessary values into the map
        Map<String, Object> map = new HashMap<>();

        String titleString = title.getText().toString();
        if(titleString.equals(""))
            titleString = "<No title>";
        map.put("Title", titleString);

        String descriptionString = description.getText().toString();
        if(descriptionString.equals(""))
            descriptionString = "<No description>";
        map.put("Description", descriptionString);

        String medicineIsPresentString = medicinePresent ? "true" : "false";
        map.put("MedicineIsPresent", medicineIsPresentString);
        String auxMedicine = medicine.getText().toString();
        if(medicinePresent) {
            if (auxMedicine.equals("")) {
                Toast.makeText(getContext(), "Write a medicine!", Toast.LENGTH_SHORT).show();
                requireView().findViewById(R.id.loading_adding_medical_history).setVisibility(View.GONE);
                requireView().findViewById(R.id.new_medical_history_confirm_button).setVisibility(View.VISIBLE);
                requireView().findViewById(R.id.new_medical_history_cancel_button).setVisibility(View.VISIBLE);
                return;
            } else
                map.put("Medicine", auxMedicine);
        }

        String importantString = importantItem ? "yes" : "no";
        map.put("ItemIsImportant", importantString);

        long miliseconds = new Time(System.currentTimeMillis()).getTime();
        String timestamp = new Timestamp(miliseconds).toInstant().toString();
        timestamp = timestamp.replace('.', ',');
        map.put("Timestamp", timestamp);

        // We put the map in Firebase
        String finalTimestamp = timestamp;
        new Thread(() -> {
            mDatabase.child("Pets").child(petId).child("MedicalHistory").child(finalTimestamp).setValue(map).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Item added!", Toast.LENGTH_SHORT).show();
                    final NavController navController = Navigation.findNavController(requireView());
                    navController.navigateUp();
                } else {
                    requireView().findViewById(R.id.loading_adding_medical_history).setVisibility(View.GONE);
                    requireView().findViewById(R.id.new_medical_history_confirm_button).setVisibility(View.VISIBLE);
                    requireView().findViewById(R.id.new_medical_history_cancel_button).setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Could not add new item!", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}
