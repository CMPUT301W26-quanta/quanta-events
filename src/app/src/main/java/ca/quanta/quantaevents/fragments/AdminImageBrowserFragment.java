package ca.quanta.quantaevents.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.ImageCardAdapter;
import ca.quanta.quantaevents.databinding.FragmentAdminImageBrowserBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;

public class AdminImageBrowserFragment extends Fragment {
    private FragmentAdminImageBrowserBinding binding;

    private EventViewModel eventModel;
    private ImageViewModel imageModel;
    private boolean hasLoaded = false;

    private UUID userId;
    private UUID deviceId;

    private void listImageCards() {
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(
            this.eventModel.getEvents(
                            this.userId,
                            this.deviceId,
                            -1,
                            null,
                            EventViewModel.Fetch.ALL,
                            null,
                            null,
                            null,
                            EventViewModel.SortBy.REGISTRATION_END)
                    .addOnSuccessListener(events -> {
                        // use the adapter to display them

                        ImageCardAdapter imageCardsAdapter = new ImageCardAdapter(events, this);

                        binding.imageCardsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                        binding.imageCardsRecyclerView.setAdapter(imageCardsAdapter);
                    })
                    .addOnFailureListener(exception -> {
                        Log.e("AdminImageBrowserFragment", "Failed to fetch events.", exception);

                        ToastManager.show(requireContext(), "Failed to fetch events", Toast.LENGTH_LONG);

                        if (exception instanceof FirebaseFunctionsException) {
                            Log.e("AdminImageBrowserFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                        }
                    })
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // **** set up the header

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);

        infoStore.setTitle("Admin Image Browser");
        infoStore.setSubtitle("Browse and remove images.");
        infoStore.setIconRes(R.drawable.material_symbols_image_search_outline);

        // **** set up the view models

        this.eventModel = new ViewModelProvider(this.requireActivity()).get(EventViewModel.class);
        this.imageModel = new ViewModelProvider(this.requireActivity()).get(ImageViewModel.class);

        // **** set up the session store

        SessionStore sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);

        sessionStore.observeSession(getViewLifecycleOwner(), (userId, deviceId) -> {
            this.userId = userId;
            this.deviceId = deviceId;

            // set up the images recycler view once the userId and deviceId are ready
            if (!hasLoaded) {
                hasLoaded = true;
                listImageCards();
            }
        });

        // **** set up the buttons

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ToastManager.cancel();
        hasLoaded = false;
        binding = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminImageBrowserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

}
