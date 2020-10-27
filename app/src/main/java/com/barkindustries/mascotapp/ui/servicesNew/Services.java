package com.barkindustries.mascotapp.ui.servicesNew;


import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.HomeSPActivity;
import com.barkindustries.mascotapp.R;
import com.barkindustries.mascotapp.ui.maps.ServiceProviderDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Services extends Fragment {

    private List<String> servicesIds;
    private List<String> servicesTypes;

    private static List<String> currentTypesSelected;

    private Button filterButton;

    private DatabaseReference mDatabase;

    private ServicesAdapter adapter;

    private static List<String> services;

    private ProgressBar progressBar;

    private RecyclerView recyclerView;

    private static boolean shouldBringAll = true;

    private TextView emptyTextView;

    private Boolean imService;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.services, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        servicesIds = new ArrayList<>();
        servicesTypes = new ArrayList<>();

        progressBar = view.findViewById(R.id.progressBar);
        filterButton = view.findViewById(R.id.filter_sort_button);
        emptyTextView = view.findViewById(R.id.empty_textView);

        progressBar.setVisibility(View.INVISIBLE);

        filterButton.setOnClickListener(v -> filterDialog());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        recyclerView = view.findViewById(R.id.services_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ServicesAdapter(getContext(), servicesIds, servicesTypes, this);
        recyclerView.setAdapter(adapter);


        if(getActivity().getClass() == HomeSPActivity.class){
            imService = true;
        }else
            imService = false;

        if(shouldBringAll) {
            shouldBringAll = false;
            services = new ArrayList<>();
            services.add("Breeder");
            services.add("Groomer");
            services.add("Walker");
            services.add("Vet");
            services.add("Pet Shop");
            services.add("Sitter");
            services.add("Shelter");
        }

        getSelectedTypes(services);

    }


    public void clearRecyclerView(List<String> list){
        services = list;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            ft.setReorderingAllowed(false);
        }
        ft.detach(this).attach(this).commit();
    }

    void currentServiceProviderDialog(View v, int position){
        ServiceProviderDialog dialog = new ServiceProviderDialog(requireContext(), this, servicesIds.get(position), servicesTypes.get(position), imService);
        dialog.show();
    }

    public void filterDialog(){
        ServiceFilterDialog dialog = new ServiceFilterDialog(getContext(), this, currentTypesSelected);
        dialog.show();
    }

    protected void getSelectedTypes(List<String> types) {
        currentTypesSelected = types;
        if(types.isEmpty()){
            types.add("Breeder");
            types.add("Groomer");
            types.add("Walker");
            types.add("Vet");
            types.add("Pet Shop");
            types.add("Sitter");
            types.add("Shelter");
        }
        for(String type : types)
            getLists(type);
    }

    private void getLists(String type) {
        new Thread(() -> {
            mDatabase.child("Services").child(type).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        for(DataSnapshot service : dataSnapshot.getChildren()) {
                            servicesIds.add(service.getKey());
                            servicesTypes.add(type);
                            adapter.notifyItemInserted(servicesIds.size() - 1);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        if(servicesIds.size() != 0)
                            emptyTextView.setVisibility(View.INVISIBLE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }).start();
    }
}
