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
    private boolean isDrawn;

    private boolean isPrivate;
    EventViewModel eventModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventManagerFragmentArgs args = EventManagerFragmentArgs.fromBundle(getArguments());
        this.eventId = args.getEventId();
        this.isDrawn = args.getIsDrawn();
        this.isPrivate = args.getIsPrivate();

        // **** set up the view models

        this.eventModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
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
            this.userId = uid;
            this.deviceId = did;
        });

        readArgs();

        // sets on click listener to navigate to editor fragment
        this.binding.editDetailsButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToCreateeditorfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );

        if (this.isPrivate) {
            this.binding.shareQrButton.setText("Invite Entrants");
            this.binding.shareQrButton.setOnClickListener(
                    v -> {
                        NavDirections action = EventManagerFragmentDirections.actionEventManagerFragmentToEventInviteFragment(eventId);
                        Navigation.findNavController(v).navigate(action);
                    }
            );
        } else {
            this.binding.shareQrButton.setText("Share QR Code");
            // sets onc lick listener to navigate to show qr fragment
            this.binding.shareQrButton.setOnClickListener(
                    v -> {
                        NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToShowqrfragment(eventId);
                        Navigation.findNavController(v).navigate(action);
                    }
            );
        }

        //sets on click listener to navigate to waiting list fragment
        this.binding.viewWaitListButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToEventwaitinglistfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );

        // set on click listner to navigate to send notification fragment
        this.binding.sendNotificationButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToSendnotificationfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );

        // set on click listener to navigate to waitlist map fragment
        this.binding.viewWaitListMapButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToEventwaitinglistmapfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );

        if (this.isDrawn) {
            // disable draw lottery button
            this.binding.drawLotteryButton.setEnabled(false);
            this.binding.drawLotteryButton.setAlpha(0.5f);

            // setup cancel selected button
            this.binding.cancelSelectedButton.setOnClickListener(v -> {
                loader.loadTask(eventModel.cancelSelected(this.userId, this.deviceId, this.eventId)
                        .addOnSuccessListener(x -> {
                            if (!this.isAdded() || this.binding == null) return;
                            ToastManager.show(this.getContext(), "Successfully canceled selected entrants.");
                        })
                        .addOnFailureListener(exception -> {
                            if (!this.isAdded() || this.binding == null) return;
                            exception.printStackTrace();
                            ToastManager.show(this.getContext(), "Failed to cancel selected entrants");
                        })
                );
            });
        } else {
            // disable cancel selected button
            this.binding.cancelSelectedButton.setEnabled(false);
            this.binding.cancelSelectedButton.setAlpha(0.5f);

            // setup draw lottery button
            this.binding.drawLotteryButton.setOnClickListener(v -> {
                this.binding.drawLotteryButton.setEnabled(false);
                this. binding.drawLotteryButton.setAlpha(0.5f);
                loader.loadTask(eventModel.drawLottery(this.userId, this.deviceId, this.eventId)
                        .addOnSuccessListener(x -> {
                            if (!this.isAdded() || this.binding == null) return;
                            ToastManager.show(this.getContext(), "Successfully drew lottery.");
                        })
                        .addOnFailureListener(exception -> {
                            if (!this.isAdded() || this.binding == null) return;
                            exception.printStackTrace();
                            this.binding.drawLotteryButton.setEnabled(true);
                            this.binding.drawLotteryButton.setAlpha(1.0f);
                            ToastManager.show(this.getContext(), "Failed to draw lottery.");
                        })
                );
            });
        }

        // send on click listener to navigate back to previous fragment
        this.binding.backButton.setOnClickListener(
                v -> {
                    Navigation.findNavController(v).popBackStack();
                }
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.binding = FragmentEventManagerBinding.inflate(inflater, container, false);
        return this.binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ToastManager.cancel();
        this.binding = null;
    }

    private void readArgs() {
        EventManagerFragmentArgs args = EventManagerFragmentArgs.fromBundle(getArguments());
        this.eventId = args.getEventId();
        this.isDrawn = args.getIsDrawn();
    }
}
