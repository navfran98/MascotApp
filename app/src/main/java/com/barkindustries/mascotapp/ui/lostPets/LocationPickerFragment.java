package com.barkindustries.mascotapp.ui.lostPets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.barkindustries.mascotapp.R;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.List;


public class LocationPickerFragment extends Fragment implements
        OnMapReadyCallback, PermissionsListener {


    public static Double lat = null, lon = null;

    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_lab_location_picker, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Mapbox.getInstance(getContext(), getString(R.string.access_token));
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    //En este metodo basicamente se prepara el mapa, se elije el estilo y se ponen los pins y eso.
    // Hay varios estilos que podemos elegir, solo hay que googlear urls y fijarse, tambien se puede customizar.
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap){
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v11"),
                style -> enableLocationComponent(style));
        mapboxMap.addOnMapClickListener(point -> {
            if(mapboxMap.getMarkers().isEmpty()) {
                Marker marker = mapboxMap.addMarker(new MarkerOptions()
                        .position(point));
            };
            mapboxMap.getMarkers().get(0).setPosition(point);
            Toast.makeText(getContext(), "Loss location selected", Toast.LENGTH_SHORT).show();
            lat = point.getLatitude();
            lon = point.getLongitude();
            return false;
        });
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
        Toast.makeText(getContext(), R.string.permission_explanation, Toast.LENGTH_LONG).show();

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
            Toast.makeText(getContext(), R.string.location_permission_not_granted, Toast.LENGTH_LONG).show();
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
