package it2051229.genealogy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import it2051229.genealogy.entities.Genealogy;


public class SearchNameActivity extends ActionBarActivity {

    private ArrayList<String> arrayListNames;
    private ArrayAdapter<String> arrayAdapterNames;

    private Genealogy genealogy;

    /**
     * Initialize stuff we need for searching
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_name);

        // Initialize the list view where to display the added names
        genealogy = (Genealogy)getIntent().getSerializableExtra("genealogy");
        arrayListNames = genealogy.getNames();
        arrayAdapterNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayListNames);

        ListView listViewNames = (ListView) findViewById(R.id.listViewNames);
        listViewNames.setAdapter(arrayAdapterNames);

        // Add an event to the list view such that after selection, it will return the selected item
        listViewNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = ((TextView)view).getText().toString();

                // Close the activity and return the result
                Intent intent = getIntent();
                intent.putExtra("name", name);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

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
}
