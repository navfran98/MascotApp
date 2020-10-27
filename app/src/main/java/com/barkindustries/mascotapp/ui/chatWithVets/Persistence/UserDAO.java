package com.barkindustries.mascotapp.ui.chatWithVets.Persistence;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Firebase.ServiceProvider;
import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Firebase.User;
import com.barkindustries.mascotapp.ui.chatWithVets.Entities.Logic.LUser;
import com.barkindustries.mascotapp.ui.chatWithVets.Utilities.Constants;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserDAO {

    public interface IReturnUser {
        public void returnUser(LUser lUser);
        public void returnError(String error);
    }

    public interface IReturnUrlPicture {
        public void returnUrlString(String url);
    }

    private static UserDAO userDAO;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private DatabaseReference usersReference;
    private StorageReference profilePicReference;

    public static UserDAO getInstance(){
        if(userDAO == null)
            userDAO = new UserDAO();
        return userDAO;
    }

    private UserDAO(){
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        usersReference = database.getReference(Constants.USERS_NODE);
        profilePicReference = storage.getReference("Pictures/ProfilePictures"+ getUserKey());
    }

    public String getUserKey(){
        return FirebaseAuth.getInstance().getUid();
    }

    public boolean isUserLogged(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        return firebaseUser!=null;
    }

    public long creationDateLong(){
        return FirebaseAuth.getInstance().getCurrentUser().getMetadata().getCreationTimestamp();
    }

    public long lastLoginDateLong(){
        return FirebaseAuth.getInstance().getCurrentUser().getMetadata().getLastSignInTimestamp();
    }

    public void getUserInformationFromKey(final String key, final IReturnUser iReturnUser, Boolean isService, String serviceType) {

        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isService) {
                    User user = snapshot.child(Constants.USERS_NODE).child(key).getValue(User.class);
                    LUser lUser = new LUser(key, user);
                    iReturnUser.returnUser(lUser);
                } else {
                    User user = snapshot.child(Constants.SERVICES_NODE).child(serviceType).child(key).getValue(User.class);
                    LUser lUser = new LUser(key, user);
                    iReturnUser.returnUser(lUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                iReturnUser.returnError(error.getMessage());
            }
        });



    }

    public void addProfilePicToProfilesWithoutOne(){
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<LUser> lUsersList = new ArrayList<>();
                for(DataSnapshot childDataSnapShot : dataSnapshot.getChildren()){
                    User user = childDataSnapShot.getValue(User.class);
                    LUser lUser = new LUser(childDataSnapShot.getKey(),user);
                    lUsersList.add(lUser);
                }

                for(LUser lUser : lUsersList){
                    if(lUser.getUser().getProfilePicURL() == null){
                        usersReference.child(lUser.getKey()).child(Constants.PROFILE_PIC_URL).setValue(Constants.DEFAULT_URI_PROFILE_PIC);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void uploadPhotoUri(Uri uri, final IReturnUrlPicture iReturnUrlPicture){
        String photoName = "";
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("SSS.ss-mm-hh-dd-MM-yyyy", Locale.getDefault());
        photoName = simpleDateFormat.format(date);
        final StorageReference photoReference = profilePicReference.child(photoName);
        photoReference.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return photoReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri uri = task.getResult();
                    iReturnUrlPicture.returnUrlString(uri.toString());
                }
            }
        });
    }

}
