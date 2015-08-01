package com.sqisland.nfc.hunt;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sqisland.nfc.hunt.database.DatabaseUtility;
import com.sqisland.nfc.hunt.database.NfcItem;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class EditTagActivity extends Activity {
  public static final String KEY_NFC_TAG = "nfcTag";

  private TextView instructionView;
  private TextView nfcLabelView;
  private View saveButton;

  private SQLiteDatabase db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_tag);

    instructionView = (TextView) findViewById(R.id.instructions);
    nfcLabelView = (TextView) findViewById(R.id.nfc_label);
    saveButton = findViewById(R.id.save_button);

    DatabaseUtility databaseUtility = new DatabaseUtility(this);
    db = databaseUtility.getWritableDatabase();
  }

  @Override
  protected void onResume() {
    super.onResume();
    NFCUtil.enable(this);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    String nfcTag = NFCUtil.extractTag(intent);
    if (nfcTag != null) {
      setTitle(nfcTag);
      String label = loadLabel(nfcTag);
      if (!TextUtils.isEmpty(label)) {
        nfcLabelView.setText(label);
      }

      instructionView.setVisibility(View.GONE);
      nfcLabelView.setVisibility(View.VISIBLE);
      saveButton.setVisibility(View.VISIBLE);
    }
  }

  public void save(View v) {
    String label = nfcLabelView.getText().toString();
    if (!TextUtils.isEmpty(label)) {
      saveLabel(getTitle().toString(), label);
      Toast.makeText(this, getString(R.string.saved, label), Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  private String loadLabel(String tag) {
    NfcItem item = cupboard().withDatabase(db)
        .query(NfcItem.class)
        .withSelection("tag = ?", tag).get();
    if (item == null) {
      return null;
    }
    return item.label;
  }

  private void saveLabel(String tag, String label) {
    NfcItem item = cupboard().withDatabase(db)
        .query(NfcItem.class)
        .withSelection("tag = ?", tag).get();
    if (item == null) {
      item = new NfcItem();
      item.tag = tag;
    }
    item.label = label;
    cupboard().withDatabase(db).put(item);
  }
}