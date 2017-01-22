package com.telpon.main;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.telpon.utils.AppUtils;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();

    private static final int CALL_PHONE_PERMISSION = 1;
    private static final int PICK_CONTACT = 2;

    private TextInputLayout tilDialName, tilDialNumber;
    private TextInputEditText tiedDialName, tiedDialNumber;
    private Button btCreateShortcut;
    private TextView tvGetContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PHONE_PERMISSION);
            return;
        }

        initView();
        setActionView();
    }

    private void initView() {
        tilDialName = (TextInputLayout) findViewById(R.id.til_dial_name);
        tilDialNumber = (TextInputLayout) findViewById(R.id.til_dial_number);
        tiedDialName = (TextInputEditText) findViewById(R.id.tied_dial_name);
        tiedDialNumber = (TextInputEditText) findViewById(R.id.tied_dial_number);
        tvGetContact = (TextView) findViewById(R.id.tv_get_contact);
        btCreateShortcut = (Button) findViewById(R.id.bt_create_shortcut);
    }

    private void setActionView() {
        tvGetContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intentContact, PICK_CONTACT);
            }
        });

        btCreateShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tilDialName.setError(null);
                tilDialNumber.setError(null);

                if (TextUtils.isEmpty(tiedDialName.getText().toString())) {
                    tilDialName.setError(getResources().getString(R.string.cannot_blank));
                    tiedDialName.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(tiedDialNumber.getText().toString())) {
                    tilDialNumber.setError(getResources().getString(R.string.cannot_blank));
                    tiedDialNumber.requestFocus();
                    return;
                }

                if (tiedDialName.getText().toString().length() > 10) {
                    tilDialName.setError(getResources().getString(R.string.invalid_dial_name_length));
                    tiedDialName.requestFocus();
                    return;
                }

//                if (android.util.Patterns.PHONE.matcher(tiedDialNumber.getText().toString()).matches()) {
//                    tilDialNumber.setError(getResources().getString(R.string.invalid_phone_number));
//                    tiedDialNumber.requestFocus();
//                    return;
//                }

                createDialShortcut(tiedDialName.getText().toString(), tiedDialNumber.getText().toString());
                tiedDialName.setText(null);
                tiedDialNumber.setText(null);
                tiedDialName.requestFocus();
                Toast.makeText(getApplicationContext(), "Success add dial shortcut", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CALL_PHONE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            CALL_PHONE_PERMISSION);
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    tiedDialName.setText(null);
                    tiedDialNumber.setText(null);

                    Log.e(TAG, "OKE BRO");
                    String contactName = retrieveContactName(data.getData());
                    if (contactName != null)
                        tiedDialName.setText(contactName);

                    String contactNo = retrieveContactNumber(data.getData());
                    if (contactNo != null)
                        tiedDialNumber.setText(contactNo);

                    if (contactName == null && contactNo == null)
                        Toast.makeText(getApplicationContext(), "Failed get contact name & phone number", Toast.LENGTH_SHORT).show();
                    else if (contactName == null) {
                        tiedDialName.requestFocus();
                        Toast.makeText(getApplicationContext(), "Failed get contact name", Toast.LENGTH_SHORT).show();
                    } else if (contactNo == null) {
                        tiedDialNumber.requestFocus();
                        Toast.makeText(getApplicationContext(), "Failed get phone number", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private String retrieveContactName(Uri uriContact) {
        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {
            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();
        Log.e(TAG, "Contact Name: " + contactName);
        return contactName;
    }

    private String retrieveContactNumber(Uri uriContact) {
        String contactID = null;
        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }
        cursorID.close();

        if (contactID == null)
            return null;

        Log.e(TAG, "Contact ID: " + contactID);
        // Using the contact ID now we will get contact phone number
//        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
//                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
//                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
//                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
//
//                new String[]{contactID},
//                null);
//
//        if (cursorPhone.moveToFirst()) {
//            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//        }
//        cursorPhone.close();

        Cursor cursorPhone = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactID}, null);
        while (cursorPhone.moveToNext()) {
            // Get first phone number
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        cursorPhone.close();

        Log.e(TAG, "Contact Phone Number: " + contactNumber);
        return contactNumber;
    }

    private void createDialShortcut(String initialName, String phoneNumber) {
        //Adding shortcut on Home screen
        Intent shortcutIntent = createIntentDialPhoneNumber(phoneNumber);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, initialName.toUpperCase());

        int tileSize = getResources().getDimensionPixelSize(R.dimen.letter_tile_size);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                AppUtils.generateBitmapFromLetter(initialName,
                        getRandomColorSequence(), getApplicationContext(), tileSize));

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);
    }

    private Intent createIntentDialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        return intent;
    }

    private int getRandomColorSequence() {
        Random r = new Random();
        int Low = 0;
        int High = 18;
        return r.nextInt(High - Low) + Low;
    }

}
