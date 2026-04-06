package ca.quanta.quantaevents.viewmodels;

import java.util.UUID;

public class UUIDHolder {
    private UUID uuid;

    UUIDHolder() {
        this.uuid = null;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
