package com.barkindustries.mascotapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.barkindustries.mascotapp.ui.chatWithVets.Utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LogInActivity extends AppCompatActivity {

    // Screen controls
    private EditText email, password;
    private Button logInButton;
    private TextView signUpButton;
    private ProgressBar loading;

    private String emailString, passwordString;

    private FirebaseAuth fAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        if(fAuth.getCurrentUser() != null){
            loading = findViewById(R.id.progressBar3);
            setContentView(R.layout.loading_layout);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if( snapshot.child("Users").hasChild(fAuth.getUid())){
                        startActivity(new Intent(LogInActivity.this, HomeActivity.class));
                    }
                    else {
                        Intent intent = new Intent(LogInActivity.this, HomeSPActivity.class);
                        for(DataSnapshot ds : snapshot.child(Constants.SERVICES_NODE).getChildren()){
                            if(ds.hasChild(fAuth.getUid())){
                                intent.putExtra("Type", ds.getKey());
                            }
                        }

                        startActivity(intent);
                    }
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        }
        else {
            setContentView(R.layout.activity_login);

            ActionBar actionBar = getSupportActionBar();
            assert actionBar != null;
            actionBar.hide();

            initScreenControls();


            loading = findViewById(R.id.loading);
            loading.setVisibility(View.INVISIBLE);
        }
    }

    private void initScreenControls() {
        email = findViewById(R.id.email_input_from_login);
        password = findViewById(R.id.password_input_from_login);
        logInButton = findViewById(R.id.log_in_button_from_login);
        signUpButton = findViewById(R.id.login_signup_btn);

        logInButton.setOnClickListener(v -> {
            if(email.getText().toString().length() == 0) {
                Toast.makeText(this, "Email missing!", Toast.LENGTH_SHORT).show();
            } else if(password.getText().toString().length() == 0) {
                Toast.makeText(this, "Password missing!", Toast.LENGTH_SHORT).show();
            } else {
                emailString = email.getText().toString();
                passwordString = password.getText().toString();
                logIn();
            }

        });

        signUpButton.setOnClickListener(v -> startActivity(new Intent(LogInActivity.this, SignUpActivity.class)));
    }
    
    private void logIn(){
        logInButton.setVisibility(View.INVISIBLE);
        loading.setVisibility(View.VISIBLE);
        new Thread(() -> {
            final boolean[] found = {false};
            fAuth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                for(DataSnapshot user : snapshot.child("Users").getChildren()) {
                                    if(Objects.equals(user.getKey(), fAuth.getUid())) {
                                        loading.setVisibility(View.GONE);
                                        found[0] = true;
                                        startActivity(new Intent(LogInActivity.this, HomeActivity.class));
                                        finish();
                                    }
                                }
                                if(!found[0]) {
                                    for(DataSnapshot serviceType : snapshot.child("Services").getChildren()) {
                                            if(serviceType.hasChild(fAuth.getUid())) {
                                                loading.setVisibility(View.GONE);
                                                found[0] = true;

                                                Intent intent = new Intent(LogInActivity.this, HomeSPActivity.class);
                                                intent.putExtra("Type", serviceType.getKey());

                                                startActivity(intent);
                                                finish();
                                            }

                                    }
                                }
                                if(!found[0]) {
                                    Toast.makeText(LogInActivity.this, "Could not log in", Toast.LENGTH_SHORT).show();
                                    loading.setVisibility(View.GONE);
                                    logInButton.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            loading.setVisibility(View.GONE);
                            logInButton.setVisibility(View.VISIBLE);
                            Toast.makeText(LogInActivity.this, "Could not log in", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(LogInActivity.this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
                    logInButton.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.GONE);
                }
            });
        }).start();
    }
}
