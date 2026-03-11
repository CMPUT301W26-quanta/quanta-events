package ca.quanta.quantaevents.fragments;

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
import ca.quanta.quantaevents.databinding.FragmentEventQrCodeBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class EventQRCodeFragment extends Fragment {
    private FragmentEventQrCodeBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Scan QR Code");
        infoStore.setSubtitle("Scan a promotional QR code");
        infoStore.setIconRes(R.drawable.material_symbols_qr_code_outline);
        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_eventqrcodefragment_to_eventbrowserfragment)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventQrCodeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

}
