package com.barkindustries.mascotapp.ui.yourPets;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.barkindustries.mascotapp.R;
import com.bumptech.glide.Glide;
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
import java.util.Calendar;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class PetProfileFragment extends Fragment {

    private TextView nameTextView, ageTextView, breedTextView, weightTextView, birthdayTextView, genderTextView, lostTextView;
    private ImageView photoImageView;
    private CardView ageCardView, breedCardView, weightCardView, birthdayCardView, genderCardView, lostCardView;
    private ProgressBar loadingProgressBar, photoProgressBar;

    private FloatingActionButton medicalHistoryButton, deleteButton;

    private String newValue = "";
    private AlertDialog.Builder builder;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private String userId;

    // used for dealing with photos
    private Uri petPhotoUri;
    private String petPhotoURL; // will be stored in DB, so that PetProfileFragment can take it
    private static final int GALLERY_INTENT = 1000;

    private String petId;
    private Boolean deleted = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pet_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        userId = mAuth.getUid();

        nameTextView = view.findViewById(R.id.name_textView);

        ageTextView = view.findViewById(R.id.age_textView);
        breedTextView = view.findViewById(R.id.breed_textView);
        weightTextView = view.findViewById(R.id.weight_textView);
        birthdayTextView = view.findViewById(R.id.birthday_textView);
        genderTextView = view.findViewById(R.id.gender_textView);
        lostTextView = view.findViewById(R.id.lost_textView);
        photoImageView = view.findViewById(R.id.photo_imageView);

        ageCardView = view.findViewById(R.id.age_cardView);
        breedCardView = view.findViewById(R.id.breed_cardView);
        weightCardView = view.findViewById(R.id.weight_cardView);
        birthdayCardView = view.findViewById(R.id.birthday_cardView);
        genderCardView = view.findViewById(R.id.gender_cardView);
        lostCardView = view.findViewById(R.id.lost_cardView);

        loadingProgressBar = view.findViewById(R.id.loading_progressBar);
        photoProgressBar = view.findViewById(R.id.photo_progressBar);

        medicalHistoryButton = view.findViewById(R.id.pet_doc_button);
        deleteButton = view.findViewById(R.id.delete_floatingActionButton);

        nameTextView.setVisibility(View.INVISIBLE);
        ageCardView.setVisibility(View.INVISIBLE);
        breedCardView.setVisibility(View.INVISIBLE);
        weightCardView.setVisibility(View.INVISIBLE);
        birthdayCardView.setVisibility(View.INVISIBLE);
        genderCardView.setVisibility(View.INVISIBLE);
        lostCardView.setVisibility(View.GONE);
        photoProgressBar.setVisibility(View.INVISIBLE);

        nameTextView.setOnClickListener(v -> nameTextPressed());
        breedCardView.setOnClickListener(v -> breedCardPressed());
        weightCardView.setOnClickListener(v -> weightCardPressed());
        birthdayCardView.setOnClickListener(v -> birthdayCardPressed());
        genderCardView.setOnClickListener(v -> genderCardPressed());
        lostCardView.setOnClickListener(v -> lostCardPressed());
        photoImageView.setOnClickListener(v -> photoPressed());
        deleteButton.setOnClickListener(v -> deleteButtonPressed());

        medicalHistoryButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("petId", petId);
            bundle.putString("petName", nameTextView.getText().toString());
            final NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.medicalHistory, bundle);
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            petId = bundle.getString("petId");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Pets").child(petId);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(getActivity() == null || deleted)
                    return;
                if (dataSnapshot.exists()) {
                    nameTextView.setText(dataSnapshot.child("Name").getValue().toString());
                    ageTextView.setText(dataSnapshot.child("Age").getValue().toString() + " years old");
                    breedTextView.setText(dataSnapshot.child("Breed").getValue().toString());
                    weightTextView.setText(dataSnapshot.child("Weight").getValue().toString() + "Kg");
                    birthdayTextView.setText(dataSnapshot.child("Birthday").getValue().toString());
                    genderTextView.setText(dataSnapshot.child("Gender").getValue().toString());

                    if(dataSnapshot.child("IsLost").getValue() == null || dataSnapshot.child("IsLost").getValue().toString().equals("false")) {

                    } else {
                        lostCardView.setVisibility(View.VISIBLE);
                    }


                    Object photoUrlObject = dataSnapshot.child("PhotoURL").getValue();
                    if (photoUrlObject != null) {
                        String photoURL = photoUrlObject.toString();
                        photoProgressBar.setVisibility(View.INVISIBLE);
                        Glide.with(PetProfileFragment.this).load(photoURL).into(photoImageView);
                    }

                    nameTextView.setVisibility(View.VISIBLE);
                    ageCardView.setVisibility(View.VISIBLE);
                    breedCardView.setVisibility(View.VISIBLE);
                    weightCardView.setVisibility(View.VISIBLE);
                    birthdayCardView.setVisibility(View.VISIBLE);
                    genderCardView.setVisibility(View.VISIBLE);

                    loadingProgressBar.setVisibility(View.INVISIBLE);
                } else
                    Toast.makeText(getContext(), "Could not retrieve data", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Could not retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void delete(View v){
            DatabaseReference mdb = FirebaseDatabase.getInstance().getReference();
            mdb.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Long petsAmount;
                    Long aux = (Long) dataSnapshot.child("PetsAmount").getValue();
                    petsAmount = aux - 1;
                    mdb.child("Users").child(userId).child("PetsAmount").setValue(petsAmount);
                    mdb.child("Users").child(userId).child("Pets").child(petId).removeValue();
                    mdb.child("LostPets").child(petId).removeValue();
                    mdb.child("Pets").child(petId).removeValue();
                    deleted = true;
                    Toast.makeText(getContext(), nameTextView.getText() + " deleted!", Toast.LENGTH_SHORT).show();
                    final NavController navController = Navigation.findNavController(v);
                    navController.navigate(R.id.nav_your_pets);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Error deleting Pet", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void deleteButtonPressed() {
        AlertDialog ad = new AlertDialog.Builder(getActivity())
                .create();
        ad.setCancelable(false);
        ad.setTitle("Are you sure?");
        ad.setMessage("Removing " + nameTextView.getText() + " will permanently delete all its information, including medical history, stats, etc.");
        ad.setButton(AlertDialog.BUTTON_NEGATIVE,"Delete", (dialog, which) -> delete(getView()));
        ad.setButton(AlertDialog.BUTTON_POSITIVE, "Cancel", (dialog, which) -> dialog.dismiss());
        ad.show();
    }

    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    private void editPetItemHelper(String field, String title, String message, int inputType) {
        builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(message);

        final EditText input = new EditText(getContext());
        input.setInputType(inputType);
        builder.setView(input);

        builder.setPositiveButton("Accept", (dialog, which) -> {
            boolean shouldEdit = true;

            newValue = input.getText().toString();
            if(newValue.length() == 0) {
                shouldEdit = false;
                Toast.makeText(getContext(), "You must enter something!", Toast.LENGTH_SHORT).show();
            } else
                switch (field) {
                    case "Name":
                    case "Breed":
                        if(!newValue.matches("[a-zA-Z ]+")) {
                            shouldEdit = false;
                            Toast.makeText(getContext(), "You must enter only letters and spaces!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "Weight":
                        if(!newValue.matches("0*[1-9]+[0-9]*")) {
                            shouldEdit = false;
                            Toast.makeText(getContext(), "You must enter a number without decimals!", Toast.LENGTH_SHORT).show();
                        }
                        if(Integer.parseInt(newValue) > 100) {
                            shouldEdit = false;
                            Toast.makeText(getContext(), "Is your pet really over 100Kg?", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            if(shouldEdit)
                editPetItem(field, newValue);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Once an image has been selected from Gallery, we store its URI
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_INTENT){
            if(resultCode == RESULT_OK) {
                //get image uri to set the Image View
                photoImageView.setVisibility(View.INVISIBLE);
                photoProgressBar.setVisibility(View.VISIBLE);
                petPhotoUri = data.getData();

                if(petPhotoUri != null) {
                    String aux = petId.substring(0, petId.indexOf("~"));
                    // creates a path to where the pet-photo will be stored --> Example: Pictures/ProfilePictures/userID/PET3.jpg
                    final StorageReference filePath = mStorage.getReference("Pictures/ProfilePictures/" + userId + "/" + aux+ ".jpg");
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
                            mDatabase.child("PhotoURL").setValue(petPhotoURL);
                            photoImageView.setImageURI(petPhotoUri);
                            photoImageView.setVisibility(View.VISIBLE);
                            photoProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getContext(), "Photo updated!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        }
    }

    private void nameTextPressed() {
        editPetItemHelper("Name", "Change name", "New name:", InputType.TYPE_CLASS_TEXT);
    }



    private void breedCardPressed() {
        editPetItemHelper("Breed", "Change breed", "New breed:", InputType.TYPE_CLASS_TEXT);
    }

    private void weightCardPressed() {
        editPetItemHelper("Weight", "Change weight", "New weight (in Kg, without decimals):", InputType.TYPE_CLASS_NUMBER);

    }

    private void birthdayCardPressed() {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                R.style.DatePicker, datePickerListener,year,month,day);
        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        datePickerDialog.show();

    }

    private void genderCardPressed() {
        builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Change gender");

        builder.setPositiveButton("Male", (dialog, which) -> editPetItem("Gender", "Male"));
        builder.setNegativeButton("Female", (dialog, which) -> editPetItem("Gender", "Female"));

        builder.show();
    }

    private void lostCardPressed() {
        builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Missing!");

        builder.setPositiveButton("Found it!", (dialog, which) -> editPetItem("IsLost", "No :)"));

        builder.show();
    }

    private void photoPressed() {
        Intent open_gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        open_gallery.setType("image/*");
        startActivityForResult(open_gallery, GALLERY_INTENT);
    }

    private void editPetItem(String field, String newValue){
        String aux;
        if(field.equals("IsLost")) {
            aux = "false";
            DatabaseReference mdb = FirebaseDatabase.getInstance().getReference().child("LostPets").child(petId);
            mdb.removeValue();
            lostCardView.setVisibility(View.GONE);
            Toast.makeText(getContext(), nameTextView.getText() + " was found. Great! :)", Toast.LENGTH_SHORT).show();
        } else {
            aux = newValue;
        }
        new Thread(() -> {

            mDatabase.child(field).setValue(aux).addOnCompleteListener(savePet -> {
                if (savePet.isSuccessful()) {
                     if(field != "IsLost")
                        Toast.makeText(getContext(), field + " changed to " + aux, Toast.LENGTH_SHORT).show();

                    switch (field) {
                        case "Name":
                            nameTextView.setText(newValue);
                            break;
                        case "Age":
                            ageTextView.setText(newValue + " years old");
                            break;
                        case "Breed":
                            breedTextView.setText(newValue);
                            break;
                        case "Weight":
                            weightTextView.setText(newValue + "Kg");
                            break;
                        case "Birthday":
                            birthdayTextView.setText(newValue);
                            break;
                        case "Gender":
                            genderTextView.setText(newValue);
                            break;
                        case "Lost":
                            lostTextView.setText(newValue);
                            break;
                    }
                } else {
                    Toast.makeText(getContext(), "Error editing field " + field, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH,day);
            editPetItem("Birthday", new SimpleDateFormat("dd MMM YYYY").format(c.getTime()));
            editPetItem("Age", Integer.toString(calculateAge(c.getTimeInMillis())));
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

