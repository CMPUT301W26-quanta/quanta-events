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

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentTestTwoBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class TestFragmentTwo extends Fragment {
    private FragmentTestTwoBinding binding;

    public TestFragmentTwo() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Account");
        infoStore.setSubtitle("Change account details.");
        infoStore.setIconRes(R.drawable.material_symbols_person_outline);

        binding.homeButton.setOnClickListener(_view -> {
            NavDirections action = TestFragmentTwoDirections.actionGlobalTestFragmentOne();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTestTwoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}