//package ca.quanta.quantaevents.fragments;
//
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.navigation.NavDirections;
//import androidx.navigation.Navigation;
//
//import com.google.android.material.textfield.TextInputEditText;
//
//import java.time.format.DateTimeFormatter;
//import java.util.UUID;
//
//import ca.quanta.quantaevents.R;
//import ca.quanta.quantaevents.adapters.EventCardAdapter;
//import ca.quanta.quantaevents.burger.Tagged;
//import ca.quanta.quantaevents.databinding.FragmentEntrantEventListBinding;
//import ca.quanta.quantaevents.databinding.FragmentEventDetailsBinding;
//import ca.quanta.quantaevents.loading.LoaderState;
//import ca.quanta.quantaevents.stores.SessionStore;
//import ca.quanta.quantaevents.utils.ToastManager;
//import ca.quanta.quantaevents.viewmodels.EventViewModel;
//import ca.quanta.quantaevents.viewmodels.ImageViewModel;
//
//public class CommentsFragment extends Fragment {
//
//
//
//
//
//
//
//    // update comment details in database and
//    private void saveComment() {
//
//        String comment = safeText(binding.inputComment.getText());
//        String time = getUtcValue(binding.getCommentTime);
//    }
//
//    private static String safeText(@Nullable CharSequence text) {
//        return text == null ? "" : text.toString().trim();
//    }
//
//    private String getUtcValue(TextInputEditText input) {
//        Object tag = input.getTag();
//        if (tag instanceof String) {
//            return (String) tag;
//        }
//        return safeText(input.getText());
//    }
//
//
//}
