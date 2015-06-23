package it2051229.genealogy;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import it2051229.genealogy.entities.Application;
import it2051229.genealogy.entities.Genealogy;
import it2051229.genealogy.entities.Person;


public class AddNameActivity extends ActionBarActivity {

    private Genealogy genealogy;

    /**
     * Initialize what needs to be initialized for this activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_name);

        genealogy = (Genealogy)getIntent().getSerializableExtra("genealogy");
    }

    /**
     * Check for weird relationship usually happens when names are all the same
     */
    private boolean isAWeirdRelationship(String[] names) {
        ArrayList<String> nonEmptyNames = new ArrayList<>();

        for(String name : names) {
            if(name != null && !name.isEmpty()) {
                if(nonEmptyNames.contains(name)) {
                    return true;
                }

                nonEmptyNames.add(name);
            }
        }

        return false;
    }

    /**
     * Handle the validation of the name and adding as well
     */
    public void buttonAddNameTapped(View view) {
        String name = ((EditText)findViewById(R.id.editTextName)).getText().toString().trim();

        // Perform validation
        if(name.isEmpty()) {
            Toast.makeText(this, "Please enter a name.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if name is already registered
        if(genealogy.getPerson(name) != null) {
            Toast.makeText(this, "The name is already taken.", Toast.LENGTH_SHORT).show();
            return;
        }

        // If the spouse, mom and dad is given, then make sure they are validated and existing in the database
        String spouseName = ((EditText)findViewById(R.id.editTextSpouseName)).getText().toString();
        String dadName = ((EditText)findViewById(R.id.editTextDadName)).getText().toString();
        String momName = ((EditText)findViewById(R.id.editTextMomName)).getText().toString();
        String notes = ((EditText)findViewById(R.id.editTextNotes)).getText().toString();

        spouseName = genealogy.normalizeName(spouseName);
        dadName = genealogy.normalizeName(dadName);
        momName = genealogy.normalizeName(momName);

        if(isAWeirdRelationship(new String[] { name, spouseName, dadName, momName })) {
            Toast.makeText(this, "This is a weird relationship don't you think?", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!spouseName.isEmpty() && genealogy.getPerson(spouseName) == null) {
            Toast.makeText(this, "The spouse name isn't in the record.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!dadName.isEmpty() && genealogy.getPerson(dadName) == null) {
            Toast.makeText(this, "The dad's name isn't in the record.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!momName.isEmpty() && genealogy.getPerson(momName) == null) {
            Toast.makeText(this, "The mom's name isn't in the record.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation complete
        if(!genealogy.addPerson(name)) {
            Toast.makeText(this, "Oh snap! developer error.", Toast.LENGTH_SHORT).show();
            return;
        }

        Person person = genealogy.getPerson(name);
        person.setNotes(notes);

        if(!spouseName.isEmpty()) {
            person.setSpouse(genealogy.getPerson(spouseName));
        }

        if(!dadName.isEmpty()) {
            person.setDad(genealogy.getPerson(dadName));
        }

        if(!momName.isEmpty()) {
            person.setMom(genealogy.getPerson(momName));
        }

        // Signal the main activity to add the newly added person
        Intent intent = getIntent();
        intent.putExtra("name", genealogy.getPerson(name).getName());
        intent.putExtra("genealogy", genealogy);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Show the search name activity to let the user browse for dad's name
     */
    public void buttonSearchDadNameTapped(View view) {
        Intent intent = new Intent(this, SearchNameActivity.class);
        intent.putExtra("genealogy", genealogy);
        startActivityForResult(intent, Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE + 1);
    }

    /**
     * Show the search name activity to let the user browse for mom's name
     */
    public void buttonSearchMomNameTapped(View view) {
        Intent intent = new Intent(this, SearchNameActivity.class);
        intent.putExtra("genealogy", genealogy);
        startActivityForResult(intent, Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE + 2);
    }

    /**
     * Show the search name activity to let the user browse for the spouse name
     */
    public void buttonSearchSpouseNameTapped(View view) {
        Intent intent = new Intent(this, SearchNameActivity.class);
        intent.putExtra("genealogy", genealogy);
        startActivityForResult(intent, Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE + 3);
    }

    /**
     * Get the selected dad's name and put it on display
     */
    private void setDadName(int resultCode, Intent intent) {
        if(resultCode != RESULT_OK) {
            return;
        }

        String name = intent.getExtras().getString("name");
        ((EditText)findViewById(R.id.editTextDadName)).setText(name);
    }

    /**
     * Get the selected mom's name and put it on display
     */
    private void setMomName(int resultCode, Intent intent) {
        if(resultCode != RESULT_OK) {
            return;
        }

        String name = intent.getExtras().getString("name");
        ((EditText)findViewById(R.id.editTextMomName)).setText(name);
    }

    /**
     * Get the selected spouse name and put it on display
     */
    private void setSpouseName(int resultCode, Intent intent) {
        if(resultCode != RESULT_OK) {
            return;
        }

        String name = intent.getExtras().getString("name");
        ((EditText)findViewById(R.id.editTextSpouseName)).setText(name);
    }

    /**
     * Handle the return call back of the selection of the name
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE + 1) {
            setDadName(resultCode, intent);
        } else if(requestCode == Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE + 2) {
            setMomName(resultCode, intent);
        } else if(requestCode == Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE + 3) {
            setSpouseName(resultCode, intent);
        }
    }
}
