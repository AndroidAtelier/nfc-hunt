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
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URLEncoder;

public class MainActivity extends Activity {
  private static final String TAG = "nfc_hunt";
  
  public static final MediaType MEDIA_TYPE_JSON
      = MediaType.parse("application/json; charset=utf-8");

  private static final String USERNAME = "9242bc24-02ac-45c0-bb2f-53f6f06ae7d9";
  private static final String PASSWORD = "HxoKXnZqz4Qu";

  OkHttpClient client = new OkHttpClient();

  private TextView textView;
  private TextView ttsInputView;
  private TextView statusView;

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
    ttsInputView = (TextView) findViewById(R.id.tts_input);
    statusView = (TextView) findViewById(R.id.status);

    client.setAuthenticator(new Authenticator() {
      @Override
      public Request authenticate(Proxy proxy, Response response) throws IOException {
        String credential = Credentials.basic(USERNAME, PASSWORD);
        return response.request().newBuilder().header("Authorization", credential).build();
      }

      @Override
      public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
        return null;
      }
    });
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

  public void textToSpeech(View v) {
    String text = ttsInputView.getText().toString();
    if (TextUtils.isEmpty(text)) {
      return;
    }

    try {
      fetchSoundFile(text);
    } catch (IOException e) {
      Log.e(TAG, "Failed to fetch sound", e);
      statusView.setText(e.getMessage());
    }
  }

  private void fetchSoundFile(String text) throws IOException {
    final File file = getSoundFile(text);

    if (file.isFile()) {
      statusView.setText(R.string.previously_fetched);
      Log.e(TAG, file.toString());
      return;
    }

    String json = "{\"text\":\"" + text + "\"}";
    Request request = new Request.Builder()
        .url("https://stream.watsonplatform.net/text-to-speech/api/v1/synthesize")
        .addHeader("Content-Type", "application/json")
        .addHeader("Accept", "audio/wav")
        .post(RequestBody.create(MEDIA_TYPE_JSON, json))
        .build();

    client.newCall(request).enqueue(new Callback() {
      @Override public void onFailure(Request request, final IOException e) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            statusView.setText(e.getMessage());
          }
        });
        Log.e(TAG, Log.getStackTraceString(e));
      }

      @Override public void onResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
          throw new IOException("Unexpected code " + response);
        }

        writeToFile(response.body().byteStream(), file);
        Log.e(TAG, file.toString());
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            statusView.setText(R.string.fetched);
          }
        });
      }
    });
  }

  private File getSoundFile(String text) {
    File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "nfc_hunt");
    if (!dir.isDirectory()) {
      dir.mkdir();
    }

    String filename = "output.wav";
    try {
      filename = URLEncoder.encode(text, "utf-8") + ".wav";
    } catch (UnsupportedEncodingException e) {
      Log.e(TAG, "Error encoding filename" + e);
    }
    return new File(dir, filename);
  }

  private void writeToFile(InputStream input, File file) throws IOException {
    FileOutputStream output = new FileOutputStream(file);
    int bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];
    int len;
    while ((len = input.read(buffer)) != -1) {
      output.write(buffer, 0, len);
    }
  }
}