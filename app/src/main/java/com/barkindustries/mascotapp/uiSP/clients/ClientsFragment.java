package com.barkindustries.mascotapp.uiSP.clients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientsFragment extends Fragment {

    private ClientsAdapter adapter;

    private List<String> clientUsernames;
    private List<String> clientRealNames;
    private List<String> clientProfilePicURLs;
    private List<String> clientPhones;

    // Firebase variables
    private DatabaseReference servicesReference;
    private String userId;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clients_sp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        clientUsernames = new ArrayList<>();
        clientRealNames = new ArrayList<>();
        clientProfilePicURLs = new ArrayList<>();
        clientPhones = new ArrayList<>();

        userId = FirebaseAuth.getInstance().getUid();
        servicesReference = FirebaseDatabase.getInstance().getReference("Services");

        setupRecyclerView(view);

        obtainTypeOfService(view);
    }

    private void obtainTypeOfService(View view) {
        new Thread(() -> {
            final boolean[] found = {false};
            servicesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        for(DataSnapshot serviceType : snapshot.getChildren()) {
                            for(DataSnapshot serviceProvider : serviceType.getChildren()) {
                                if(Objects.equals(serviceProvider.getKey(), userId)) {
                                    String typeOfService = serviceType.getKey();
                                    displayClientsList(typeOfService, view);
                                    found[0] = true;
                                    break;
                                }
                            }
                            if(found[0])
                                break;
                        }
                        if(!found[0])
                            errorInClients(view);
                    } else
                        errorInClients(view);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    errorInClients(view);
                }
            });
        }).start();
    }

    private void displayClientsList(String typeOfService, View view) {
        servicesReference.child(typeOfService).child(userId).child("Friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int numberOfFriends = 0;
                for (DataSnapshot friend : snapshot.getChildren()) {
                    Client client = friend.getValue(Client.class);

                    clientUsernames.add(client.getUsernameText());

                    String realName = client.getRealName();
                    if (realName == null)
                        clientRealNames.add("null");
                    else
                        clientRealNames.add(realName);

                    String phone = client.getPhone();
                    if (phone == null)
                        clientPhones.add("null");
                    else
                        clientPhones.add(phone);

                    String profilePicURL = client.getProfilePicURL();
                    if (profilePicURL == null)
                        clientProfilePicURLs.add("null");
                    else
                        clientProfilePicURLs.add(profilePicURL);

                    // notify adapter
                    adapter.notifyItemInserted(clientUsernames.size() - 1);
                    numberOfFriends++;
                    view.findViewById(R.id.loading_clients).setVisibility(View.GONE);
                }
                if (numberOfFriends == 0)
                    displayZeroClients(view);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                errorInClients(view);
            }
        });
    }

    private void displayZeroClients(@NonNull View view) {
        view.findViewById(R.id.loading_clients).setVisibility(View.GONE);
        view.findViewById(R.id.clients_recyclerView).setVisibility(View.GONE);
        view.findViewById(R.id.zero_clients).setVisibility(View.VISIBLE);
    }

    private void errorInClients(@NonNull View view) {
        view.findViewById(R.id.loading_clients).setVisibility(View.GONE);
        view.findViewById(R.id.clients_recyclerView).setVisibility(View.GONE);
        view.findViewById(R.id.error_in_clients).setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView(@NonNull View view) {
        RecyclerView recyclerView = view.findViewById(R.id.clients_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ClientsAdapter(getContext(), clientUsernames, clientRealNames, clientProfilePicURLs, clientPhones);
        recyclerView.setAdapter(adapter);
    }
}
