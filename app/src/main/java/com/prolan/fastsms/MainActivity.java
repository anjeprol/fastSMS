package com.prolan.fastsms;


import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private final int   REQ_CODE_SPEECH_INPUT = 100;
    private final static String DATA = "data";
    private final static String FILE_NAME = "myLstPhone";
    private Button      mSendBtn;
    private EditText    mPhoneNoEditText;
    private EditText    mMessagaEditText;
    private ImageButton mMicImageButton;
    private AdView mAdView;
    private TextInputLayout inputNumber, inputMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Load an ad into the AdMob banner view.
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                                                .setRequestAgent("android_studio:ad_template")
                                                .build();
        mAdView.loadAd(adRequest);

        //Loading views
        mSendBtn = (Button) findViewById(R.id.btnSendSMS);
        mPhoneNoEditText    = (EditText) findViewById(R.id.editText);
        mMessagaEditText    = (EditText) findViewById(R.id.editText2);
        inputMessage        = (TextInputLayout) findViewById(R.id.input_layout_textMessage);
        inputNumber         = (TextInputLayout) findViewById(R.id.input_layout_textPhone);
        mMicImageButton     = (ImageButton) findViewById(R.id.btnSpeak);


        //Reading the last phone number used
        SharedPreferences prefs = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String infoCollected = prefs.getString(DATA, "");
        mPhoneNoEditText.setText(infoCollected.trim());
        Log.d("DataCollected->" + infoCollected, "");


        //Listener from button
        mSendBtn.setOnClickListener(this);
        mMicImageButton.setOnClickListener(this);


    }

    @Override
    protected void onStop() {
        //Saving the last phone number used
        SharedPreferences prefs = getSharedPreferences("myLstPhone", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        //Data is the key to retrieve the information
        editor.putString("data", mPhoneNoEditText.getText().toString());
        editor.commit();
        Log.d("DataSaved->" + mPhoneNoEditText.getText().toString(), "");
        super.onStop();
    }

    protected void sendSMS() {
        String errLabelNum = getString(R.string.err_number);
        String errLabelMsg = getString(R.string.err_messageSMS);
        Log.d("Send msg", "");
        String phonNum = mPhoneNoEditText.getText().toString();
        String txtSMS = mMessagaEditText.getText().toString();
        if (phonNum.trim().isEmpty()) {
            inputNumber.setError(errLabelNum);
            inputMessage.setErrorEnabled(false);
            mPhoneNoEditText.requestFocus();
        } else if (txtSMS.trim().isEmpty()) {
            inputMessage.setError(errLabelMsg);
            mMessagaEditText.requestFocus();
            inputNumber.setErrorEnabled(false);
        } else {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phonNum, null, txtSMS, null, null);
                Toast.makeText(getApplicationContext(), "SMS Sent", Toast.LENGTH_LONG).show();
                mMessagaEditText.setText("");
                inputMessage.setErrorEnabled(false);
                inputNumber.setErrorEnabled(false);
                Log.d("Finished sending SMS...", "");
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "SMS failed, please try again later", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSendSMS:
                send();
                break;
            case R.id.btnSpeak:
                promptSpeechInput();
                break;
        }
    }
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,getString(R.string.speech_prompt));

        try
        {
            startActivityForResult(intent,REQ_CODE_SPEECH_INPUT);
        }
        catch (ActivityNotFoundException activityError)
        {
            Toast.makeText(getApplicationContext(),getString(R.string.speech_not_supported),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQ_CODE_SPEECH_INPUT:
                if(resultCode == RESULT_OK && null != data)
                {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mMessagaEditText.setText(result.get(0));
                }
                break;
        }
    }

    private void send(){
        //Checking the version of the current SDK
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Asking for permissions before send the message
            int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.SEND_SMS);
            if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
            }
            else
            {
                sendSMS();
            }
        }
        else
        {
            sendSMS();
        }
    }  @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}
