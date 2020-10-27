package com.barkindustries.mascotapp.ui.maps;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.barkindustries.mascotapp.HomeSPActivity;
import com.barkindustries.mascotapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsFragment extends Fragment implements
        OnMapReadyCallback, PermissionsListener {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase firebaseDatabase;

    Context context = getContext();
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;

    Map<String, MapsFragment.DialogInformation> informationMap = new HashMap<>();

    private Boolean imService;




    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_lab_location_picker, container, false);
        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        if(getActivity().getClass() == HomeSPActivity.class){
            imService = true;
        }else
            imService = false;
    }


    //En este metodo basicamente se prepara el mapa, se elije el estilo y se ponen los pins y eso.
    // Hay varios estilos que podemos elegir, solo hay que googlear urls y fijarse, tambien se puede customizar.
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap){
        MapsFragment.this.mapboxMap = mapboxMap;
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
                                        star_avg = stars / Double.valueOf(Long.toString((Long) id.child("starCounter").getValue()));
                                    }
                                    String description = star_avg.toString();

                                    String url = (String) id.child("profilePicURL").getValue();

                                    Marker marker = mapboxMap.addMarker(new MarkerOptions()
                                            .position(new LatLng((Double) id.child("Location").child("lat").getValue(),(Double) id.child("Location").child("long").getValue())));
                                    marker.setTitle(id.getKey());
                                    IconFactory iconFactory = IconFactory.getInstance(context);
                                    Icon icon = iconFactory.fromResource(R.drawable.orange_pin);
                                    marker.setIcon(icon);

                                    informationMap.put(id.getKey(), new MapsFragment.DialogInformation(title, description, url, id.child("Email").getValue().toString(), snapshot.getKey()) );


                                }
                            }

                        }

                        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v11"),
                                style -> enableLocationComponent(style));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });



        mapboxMap.setOnMarkerClickListener(marker -> {
            DialogInformation info = informationMap.get(marker.getTitle());
            ServiceProviderDialog dialog = new ServiceProviderDialog(requireContext(), getParentFragment(), marker.getTitle(), info.type, imService);
            dialog.show();

            return true;
        });

    }
    public class DialogInformation{
        String title, description, photoURL, providerEmail, type;
        DialogInformation(String title, String description, String photoURL, String providerEmail, String type){
            this.type = type;
            this.title = title;
            this.description = description;
            this.photoURL = photoURL;
            this.providerEmail = providerEmail;
        }
    }


    //Bueno primero veamos lo de la location. Hay que dar permisos primero.
    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle){
        //Checkea si tiene permisos y si no las pide
        if(PermissionsManager.areLocationPermissionsGranted(getContext()))
        {
            //get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            //Activate with options
            locationComponent.activateLocationComponent(LocationComponentActivationOptions
                    .builder(getContext(),loadedMapStyle).build());

            //Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            //Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            //Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(context, R.string.permission_explanation, Toast.LENGTH_LONG).show();

    }

    //LO QUE PASA DESPUÉS DE QUE HAYA PEDIDO PERMISO DE LOCATION
    @Override
    public void onPermissionResult(boolean granted){
        if(granted){
            mapboxMap.getStyle(style -> enableLocationComponent(style));
        } else {
            Toast.makeText(context, R.string.location_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }



    //el MapView contiene sus propios metodos de lifecycle para manegar Android's Open GL lifecycle
    //estos tienen que ser llamados directamente desde la activvidad. Asique para que nuestra app llame correctamente los metodos de lifecycle
    //necesitamos sobrescribir los metodos de lifecycle de nuestra activity, y agregar los de mapView
    //asique en onCreate vamos a tener que hacer onCreate. Tenemos que hacer esto con todos los metodos.
    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onStart(){
        super.onStart();
        //esto se hace por la misma razon que hicimos mapView.onCreate() antes.
        mapView.onStart();
    }
    @Override
    public void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onStop(){
        super.onStop();
        mapView.onStop();
    }
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }

}
