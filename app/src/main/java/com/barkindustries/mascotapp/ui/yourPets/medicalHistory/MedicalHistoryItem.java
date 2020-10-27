package com.barkindustries.mascotapp.ui.yourPets.medicalHistory;

import androidx.annotation.NonNull;

public class MedicalHistoryItem {

    private String title, description, timestamp, medicine;
    private boolean medicineIsPresent, isImportant;

    public MedicalHistoryItem(String title, String description, String timestamp, @NonNull String medicineIsPresent,
                          String medicine, @NonNull String isImportant) {
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.medicineIsPresent = medicineIsPresent.equals("true");
        this.medicine = medicine;
        this.isImportant = isImportant.equals("yes");
    }

    // GETTERS
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getMedicine() {
        return medicine;
    }
    public boolean medicineIsPresent() {
        return medicineIsPresent;
    }
    public boolean isImportant() {
        return isImportant;
    }
}
