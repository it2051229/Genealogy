package it2051229.genealogy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import it2051229.genealogy.entities.Application;
import it2051229.genealogy.entities.Genealogy;

public class BuildFamilyTreeActivity extends ActionBarActivity {
    private Genealogy genealogy;

    /**
     * Build a graph so we can use it to build a family tree
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_family_tree);

        Bundle extras = getIntent().getExtras();
        genealogy = (Genealogy)extras.getSerializable("genealogy");

        ((EditText) findViewById(R.id.editTextFamilyTree)).setHorizontallyScrolling(true);
    }

    /**
     * Show dialog for searching a name
     */
    public void buttonSearchNameTapped(View view) {
        Intent intent = new Intent(this, SearchNameActivity.class);
        intent.putExtra("genealogy", genealogy);
        startActivityForResult(intent, Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Handle the return call back of the selection of the name
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE) {
            String name = intent.getExtras().getString("name");
            ((EditText)findViewById(R.id.editTextFamilyTreeName)).setText(name);
        }
    }

    /**
     * Given the person's name, build the family tree of that person
     */
    public void buttonBuildFamilyTreeTapped(View view) {
        String name = ((EditText)findViewById(R.id.editTextFamilyTreeName)).getText().toString();

        // Perform validation
        if(name.isEmpty()) {
            Toast.makeText(this, "Please enter a name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(genealogy.getPerson(name) == null) {
            Toast.makeText(this, "The name " + name + " does not exist.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Alright, person exists so we build the family tree
        String familyTree = genealogy.buildFamilyTreeOf(name);
        ((EditText) findViewById(R.id.editTextFamilyTree)).setText(familyTree);
    }
}
