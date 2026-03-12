package ca.quanta.quantaevents.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.burger.SmartBurgerState;
import ca.quanta.quantaevents.burger.Tagged;
import ca.quanta.quantaevents.databinding.FragmentAdminPanelBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class AdminPanelFragment extends Fragment implements Tagged {
    private FragmentAdminPanelBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Admin Panel");
        infoStore.setSubtitle("Access moderation tools");
        infoStore.setIconRes(R.drawable.material_symbols_security);
        binding.adminBrowseEventsButton.setOnClickListener(
                v -> {
                    NavDirections action = AdminPanelFragmentDirections.actionAdminpanelFragmentToAdmineventbrowserFragment();
                    Navigation.findNavController(v).navigate(action);
                }
        );
        binding.adminBrowseProfilesButton.setOnClickListener(
                v -> {
                    NavDirections action = AdminPanelFragmentDirections.actionAdminpanelFragmentToAdminprofilebrowserFragment();
                    Navigation.findNavController(v).navigate(action);
                }
        );
        new ViewModelProvider(requireActivity()).get(SmartBurgerState.class).show(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminPanelBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private static final UUID TAG = UUID.randomUUID();

    @NonNull
    @Override
    public UUID getUniqueTag() {
        return TAG;
    }

}
