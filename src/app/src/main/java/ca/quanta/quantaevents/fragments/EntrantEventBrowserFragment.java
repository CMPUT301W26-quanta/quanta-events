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
import ca.quanta.quantaevents.databinding.FragmentEntrantEventBrowserBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class EntrantEventBrowserFragment extends Fragment {
    private FragmentEntrantEventBrowserBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Event Browser");
        infoStore.setSubtitle("Browse Events to enroll");
        infoStore.setIconRes(R.drawable.material_symbols_search_outline);

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );
        binding.filterButton.setOnClickListener(
                v -> {
                    NavDirections action = EntrantEventBrowserFragmentDirections.actionEntranteventbrowserfragmentToEventfilterfragment();
                    Navigation.findNavController(v).navigate(action);
                }
        );
        binding.qrButton.setOnClickListener(
                v -> {
                    NavDirections action = EntrantEventBrowserFragmentDirections.actionEntranteventbrowserfragmentToEventqrcodefragment();
                    Navigation.findNavController(v).navigate(action);
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEntrantEventBrowserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
