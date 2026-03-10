package ca.quanta.quantaevents.burger;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.UUID;

public class SmartBurgerState extends ViewModel {
    private UUID active;
    private final MutableLiveData<Boolean> display;

    SmartBurgerState() {
        display = new MutableLiveData<>(false);
    }

    UUID getActive() {
        return active;
    }

    LiveData<Boolean> getDisplay() {
        return display;
    }

    public <F extends Fragment & Tagged> void show(F fragment) {
        this.active = fragment.getUniqueTag();
        this.display.setValue(true);
    }

    public void hide() {
        this.display.setValue(false);
    }
}
