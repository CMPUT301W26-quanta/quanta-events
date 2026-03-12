package ca.quanta.quantaevents;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessagingService;

public class NotificationService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    public Task updateToken(@NonNull String token) {
        
    }
}
