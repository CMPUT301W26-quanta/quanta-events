package ca.quanta.quantaevents;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import ca.quanta.quantaevents.databinding.ActivityMainBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FragmentInfoStore infoStore = new ViewModelProvider(this).get(FragmentInfoStore.class);

        infoStore.getTitle()
                .observe(
                        this,
                        binding.titleView::setText
                );

        infoStore.getSubtitle()
                .observe(
                        this,
                        binding.subtitleView::setText
                );

        infoStore.getIconRes()
                .observe(
                        this,
                        iconRes -> binding.iconView.setImageDrawable(AppCompatResources.getDrawable(this, iconRes))
                );
    }
}