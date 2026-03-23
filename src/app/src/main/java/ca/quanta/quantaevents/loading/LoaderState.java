package ca.quanta.quantaevents.loading;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;

public class LoaderState extends ViewModel {
    private final MutableLiveData<Boolean> activeLoader;

    LoaderState() {
        this.activeLoader = new MutableLiveData<>(false);
    }


    LiveData<Boolean> getActive() {
        return activeLoader;
    }

    public <T> void loadTask(Task<T> task) {
        activeLoader.setValue(true);
        task.addOnCompleteListener(_task -> {
            activeLoader.setValue(false);
        });
    }
}
