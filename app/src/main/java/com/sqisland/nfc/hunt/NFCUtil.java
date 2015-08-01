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

// https://gist.github.com/luixal/5768921
public abstract class NFCUtil {
  // List of NFC technologies detected
  private static final String[][] techList = new String[][] {
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

  public static void enable(Activity activity) {
    PendingIntent pendingIntent = PendingIntent.getActivity(
        activity, 0, new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

    IntentFilter filter = new IntentFilter();
    filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
    filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
    filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);

    // Enable foreground dispatch for getting intent from NFC event
    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
    nfcAdapter.enableForegroundDispatch(
        activity, pendingIntent, new IntentFilter[]{filter}, techList);
  }

  public static String extractTag(Intent intent) {
    if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
      return ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
    }
    return null;
  }

  private static String ByteArrayToHexString(byte[] inarray) {
    int i, j, in;
    String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    String out = "";

    for (j = 0; j < inarray.length; ++j) {
      in = (int) inarray[j] & 0xff;
      i = (in >> 4) & 0x0f;
      out += hex[i];
      i = in & 0x0f;
      out += hex[i];
    }
    return out;
  }
}