package com.barkindustries.mascotapp;



import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.barkindustries.mascotapp.ui.chatWithVets.SeeUsersFragment;
import com.barkindustries.mascotapp.ui.yourPets.PetProfileFragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.BitmapFactory;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static java.lang.System.in;
import static java.lang.System.setOut;
import static java.lang.Thread.sleep;


public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    FirebaseDatabase firebaseDatabase;


    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";
    Context context = this;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;

    Map<String, DialogInformation> informationMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //esto tiene que ver con la licencia de mapbox.
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_maps);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    //En este metodo basicamente se prepara el mapa, se elije el estilo y se ponen los pins y eso.
    // Hay varios estilos que podemos elegir, solo hay que googlear urls y fijarse, tambien se puede customizar.
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap){
        MapsActivity.this.mapboxMap = mapboxMap;
        //los features es un objeto que extiende de la clase geometry. En este caso solo van a ser puntos 2D
        //En esta lista deberiamos poner los Points de los service providers.

        FirebaseDatabase.getInstance().getReference().child("Services")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            for( DataSnapshot id : snapshot.getChildren()) {

                                if (id.child("Location").exists()) {
                                    String title = id.child("UsernameText").getValue().toString();
                                    Double star_avg = 0.0;
                                    Long stars = (Long) id.child("Stars").getValue();
                                    if(stars != 0) {
                                        star_avg = stars / Double.valueOf(Long.toString((Long) id.child("starsCounter").getValue()));
                                    }
                                    String description = star_avg.toString();

                                    String url = (String) id.child("profilePicURL").getValue();

                                    Marker marker = mapboxMap.addMarker(new MarkerOptions()
                                            .position(new LatLng((Double) id.child("Location").child("lat").getValue(),(Double) id.child("Location").child("long").getValue())));
                                    marker.setTitle(id.getKey());
                                    IconFactory iconFactory = IconFactory.getInstance(context);
                                    Icon icon = iconFactory.fromResource(R.drawable.orange_pin);
                                    marker.setIcon(icon);

                                    informationMap.put(id.getKey(), new DialogInformation(title, description, url, id.child("Email").getValue().toString()) )

                                    /*
                                    symbolLayerIconFeatureList.add(Feature.fromGeometry(
                                            Point.fromLngLat((Double) id.child("Location").child("long").getValue(), (Double) id.child("Location").child("lat").getValue())*/
                                    ;
                                }
                            }

                        }

                        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v11"),
                                new Style.OnStyleLoaded() {
                                    @Override
                                    public void onStyleLoaded(@NonNull Style style) {
                                        enableLocationComponent(style);
                                    }
                                });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        Context context = this;


        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                DialogInformation info = informationMap.get(marker.getTitle());
                generateProfileDialog(info.title,info.description, info.photoURL, info.providerEmail);

                return true;
            }
        });

    }
    public class DialogInformation{
        String title, description, photoURL, providerEmail;
        DialogInformation(String title, String description, String photoURL, String providerEmail){
            this.title = title;
            this.description = description;
            this.photoURL = photoURL;
            this.providerEmail = providerEmail;
        }
    }

    public void generateProfileDialog(String title, String description, String photoURL, String providerEmail){

        Dialog profile = new Dialog(this);
        profile.setContentView(R.layout.pin_dialog);
        profile.setTitle(title);
        TextView name  = (TextView) profile.findViewById(R.id.title);
        name.setText(title);
        Button addContactBtn = (Button) profile.findViewById(R.id.add_contact_provider);
        Button cancel = (Button)profile.findViewById(R.id.cancel);
        TextView desc = (TextView) profile.findViewById(R.id.description);
        desc.setText("Stars: " + description);
        ImageView profilePic = (ImageView) profile.findViewById(R.id.portrait);
        if(photoURL != null) {
            Glide.with(this).load(photoURL).into(profilePic);
        }

        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeeUsersFragment addContact = new SeeUsersFragment();
                addContact.becomeFriendWithUser(providerEmail, mAuth.getUid(), firebaseDatabase, false, false);
                Toast.makeText(context, "Contact Added", Toast.LENGTH_SHORT).show();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.cancel();
            }
        });
        profile.show();
    }


    //Bueno primero veamos lo de la location. Hay que dar permisos primero.
    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle){
        //Checkea si tiene permisos y si no las pide
        if(PermissionsManager.areLocationPermissionsGranted(this))
        {
            //get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            //Activate with options
            locationComponent.activateLocationComponent(LocationComponentActivationOptions
                    .builder(this,loadedMapStyle).build());

            //Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            //Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            //Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.permission_explanation, Toast.LENGTH_LONG).show();

    }

    //LO QUE PASA DESPUÉS DE QUE HAYA PEDIDO PERMISO DE LOCATION
    @Override
    public void onPermissionResult(boolean granted){
        if(granted){
            mapboxMap.getStyle(new Style.OnStyleLoaded(){
                @Override
                public void onStyleLoaded(@NonNull Style style){
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }



    //el MapView contiene sus propios metodos de lifecycle para manegar Android's Open GL lifecycle
    //estos tienen que ser llamados directamente desde la activvidad. Asique para que nuestra app llame correctamente los metodos de lifecycle
    //necesitamos sobrescribir los metodos de lifecycle de nuestra activity, y agregar los de mapView
    //asique en onCreate vamos a tener que hacer onCreate. Tenemos que hacer esto con todos los metodos.
    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onStart(){
        super.onStart();
        //esto se hace por la misma razon que hicimos mapView.onCreate() antes.
        mapView.onStart();
    }
    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }
    @Override
    protected void onStop(){
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }

}
