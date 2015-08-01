package com.sqisland.nfc.hunt.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class DatabaseUtility extends SQLiteOpenHelper {
  private static final String DATABASE_NAME = "nfc_hunt.db";
  private static final int DATABASE_VERSION = 1;

  public DatabaseUtility(Context context) {
    this(context, DATABASE_NAME);
  }

  public DatabaseUtility(Context context, String name) {
    super(context, name, null, DATABASE_VERSION);
  }

  static {
    cupboard().register(NfcItem.class);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    cupboard().withDatabase(db).createTables();
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    cupboard().withDatabase(db).upgradeTables();
  }
}