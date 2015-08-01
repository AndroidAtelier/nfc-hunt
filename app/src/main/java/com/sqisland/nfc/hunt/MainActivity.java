package com.sqisland.nfc.hunt;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
  private TextView textView;

  // https://gist.github.com/luixal/5768921
  // List of NFC technologies detected
  private final String[][] techList = new String[][] {
      new String[] {
          NfcA.class.getName(),
          NfcB.class.getName(),
          NfcF.class.getName(),
          NfcV.class.getName(),
          IsoDep.class.getName(),
          MifareClassic.class.getName(),
          MifareUltralight.class.getName(),
          Ndef.class.getName()
      }
  };
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    textView = (TextView) findViewById(R.id.text);
  }

  @Override
  protected void onResume() {
    super.onResume();

    PendingIntent pendingIntent = PendingIntent.getActivity(
        this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

    IntentFilter filter = new IntentFilter();
    filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
    filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
    filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);

    // Enable foreground dispatch for getting intent from NFC event
    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    nfcAdapter.enableForegroundDispatch(
        this, pendingIntent, new IntentFilter[]{filter}, this.techList);
  }

  @Override
  protected void onPause() {
    super.onPause();

    // Disable foreground dispatch
    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    nfcAdapter.disableForegroundDispatch(this);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
      textView.setText(ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)));
    }
  }

  private String ByteArrayToHexString(byte [] inarray) {
    int i, j, in;
    String [] hex = { "0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F" };
    String out= "";

    for (j = 0 ; j < inarray.length ; ++j) {
      in = (int) inarray[j] & 0xff;
      i = (in >> 4) & 0x0f;
      out += hex[i];
      i = in & 0x0f;
      out += hex[i];
    }
    return out;
  }
}