package com.prolan.fastsms;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private Button sendBtn;
    private EditText txtphoneNo;
    private EditText txtMessage;
    private TextInputLayout inputNumber, inputMessage ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Loading views
        sendBtn = (Button) findViewById(R.id.btnSendSMS);
        txtphoneNo = (EditText) findViewById(R.id.editText);
        txtMessage = (EditText) findViewById(R.id.editText2);
        inputMessage = (TextInputLayout) findViewById(R.id.input_layout_textMessage);
        inputNumber = (TextInputLayout) findViewById(R.id.input_layout_textPhone);



        //Reading the last phone number used
        SharedPreferences prefs = getSharedPreferences("myLstPhone", Context.MODE_PRIVATE);
        String infoCollected = prefs.getString("data", "");
        txtphoneNo.setText(infoCollected.trim());
        Log.d("DataCollected->"+infoCollected,"");


        //Listener from button
        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //Checking the version of the current SDK
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    //Asking for permissions before send the message
                    int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.SEND_SMS);
                    if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED)
                        requestPermissions(new String[] {Manifest.permission.SEND_SMS},REQUEST_CODE_ASK_PERMISSIONS);
                    else
                        sendSMS();
                }else
                    sendSMS();
            }
        });


    }

    @Override
    protected void onStop() {
        //Saving the last phone number used
        SharedPreferences prefs =getSharedPreferences("myLstPhone",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        //Data is the key to retrieve the information
        editor.putString("data", txtphoneNo.getText().toString());
        editor.commit();
        Log.d("DataSaved->" + txtphoneNo.getText().toString(),"");
        super.onStop();
    }

    protected void sendSMS(){
        String labelNumber = getString(R.string.need_a_number);
        String labelMessage = getString(R.string.need_a_message);
        String errLabelNum = getString(R.string.err_number);
        String errLabelMsg = getString(R.string.err_messageSMS);
        Log.d("Send msg","");
        String phonNum = txtphoneNo.getText().toString();
        String txtSMS = txtMessage.getText().toString();
        if(phonNum.trim().isEmpty() ){
            Toast.makeText(getApplicationContext(),labelNumber, Toast.LENGTH_LONG).show();
            inputNumber.setError(errLabelNum);
            inputMessage.setErrorEnabled(false);
            txtphoneNo.requestFocus();
        }
        else if (txtSMS.trim().isEmpty()){
            Toast.makeText(getApplicationContext(),labelMessage, Toast.LENGTH_LONG).show();
            inputMessage.setError(errLabelMsg);
            txtMessage.requestFocus();
            inputNumber.setErrorEnabled(false);
        }
        else{
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phonNum,null,txtSMS,null,null);
                Toast.makeText(getApplicationContext(),"SMS Sent", Toast.LENGTH_LONG).show();
                txtMessage.setText("");
                inputMessage.setErrorEnabled(false);
                inputNumber.setErrorEnabled(false);
                Log.d("Finished sending SMS...","");
            }catch (android.content.ActivityNotFoundException ex){
                Toast.makeText(MainActivity.this,"SMS failed, please try again later", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
