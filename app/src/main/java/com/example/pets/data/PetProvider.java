package com.example.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pets.data.PetContract.PetEntry;


public class PetProvider extends ContentProvider {

    public static final String LOG_TAG = "PetProvider";

    public PetDbHelper mDbHelper;
    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;




    public boolean onCreate(){
        mDbHelper = new PetDbHelper(getContext());

        return true;
    }

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{

        sUriMatcher.addURI(PetEntry.CONTENT_AUTHORITY, PetEntry.PATH_PETS, PETS);
        sUriMatcher.addURI(PetEntry.CONTENT_AUTHORITY, PetEntry.PATH_PETS+"/#",PET_ID);

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch(match){
            case PETS:

                cursor = database.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null,null);
                break;
            case PET_ID:
                selection = PetEntry._ID+"=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder,null);
                break;
            default:
                throw new IllegalArgumentException("Connot query inkown Uri "+uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for "+uri);
        }

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return updatePet(uri, values, selection, selectionArgs);
            case PET_ID:
                selection = PetEntry._ID+"=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for "+uri);

        }

    }

    private Uri insertPet(Uri uri, ContentValues values){


        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }

        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }

        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(PetEntry.TABLE_NAME, null, values);

        if(id==-1){
            Log.e(LOG_TAG,"Failed to insert row for "+uri);

            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri,id);
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;


    }
}
