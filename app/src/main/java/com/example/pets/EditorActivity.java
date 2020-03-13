package com.example.pets;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import com.example.pets.data.PetContract.PetEntry;
import com.example.pets.data.PetDbHelper;

import android.os.Bundle;
import android.widget.Toast;

public class EditorActivity extends AppCompatActivity {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = PetEntry.GENDER_UNKNOWN;

    PetDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();

        mDbHelper = new PetDbHelper(this);

    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */

    public void setupSpinner(){

        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,R.array.array_gender_options,android.R.layout.simple_spinner_item);
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if(!TextUtils.isEmpty(selection)){
                    if (selection.equals(getString(R.string.gender_male))){
                        mGender=PetEntry.GENDER_MALE;
                    }else if(selection.equals(getString(R.string.gender_female))){
                        mGender=PetEntry.GENDER_FEMALE;
                    }else{
                        mGender=PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetEntry.GENDER_UNKNOWN;
            }


        });

    }

    private void insertPet(){
        String petName = mNameEditText.getText().toString().trim();
        String petBreed = mBreedEditText.getText().toString().trim();
        String petWeight = mWeightEditText.getText().toString().trim();
        Integer petGender;
        if(mGender==1){
            petGender = 1;
        }else if(mGender==2){
            petGender = 2;
        } else{
            petGender = 0;
        }

        ContentValues values = new ContentValues();

        values.put(PetEntry.COLUMN_PET_NAME, petName);
        values.put(PetEntry.COLUMN_PET_BREED, petBreed);
        values.put(PetEntry.COLUMN_PET_GENDER, petGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, petWeight);

        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI,values);



        if(newUri == null){
            Toast.makeText(EditorActivity.this, "Pet insert error ", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(EditorActivity.this, "Pet insert Success", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Do nothing for now
                insertPet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
