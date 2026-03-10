package ca.quanta.quantaevents.burger;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.UUID;

public class SmartBurgerState extends ViewModel {
    private UUID active;
    private int groupFilter;
    private final MutableLiveData<Boolean> display;

    SmartBurgerState() {
        display = new MutableLiveData<>(false);
    }

    UUID getActive() {
        return active;
    }

    int getGroupFilter() {
        return groupFilter;
    }

    LiveData<Boolean> getDisplay() {
        return display;
    }

    /**
     * Allows a given bitmask of groups to be displayed in the {@link SmartBurger}
     *
     * @param group A bitmask combination of {@link SmartBurger#ENTRANT_GROUP}, {@link SmartBurger#ORGANIZER_GROUP}, and {@link SmartBurger#ADMIN_GROUP}
     */
    public void allowGroup(int group) {
        this.groupFilter |= group;
    }

    /**
     * Disallows a given bitmask of groups from being displayed in the {@link SmartBurger}
     *
     * @param group A bitmask combination of {@link SmartBurger#ENTRANT_GROUP}, {@link SmartBurger#ORGANIZER_GROUP}, and {@link SmartBurger#ADMIN_GROUP}
     */
    public void disallowGroup(int group) {
        this.groupFilter &= ~group;
    }

    /**
     * Sets a given bitmask of groups to be displayed in the {@link SmartBurger}
     *
     * @param group A bitmask combination of {@link SmartBurger#ENTRANT_GROUP}, {@link SmartBurger#ORGANIZER_GROUP}, and {@link SmartBurger#ADMIN_GROUP}
     */
    public void setGroupFilter(int group) {
        this.groupFilter = group;
    }

    public <F extends Fragment & Tagged> void show(F fragment) {
        this.active = fragment.getUniqueTag();
        this.display.setValue(true);
    }

    public void hide() {
        this.display.setValue(false);
    }
}
