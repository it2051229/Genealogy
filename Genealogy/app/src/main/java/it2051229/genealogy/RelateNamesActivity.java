package it2051229.genealogy;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import it2051229.genealogy.entities.Application;
import it2051229.genealogy.entities.Genealogy;
import it2051229.genealogy.entities.Graph;


public class RelateNamesActivity extends ActionBarActivity {
    private Genealogy genealogy;
    private Graph graph;

    /**
     * Build a graph so we can use it to find paths
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relate_names);

        Bundle extras = getIntent().getExtras();
        genealogy = (Genealogy)extras.getSerializable("genealogy");
        graph = genealogy.buildGraph();
    }

    /**
     * Show dialog for searching a source name
     */
    public void buttonSearchSourceNameTapped(View view) {
        Intent intent = new Intent(this, SearchNameActivity.class);
        intent.putExtra("genealogy", genealogy);
        startActivityForResult(intent, Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE + 1);
    }

    /**
     * Show a dialog for searching a destination name
     */
    public void buttonSearchDestinationNameTapped(View view) {
        Intent intent = new Intent(this, SearchNameActivity.class);
        intent.putExtra("genealogy", genealogy);
        startActivityForResult(intent, Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE + 2);
    }

    /**
     * Get the selected source name and put it on display
     */
    private void setSourceName(int resultCode, Intent intent) {
        if(resultCode != RESULT_OK) {
            return;
        }

        String name = intent.getExtras().getString("name");
        ((EditText)findViewById(R.id.editTextSourceName)).setText(name);
    }

    /**
     * Get the selected destination name and put it on display
     */
    private void setDestinationName(int resultCode, Intent intent) {
        if(resultCode != RESULT_OK) {
            return;
        }

        String name = intent.getExtras().getString("name");
        ((EditText)findViewById(R.id.editTextDestinationName)).setText(name);
    }

    /**
     * Handle the return call back of the selection of the name
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE + 1) {
            setSourceName(resultCode, intent);
        } else if(requestCode == Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE + 2) {
            setDestinationName(resultCode, intent);
        }
    }

    /**
     * Using the graph find the path between two relatives
     */
    public void buttonFindRelationshipPathTapped(View view) {
        String sourceName = ((EditText)findViewById(R.id.editTextSourceName)).getText().toString();
        String destinationName = ((EditText)findViewById(R.id.editTextDestinationName)).getText().toString();

        // Perform validation
        if(sourceName.isEmpty() || destinationName.isEmpty()) {
            Toast.makeText(this, "Please enter 2 names.", Toast.LENGTH_SHORT).show();
            return;
        }

        sourceName = genealogy.normalizeName(sourceName);
        destinationName = genealogy.normalizeName(destinationName);

        if(genealogy.getPerson(sourceName) == null) {
            Toast.makeText(this, "The name " + sourceName + " does not exist.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(genealogy.getPerson(destinationName) == null) {
            Toast.makeText(this, "The name " + destinationName + " does not exist.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(sourceName.equals(destinationName)) {
            Toast.makeText(this, "Self relationship is not possible.", Toast.LENGTH_SHORT).show();
        }

        // Alright, if both person exists, then find the relationship in the graph
        String relationshipPath = graph.getShortestPath(sourceName, destinationName);
        ((EditText) findViewById(R.id.editTextRelationshipPath)).setText(relationshipPath);
    }
}
