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
import ca.quanta.quantaevents.databinding.FragmentEntrantEventHistoryBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class AdminNotificationHistoryFragment extends Fragment {
    private FragmentEntrantEventHistoryBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Notification History");
        infoStore.setSubtitle("View event notification history.");
        infoStore.setIconRes(R.drawable.material_symbols_history);

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_eventhistoryfragment_to_entranteventlistfragment)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEntrantEventHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
