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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.UndismissedNotificationAdapter;
import ca.quanta.quantaevents.burger.SmartBurgerState;
import ca.quanta.quantaevents.burger.Tagged;
import ca.quanta.quantaevents.databinding.FragmentHomeBinding;
import ca.quanta.quantaevents.models.Event;
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

        UndismissedNotificationAdapter.AsyncHandler handler = new UndismissedNotificationAdapter.AsyncHandler() {
            UndismissedNotificationAdapter adapter = null;

            @Override
            public void getEvent(UUID eventId, Consumer<Event> consumer, Runnable onFail) {
                MutableLiveData<Void> failObserver = new MutableLiveData<>();
                MutableLiveData<Event> successObserver = new MutableLiveData<>();
                failObserver.observe(HomeFragment.this, _void -> onFail.run());
                successObserver.observe(HomeFragment.this, consumer::accept);
                eventModel.getEvent(eventId, userId, deviceId)
                        .addOnSuccessListener(successObserver::postValue)
                        .addOnFailureListener(exc -> {
                            failObserver.postValue(null);
                            Log.e("HomeFragment", "Failed to fetch event name.", exc);

                            if (isAdded())
                                ToastManager.show(getContext(), "Failed to fetch event name.", Toast.LENGTH_LONG);

                            if (exc instanceof FirebaseFunctionsException) {
                                Log.e("HomeFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exc).getCode());
                            }
                        });
            }

            @Override
            public void dismissed(int position, UUID notificationId) {
                notifications.remove(position);
                if (adapter != null) adapter.notifyItemRemoved(position);
                notificationModel.dismissNotification(userId, deviceId, notificationId)
                        .addOnFailureListener(exc -> {
                            Log.e("HomeFragment", "Failed to dismiss notification.", exc);

                            if (isAdded())
                                ToastManager.show(getContext(), "Failed to dismiss notification.", Toast.LENGTH_LONG);

                            if (exc instanceof FirebaseFunctionsException) {
                                Log.e("HomeFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exc).getCode());
                            }
                        });
            }

            @Override
            public void accepted(UUID eventId) {
                inviteModel.inviteAccept(userId, deviceId, eventId)
                        .addOnSuccessListener(x -> {
                            if (isAdded())
                                ToastManager.show(getContext(), "Successfully accepted invitation.", Toast.LENGTH_LONG);
                        })
                        .addOnFailureListener(exc -> {
                            Log.e("HomeFragment", "Failed to accept invite..", exc);

                            if (isAdded())
                                ToastManager.show(getContext(), "Failed to accept invite.", Toast.LENGTH_LONG);

                            if (exc instanceof FirebaseFunctionsException) {
                                Log.e("HomeFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exc).getCode());
                            }
                        });
            }

            @Override
            public void declined(UUID eventId) {
                inviteModel.inviteReject(userId, deviceId, eventId)
                        .addOnSuccessListener(x -> {
                            if (isAdded())
                                ToastManager.show(getContext(), "Successfully rejected invitation.", Toast.LENGTH_LONG);
                        })
                        .addOnFailureListener(exc -> {
                            Log.e("HomeFragment", "Failed to reject invite..", exc);

                            if (isAdded())
                                ToastManager.show(getContext(), "Failed to reject invite.", Toast.LENGTH_LONG);

                            if (exc instanceof FirebaseFunctionsException) {
                                Log.e("HomeFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exc).getCode());
                            }
                        });
            }

            @Override
            public void goToEvent(UUID eventId) {
                NavDirections action = ca.quanta.quantaevents.fragments.HomeFragmentDirections.actionHomeFragmentToEventDetailsFragment(eventId);
                Navigation.findNavController(binding.getRoot()).navigate(action);
            }

            @Override
            public void setAdapter(UndismissedNotificationAdapter adapter) {
                this.adapter = adapter;
            }
        };

        UndismissedNotificationAdapter adapter = new UndismissedNotificationAdapter(notifications, handler);

        // **** set up the notification recycler views

        binding.undismissedNotificationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.undismissedNotificationsRecyclerView.setAdapter(adapter);

        // **** display toast in case there are none

        if (notifications.isEmpty()) {
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
