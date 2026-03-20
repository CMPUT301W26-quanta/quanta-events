package ca.quanta.quantaevents.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentShowQrBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class ShowQRFragment extends Fragment {
    private FragmentShowQrBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UUID eventId = ShowQRFragmentArgs.fromBundle(getArguments()).getEventId();

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to QR code and the subtitle to share an event qr code.
        // also sets the icon for the page
        infoStore.setTitle("QR Code");
        infoStore.setSubtitle("Share an event QR code");
        infoStore.setIconRes(R.drawable.material_symbols_group_outline);

        // sets up the qrcode image view to generate the QR code bitmap and set it as the image source
        binding.qrCodeImage.post(() -> {
            try {
                binding.qrCodeImage.setImageBitmap(generateQrCode(eventId, binding.qrCodeImage.getWidth()));
            } catch (WriterException e) {
                throw new RuntimeException(e);
            }
        });

        // sets up the back button listener
        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );

        // set up the share button listener
        binding.shareButton.setOnClickListener(v -> {
            Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) binding.qrCodeImage.getDrawable()).getBitmap();

            try {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "qr_code.png");
                values.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png");

                android.net.Uri uri = requireContext().getContentResolver().insert(
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                );

                try (java.io.OutputStream stream = requireContext().getContentResolver().openOutputStream(uri)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Scan this QR code to join the event!");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share QR Code"));

            } catch (Exception e) {
                android.widget.Toast.makeText(requireContext(), "Failed to share QR code", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    // generates th bit map QRCode
    private Bitmap generateQrCode(UUID eventId, int edgeSize) throws WriterException {
        BarcodeEncoder writer = new BarcodeEncoder();

        return writer.encodeBitmap("quanta-events:" + eventId.toString(), BarcodeFormat.QR_CODE, edgeSize, edgeSize);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentShowQrBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
