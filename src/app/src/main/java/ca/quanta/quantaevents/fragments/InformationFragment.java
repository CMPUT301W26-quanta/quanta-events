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

import java.util.zip.Inflater;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentInformationBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InformationFragment extends Fragment {

    public FragmentInformationBinding binding;


    public InformationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InformationFragment.
     */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Information");
        infoStore.setSubtitle("Lottery selection information here.");
        infoStore.setIconRes(R.drawable.material_symbols_info_outline);

        binding.backButton.setOnClickListener(_view -> {
            NavDirections action = InformationFragmentDirections.actionGlobalTestFragmentOne();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInformationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}