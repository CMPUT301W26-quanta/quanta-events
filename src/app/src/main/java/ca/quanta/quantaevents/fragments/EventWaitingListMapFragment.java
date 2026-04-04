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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentEventWaitlistMapBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class EventWaitingListMapFragment extends Fragment implements OnMapReadyCallback {
    private FragmentEventWaitlistMapBinding binding;

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
        // set title of the page to Event Manager and the subtitle.
        // also sets the icon for the page
        infoStore.setTitle("Event Waitlist Map");
        infoStore.setSubtitle("View where entrants enrolled from");
        infoStore.setIconRes(R.drawable.material_symbols_map_outline);

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            showMapError();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null) {
            showMapError();
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            showMapError();
        }
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null || binding == null) return;
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
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
    }
}
