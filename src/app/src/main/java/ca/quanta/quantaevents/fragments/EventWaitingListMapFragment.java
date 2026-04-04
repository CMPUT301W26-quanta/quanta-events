package ca.quanta.quantaevents.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentEventWaitlistMapBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.EventViewModel;

public class EventWaitingListMapFragment extends Fragment implements OnMapReadyCallback {
    private FragmentEventWaitlistMapBinding binding;
    private GoogleMap googleMap;
    private UUID eventId;
    private UUID userId;
    private UUID deviceId;
    private EventViewModel eventViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventWaitlistMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Event Waitlist Map");
        infoStore.setSubtitle("View where entrants enrolled from");
        infoStore.setIconRes(R.drawable.material_symbols_map_outline);

        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        // Read eventId from nav args
        EventWaitingListMapFragmentArgs args = EventWaitingListMapFragmentArgs.fromBundle(getArguments());
        eventId = args.getEventId();

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );

        SessionStore sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
            maybeLoadMap();
        });

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            showMapError();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (map == null) {
            showMapError();
            return;
        }

        googleMap = map;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        maybeLoadMap();
    }

    private void maybeLoadMap() {
        if (googleMap == null || userId == null || deviceId == null || eventId == null) return;

        eventViewModel.getEvent(eventId, userId, deviceId)
                .addOnSuccessListener(event -> {
                    if (!isAdded() || binding == null || event == null) return;

                    Double lat = event.getLocationLat();
                    Double lng = event.getLocationLng();

                    if (lat == null || lng == null) {
                        showMapError();
                        return;
                    }

                    LatLng eventLocation = new LatLng(lat, lng);

                    // Drop pin at event location
                    googleMap.addMarker(new MarkerOptions()
                            .position(eventLocation)
                            .title(event.getEventName()));

                    // Draw 15km radius circle
                    googleMap.addCircle(new CircleOptions()
                            .center(eventLocation)
                            .radius(15000)
                            .strokeWidth(4f)
                            .strokeColor(0xFFD8B4FE)
                            .fillColor(0x40E9D5FF));

                    // Zoom to fit the circle
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 10f));
                })
                .addOnFailureListener(ex -> {
                    if (!isAdded() || binding == null) return;
                    showMapError();
                });
    }

    private void showMapError() {
        if (binding == null) return;
        binding.mapErrorText.setVisibility(View.VISIBLE);
        binding.errorText.setText("Map unavailable");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        googleMap = null;
    }
}