package com.barkindustries.mascotapp;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.barkindustries.mascotapp.ui.chatWithVets.Utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SignUpActivity  extends AppCompatActivity {

    private static final int SERVICE_SIGNUP = 1;
    private static final int OWNER_SIGNUP = 0;

    private EditText username, email, password, passwordConfirm, locality, street, postalCode;
    private Button signUpButton;
    private TextView logInButton;
    private Switch serviceSwitch;
    private Spinner serviceTypeSpinner, sp_provinces;
    private String selectedService;
    private ProgressBar loading;

    private Integer signupType;


    String address;


    FirebaseAuth mAuth;
    DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        loading = findViewById(R.id.progress_bar_sign_up);
        loading.setVisibility(View.INVISIBLE);

        email = findViewById(R.id.email_input_from_signup);
        username = findViewById(R.id.username_input_from_signup);
        password = findViewById(R.id.password_input_from_signup);
        passwordConfirm = findViewById(R.id.password_confirm_input_from_signup);
        serviceSwitch = findViewById(R.id.signup_switch);
        serviceTypeSpinner = findViewById(R.id.signup_service_type);
        sp_provinces = findViewById(R.id.provinces);
        locality = findViewById(R.id.locality);
        street = findViewById(R.id.street);
        postalCode = findViewById(R.id.postal_code);
        signUpButton = findViewById(R.id.signup_button_from_signup);
        logInButton = findViewById(R.id.login_signup_btn);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        List<String> provinces = new ArrayList<>();
        provinces.add("Rio Negro");
        provinces.add("Buenos Aires");
        provinces.add("Córdoba");
        provinces.add("Santa fe");
        provinces.add("Mendoza");
        provinces.add("Tierra del Fuego");
        provinces.add("Corrientes");
        provinces.add("Salta");
        provinces.add("Catamarca");
        provinces.add("Santiago del Estero");
        provinces.add("Formosa");
        provinces.add("La Rioja");
        provinces.add("Misiones");
        provinces.add("San Juan");
        provinces.add("San Luis");
        provinces.add("Santa Cruz");
        provinces.add("Chaco");
        provinces.add("Neuquén");
        provinces.add("Entre rios");
        provinces.add("Tucumán");
        provinces.add("La Pampa");
        provinces.add("Jujuy");
        provinces.add("Chubut");

        sp_provinces.setVisibility(View.GONE);
        locality.setVisibility(View.GONE);
        street.setVisibility(View.GONE);
        postalCode.setVisibility(View.GONE);
        serviceTypeSpinner.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            provinces.sort((o1, o2) -> o1.compareTo(o2));
        }

        ArrayAdapter<String> provinceadapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinces);
        provinceadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sp_provinces.setAdapter(provinceadapter);

        signupType = OWNER_SIGNUP;

        handleSwitch();

        signUpButton.setOnClickListener(v -> {
            if(formIsValid())
                signUp();
        });
        logInButton.setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, LogInActivity.class)));


    }

    private void signUp() {
        signUpButton.setVisibility(View.INVISIBLE);
        loading.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("UsernameText", username.getText().toString());
                    map.put("Email", email.getText().toString());
                    map.put("Password", password.getText().toString());
                    map.put("PetsAmount", 0);
                    map.put("profilePicURL", Constants.DEFAULT_URI_PROFILE_PIC); //All users will start with a default picture

                    Map<String, Object> mapSP = new HashMap<>();
                    mapSP.put("UsernameText", username.getText().toString());
                    mapSP.put("Email", email.getText().toString());
                    mapSP.put("Password", password.getText().toString());
                    mapSP.put("Stars", 0);
                    mapSP.put("starCounter", 0);
                    mapSP.put("profilePicURL", Constants.DEFAULT_URI_PROFILE_PIC); //All users will start with a default picture


                    String id = mAuth.getCurrentUser().getUid();

                    if (signupType == OWNER_SIGNUP) {

                        signUpOwner(map, id);
                    } else {
                        signUpServiceProvider(mapSP, id);
                    }

                } else {
                    Toast.makeText(SignUpActivity.this, "Could not create user", Toast.LENGTH_SHORT).show();
                    signUpButton.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void signUpOwner(Map<String, Object> map, String id) {
        mDatabase.child("Users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task2) {
                if (task2.isSuccessful()) {

                    Intent goToHome = new Intent(SignUpActivity.this, HomeActivity.class);

                    //Send type of sign up to Home Activity in order to change Nav Drawer
                   /* Bundle myParams = new Bundle();
                    myParams.putString("signupType", signupType);
                    goToHome.putExtras(myParams);*/
                    loading.setVisibility(View.INVISIBLE);
                    startActivity(goToHome);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Could not save data", Toast.LENGTH_SHORT).show();
                    signUpButton.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void signUpServiceProvider(Map<String, Object> map, String id) {
        address = street.getText().toString() + ", " + locality.getText().toString() +", "+ sp_provinces.getSelectedItem() + ", Argentina" ;
        Double latitude = 0.0;
        Double longitude = 0.0;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(address,1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses.size() > 0){
            latitude = addresses.get(0).getLatitude();
            longitude = addresses.get(0).getLongitude();
        }
        

        Map<String,Double> map2 = new HashMap<>();
        map2.put("lat",latitude);
        map2.put("long",longitude);

        map.put("Location", map2);
        mDatabase.child("Services").child(selectedService).child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task2) {
                if (task2.isSuccessful()) {

                    Intent goToHome = new Intent(SignUpActivity.this, HomeSPActivity.class);
                    loading.setVisibility(View.INVISIBLE);
                    startActivity(goToHome);
                    finish();
                } else {
                    Toast.makeText(SignUpActivity.this, "Could not save data", Toast.LENGTH_SHORT).show();
                    signUpButton.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    private void handleSwitch() {
        List<String> services = new ArrayList<>();
        services.add("Breeder");
        services.add("Groomer");
        services.add("Walker");
        services.add("Vet");
        services.add("Pet Shop");
        services.add("Sitter");
        services.add("Shelter");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, services);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        serviceTypeSpinner.setAdapter(adapter);

        serviceSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (serviceSwitch.isChecked()) {
                    signupType = SERVICE_SIGNUP;
                    sp_provinces.setVisibility(View.VISIBLE);
                    locality.setVisibility(View.VISIBLE);
                    street.setVisibility(View.VISIBLE);
                    postalCode.setVisibility(View.VISIBLE);
                    serviceTypeSpinner.setVisibility(View.VISIBLE);
                } else {
                    signupType = OWNER_SIGNUP;
                    sp_provinces.setVisibility(View.GONE);
                    locality.setVisibility(View.GONE);
                    street.setVisibility(View.GONE);
                    postalCode.setVisibility(View.GONE);
                    serviceTypeSpinner.setVisibility(View.GONE);
                }
            }
        });

        serviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedService = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


        private boolean isEmail (EditText text){
            CharSequence email = text.getText().toString();
            return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
        }

        //esta función podemos editarla para seguir nuestras condiciones..
        private boolean isValid (EditText text){
            CharSequence str = text.getText().toString();
            return !TextUtils.isEmpty(str);
        }

        private boolean formIsValid () {
            boolean ready = true;
            if (!isEmail(email)) {
                email.setError("invalid mail");
                ready = false;
            }
            if (!isValid(username)) {
                username.setError("invalid username");
                ready = false;
            }
            if (password.getText().length() < 6) {
                password.setError("invalid password");
                ready = false;
            }

            if (!passwordConfirm.getText().toString().equals(password.getText().toString())) {
                passwordConfirm.setError("passwords don't match!");
                ready = false;
            }
            if(signupType == SERVICE_SIGNUP){
                if(!isValid(street)){
                    street.setError("invalid street");
                    ready = false;
                }
                if(!isValid(locality)){
                    locality.setError("invalid street");
                    ready = false;
                }
                if(!isValid(postalCode)){
                    postalCode.setError("invalid street");
                    ready = false;
                }
            }
            return ready;

        }
}
