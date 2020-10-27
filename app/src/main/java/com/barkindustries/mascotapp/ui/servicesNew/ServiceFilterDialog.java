package com.barkindustries.mascotapp.ui.servicesNew;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;

import com.barkindustries.mascotapp.R;

import java.util.ArrayList;
import java.util.List;

public class ServiceFilterDialog extends Dialog implements android.view.View.OnClickListener {

    private CheckBox breederButton, groomerButton, walkerButton, vetButton, petShopButton, sitterButton, shelterButton, allButton;
    private Button confirmButton, closeButton;

    private Services serviceFragment;

    private List<String> selectedServices;
    private List<String> currentTypesSelected;


    public ServiceFilterDialog(@NonNull Context context, Services services, List<String> currentTypesSelected) {
        super(context);
        this.serviceFragment = services;
        this.currentTypesSelected = currentTypesSelected;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.service_filter_dialog);

        selectedServices = new ArrayList<>();

        breederButton = findViewById(R.id.breeder_button);
        groomerButton = findViewById(R.id.groomer_button);
        walkerButton = findViewById(R.id.walker_button);
        vetButton = findViewById(R.id.vet_button);
        petShopButton = findViewById(R.id.pet_shop_button);
        sitterButton = findViewById(R.id.sitter_button);
        shelterButton = findViewById(R.id.shelter_button);
        allButton = findViewById(R.id.all_button);

        confirmButton = findViewById(R.id.confirm_button);
        closeButton = findViewById(R.id.close_button);

        closeButton.setOnClickListener( v -> dismiss());
        confirmButton.setOnClickListener( v -> confirm());
        allButton.setOnClickListener( v -> allPressed());

        checkButtons();

        checkButtonsPressed();

        breederButton.setOnClickListener( v -> checkButtonsPressed());
        groomerButton.setOnClickListener( v -> checkButtonsPressed());
        walkerButton.setOnClickListener( v -> checkButtonsPressed());
        vetButton.setOnClickListener( v -> checkButtonsPressed());
        petShopButton.setOnClickListener( v -> checkButtonsPressed());
        sitterButton.setOnClickListener( v -> checkButtonsPressed());
        shelterButton.setOnClickListener( v -> checkButtonsPressed());
    }

    private void checkButtons() {
        if(currentTypesSelected == null)
            return;

        for(String type : currentTypesSelected) {
            switch (type) {
                case "Breeder":
                    breederButton.setChecked(true);
                    break;
                case "Groomer":
                    groomerButton.setChecked(true);
                    break;
                case "Walker":
                    walkerButton.setChecked(true);
                    break;
                case "Vet":
                    vetButton.setChecked(true);
                    break;
                case "Pet Shop":
                    petShopButton.setChecked(true);
                    break;
                case "Sitter":
                    sitterButton.setChecked(true);
                    break;
                case "Shelter":
                    shelterButton.setChecked(true);
                    break;
            }
        }
    }

    private void checkButtonsPressed() {
        if(!breederButton.isChecked() || !groomerButton.isChecked() || !walkerButton.isChecked() || !vetButton.isChecked() || !petShopButton.isChecked() || !sitterButton.isChecked() || !shelterButton.isChecked())
            allButton.setChecked(false);
        else {
            allButton.setChecked(true);
            allPressed();
        }

    }

    private void allPressed() {
//        if(allButton.isChecked()) {
//            breederButton.setChecked(true);
//            groomerButton.setChecked(true);
//            walkerButton.setChecked(true);
//            vetButton.setChecked(true);
//            petShopButton.setChecked(true);
//            sitterButton.setChecked(true);
//            shelterButton.setChecked(true);
//        } else {
            breederButton.setChecked(false);
            groomerButton.setChecked(false);
            walkerButton.setChecked(false);
            vetButton.setChecked(false);
            petShopButton.setChecked(false);
            sitterButton.setChecked(false);
            shelterButton.setChecked(false);
//        }

    }

    private void confirm(){
        if(breederButton.isChecked()){
            selectedServices.add("Breeder");
        }
        if(groomerButton.isChecked()){
            selectedServices.add("Groomer");
        }
        if(walkerButton.isChecked()){
            selectedServices.add("Walker");
        }
        if(vetButton.isChecked()){
            selectedServices.add("Vet");
        }
        if(petShopButton.isChecked()){
            selectedServices.add("Pet Shop");
        }
        if(sitterButton.isChecked()){
            selectedServices.add("Sitter");
        }
        if(shelterButton.isChecked()){
            selectedServices.add("Shelter");
        }
        if(allButton.isChecked()) {
            selectedServices.add("Breeder");
            selectedServices.add("Groomer");
            selectedServices.add("Walker");
            selectedServices.add("Vet");
            selectedServices.add("Pet Shop");
            selectedServices.add("Sitter");
            selectedServices.add("Shelter");
        }

        serviceFragment.clearRecyclerView(selectedServices);
        dismiss();
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
