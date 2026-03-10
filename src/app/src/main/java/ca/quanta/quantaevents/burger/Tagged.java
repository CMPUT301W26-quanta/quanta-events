package ca.quanta.quantaevents.burger;

import androidx.annotation.NonNull;

import java.util.UUID;

public interface Tagged {
    @NonNull
    UUID getUniqueTag();
}
