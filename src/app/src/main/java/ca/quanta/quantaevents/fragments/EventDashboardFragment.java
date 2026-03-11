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
import ca.quanta.quantaevents.databinding.FragmentEventDashboardBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class EventDashboardFragment extends Fragment {
    private FragmentEventDashboardBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Event Dashboard");
        infoStore.setSubtitle("View events you have created");
        infoStore.setIconRes(R.drawable.material_symbols_dashboard_outline);
        binding.createButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_eventdashboardfragment_to_eventeditorfragment)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
