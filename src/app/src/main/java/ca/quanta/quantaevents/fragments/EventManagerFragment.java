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

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentEventManagerBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;

public class EventManagerFragment extends Fragment {
    private FragmentEventManagerBinding binding;
    private UUID userId;
    private UUID deviceId;
    private UUID eventId;
    private Boolean isDrawn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventManagerFragmentArgs args = EventManagerFragmentArgs.fromBundle(getArguments());
        eventId = args.getEventId();
        isDrawn = args.getIsDrawn();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to Event Manager and the subtitle.
        // also sets the icon for the page
        infoStore.setTitle("Event Manager");
        infoStore.setSubtitle("Manage your Events");
        infoStore.setIconRes(R.drawable.material_symbols_edit_outline);

        // reads event id passed as an argument in the bundle
        SessionStore sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);

        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
        });

        // sets on click listener to navigate to editor fragment
        binding.editDetailsButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToCreateeditorfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );
        // sets onc lick listener to navigate to show qr fragment
        binding.shareQrButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToShowqrfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );
        //sets on click listener to navigate to waiting list fragment
        binding.viewWaitListButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToEventwaitinglistfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );
        // set on click listner to navigate to send notification fragment
        binding.sendNotificationButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToSendnotificationfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );

        if(isDrawn) {
            binding.drawLotteryButton.setEnabled(false);
            binding.drawLotteryButton.setAlpha(0.5f);
        } else {
            binding.drawLotteryButton.setOnClickListener(v -> {
                binding.drawLotteryButton.setEnabled(false);
                binding.drawLotteryButton.setAlpha(0.5f);
                EventViewModel events = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
                loader.loadTask(events.drawLottery(userId, deviceId, eventId)
                                .addOnSuccessListener(_void -> ToastManager.show(getContext(), "Drew lottery"))
                        .addOnFailureListener(exc -> {
                            exc.printStackTrace();
                            binding.drawLotteryButton.setEnabled(true);
                            binding.drawLotteryButton.setAlpha(1.0f);
                            ToastManager.show(getContext(), "Failed to draw lottery");
                        })
                );
            });
        }

        // send on click listener to navigate back to previous fragment
        binding.backButton.setOnClickListener(
                v -> {
                    Navigation.findNavController(v).popBackStack();
                }
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventManagerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
