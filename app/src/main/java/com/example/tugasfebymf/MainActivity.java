package com.example.tugasfebymf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int IJIN_MENGIRIM_SMS=101;
    private Button btnkirim;
    private EditText txNoHp , txPesan;
    BroadcastReceiver smsKirimReceiver,smsSampaiReceiver;
    private final String SEND="PESAN TERKIRIM";
    private final String DELIVERED="PESAN TELAH SAMPAI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnkirim=findViewById(R.id.btnkirim);
        txNoHp=findViewById(R.id.txNoHp);
        txPesan=findViewById(R.id.txPesan);

        btnkirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cek nilai Edit text
                String NoHp=txNoHp.getText().toString();
                String pesanSMS=txPesan.getText().toString();

                if(NoHp.equals("")){
                    Toast.makeText(MainActivity.this, "No HP Kosong", Toast.LENGTH_SHORT).show();
                    txNoHp.requestFocus();
                    return;
                }
                if(pesanSMS.equals("")){
                    Toast.makeText(MainActivity.this, "Pesan SMS Kosong", Toast.LENGTH_SHORT).show();
                    txNoHp.requestFocus();
                    return;
                }
                //cek ijin kirim sms di manifest
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS)
                          != PackageManager.PERMISSION_GRANTED){
                    //minta ijin untuk kirim SMS
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS}, IJIN_MENGIRIM_SMS);
                } else {
                    kirimSMS(NoHp,pesanSMS);
                }
            }
        });
    }

    private void kirimSMS(String noHp, String pesanSMS) {

        PendingIntent kirimPI=PendingIntent.getBroadcast(this,0,new Intent(SEND),0);
        PendingIntent SampaiPI=PendingIntent.getBroadcast(this,0,new Intent(DELIVERED),0);

        SmsManager smsManager= SmsManager.getDefault();
        smsManager.sendTextMessage(noHp,null,pesanSMS,kirimPI,SampaiPI);
    }

    @Override
    protected void onResume() {
        super.onResume();

        smsKirimReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "Pesan Terkirim", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "Tidak Ada Signal", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Airplane Mode / OFF Air", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        smsSampaiReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "Pesan telah Sampai", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_CANCELLED:
                        Toast.makeText(context, "Pesan tidak Sampai", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };
        registerReceiver(smsKirimReceiver,new IntentFilter(SEND));
        registerReceiver(smsSampaiReceiver,new IntentFilter(DELIVERED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(smsKirimReceiver);
        unregisterReceiver(smsSampaiReceiver);
    }
}