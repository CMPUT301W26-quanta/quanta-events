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

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.burger.SmartBurgerState;
import ca.quanta.quantaevents.burger.Tagged;
import ca.quanta.quantaevents.databinding.FragmentEntrantEventListBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class EntrantEventListFragment extends Fragment implements Tagged {
    private FragmentEntrantEventListBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Event List");
        infoStore.setSubtitle("View events in which you are enrolled");
        infoStore.setIconRes(R.drawable.material_symbols_event_list_outline);

        binding.historyButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_entranteventlist_to_entranteventhistoryfragment)
        );
        binding.searchButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_entranteventlist_to_entranteventbrowserfragment)
        );

        new ViewModelProvider(requireActivity()).get(SmartBurgerState.class).show(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEntrantEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private final static UUID TAG = UUID.randomUUID();

    @NonNull
    @Override
    public UUID getUniqueTag() {
        return TAG;
    }

}
