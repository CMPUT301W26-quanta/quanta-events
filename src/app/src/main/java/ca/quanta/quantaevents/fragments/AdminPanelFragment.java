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
import ca.quanta.quantaevents.databinding.FragmentAdminPanelBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class AdminPanelFragment extends Fragment {
    private FragmentAdminPanelBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Admin Panel");
        infoStore.setSubtitle("Access moderation tools");
        infoStore.setIconRes(R.drawable.material_symbols_security);
        binding.adminBrowseEventsButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_adminpanelFragment_to_admineventbrowserFragment)
        );
        binding.adminBrowseProfilesButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_adminpanelFragment_to_adminprofilebrowserFragment)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminPanelBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


}
