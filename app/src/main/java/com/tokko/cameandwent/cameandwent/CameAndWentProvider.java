package com.tokko.cameandwent.cameandwent;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.Calendar;

public class CameAndWentProvider extends ContentProvider {

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".CameAndWentProvider";
    private static final String URI_TEMPLATE = "content://" + AUTHORITY + "/";

    private static final String DATABASE_NAME = "cameandwent";

    private static final String TABLE_LOG_NAME = "cameandwent";
    private static final String VIEW_DURATION = "durations";

    public static final String ID = "_id";
    public static final String CAME = "came";
    public static final String WENT = "went";

    public static final String DATE = "date";
    public static final String DURATION = "duration";

    private static final String ACTION_CAME = "ACTION_CAME";
    private static final String ACTION_WENT = "ACTION_WENT";
    private static final String ACTION_GET_LOG_ENTRIES = "ACTION_GET_LOG_ENTRIES";
    private static final String ACTION_DELETE_ALL = "ACTION_DELETE_ALL";
    private static final String ACTION_GET_DETAILS = "ACTION_GET_DETAILS";
    private static final String ACTION_DELETE_DETAIL = "ACTION_DELETE_DETAIL";
    private static final String ACTION_UPDATE_PARTICULAR_LOG_ENTRY = "ACTION_UPDATE_PARTICULAR_LOG_ENTRY";

    private static final int KEY_CAME = 0;
    private static final int KEY_WENT = 1;
    private static final int KEY_GET_LOG_ENTRIES = 2;
    private static final int KEY_DELETE_ALL = 3;
    private static final int KEY_GET_DETAILS = 4;
    private static final int KEY_DELETE_DETAIL = 4;
    private static final int KEY_UPDATE_PARTICULAR_LOG_ENTRY = 5;

    public static final Uri URI_CAME = Uri.parse(URI_TEMPLATE + ACTION_CAME);
    public static final Uri URI_WENT = Uri.parse(URI_TEMPLATE + ACTION_WENT);
    public static final Uri URI_GET_LOG_ENTRIES = Uri.parse(URI_TEMPLATE + ACTION_GET_LOG_ENTRIES);
    public static final Uri URI_DELETE_ALL = Uri.parse(URI_TEMPLATE + ACTION_DELETE_ALL);
    public static final Uri URI_GET_DETAILS = Uri.parse(URI_TEMPLATE + ACTION_GET_DETAILS);
    public static final Uri URI_DELETE_DETAIL = Uri.parse(URI_TEMPLATE + ACTION_DELETE_DETAIL + "/#");
    public static final Uri URI_UPDATE_PARTICULAR_LOG_ENTRY = Uri.parse(URI_TEMPLATE + ACTION_UPDATE_PARTICULAR_LOG_ENTRY + "/#");

    private static UriMatcher uriMatcher;

    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, ACTION_CAME, KEY_CAME);
        uriMatcher.addURI(AUTHORITY, ACTION_WENT, KEY_WENT);
        uriMatcher.addURI(AUTHORITY, ACTION_GET_LOG_ENTRIES, KEY_GET_LOG_ENTRIES);
        uriMatcher.addURI(AUTHORITY, ACTION_DELETE_ALL, KEY_DELETE_ALL);
        uriMatcher.addURI(AUTHORITY, ACTION_GET_DETAILS, KEY_GET_DETAILS);
        uriMatcher.addURI(AUTHORITY, ACTION_DELETE_DETAIL, KEY_DELETE_DETAIL);
        uriMatcher.addURI(AUTHORITY, ACTION_UPDATE_PARTICULAR_LOG_ENTRY, KEY_UPDATE_PARTICULAR_LOG_ENTRY);
    }

    private DatabaseOpenHelper db;

    @Override
    public boolean onCreate() {
        db = new DatabaseOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase sdb = db.getReadableDatabase();
        Cursor c;
        switch (uriMatcher.match(uri)){
            case KEY_GET_LOG_ENTRIES:
                c = sdb.query(VIEW_DURATION, projection, selection, selectionArgs, null, null, sortOrder);
                c.setNotificationUri(getContext().getContentResolver(), URI_GET_LOG_ENTRIES);
                return c;
            case KEY_GET_DETAILS:
                c = sdb.query(TABLE_LOG_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                c.setNotificationUri(getContext().getContentResolver(), URI_GET_DETAILS);
                return c;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
    }


    @Override
    public String getType(Uri uri) {
        return "";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase sdb = db.getWritableDatabase();
        switch (uriMatcher.match(uri)){
            case KEY_CAME:
                long came = values.getAsLong(CAME);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(came);
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                values.put(DATE, c.getTimeInMillis());
                long id = sdb.insert(TABLE_LOG_NAME, null, values);
                if(id > -1) {
                    getContext().getContentResolver().notifyChange(URI_GET_LOG_ENTRIES, null);
                    getContext().getContentResolver().notifyChange(URI_GET_DETAILS, null);
                }
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sdb = db.getWritableDatabase();
        int deleted;
        switch (uriMatcher.match(uri)){
            case KEY_DELETE_ALL:
                deleted = sdb.delete(TABLE_LOG_NAME, null, null);
                if(deleted > 0) {
                    getContext().getContentResolver().notifyChange(URI_GET_LOG_ENTRIES, null);
                    getContext().getContentResolver().notifyChange(URI_GET_DETAILS, null);
                }
                return deleted;
            case KEY_DELETE_DETAIL:
                deleted = sdb.delete(TABLE_LOG_NAME, selection, selectionArgs);
                if(deleted > 0){
                    getContext().getContentResolver().notifyChange(URI_GET_LOG_ENTRIES, null);
                    getContext().getContentResolver().notifyChange(URI_GET_DETAILS, null);
                }
                return deleted;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase sdb = db.getWritableDatabase();
        int updated;
        switch (uriMatcher.match(uri)){
            case KEY_WENT:
                 updated = sdb.update(TABLE_LOG_NAME, values, WENT + " = 0", null);
                if(updated > 0) {
                    getContext().getContentResolver().notifyChange(URI_GET_LOG_ENTRIES, null);
                    getContext().getContentResolver().notifyChange(URI_GET_DETAILS, null);
                }
                return updated;
            case KEY_UPDATE_PARTICULAR_LOG_ENTRY:
                updated = sdb.update(TABLE_LOG_NAME, values, selection, selectionArgs);
                if(updated > 0) {
                    getContext().getContentResolver().notifyChange(URI_GET_LOG_ENTRIES, null);
                    getContext().getContentResolver().notifyChange(URI_GET_DETAILS, null);
                }
                return updated;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
    }

    private class DatabaseOpenHelper extends SQLiteOpenHelper{
        private static final int DATABASE_VERSION = 19;
        private static final String CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_LOG_NAME + "(" +
                ID + " INTEGER PRIMARY KEY, " +
                DATE + " INTEGER NOT NULL DEFAULT 0, " +
                CAME + " INTEGER NOT NULL," +
                WENT + " INTEGER NOT NULL DEFAULT 0);";

        private static final String DURATION_CALC = "(" + WENT + "-" + CAME + ") AS " + DURATION;
        private static final String CREATE_DURATION_VIEW = "CREATE VIEW " + VIEW_DURATION + " AS SELECT " + ID + ", " + DATE + ", " + DURATION_CALC + " FROM " + TABLE_LOG_NAME + " GROUP BY " + DATE ;

        public DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE);
            db.execSQL(CREATE_DURATION_VIEW);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP VIEW IF EXISTS " + VIEW_DURATION);
            for(int version = oldVersion; version <= newVersion; version++){
                switch (version){
                    case 16:
                        db.execSQL("ALTER TABLE " + TABLE_LOG_NAME + " ADD COLUMN " + DATE + " INTEGER NOT NULL DEFAULT 0");
                        break;
                }
            }
            onCreate(db);
        }
    }
}
