package ca.quanta.quantaevents.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentTestOneBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventList extends Fragment {
    private EventList binding;

    public EventList() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Home");
        infoStore.setSubtitle("Receive lottery selection updates here.");
        infoStore.setIconRes(R.drawable.material_symbols_home_outline);

        binding.eventListButton.setOnClickListener(_view -> {
            NavDirections action = TestFragmentOneDirections.actionTestFragmentOneToTestFragmentTwo();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }
}