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
import ca.quanta.quantaevents.databinding.FragmentAdminProfileBrowserBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class AdminProfileBrowserFragment extends Fragment {
    private FragmentAdminProfileBrowserBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Admin Profile Browser");
        infoStore.setSubtitle("Browse and Delete Profiles");
        infoStore.setIconRes(R.drawable.material_symbols_person_shield_outline);
        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminProfileBrowserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
