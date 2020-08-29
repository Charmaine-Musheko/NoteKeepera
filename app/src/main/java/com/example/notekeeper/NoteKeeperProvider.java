package com.example.notekeeper;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

import static com.example.notekeeper.NoteKeeperProviderContract.AUTHORITY;

public class NoteKeeperProvider extends ContentProvider {

    public static final String MIME_VENDOR_TYPE = "vnd. " + AUTHORITY + ".";
    private NoteKeeperOpenHelper mDbOpenHelper;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;
    public static final int NOTES = 1;

    public static final int NOTES_EXPANDED = 2;
    public static final int NOTES_ROW = 3;

    static {
        sUriMatcher.addURI(AUTHORITY, NoteKeeperProviderContract.Courses.PATH, COURSES);
        sUriMatcher.addURI(AUTHORITY, NoteKeeperProviderContract.Notes.PATH, NOTES);
        sUriMatcher.addURI(AUTHORITY, NoteKeeperProviderContract.Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(AUTHORITY, NoteKeeperProviderContract.Notes.PATH +"/0", NOTES_ROW);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        String mimeType = null;
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case COURSES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        MIME_VENDOR_TYPE + NoteKeeperProviderContract.Courses.PATH;
                break;
            case NOTES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Notes.PATH;
                break;
            case NOTES_EXPANDED:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Notes.PATH_EXPANDED;
                break;
            case NOTES_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Notes.PATH;
                break;

        }
        return  mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
       SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
       long rowId = -1;
       Uri rowUri = null;
       int uriMatch = sUriMatcher.match(uri);
       switch (uriMatch) {
           case  NOTES:
               rowId = db.insert(NotekeeperDatabaseContract.NoteInfoEntry.TABLE_NAME, null, values);
           //content://com.example.notekeeper.provider/notes/;
               rowUri = ContentUris.withAppendedId(NoteKeeperProviderContract.Notes.CONTENT_URI, rowId);
               break;
           case COURSES:
               rowId = db.insert(NotekeeperDatabaseContract.CourseInfoEntry.TABLE_NAME, null, values);

               rowUri = ContentUris.withAppendedId(NoteKeeperProviderContract.Notes.CONTENT_URI, rowId);
               break;
           case NOTES_EXPANDED:
               break;

       }
       return rowUri;
    }

    @Override
    public boolean onCreate() {
        mDbOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case COURSES:
                cursor = db.query(NotekeeperDatabaseContract.CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case NOTES:
                cursor = db.query(NotekeeperDatabaseContract.NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder);
                break;
            case NOTES_ROW:
                long rowId = ContentUris.parseId(uri);
                String rowSelection = NotekeeperDatabaseContract.NoteInfoEntry._ID + " = ? ";
                String [] rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = db.query(NotekeeperDatabaseContract.NoteInfoEntry.TABLE_NAME, projection, rowSelection, rowSelectionArgs, null, null, null);
                break;
        }
        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder) {

        String[] columns = new String[projection.length];
        for(int idx=0; idx < projection.length; idx++) {
            columns[idx] = projection[idx].equals(BaseColumns._ID) ||
                    projection[idx].equals(NoteKeeperProviderContract.CourseIdColumns.COLUMN_COURSE_ID) ?
                    NotekeeperDatabaseContract.NoteInfoEntry.getQName(projection[idx]) : projection[idx];
        }

        String tablesWithJoin = NotekeeperDatabaseContract.NoteInfoEntry.TABLE_NAME + " JOIN " +
                NotekeeperDatabaseContract.CourseInfoEntry.TABLE_NAME + " ON " +
                NotekeeperDatabaseContract.NoteInfoEntry.getQName(NotekeeperDatabaseContract.NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                NotekeeperDatabaseContract.CourseInfoEntry.getQName(NotekeeperDatabaseContract.CourseInfoEntry.COLUMN_COURSE_ID);

        return db.query(tablesWithJoin, columns, selection, selectionArgs,
                null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}


