package ca.quanta.quantaevents.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentEventFilterBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class EventFilterFragment extends Fragment {
    private FragmentEventFilterBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Filter Events");
        infoStore.setSubtitle("Apply filters to search events");
        infoStore.setIconRes(R.drawable.material_symbols_filter_alt_outline);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
