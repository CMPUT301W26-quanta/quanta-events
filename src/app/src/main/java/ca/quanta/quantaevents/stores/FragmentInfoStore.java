package ca.quanta.quantaevents.stores;

import androidx.annotation.DrawableRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;



public class FragmentInfoStore extends ViewModel {
    MutableLiveData<Integer> iconRes = new MutableLiveData<>();

    MutableLiveData<String> title = new MutableLiveData<>();


    MutableLiveData<String> subtitle = new MutableLiveData<>();

    public LiveData<Integer> getIconRes() {
        return this.iconRes;
    }

    public LiveData<String> getTitle() {
        return title;
    }

    public LiveData<String> getSubtitle() {
        return subtitle;
    }

    public void setIconRes(@DrawableRes int resId) {
        this.iconRes.setValue(resId);
    }

    public void setTitle(String title) {
        this.title.setValue(title);
    }

    public void setSubtitle(String subtitle) {
        this.subtitle.setValue(subtitle);
    }
}
