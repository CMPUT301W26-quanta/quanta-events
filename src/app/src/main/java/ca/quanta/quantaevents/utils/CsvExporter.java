package ca.quanta.quantaevents.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import ca.quanta.quantaevents.models.ExternalUser;

public class CsvExporter {

    private static final String TAG = "CsvExporter";

    public static void saveToDownloads(Context context, List<ExternalUser> users, String listType) throws IOException {
        String fileName = listType + "_export.csv";
        String csvContent = buildCsvContent(users);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, fileName, csvContent);
        } else {
            saveViaFile(fileName, csvContent);
        }
        Log.d(TAG, "CSV saved to Downloads: " + fileName);
    }


    // For device withs API 29+
    private static Uri saveViaMediaStore(Context context, String fileName, String csvContent) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri itemUri = context.getContentResolver().insert(collection, values);
        if (itemUri == null) throw new IOException("MediaStore insert failed");

        try (OutputStream os = context.getContentResolver().openOutputStream(itemUri);
             OutputStreamWriter writer = new OutputStreamWriter(os)) {
            writer.write(csvContent);
        }
        return itemUri;
    }

    // for devices With between API 26-28
    private static void saveViaFile(String fileName, String csvContent) throws IOException {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(csvContent);
        }
    }

    private static String buildCsvContent(List<ExternalUser> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name,Email,Phone\n");
        for (ExternalUser user : users) {
            sb.append(escapeCsv(user.getName())).append(",")
                    .append(escapeCsv(user.getEmail())).append(",")
                    .append(escapeCsv(user.getPhone())).append("\n");
        }
        return sb.toString();
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}