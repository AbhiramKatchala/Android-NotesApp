package io.praveen.typenote.SQLite;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import static io.praveen.typenote.SQLite.DatabaseContract.DatabaseEntry.KEY_DATE;
import static io.praveen.typenote.SQLite.DatabaseContract.DatabaseEntry.KEY_ID;
import static io.praveen.typenote.SQLite.DatabaseContract.DatabaseEntry.KEY_NOTE;
import static io.praveen.typenote.SQLite.DatabaseContract.DatabaseEntry.KEY_STAR;
import static io.praveen.typenote.SQLite.DatabaseContract.DatabaseEntry.TABLE_NOTES;

public class BinDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "noteManagerBin.db";


    public BinDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NOTES + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NOTE + " TEXT," + KEY_DATE + " TEXT," + KEY_STAR + " INTEGER DEFAULT 0);";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion){
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + KEY_STAR + " INTEGER DEFAULT 0");
        } else{
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        }
    }

    @NonNull
    public Note getNote(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.query(TABLE_NOTES, new String[]{KEY_ID, KEY_NOTE, KEY_DATE, KEY_STAR}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        assert cursor != null;
        return new Note(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getInt(3));

    }

    @NonNull
    public List<Note> getAllNotes() {
        List<Note> noteList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NOTES;
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setID(Integer.parseInt(cursor.getString(0)));
                note.setNote(cursor.getString(1));
                note.setDate(cursor.getString(2));
                note.setStar(cursor.getInt(3));
                noteList.add(note);
            } while (cursor.moveToNext());
        }
        return noteList;
    }

    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }

    public void updateNote(@NonNull Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE, note.getNote());
        values.put(KEY_DATE, note.getDate());
        values.put(KEY_STAR, note.getStar());
        db.update(TABLE_NOTES, values, KEY_ID + " = ?", new String[]{String.valueOf(note.getID())});
    }

    public void deleteNote(@NonNull Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, KEY_ID + " = ?", new String[]{String.valueOf(note.getID())});
        db.close();
    }

    public void addNote(@NonNull Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE, note.getNote());
        values.put(KEY_DATE, note.getDate());
        values.put(KEY_STAR, note.getStar());
        db.insert(TABLE_NOTES, null, values);
        db.close();
    }
}
