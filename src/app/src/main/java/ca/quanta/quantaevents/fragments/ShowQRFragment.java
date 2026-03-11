package ca.quanta.quantaevents.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentShowQrBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class ShowQRFragment extends Fragment {
    private FragmentShowQrBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("QR Code");
        infoStore.setSubtitle("Share an event QR code");
        infoStore.setIconRes(R.drawable.material_symbols_group_outline);

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_showqrfragment_to_eventmanagerfragment)
        );

        binding.shareButton.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Scan this QR code to join the event!");
            startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentShowQrBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
