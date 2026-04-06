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
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.CoInviteNotificationAdapter;
import ca.quanta.quantaevents.adapters.InviteNotificationAdapter;
import ca.quanta.quantaevents.adapters.LotteryNotificationAdapter;
import ca.quanta.quantaevents.adapters.MessageNotificationAdapter;
import ca.quanta.quantaevents.burger.SmartBurgerState;
import ca.quanta.quantaevents.burger.Tagged;
import ca.quanta.quantaevents.databinding.FragmentHomeBinding;
import ca.quanta.quantaevents.models.ExternalUndismissedNotification;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.InviteViewModel;
import ca.quanta.quantaevents.viewmodels.NotificationViewModel;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class HomeFragment extends Fragment implements Tagged {
    private FragmentHomeBinding binding;
    private SessionStore sessionStore;
    private UserViewModel userModel;
    private EventViewModel eventModel;
    private NotificationViewModel notificationModel;
    private InviteViewModel inviteModel;
    private UUID userId;
    private UUID deviceId;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to Home and subtitle to what the page does
        // also sets the icon for the page
        infoStore.setTitle("Home");
        infoStore.setSubtitle("Receive lottery selection updates here.");
        infoStore.setIconRes(R.drawable.material_symbols_home_outline);

        // manages user session which checks if user is registered
        this.sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);

        this.userModel = new ViewModelProvider(this).get(UserViewModel.class);
        this.eventModel = new ViewModelProvider(this).get(EventViewModel.class);
        this.notificationModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        this.inviteModel = new ViewModelProvider(this).get(InviteViewModel.class);

        this.sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            this.userId = uid;
            this.deviceId = did;

            maybeValidateUser();
        });

        // listener for info button

        this.binding.infoButton.setOnClickListener(_view -> {
            NavDirections action = HomeFragmentDirections.actionHomeFragmentToInformationFragment();
            Navigation.findNavController(requireView()).navigate(action);
        });

        new ViewModelProvider(requireActivity()).get(SmartBurgerState.class).show(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // validates user by checking if they
    // exist in database if not handle it properly.
    private void maybeValidateUser() {
        if (this.userId == null || this.deviceId == null) return;

        this.userModel.getUser(this.userId, this.deviceId)
                .addOnFailureListener(ex -> {
                    if (!isAdded() || this.binding == null) return;
                    if (isUserNotFound(ex)) {
                        handleMissingUser();
                    }
                })
                .addOnCanceledListener(this::handleMissingUser)
                .addOnSuccessListener(user -> {
                    this.loadNotifications();
                });
    }

    private void loadNotifications() {
        notificationModel.getAllUndismissedNotifications(this.userId, this.deviceId)
                .addOnSuccessListener(this::getAndDisplayNotifications)
                .addOnFailureListener(exception -> {
                    Log.e("HomeFragment", "Failed to fetch notifications.", exception);

                    Toast.makeText(getContext(), "Failed to fetch notifications." + exception.getMessage(), Toast.LENGTH_LONG).show();

                    if (exception instanceof FirebaseFunctionsException) {
                        Log.e("HomeFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                    }
                });
    }

    private void getAndDisplayNotifications(ArrayList<ExternalUndismissedNotification> notifications) {
        // **** use adapters to display them

        if (!isAdded() || this.binding == null) return;

        ArrayList<ExternalUndismissedNotification> inviteNotifications = new ArrayList<>();
        ArrayList<ExternalUndismissedNotification> lotteryNotifications = new ArrayList<>();
        ArrayList<ExternalUndismissedNotification> messageNotifications = new ArrayList<>();
        ArrayList<ExternalUndismissedNotification> coInviteNotifications = new ArrayList<>();

        for (ExternalUndismissedNotification notification : notifications) {
            switch (notification.getKind()) {
                case "INVITE": inviteNotifications.add(notification); break;
                case "LOTTERY": lotteryNotifications.add(notification); break;
                case "MESSAGE": messageNotifications.add(notification); break;
                case "COINVITE": coInviteNotifications.add(notification); break;
            }
        }

        InviteNotificationAdapter inviteNotificationAdapter = new InviteNotificationAdapter(inviteNotifications, this, this.eventModel, this.notificationModel, this.userId, this.deviceId);
        LotteryNotificationAdapter lotteryNotificationAdapter = new LotteryNotificationAdapter(lotteryNotifications, this, this.eventModel, this.notificationModel, this.inviteModel, this.userId, this.deviceId);
        MessageNotificationAdapter messageNotificationAdapter = new MessageNotificationAdapter(messageNotifications, this, this.eventModel, this.notificationModel, this.userId, this.deviceId);
        CoInviteNotificationAdapter coInviteNotificationAdapter = new CoInviteNotificationAdapter(coInviteNotifications, this, this.eventModel, this.notificationModel, this.inviteModel, this.userId, this.deviceId);

        // **** set up the notification recycler views

        this.binding.inviteNotificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this.requireContext()));
        this.binding.inviteNotificationsRecyclerView.setAdapter(inviteNotificationAdapter);

        this.binding.lotteryNotificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this.requireContext()));
        this.binding.lotteryNotificationsRecyclerView.setAdapter(lotteryNotificationAdapter);

        this.binding.messageNotificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this.requireContext()));
        this.binding.messageNotificationsRecyclerView.setAdapter(messageNotificationAdapter);

        this.binding.coInviteNotificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this.requireContext()));
        this.binding.coInviteNotificationsRecyclerView.setAdapter(coInviteNotificationAdapter);

        // **** display toast in case there are none

        if (inviteNotifications.isEmpty() && lotteryNotifications.isEmpty() && messageNotifications.isEmpty()) {
            ToastManager.show(this.getContext(), "No notifications to display.", Toast.LENGTH_LONG);
        }
    }

    private boolean isUserNotFound(Exception ex) {
        if (ex instanceof FirebaseFunctionsException) {
            FirebaseFunctionsException.Code code = ((FirebaseFunctionsException) ex).getCode();
            return code == FirebaseFunctionsException.Code.NOT_FOUND;
        }
        throw new RuntimeException(ex);
    }

    // clears shared_pereferencs file if the userid does not exist on database
    private void handleMissingUser() {
        if (!isAdded() || binding == null) return;
        sessionStore.clearSession();
        if (isAdded()) {
            Navigation.findNavController(requireView()).navigate(R.id.registerFragment);
        }
    }

    private static final UUID TAG = UUID.randomUUID();

    @NonNull
    @Override
    public UUID getUniqueTag() {
        return TAG;
    }
}
