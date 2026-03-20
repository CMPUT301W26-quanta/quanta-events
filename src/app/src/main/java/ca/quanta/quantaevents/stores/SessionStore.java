package ca.quanta.quantaevents.stores;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
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
        userId.setValue(prefs.getString(KEY_USER_ID, null));
        deviceId.setValue(prefs.getString(KEY_DEVICE_ID, null));
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
        MediatorLiveData<SessionPair> combined = new MediatorLiveData<>();
        Runnable emit = () -> {
            UUID currentUser = parseUUID(getUserId().getValue());
            UUID currentDevice = parseUUID(getDeviceId().getValue());
            combined.setValue(new SessionPair(currentUser, currentDevice));
        };
        combined.addSource(getUserId(), _value -> emit.run());
        combined.addSource(getDeviceId(), _value -> emit.run());

        final SessionPair[] lastEmitted = new SessionPair[1];
        combined.observe(owner, pair -> {
            if (pair == null) {
                return;
            }
            boolean bothPresent = pair.userId != null && pair.deviceId != null;
            boolean bothMissing = pair.userId == null && pair.deviceId == null;
            if (!bothPresent && !bothMissing) {
                return;
            }
            if (pair.equals(lastEmitted[0])) {
                return;
            }
            lastEmitted[0] = pair;
            listener.onSessionChanged(pair.userId, pair.deviceId);
        });
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

    private static final class SessionPair {
        @Nullable
        final UUID userId;
        @Nullable
        final UUID deviceId;

        private SessionPair(@Nullable UUID userId, @Nullable UUID deviceId) {
            this.userId = userId;
            this.deviceId = deviceId;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SessionPair)) {
                return false;
            }
            SessionPair other = (SessionPair) obj;
            return java.util.Objects.equals(userId, other.userId)
                    && java.util.Objects.equals(deviceId, other.deviceId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(userId, deviceId);
        }
    }
}
