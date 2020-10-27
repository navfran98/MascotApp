package com.barkindustries.mascotapp.ui.yourPets;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.barkindustries.mascotapp.R;
import com.barkindustries.mascotapp.ui.chatWithVets.Utilities.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class AddPetFragment extends Fragment {

    private EditText name,  breed, weight;
    private TextView birthday;
    private Button addPetBtn, uploadPhotoBtn, cancelBtn;
    private FloatingActionButton selectDateBtn;
    private Switch genderSwitch;
    private ImageView profile_photo;
    private ProgressBar progressBar;

    private Long petsAmount;
    private String petId;
    private String nameString, ageString, breedString, weightString, birthdayString;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private String userId;

    // used for dealing with photos
    private Uri petPhotoUri;
    private String petPhotoURL; // will be stored in DB, so that PetProfileFragment can take it
    private static final int GALLERY_INTENT = 1000;




    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_pet, container, false);
    }


    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        userId = mAuth.getUid();


        name = view.findViewById(R.id.add_pet_name);
        breed = view.findViewById(R.id.add_pet_breed);
        weight = view.findViewById(R.id.add_pet_weight);
        birthday = view.findViewById(R.id.tv_birthday);
        genderSwitch = view.findViewById(R.id.gender_switch);
        addPetBtn = view.findViewById(R.id.add_pet_btn);
        uploadPhotoBtn = view.findViewById(R.id.add_pet_upload_btn);
        profile_photo = view.findViewById(R.id.add_petphoto_img);
        cancelBtn = view.findViewById(R.id.add_cancel_btn);

        progressBar = view.findViewById(R.id.progress_bar);

        progressBar.setVisibility(View.INVISIBLE);

        selectDateBtn = view.findViewById(R.id.calendar_button);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        selectDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        R.style.DatePicker, datePickerListener,year,month,day);
                datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                datePickerDialog.show();
            }
        });


        //UPLOAD PHOTO BUTTON : opens gallery
        uploadPhotoBtn.setOnClickListener(v -> {
            //imagePicker.pickImage();
            Intent open_gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            open_gallery.setType("image/*");
            startActivityForResult(open_gallery, GALLERY_INTENT);
        });

        //ADD PET BUTTON
        addPetBtn.setOnClickListener(this::addPetClicked);
        cancelBtn.setOnClickListener(this::cancel);
    }


    private void addPetClicked(View view) {


        nameString = name.getText().toString();
        breedString = breed.getText().toString();
        weightString = weight.getText().toString();

        if(nameString.isEmpty() || ageString == null|| breedString.isEmpty() || weightString.isEmpty() || birthdayString == null){
            Toast.makeText(getContext(), "Please complete empty data", Toast.LENGTH_SHORT).show();
        }
        else if(validateData()){
            addPetBtn.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            addPet(view);
        }
    }

    private boolean validateData(){
        if(!nameString.matches("[a-zA-Z ]+")) {
            Toast.makeText(getContext(), "Invalid name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!breedString.matches("[a-zA-Z ]+")) {
            Toast.makeText(getContext(), "Invalid breed", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(Integer.parseInt(weightString) > 100) {
            Toast.makeText(getContext(), "Is your pet really over 100Kg?", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void cancel(View view) {
        final NavController navController =  Navigation.findNavController(view);
        navController.navigate(R.id.nav_your_pets);
    }

    private void addPet(final View view){
        new Thread(() -> {
            mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Long aux = (Long) dataSnapshot.child("PetsAmount").getValue();
                    if(aux != null)
                        petsAmount = aux + 1;
                    else
                        petsAmount = 1L;
                    mDatabase.child("Users").child(userId).child("PetsAmount").setValue(petsAmount);

                    petId = "PET" + petsAmount + "~" + userId;

                    if(petPhotoUri != null) {
                        // creates a path to where the pet-photo will be stored --> Example: Pictures/ProfilePictures/userID/PET3.jpg
                        final StorageReference filePath = mStorage.getReference("Pictures/ProfilePictures/" + userId + "/" + "PET" + petsAmount + ".jpg");
                        // we make a few steps to obtains the photo URL
                        filePath.putFile(petPhotoUri).continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return filePath.getDownloadUrl();
                        }).addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                // we have the URL !!
                                petPhotoURL = task.getResult().toString();
                                savePetInDatabase(view);
                            }
                        });
                    } else
                        savePetInDatabase(view);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Error adding new Pet", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void savePetInDatabase(View view) {
        mDatabase.child("Users").child(userId).child("Pets").child(petId).setValue(petId);
        Map<String, Object> map = new HashMap<>();
        map.put("Owner", userId);
        map.put("Name", nameString);
        map.put("Age", ageString);
        map.put("Breed", breedString);
        map.put("Weight", weightString);
        map.put("Gender", genderSwitch.isChecked() ? "Female" : "Male");
        map.put("Birthday", birthdayString);
        map.put("IsLost", "false");
        if(petPhotoURL != null)
            map.put("PhotoURL", petPhotoURL);   // useful for fast photo-retrieving in PetProfileFragment
        else
            map.put("PhotoURL", Constants.DEFAULT_URI_PET_PIC);
        //Save new Pet in Database
        mDatabase.child("Pets").child(petId).setValue(map).addOnCompleteListener(savePet -> {
            if (savePet.isSuccessful()) {
                Toast.makeText(getContext(), "Pet added", Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                bundle.putString("petId", petId);
                // we go back to YourPetsFragment
                progressBar.setVisibility(View.INVISIBLE);
                final NavController navController = Navigation.findNavController(view);
                navController.navigateUp();
            } else {
                Toast.makeText(getContext(), "Error adding new Pet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Once an image has been selected from Gallery, we store its URI
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_INTENT){
            if(resultCode == RESULT_OK) {
                //get image uri to set the Image View
                petPhotoUri = data.getData();
                profile_photo.setImageURI(petPhotoUri);
            }
        }
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH,day);
            birthdayString = new SimpleDateFormat("dd MMM YYYY").format(c.getTime());
            birthday.setText(birthdayString);
            ageString = Integer.toString(calculateAge(c.getTimeInMillis()));
        }
    };

    int calculateAge(long date){
        Calendar birth = Calendar.getInstance();
        birth.setTimeInMillis(date);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if(today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))
            age--;
        System.out.println(age);
        return age;
    }
}
