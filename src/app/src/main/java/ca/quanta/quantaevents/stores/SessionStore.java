package ca.quanta.quantaevents.stores;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.UUID;

/**
 * Shared session store backed by SharedPreferences.
 */
public class SessionStore extends AndroidViewModel {

    private static final String PREFS_NAME = "session_store";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_DEVICE_ID = "device_id";

    private final SharedPreferences prefs;
    private final MutableLiveData<String> userId = new MutableLiveData<>();
    private final MutableLiveData<String> deviceId = new MutableLiveData<>();
    private final MutableLiveData<Integer> roleMask = new MutableLiveData<>(0);

    @FunctionalInterface
    public interface SessionListener {
        void onSessionChanged(@Nullable UUID userId, @Nullable UUID deviceId);
    }

    public SessionStore(@NonNull Application application) {
        super(application);
        prefs = application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE);
        userId.setValue("26dee92a-d4d1-4faa-8157-cbd5d8ada4f1");//prefs.getString(KEY_USER_ID, null));
        deviceId.setValue("e291f380-2464-44e2-9f6f-874deee512e2");//prefs.getString(KEY_DEVICE_ID, null));
    }

    public LiveData<String> getUserId() {
        return userId;
    }

    public LiveData<String> getDeviceId() {
        return deviceId;
    }

    public LiveData<Integer> getRoleMask() {
        return roleMask;
    }

    public void setRoleMask(int mask) {
        roleMask.setValue(mask);
    }

    public boolean hasSession() {
        String user = userId.getValue();
        String device = deviceId.getValue();
        return user != null && !user.isEmpty() && device != null && !device.isEmpty();
    }

    public void observeSession(@NonNull LifecycleOwner owner, @NonNull SessionListener listener) {
        getUserId().observe(owner, value -> listener.onSessionChanged(parseUUID(value), parseUUID(getDeviceId().getValue())));
        getDeviceId().observe(owner, value -> listener.onSessionChanged(parseUUID(getUserId().getValue()), parseUUID(value)));
    }

    public void setSession(UUID userId, UUID deviceId) {
        String userIdValue = userId == null ? null : userId.toString();
        String deviceIdValue = deviceId == null ? null : deviceId.toString();
        this.userId.setValue(userIdValue);
        this.deviceId.setValue(deviceIdValue);
        prefs.edit()
                .putString(KEY_USER_ID, userIdValue)
                .putString(KEY_DEVICE_ID, deviceIdValue)
                .apply();
    }

    public void clearSession() {
        userId.setValue(null);
        deviceId.setValue(null);
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_DEVICE_ID)
                .apply();
    }

    @Nullable
    private static UUID parseUUID(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}