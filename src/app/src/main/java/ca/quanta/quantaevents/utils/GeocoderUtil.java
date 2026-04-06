package ca.quanta.quantaevents.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocoderUtil {

    public interface GeocodingCallback {
        void onResult(String locationName);
    }

    public static void reverseGeocode(Context context, LatLng latLng, GeocodingCallback callback) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String fallback = String.format("%.5f, %.5f", latLng.latitude, latLng.longitude);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1, addresses -> {
                if (addresses != null && !addresses.isEmpty()) {
                    callback.onResult(addresses.get(0).getAddressLine(0));
                } else {
                    callback.onResult(fallback);
                }
            });
        } else {
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    callback.onResult(addresses.get(0).getAddressLine(0));
                } else {
                    callback.onResult(fallback);
                }
            } catch (IOException e) {
                callback.onResult(fallback);
            }
        }
    }
}