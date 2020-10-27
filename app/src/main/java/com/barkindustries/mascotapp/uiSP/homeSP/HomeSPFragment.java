package com.barkindustries.mascotapp.uiSP.homeSP;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import java.text.DecimalFormat;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class HomeSPFragment extends Fragment {

    // Screen Controls
    private TextView businessName, businessMotto, businessPhone, businessEmail, businessAddress, rating, editPhoto;
    private EditText editName, editMotto, editPhone, editAddress;
    private ImageView businessPicture, star1, star2, star3, star4, star5;
    private FloatingActionButton editButton, doneEditingButton;

    private String serviceType;
    private String db_stars;

    // Photo variables
    private static final int GALLERY_INTENT = 1000;
    private Uri businessPictureUri;
    private String businessPictureURL;

    // String
    private String nameString, mottoString, phoneString, addressString;

    // Firebase
    private FirebaseStorage mStorage;
    private DatabaseReference databaseReference;
    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_sp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        initScreenControls(view);

        hideEverything(view);

        getDataFromDatabase(view);
    }

    private void initScreenControls(@NonNull View view) {
        businessName = view.findViewById(R.id.name_textView);
        businessMotto = view.findViewById(R.id.motto_textView);
        businessPhone = view.findViewById(R.id.phone_textView);
        businessEmail = view.findViewById(R.id.email_textView);
        businessAddress = view.findViewById(R.id.address_textView);
        businessPicture = view.findViewById(R.id.photo_imageView);
        rating = view.findViewById(R.id.rating_textView);
        editPhoto = view.findViewById(R.id.textView);


        editName = view.findViewById(R.id.name_editText);
        editMotto = view.findViewById(R.id.motto_editText);
        editPhone = view.findViewById(R.id.phone_editText);
        editAddress = view.findViewById(R.id.address_editText);

        editButton = view.findViewById(R.id.edit_my_business_fab);
        editButton.setOnClickListener(this::editBusiness);

        doneEditingButton = view.findViewById(R.id.done_editing_my_business_fab);
        doneEditingButton.setOnClickListener(this::doneEditingBusiness);

        businessPicture.setOnClickListener(v -> {
            //imagePicker.pickImage();
            Intent open_gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            open_gallery.setType("image/*");
            startActivityForResult(open_gallery, GALLERY_INTENT);
        });

        // Rating controls
        star1 = view.findViewById(R.id.ratings_star1_off);
        star2 = view.findViewById(R.id.ratings_star2_off);
        star3 = view.findViewById(R.id.ratings_star3_off);
        star4 = view.findViewById(R.id.ratings_star4_off);
        star5 = view.findViewById(R.id.ratings_star5_off);
    }

    private void hideEverything(@NonNull View view) {
        // hide name
        businessName.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.name_editText).setVisibility(View.INVISIBLE);

        // hide motto
        businessMotto.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.motto_editText).setVisibility(View.INVISIBLE);

        // hide phone
        businessPhone.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.aux_textView).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.phone_editText).setVisibility(View.INVISIBLE);

        // hide email
        businessEmail.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.aux3_textView).setVisibility(View.INVISIBLE);

        // hide address
        businessAddress.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.aux2_textView).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.address_editText).setVisibility(View.INVISIBLE);

        // hide picture
        businessPicture.setVisibility(View.INVISIBLE);
        editPhoto.setVisibility(View.INVISIBLE);

        // hide fab
        editButton.setVisibility(View.INVISIBLE);

        // hide ratings
        rating.setVisibility(View.INVISIBLE);
        star1.setVisibility(View.INVISIBLE);
        star2.setVisibility(View.INVISIBLE);
        star3.setVisibility(View.INVISIBLE);
        star4.setVisibility(View.INVISIBLE);
        star5.setVisibility(View.INVISIBLE);
    }

    private void getDataFromDatabase(View view) {
        final boolean[] found = {false};
        new Thread(() -> {
            databaseReference.child("Services").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        for (DataSnapshot typeOfService : snapshot.getChildren()) {
                            for (DataSnapshot serviceProvider : typeOfService.getChildren()) {
                                if (Objects.equals(serviceProvider.getKey(), userId)) {

                                    found[0] = true;
                                    serviceType = typeOfService.getKey();
                                    ServiceProvider sp = serviceProvider.getValue(ServiceProvider.class);
                                    if (sp != null) {

                                        String nameString = "<No name>";
                                        String mottoString = "<No motto>";
                                        String phoneString = "<No phone>";
                                        String addressString = "<No address>";
                                        String emailString = "<No email>";

                                        if (sp.getBusinessName() != null)
                                            nameString = sp.getBusinessName();
                                        businessName.setText(nameString);

                                        if (sp.getMotto() != null)
                                            mottoString = sp.getMotto();
                                        businessMotto.setText(mottoString);

                                        if (sp.getPhone() != null)
                                            phoneString = sp.getPhone();
                                        businessPhone.setText(phoneString);

                                        if (sp.getAddress() != null)
                                            addressString = sp.getAddress();
                                        businessAddress.setText(addressString);

                                        if (sp.getEmail() != null)
                                            emailString = sp.getEmail();
                                        businessEmail.setText(emailString);

                                        long count = (long) serviceProvider.child("starCounter").getValue();
                                        long starSum = (long) serviceProvider.child("Stars").getValue();
                                        double starsAvg = (double) starSum / count;
                                        rating.setText(new DecimalFormat("#.#").format(starsAvg) + " out of 5");

                                        setStarsAmount(starsAvg);

                                        Glide.with(HomeSPFragment.this).load(sp.getProfilePicURL()).into(businessPicture);

                                        showInfo(view);
                                    } else {
                                        view.findViewById(R.id.error_textView).setVisibility(View.VISIBLE);
                                    }
                                    view.findViewById(R.id.loading).setVisibility(View.INVISIBLE);
                                    break;
                                }
                            }
                            if (found[0])
                                break;
                        }
                    } else
                        showError(view);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showError(view);
                }
            });
        }).start();
    }

    private void setStarsAmount(Double rating) {
        if(rating >= 1)
            star1.setImageResource(R.drawable.ic_star_yellow);
        if(rating >= 2)
            star2.setImageResource(R.drawable.ic_star_yellow);
        if(rating >= 3)
            star3.setImageResource(R.drawable.ic_star_yellow);
        if(rating >= 4)
            star4.setImageResource(R.drawable.ic_star_yellow);
        if(rating >= 5)
            star5.setImageResource(R.drawable.ic_star_yellow);
    }

    private void showError(@NonNull View view) {
        view.findViewById(R.id.loading).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.error_textView).setVisibility(View.VISIBLE);
    }

    private void showInfo(@NonNull View view) {
        businessName.setVisibility(View.VISIBLE);
        businessMotto.setVisibility(View.VISIBLE);
        businessPhone.setVisibility(View.VISIBLE);
        businessAddress.setVisibility(View.VISIBLE);
        businessEmail.setVisibility(View.VISIBLE);
        view.findViewById(R.id.aux_textView).setVisibility(View.VISIBLE);
        view.findViewById(R.id.aux2_textView).setVisibility(View.VISIBLE);
        view.findViewById(R.id.aux3_textView).setVisibility(View.VISIBLE);
        businessPicture.setVisibility(View.VISIBLE);
        editButton.setVisibility(View.VISIBLE);
        rating.setVisibility(View.VISIBLE);
        star1.setVisibility(View.VISIBLE);
        star2.setVisibility(View.VISIBLE);
        star3.setVisibility(View.VISIBLE);
        star4.setVisibility(View.VISIBLE);
        star5.setVisibility(View.VISIBLE);
    }

    private void editBusiness(View view) {
        nameString = businessName.getText().toString();
        editName.setHint(nameString);
        mottoString = businessMotto.getText().toString();
        editMotto.setHint(mottoString);
        phoneString = businessPhone.getText().toString();
        editPhone.setHint(phoneString);
        addressString = businessAddress.getText().toString();
        editAddress.setHint(addressString);

        editName.setVisibility(View.VISIBLE);
        editMotto.setVisibility(View.VISIBLE);
        editPhone.setVisibility(View.VISIBLE);
        editAddress.setVisibility(View.VISIBLE);
        editPhoto.setVisibility(View.VISIBLE);

        businessName.setVisibility(View.INVISIBLE);
        businessMotto.setVisibility(View.INVISIBLE);
        businessPhone.setVisibility(View.INVISIBLE);
        businessAddress.setVisibility(View.INVISIBLE);

        editButton.setVisibility(View.INVISIBLE);
        doneEditingButton.setVisibility(View.VISIBLE);
    }

    private void doneEditingBusiness(View view) {

        if(!nameString.equals(editName.getText().toString()) && editName.getText().toString().length() != 0) {
            changeField("BusinessName", editName.getText().toString());
            editName.setText("");
        }


        if(!mottoString.equals(editMotto.getText().toString()) && editMotto.getText().toString().length() != 0) {
            changeField("Motto", editMotto.getText().toString());
            editMotto.setText("");

        }

        if(!phoneString.equals(editPhone.getText().toString()) && editPhone.getText().toString().length() != 0) {
            changeField("Phone", editPhone.getText().toString());
            editPhone.setText("");

        }

        if(!addressString.equals(editAddress.getText().toString()) && editAddress.getText().toString().length() != 0) {
            changeField("Address", editAddress.getText().toString());
            editAddress.setText("");

        }

        editName.setVisibility(View.INVISIBLE);
        businessName.setVisibility(View.VISIBLE);

        editMotto.setVisibility(View.INVISIBLE);
        businessMotto.setVisibility(View.VISIBLE);

        editPhone.setVisibility(View.INVISIBLE);
        businessPhone.setVisibility(View.VISIBLE);

        editAddress.setVisibility(View.INVISIBLE);
        businessAddress.setVisibility(View.VISIBLE);

        editPhoto.setVisibility(View.INVISIBLE);

        doneEditingButton.setVisibility(View.INVISIBLE);
        editButton.setVisibility(View.VISIBLE);
    }

    private void changeField(String field, String newFieldValue) {
        new Thread(() -> {
            databaseReference.child("Services").child(serviceType).child(userId).child(field).setValue(newFieldValue);
            requireActivity().runOnUiThread(() -> {
                switch(field) {
                    case "BusinessName":
                        editName.setVisibility(View.INVISIBLE);
                        businessName.setText(newFieldValue);
                        businessName.setVisibility(View.VISIBLE);
                        Toast.makeText(HomeSPFragment.this.getContext(), "Business name changed to " + newFieldValue, Toast.LENGTH_LONG).show();
                        break;
                    case "Motto":
                        editMotto.setVisibility(View.INVISIBLE);
                        businessMotto.setText(newFieldValue);
                        businessMotto.setVisibility(View.VISIBLE);
                        Toast.makeText(HomeSPFragment.this.getContext(), field + " changed to " + newFieldValue, Toast.LENGTH_LONG).show();
                        break;
                    case "Phone":
                        editPhone.setVisibility(View.INVISIBLE);
                        businessPhone.setText(newFieldValue);
                        businessPhone.setVisibility(View.VISIBLE);
                        Toast.makeText(HomeSPFragment.this.getContext(), field + " changed to " + newFieldValue, Toast.LENGTH_LONG).show();
                        break;
                    case "Address":
                        editAddress.setVisibility(View.INVISIBLE);
                        businessAddress.setText(newFieldValue);
                        businessAddress.setVisibility(View.VISIBLE);
                        Toast.makeText(HomeSPFragment.this.getContext(), field + " changed to " + newFieldValue, Toast.LENGTH_LONG).show();
                        break;
                }
            });
        }).start();
    }

    // Once an image has been selected from Gallery, we store its URI
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT) {
            if (resultCode == RESULT_OK) {
                businessPicture.setVisibility(View.INVISIBLE);
                requireView().findViewById(R.id.loading).setVisibility(View.VISIBLE);

                //get image uri to set the Image View
                businessPictureUri = data.getData();

                final StorageReference filePath = mStorage.getReference("Pictures/ProfilePictures/" + userId + "/" + "ProfilePic" + ".jpg");
                // we make a few steps to obtains the photo URL
                filePath.putFile(businessPictureUri).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return filePath.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        // we have the URL !!
                        businessPictureURL = Objects.requireNonNull(task.getResult()).toString();
                        databaseReference.child("Services").child(serviceType).child(userId).child("profilePicURL").setValue(businessPictureURL);
                        businessPicture.setImageURI(businessPictureUri);
                        requireView().findViewById(R.id.loading).setVisibility(View.INVISIBLE);
                        businessPicture.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }
}
