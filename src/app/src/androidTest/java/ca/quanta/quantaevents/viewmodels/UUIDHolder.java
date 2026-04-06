package ca.quanta.quantaevents.viewmodels;

import java.util.UUID;

public class UUIDHolder {
    UUID uuid;

    UUIDHolder(UUID uuid) {
        this.uuid = uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
