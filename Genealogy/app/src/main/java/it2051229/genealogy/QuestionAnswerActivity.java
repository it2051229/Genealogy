package it2051229.genealogy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import it2051229.genealogy.entities.Application;
import it2051229.genealogy.entities.Genealogy;
import it2051229.genealogy.entities.Person;


public class QuestionAnswerActivity extends ActionBarActivity {
    private Genealogy genealogy;
    private HashMap<String, ArrayList<String>> unconnectedNames;
    private Person person;
    private Random random;
    private String selectedCategory = "";
    private String[] categories = { "spouse", "dad", "mom" };

    /**
     * Initialize all the questions that needs to be answered
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_answer);

        Bundle extras = getIntent().getExtras();
        genealogy = (Genealogy)extras.getSerializable("genealogy");

        ArrayList<String> names = genealogy.getNamesHavingPartialConnections();

        unconnectedNames = new HashMap<>();
        random = new Random();

        for(String category : categories) {
            unconnectedNames.put(category, new ArrayList<String>());
        }

        // Shuffle the names
        Collections.shuffle(names, new Random());

        // Segregate the names into the appropriate category
        ArrayList<Integer> missingRelationships = new ArrayList<>();

        for(String name : names) {
            Person person = genealogy.getPerson(name);

            missingRelationships.clear();

            if(person.getSpouse() == null) {
                missingRelationships.add(0);
            }

            if(person.getDad() == null) {
                missingRelationships.add(1);
            }

            if(person.getMom() == null) {
                missingRelationships.add(2);
            }

            switch(missingRelationships.get(random.nextInt(missingRelationships.size()))) {
                case 0:
                    unconnectedNames.get("spouse").add(name);
                    break;
                case 1:
                    unconnectedNames.get("dad").add(name);
                    break;
                case 2:
                    unconnectedNames.get("mom").add(name);
                    break;
            }
        }

        clearEmptyCategories();
        showNextQuestion();
    }

    /**
     * Delete the categories that are empty
     */
    private void clearEmptyCategories() {
        for(String category : categories) {
            if(unconnectedNames.containsKey(category) && unconnectedNames.get(category).isEmpty()) {
                unconnectedNames.remove(category);
            }
        }
    }

    /**
     * Show the next question to be answered
     */
    private void showNextQuestion() {
        // Stop if there are no more questions
        if(unconnectedNames.isEmpty()) {
            new AlertDialog.Builder(this)
            .setTitle("Notification")
            .setMessage("There are no more survey questions to answer.")
            .setCancelable(false)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create().show();
            return;
        }

        // Randomly choose a question to show
        selectedCategory = categories[random.nextInt(categories.length)];
        person = genealogy.getPerson(unconnectedNames.get(selectedCategory).remove(0));
        ((TextView)findViewById(R.id.textViewQuestion)).setText(person.getName() + "'s " + selectedCategory + "?");

        clearEmptyCategories();
    }

    /**
     * Show the activity for selecting name
     */
    public void buttonSearchNameTapped(View view) {
        Intent intent = new Intent(this, SearchNameActivity.class);
        intent.putExtra("genealogy", genealogy);
        startActivityForResult(intent, Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Put the selected name on the edit text
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode != RESULT_OK) {
            return;
        }

        String name = intent.getExtras().getString("name");
        ((EditText) findViewById(R.id.editTextName)).setText(name);
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
     * Perform a validation and update to the current person's connections
     */
    public void buttonUpdateTapped(View view) {
        String name = ((EditText)findViewById(R.id.editTextName)).getText().toString().trim();
        name = genealogy.normalizeName(name);

        // Perform validation
        if(name.isEmpty()) {
            Toast.makeText(this, "Please enter a name.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if name is already registered
        if(genealogy.getPerson(name) == null) {
            Toast.makeText(this, "The selected " + selectedCategory + " isn't in the record.", Toast.LENGTH_SHORT).show();
            return;
        }

        // If the spouse, mom and dad is given, then make sure they are validated and existing in the database
        String spouseName = "";
        String dadName = "";
        String momName = "";

        if(person.getSpouse() != null) {
            spouseName = person.getSpouse().getName();
        }

        if(person.getDad() != null) {
            dadName = person.getDad().getName();
        }

        if(person.getMom() != null) {
            momName = person.getMom().getName();
        }

        if(selectedCategory.equalsIgnoreCase("spouse")) {
            spouseName = name;
        } else if(selectedCategory.equalsIgnoreCase("dad")) {
            dadName = name;
        } else if(selectedCategory.equalsIgnoreCase("mom")) {
            momName = name;
        }

        if(isAWeirdRelationship(new String[] { person.getName(), spouseName, dadName, momName })) {
            Toast.makeText(this, "This is a weird relationship don't you think?", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation complete, update the person's properties
        Person personToRelate = genealogy.getPerson(name);

        if(selectedCategory.equalsIgnoreCase("spouse")) {
            person.setSpouse(personToRelate);
        } else if(selectedCategory.equalsIgnoreCase("dad")) {
            person.setDad(personToRelate);
        } else if(selectedCategory.equalsIgnoreCase("mom")) {
            person.setMom(personToRelate);
        }

        Intent intent = getIntent();
        intent.putExtra("genealogy", genealogy);
        setResult(RESULT_OK, intent);

        Toast.makeText(this, "Answer saved.", Toast.LENGTH_SHORT).show();
        ((EditText) findViewById(R.id.editTextName)).setText("");

        // Move to the next question
        showNextQuestion();
    }

    /**
     * Move to the next question
     */
    public void buttonSkipTapped(View view) {
        showNextQuestion();
    }
}
