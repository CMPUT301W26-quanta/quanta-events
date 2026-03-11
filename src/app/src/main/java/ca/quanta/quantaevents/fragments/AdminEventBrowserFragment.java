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
import ca.quanta.quantaevents.databinding.FragmentAdminEventBrowserBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class AdminEventBrowserFragment extends Fragment {
    private FragmentAdminEventBrowserBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Admin Event Browser");
        infoStore.setSubtitle("Browse and Moderate Events");
        infoStore.setIconRes(R.drawable.material_symbols_calendar_lock_outline);

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_admineventbrowserFragment_to_adminpanelFragment)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminEventBrowserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
