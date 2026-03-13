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
import ca.quanta.quantaevents.burger.Tagged;
import ca.quanta.quantaevents.databinding.FragmentInformationBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class InformationFragment extends Fragment implements Tagged {
    public FragmentInformationBinding binding;

    public InformationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to Information and the subtitle to what the page has
        // also sets the icon for the page
        infoStore.setTitle("Information");
        infoStore.setSubtitle("Lottery selection information here.");
        infoStore.setIconRes(R.drawable.material_symbols_info_outline);

        //sets up the back buttton listener
        binding.backButton.setOnClickListener(v -> {
            NavDirections action = ca.quanta.quantaevents.fragments.InformationFragmentDirections.actionGlobalHomeFragment();
            Navigation.findNavController(requireView()).navigate(action);
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInformationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private final static UUID TAG = UUID.randomUUID();

    @NonNull
    @Override
    public UUID getUniqueTag() {
        return TAG;
    }
}