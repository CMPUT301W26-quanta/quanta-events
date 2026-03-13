package ca.quanta.quantaevents.fragments;

import static android.view.View.GONE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentEventQrCodeBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class EventQRCodeFragment extends Fragment {
    private FragmentEventQrCodeBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to Scan QR code and the subtitle.
        // also sets the icon for the page
        infoStore.setTitle("Scan QR Code");
        infoStore.setSubtitle("Scan a promotional QR code");
        infoStore.setIconRes(R.drawable.material_symbols_qr_code_outline);
        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(binding.getRoot()).popBackStack()
        );

        // Request camer permission

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    100
            );
        }

        // Hide the status view of the QR code scanner
        binding.qrCodeScanner.getStatusView().setVisibility(GONE);

        // keep scanning for qr codes and decode them
        binding.qrCodeScanner.decodeContinuous(result -> {
            String text = result.getText();
            if (text.startsWith("quanta-events:")) {
                String uuidStr = text.substring(14);
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    NavDirections action = EventQRCodeFragmentDirections.actionEventQRCodeFragmentToEventDetailsFragment(uuid);
                    Navigation.findNavController(binding.getRoot()).navigate(action);
                } catch (IllegalArgumentException ignored) {
                }
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.qrCodeScanner.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.qrCodeScanner.pause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventQrCodeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

}
