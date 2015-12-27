package it2051229.genealogy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import it2051229.genealogy.entities.Application;
import it2051229.genealogy.entities.Genealogy;
import it2051229.genealogy.entities.Person;

public class MainActivity extends ActionBarActivity {

    private ArrayList<String> arrayListNames;
    private ArrayAdapter<String> arrayAdapterNames;

    private Genealogy genealogy;

    /**
     * Initialize everything that needs to be initialized before the start of program
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the list view where to display the added names
        arrayListNames = new ArrayList<>();
        arrayAdapterNames = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayListNames);

        ListView listViewNames = (ListView) findViewById(R.id.listViewNames);
        listViewNames.setAdapter(arrayAdapterNames);

        // Create a context menu for the list view to be able to update and delete names
        registerForContextMenu(listViewNames);

        // Initialize the genealogy system from internal file
        loadData();

        for(String name : genealogy.getNames()) {
            arrayAdapterNames.add(name);
        }

        // Initialize the edit text to have a text changed listener for filtering names
        final EditText editTextFilterSearch = (EditText) findViewById(R.id.editTextFilterSearch);
        editTextFilterSearch.addTextChangedListener(new TextWatcher() {
            // Perform a filter
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear the old data
                arrayAdapterNames.clear();

                // Do a filter
                String keyword = editTextFilterSearch.getText().toString().trim();

                for(String name : genealogy.getNamesContaining(keyword)) {
                    arrayAdapterNames.add(name);
                }
            }

            // Nothing to do here
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            // Nothing to do here
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Load data from internal file
     */
    public void loadData() {

        try {
            FileInputStream fis = openFileInput("data.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);

            genealogy = new Genealogy((HashMap<String, Person>)ois.readObject());

            fis.close();
            ois.close();
        } catch(Exception e) {
            Log.e("loadData()", e.getMessage());
            genealogy = new Genealogy();
        }
    }

    /**
     * Save data to internal file
     */
    public void saveData() {
        try {
            FileOutputStream fos = openFileOutput("data.dat", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(genealogy.getData());

            oos.close();
            fos.close();
        } catch(Exception e) {
            Log.e("saveData()", e.getMessage());
        }
    }

    /**
     * Handle the showing of the context menu upon selection of a name, this will show a menu
     * to the user what it would like to do with the name
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add("Details");
        menu.add("Update");
        menu.add("Delete");
        menu.add("Cancel");
    }

    /**
     * Handle the showing of the "update", "delete" from the context menu
     */
    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        // Get the selected name
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        int selectedIndex = info.position;

        if(menuItem.getTitle().equals("Details")) {
            String name = arrayListNames.get(selectedIndex);
            detailsContextItemTapped(name);
        } else if(menuItem.getTitle().equals("Update")) {
            String name = arrayListNames.get(selectedIndex);
            updateContextItemTapped(name, selectedIndex);
        } else if(menuItem.getTitle().equals("Delete")) {
            String name = arrayListNames.get(selectedIndex);
            deleteContextItemTapped(name, selectedIndex);
        }

        return super.onContextItemSelected(menuItem);
    }

    /**
     * Show the detailed information of the name
     */
    private void detailsContextItemTapped(String name) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("genealogy", genealogy);
        startActivity(intent);
    }

    /**
     * Confirm the user if they want to delete the selected person
     */
    private void deleteContextItemTapped(final String name, final int selectedIndex) {
        new AlertDialog.Builder(this)
        .setTitle("Confirmation")
        .setMessage("Do you really want to delete " + name + "?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            // Handle the removal of the name upon delete
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!genealogy.removePerson(name)) {
                    Toast.makeText(MainActivity.this, "Oh snap! developer error.", Toast.LENGTH_SHORT).show();
                    return;
                }

                arrayListNames.remove(selectedIndex);
                arrayAdapterNames.notifyDataSetChanged();

                saveData();
            }
        }).setNegativeButton("No", null).show();
    }

    /**
     * Update the person's properties
     */
    private void updateContextItemTapped(String name, int selectedIndex) {
        Intent intent = new Intent(this, UpdateNameActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("list view index", selectedIndex);
        intent.putExtra("genealogy", genealogy);
        startActivityForResult(intent, Application.UPDATE_NAME_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Initialize the menu for this activity
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handle the events of the menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menuAddName:
                menuAddNameTapped();
                break;

            case R.id.menuRelateNames:
                menuRelateNamesTapped();
                break;

            case R.id.menuBuildFamilyTree:
                menuBuildFamilyTreeTapped();
                break;

            case R.id.menuExportNames:
                menuExportNamesTapped();
                break;

            case R.id.menuImportNames:
                menuImportNamesTapped();
                break;

            case R.id.menuQuestionAndAnswer:
                menuQuestionAndAnswerTapped();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Show the activity that would allow to add a new name
     */
    private void menuAddNameTapped() {
        Intent intent = new Intent(this, AddNameActivity.class);
        intent.putExtra("genealogy", genealogy);
        startActivityForResult(intent, Application.ADD_NAME_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Show the activity that would allow to relate 2 names
     */
    private void menuRelateNamesTapped() {
        Intent intent = new Intent(this, RelateNamesActivity.class);
        intent.putExtra("genealogy", genealogy);
        startActivity(intent);
    }

    /**
     * Show the activity that would allow to build the family tree of a person
     */
    private void menuBuildFamilyTreeTapped() {
        Intent intent = new Intent(this, BuildFamilyTreeActivity.class);
        intent.putExtra("genealogy", genealogy);
        startActivity(intent);
    }

    /**
     * Export the genealogy database to SD card
     */
    private void menuExportNamesTapped() {
        // Stop if there are no SD card
        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "There are no external storage where to export data.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the file
        String path = Environment.getExternalStorageDirectory().toString();
        File directory = new File(path + "/Genealogy");
        directory.mkdirs();

        File file = new File(directory, "Exported Names.txt");

        if(file.exists()) {
            file.delete();
        }

        try {
            PrintWriter outFile = new PrintWriter(new FileWriter(file));

            // Print out all the names first
            for(String name : genealogy.getNames()) {
                outFile.println(name);
            }

            outFile.println();

            // Print out the relationships for each name
            for(String name : genealogy.getNames()) {
                Person person = genealogy.getPerson(name);

                outFile.println(name);

                if(person.getSpouse() != null) {
                    outFile.println(person.getSpouse().getName());
                } else {
                    outFile.println();
                }

                if(person.getDad() != null) {
                    outFile.println(person.getDad().getName());
                } else {
                    outFile.println();
                }

                if(person.getMom() != null) {
                    outFile.println(person.getMom().getName());
                } else {
                    outFile.println();
                }

                outFile.println(person.getNotes().replace('\n', '|'));
            }

            outFile.close();

            Toast.makeText(this, "Names exported to your external storage.", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Log.e("menuExportNamesTapped()", e.getMessage());
            Toast.makeText(this, "Oh snap! Developer error.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Load all names and relationships from file
     */
    private void menuImportNamesTapped() {
        new AlertDialog.Builder(this)
        .setTitle("Confirmation")
        .setMessage("Warning all names will be deleted and replaced. Do you wish to continue?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            // Handle the clearing and importing
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Stop if there are no SD card
                if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    Toast.makeText(MainActivity.this, "There are no external storage where to import data.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Find the file, make sure it exists
                String path = Environment.getExternalStorageDirectory().toString();
                File directory = new File(path + "/Genealogy");
                directory.mkdirs();

                File file = new File(directory, "Exported Names.txt");

                if (!file.exists()) {
                    Toast.makeText(MainActivity.this, "Failed to find 'Exported Names.txt'.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    genealogy = new Genealogy();

                    Scanner inFile = new Scanner(file);

                    // Load the names first, until a blank line is detected
                    while(inFile.hasNextLine()) {
                        String name = inFile.nextLine();

                        if(name.isEmpty()) {
                            break;
                        }

                        genealogy.addPerson(name);
                    }

                    // Next load the relationships until to the end of file
                    while(inFile.hasNextLine()) {
                        String name = inFile.nextLine();
                        Person person = genealogy.getPerson(name);
                        person.setSpouse(genealogy.getPerson(inFile.nextLine()));
                        person.setDad(genealogy.getPerson(inFile.nextLine()));
                        person.setMom(genealogy.getPerson(inFile.nextLine()));
                        person.setNotes(inFile.nextLine().replace('|', '\n'));
                    }

                    inFile.close();

                    // Clear the old data
                    arrayAdapterNames.clear();

                    // Update output
                    for(String name : genealogy.getNames()) {
                        arrayAdapterNames.add(name);
                    }

                    Toast.makeText(MainActivity.this, "Import successful.", Toast.LENGTH_SHORT).show();

                    // Update new data
                    saveData();
                } catch (Exception e) {
                    Log.e("menuExportNamesTapped()", e.getMessage());
                    Toast.makeText(MainActivity.this, "Oh snap! Developer error.", Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("No", null).show();
    }

    /**
     * Show the question and answer mode for user
     */
    private void menuQuestionAndAnswerTapped() {
        Intent intent = new Intent(this, QuestionAnswerActivity.class);
        intent.putExtra("genealogy", genealogy);
        startActivityForResult(intent, Application.QUESTION_AND_ANSWER_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Handle the return callbacks of activities that returns results
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode) {
            case Application.ADD_NAME_ACTIVITY_REQUEST_CODE:
                processAddActivityResult(resultCode, intent);
                break;

            case Application.UPDATE_NAME_ACTIVITY_REQUEST_CODE:
                processUpdateActivityResult(resultCode, intent);
                break;

            case Application.QUESTION_AND_ANSWER_ACTIVITY_REQUEST_CODE:
                processQuestionAndAnswerActivityResult(resultCode, intent);
                break;
        }
    }

    /**
     * Handle the add activity return callback after adding a new name
     */
    private void processAddActivityResult(int resultCode, Intent intent) {
        if(resultCode != RESULT_OK) {
            return;
        }

        genealogy = (Genealogy)intent.getExtras().getSerializable("genealogy");
        String personName = intent.getExtras().getString("name");
        arrayAdapterNames.add(personName);

        saveData();
    }

    /**
     * Handle the update activity return callback after updating an existing name
     */
    private void processUpdateActivityResult(int resultCode, Intent intent) {
        if(resultCode != RESULT_OK) {
            return;
        }

        genealogy = (Genealogy)intent.getExtras().getSerializable("genealogy");

        String personName = intent.getExtras().getString("name");
        int listViewIndex = intent.getExtras().getInt("list view index");

        arrayListNames.set(listViewIndex, personName);

        ListView listViewNames = (ListView)findViewById(R.id.listViewNames);
        TextView textView = (TextView) listViewNames.getChildAt(listViewIndex - listViewNames.getFirstVisiblePosition());
        textView.setText(personName);

        saveData();
    }

    /**
     * Handle the update return call back after question and answer
     */
    private void processQuestionAndAnswerActivityResult(int resultCode, Intent intent) {
        if(resultCode != RESULT_OK) {
            return;
        }

        genealogy = (Genealogy)intent.getExtras().getSerializable("genealogy");
        saveData();
    }
}
