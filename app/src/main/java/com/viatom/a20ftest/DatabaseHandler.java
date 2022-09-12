package com.viatom.a20ftest;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper{


    public DatabaseHandler(LivehistoryActivity context) {
        super((Context) context, "Database1", null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CreateTable = "create table Table1(xValue INTEGER, yValue INTEGER)";
        sqLiteDatabase.execSQL(CreateTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insertToData(long ValX, int ValY){

        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues  contentValues = new ContentValues();

        contentValues.put("xValue", ValX);
        contentValues.put("yValue", ValY);

        database.insert("Table1",null,contentValues);


    }
}

