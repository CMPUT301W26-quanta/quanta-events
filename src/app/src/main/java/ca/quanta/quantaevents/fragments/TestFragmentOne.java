package ca.quanta.quantaevents.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import ca.quanta.quantaevents.databinding.FragmentTestOneBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class TestFragmentOne extends Fragment implements Tagged {
    private FragmentTestOneBinding binding;

    public TestFragmentOne() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UUID deviceID = getDeviceID(requireContext());


        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Home");
        infoStore.setSubtitle("Receive lottery selection updates here.");
        infoStore.setIconRes(R.drawable.material_symbols_home_outline);

        binding.accountButton.setOnClickListener(_view -> {
            NavDirections action = TestFragmentOneDirections.actionTestFragmentOneToTestFragmentTwo();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        binding.infoButton.setOnClickListener(_view -> {
            NavDirections action = TestFragmentOneDirections.actionTestFragmentOneToInformationFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        new ViewModelProvider(requireActivity()).get(SmartBurgerState.class).show(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTestOneBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private static final UUID TAG = UUID.randomUUID();

    @NonNull
    @Override
    public UUID getUniqueTag() {
        return TAG;
    }


    /**
     * Method to check if device is already registered for the app, if not sets up a new ID
     */
    public static UUID getDeviceID(Context context){
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String id = prefs.getString("device_id", null);

        if (id == null) {
            UUID newId = UUID.randomUUID();
            prefs.edit().putString("device_id", newId.toString()).apply();
            return newId;

        }

        return UUID.fromString(id);


    }
}