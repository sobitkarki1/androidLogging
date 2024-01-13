package com.example.exportcontactsascsv;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_READ_CONTACTS = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    // Reference the ScrollView and TextView by their ids
    ScrollView scrollView = findViewById(R.id.scrollView);
    TextView logTextView = findViewById(R.id.logTextView);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
            } else {
                // Permission already granted, proceed to read contacts

            }

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }

           // secondFunction();


            readContacts();

    }

    protected void onStart() {
        super.onStart();
    }

    private void secondFunction() {




        // Display log messages in the TextView
        for(int i=0; i<=50; i++ )
            logTextView.append("It can be anything " + 2*i + "\n");

        // Scroll to the bottom of the ScrollView
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_READ_CONTACTS:
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed
                    readContacts();
                } else {
                    // Permission denied, handle accordingly
                }
                break;
        }
    }

    private void readContacts() {
        Cursor cursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String contactNumber = getPhoneNumber(cursor);
                logTextView.append("Name: " + contactName + " | Number: " + contactNumber + "\n");

                // Save the contact to CSV file
                saveContactToCSV(contactName, contactNumber);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    @SuppressLint("Range")
    private String getPhoneNumber(Cursor cursor) {
        @SuppressLint("Range") int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
        String phoneNumber = "";

        if (hasPhoneNumber > 0) {
            @SuppressLint("Range") Cursor phoneCursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)),
                    null,
                    null
            );

            if (phoneCursor != null && phoneCursor.moveToFirst()) {
                phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneCursor.close();
            }
        }

        return phoneNumber;
    }

    private void saveContactToCSV(String name, String number) {
        // Check if external storage is available
        if (isExternalStorageWritable()) {
            // Create a CSV file in the Downloads directory
            File csvFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "contacts.csv");

            try {
                // Append contact details to the CSV file
                FileWriter writer = new FileWriter(csvFile, true);
                writer.append(name).append(",").append(number).append("\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}

