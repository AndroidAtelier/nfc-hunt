package com.sqisland.nfc.hunt;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sqisland.nfc.hunt.database.DatabaseUtility;
import com.sqisland.nfc.hunt.database.NfcItem;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.HashMap;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class MainActivity extends Activity {
  private static final String TAG = "nfc_hunt";

  private static final String USERNAME    = "9242bc24-02ac-45c0-bb2f-53f6f06ae7d9";
  private static final String PASSWORD    = "HxoKXnZqz4Qu";
  private static final String NFC_DUCK    = "0422957A712881";
  private static final String NFC_SHOE    = "370700001A37D5";
  private static final String NFC_FROG    = "04BB0B625D2B84";
  private static final String NFC_MITT    = "3707000020E0A6";
  private static final String NFC_HAT     = "370700001A3118";
  private static final String NFC_TISSUES = "370700001A3353";
  private static final HashMap<String, String> things = new HashMap<>();

  OkHttpClient client = new OkHttpClient();

  private TextView textView;
  private TextView ttsInputView;
  private TextView statusView;

  private SQLiteDatabase db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    textView = (TextView) findViewById(R.id.text);
    ttsInputView = (TextView) findViewById(R.id.tts_input);
    statusView = (TextView) findViewById(R.id.status);

    populateThings();

    DatabaseUtility databaseUtility = new DatabaseUtility(this);
    db = databaseUtility.getWritableDatabase();

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
    NFCUtil.enable(this);
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
    String nfcTag = NFCUtil.extractTag(intent);
    if (nfcTag == null) {
      return;
    }

    textView.setText(loadLabel(nfcTag));
    Log.e(TAG, nfcTag);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_edit_tag) {
      Intent intent = new Intent(this, EditTagActivity.class);
      startActivity(intent);
      return true;
    }

    return super.onOptionsItemSelected(item);
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

    String url = "https://stream.watsonplatform.net/text-to-speech/api/v1/synthesize?accept=audio/wav&text=" + urlEncode(text) + "&voice=en-US_AllisonVoice";
    Request request = new Request.Builder()
        .url(url)
        .get()
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

    String filename = urlEncode(text) + ".wav";
    return new File(dir, filename);
  }

  private String urlEncode(String text) {
    try {
      return URLEncoder.encode(text, "utf-8");
    } catch (UnsupportedEncodingException e) {
      Log.e(TAG, "Error encoding text" + e);
    }
    return text;
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

  private void populateThings() {
    things.put(NFC_DUCK, "Duck");
    things.put(NFC_SHOE, "Shiny shoe");
    things.put(NFC_FROG, "Frog");
    things.put(NFC_MITT, "Oven mitt");
    things.put(NFC_HAT, "Green hat");
    things.put(NFC_TISSUES, "Tissue box");
  }

  private String loadLabel(String tag) {
    NfcItem item = cupboard().withDatabase(db)
        .query(NfcItem.class)
        .withSelection("tag = ?", tag).get();
    if (item == null) {
      return tag;
    }
    return item.label;
  }
}