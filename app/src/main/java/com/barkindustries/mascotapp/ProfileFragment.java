package com.barkindustries.mascotapp;

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

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    // Screen controls
    private TextView username, email, phone, address, realName, editPhoto;
    private EditText editUsername, editPhone, editAddress, editRealName;
    private ImageView profilePicture;
    private FloatingActionButton editButton, doneEditingButton;

    // String
    private String usernameString, phoneString, addressString, realNameString;

    // used for dealing with photos
    private Uri profilePictureUri;
    private String profilePictureURL;
    private static final int GALLERY_INTENT = 1000;

    // Firebase
    private DatabaseReference databaseReference;
    private String userId;
    private FirebaseStorage mStorage;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        initScreenControls(view);

        hideScreenControls(view);

        getDataFromDatabase(view);
    }

    private void initScreenControls(@NonNull View view) {
        username = view.findViewById(R.id.owner_profile_username_data);
        email = view.findViewById(R.id.owner_profile_email_data);
        profilePicture = view.findViewById(R.id.photo_imageView);
        realName = view.findViewById(R.id.owner_profile_realName_data);
        address = view.findViewById(R.id.owner_profile_address_data);
        phone = view.findViewById(R.id.owner_profile_phone_data);

        editUsername = view.findViewById(R.id.owner_profile_username_editText);
        editRealName = view.findViewById(R.id.owner_profile_realName_editText);
        editAddress = view.findViewById(R.id.owner_profile_address_editText);
        editPhone = view.findViewById(R.id.owner_profile_phone_editText);
        editPhoto = view.findViewById(R.id.textView);


        profilePicture.setOnClickListener(v -> {
            //imagePicker.pickImage();
            Intent open_gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            open_gallery.setType("image/*");
            startActivityForResult(open_gallery, GALLERY_INTENT);
        });

        editButton = view.findViewById(R.id.edit_owner_profile_fab);
        doneEditingButton = view.findViewById(R.id.done_editing_owner_profile_fab);

        editButton.setOnClickListener(this::editProfile);
        doneEditingButton.setOnClickListener(this::doneEditingProfile);
    }

    private void hideScreenControls(@NonNull View view) {
        // profile pic
        profilePicture.setVisibility(View.INVISIBLE);
        editPhoto.setVisibility(View.INVISIBLE);

        // username controls
        username.setVisibility(View.INVISIBLE);
        editUsername.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.owner_profile_username).setVisibility(View.INVISIBLE);

        // real-name controls
        realName.setVisibility(View.INVISIBLE);
        editRealName.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.owner_profile_realName).setVisibility(View.INVISIBLE);

        // email controls
        email.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.owner_profile_email).setVisibility(View.INVISIBLE);

        // phone controls
        phone.setVisibility(View.INVISIBLE);
        editPhone.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.owner_profile_phone).setVisibility(View.INVISIBLE);

        // address controls
        address.setVisibility(View.INVISIBLE);
        editAddress.setVisibility(View.INVISIBLE);
        view.findViewById(R.id.owner_profile_address).setVisibility(View.INVISIBLE);
    }

    private void getDataFromDatabase(View view) {
        new Thread(() -> {
            databaseReference.child("Users").child(userId).addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(getActivity() == null)
                        return;
                    if(dataSnapshot.exists()) {

                        // username
                        String usernameText = Objects.requireNonNull(dataSnapshot.child("UsernameText").getValue()).toString();
                        username.setText(usernameText);

                        // email
                        String emailText = Objects.requireNonNull(dataSnapshot.child("Email").getValue()).toString();
                        email.setText(emailText);

                        // real-name
                        Object realNameObject = dataSnapshot.child("RealName").getValue();
                        if(realNameObject != null)
                            realName.setText(realNameObject.toString());
                        else
                            realName.setText("<Add a new Real Name>");

                        // phone
                        Object phoneObject = dataSnapshot.child("Phone").getValue();
                        if(phoneObject != null)
                            phone.setText(phoneObject.toString());
                        else
                            phone.setText("<Add a phone>");

                        // address
                        Object addressObject = dataSnapshot.child("Address").getValue();
                        if(addressObject != null)
                            address.setText(addressObject.toString());
                        else
                            address.setText("<Add an address>");

                        // profile pic
                        Object photoURL = dataSnapshot.child("profilePicURL").getValue();
                        if(photoURL != null) {
                            String photoURLstring = photoURL.toString();
                            Glide.with(ProfileFragment.this).load(photoURLstring).into(profilePicture);
                        }

                        view.findViewById(R.id.loading_owner_profile).setVisibility(View.GONE);
                        makeEverythingVisible(view);
                    } else {
                        errorInProfileData(view);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    errorInProfileData(view);
                }
            });
        }).start();
    }

    private void editProfile(View view) {
        usernameString = username.getText().toString();
        editUsername.setHint(usernameString);
        realNameString = realName.getText().toString();
        editRealName.setHint(realNameString);
        addressString = address.getText().toString();
        editAddress.setHint(addressString);
        phoneString = phone.getText().toString();
        editPhone.setHint(phoneString);

        editUsername.setVisibility(View.VISIBLE);
        editRealName.setVisibility(View.VISIBLE);
        editAddress.setVisibility(View.VISIBLE);
        editPhone.setVisibility(View.VISIBLE);
        editPhoto.setVisibility(View.VISIBLE);

        username.setVisibility(View.INVISIBLE);
        realName.setVisibility(View.INVISIBLE);
        address.setVisibility(View.INVISIBLE);
        phone.setVisibility(View.INVISIBLE);

        editButton.setVisibility(View.INVISIBLE);
        doneEditingButton.setVisibility(View.VISIBLE);
    }

    private void doneEditingProfile(View view) {

        if(!usernameString.equals(editUsername.getText().toString()) &&
                !editUsername.getText().toString().equals(""))
            changeField("UsernameText", editUsername.getText().toString());

        if(!realNameString.equals(editRealName.getText().toString()) &&
                !editRealName.getText().toString().equals(""))
            changeField("RealName", editRealName.getText().toString());

        if(!phoneString.equals(editPhone.getText().toString()) &&
                !editPhone.getText().toString().equals(""))
            changeField("Phone", editPhone.getText().toString());

        if(!addressString.equals(editAddress.getText().toString()) &&
                !editAddress.getText().toString().equals(""))
            changeField("Address", editAddress.getText().toString());

        editUsername.setText("");
        editUsername.setVisibility(View.INVISIBLE);
        username.setVisibility(View.VISIBLE);

        editRealName.setText("");
        editRealName.setVisibility(View.INVISIBLE);
        realName.setVisibility(View.VISIBLE);

        editPhone.setText("");
        editPhone.setVisibility(View.INVISIBLE);
        phone.setVisibility(View.VISIBLE);

        editAddress.setText("");
        editAddress.setVisibility(View.INVISIBLE);
        address.setVisibility(View.VISIBLE);

        doneEditingButton.setVisibility(View.INVISIBLE);
        editButton.setVisibility(View.VISIBLE);
        editPhoto.setVisibility(View.INVISIBLE);
    }

    private void changeField(String field, String newFieldValue) {
        new Thread(() -> {
            databaseReference.child("Users").child(userId).child(field).setValue(newFieldValue);
            requireActivity().runOnUiThread(() -> {
                switch(field) {
                    case "UsernameText":
                        editUsername.setVisibility(View.GONE);
                        username.setText(newFieldValue);
                        username.setVisibility(View.VISIBLE);
                        Toast.makeText(ProfileFragment.this.getContext(), "Username changed to " + newFieldValue, Toast.LENGTH_SHORT).show();
                        break;
                    case "RealName":
                        editRealName.setVisibility(View.GONE);
                        realName.setText(newFieldValue);
                        realName.setVisibility(View.VISIBLE);
                        Toast.makeText(ProfileFragment.this.getContext(), "Real name changed to " + newFieldValue, Toast.LENGTH_SHORT).show();
                        break;
                    case "Phone":
                        editPhone.setVisibility(View.GONE);
                        phone.setText(newFieldValue);
                        phone.setVisibility(View.VISIBLE);
                        Toast.makeText(ProfileFragment.this.getContext(), field + " changed to " + newFieldValue, Toast.LENGTH_SHORT).show();
                        break;
                    case "Address":
                        editAddress.setVisibility(View.GONE);
                        address.setText(newFieldValue);
                        address.setVisibility(View.VISIBLE);
                        Toast.makeText(ProfileFragment.this.getContext(), field + " changed to " + newFieldValue, Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        }).start();
    }

    private void makeEverythingVisible(@NonNull View view) {
        // pic control
        profilePicture.setVisibility(View.VISIBLE);

        // username controls
        view.findViewById(R.id.owner_profile_username).setVisibility(View.VISIBLE);
        username.setVisibility(View.VISIBLE);

        // email controls
        view.findViewById(R.id.owner_profile_email).setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);

        // address controls
        view.findViewById(R.id.owner_profile_address).setVisibility(View.VISIBLE);
        address.setVisibility(View.VISIBLE);

        // realName controls
        view.findViewById(R.id.owner_profile_realName).setVisibility(View.VISIBLE);
        realName.setVisibility(View.VISIBLE);

        // phone controls
        view.findViewById(R.id.owner_profile_phone).setVisibility(View.VISIBLE);
        phone.setVisibility(View.VISIBLE);
    }

    private void errorInProfileData(@NonNull View view) {
        view.findViewById(R.id.error_owner_profile).setVisibility(View.VISIBLE);
        view.findViewById(R.id.loading_owner_profile).setVisibility(View.GONE);
        Toast.makeText(getContext(), "Could not retrieve data", Toast.LENGTH_SHORT).show();
    }

    // Once an image has been selected from Gallery, we store its URI
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_INTENT){
            if(resultCode == RESULT_OK) {
                requireView().findViewById(R.id.loading_owner_profile).setVisibility(View.VISIBLE);
                requireView().findViewById(R.id.photo_imageView).setVisibility(View.GONE);
                //get image uri to set the Image View
                profilePictureUri = data.getData();

                final StorageReference filePath = mStorage.getReference("Pictures/ProfilePictures/" + userId + "/" + "ProfilePic" + ".jpg");
                // we make a few steps to obtains the photo URL
                filePath.putFile(profilePictureUri).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return filePath.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        // we have the URL !!
                        profilePictureURL = Objects.requireNonNull(task.getResult()).toString();
                        databaseReference.child("Users").child(userId).child("profilePicURL").setValue(profilePictureURL);
                        profilePicture.setImageURI(profilePictureUri);
                        requireView().findViewById(R.id.loading_owner_profile).setVisibility(View.GONE);
                        requireView().findViewById(R.id.photo_imageView).setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }
}
