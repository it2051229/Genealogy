package it2051229.genealogy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import it2051229.genealogy.entities.Application;
import it2051229.genealogy.entities.Genealogy;
import it2051229.genealogy.entities.Person;

public class DetailsActivity extends ActionBarActivity {
    private Person person;
    private Genealogy genealogy;

    /**
     * Load the current name properties and show the detailed information regarding a person
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Bundle extras = getIntent().getExtras();

        String name = extras.getString("name");

        genealogy = (Genealogy)extras.getSerializable("genealogy");
        person = genealogy.getPerson(name);

        ((TextView)findViewById(R.id.textViewName)).setText(name);

        showDetails();
    }

    /**
     * Format the list as one line separated by comma
     */
    private String listToString(String listName, ArrayList<String> list) {
        if(list.isEmpty()) {
            return "";
        }

        String details = listName + ": ";

        for(int i = 0; i < list.size(); i++) {
            details += list.get(i);

            if(i + 1 < list.size()) {
                details += ", ";
            }
        }

        details += "\n\n";

        return details;
    }

    /**
     * Show the details of the person
     */
    private void showDetails() {
        String details = "";

        if(person.getSpouse() != null) {
            details += "Spouse: " + person.getSpouse().getName() + "\n\n";
        }

        if(person.getDad() != null) {
            details += "Dad: " + person.getDad().getName() + "\n\n";
        }

        if(person.getMom() != null) {
            details += "Mom: " + person.getMom().getName() + "\n\n";
        }

        details += listToString("Grand Parents", genealogy.getGrandParentsOf(person.getName()));
        details += listToString("Siblings", genealogy.getSiblingsOf(person.getName()));
        details += listToString("Children", genealogy.getChildrenOf(person.getName()));
        details += listToString("Grand Children", genealogy.getGrandChildrenOf(person.getName()));

        if(!person.getNotes().isEmpty()) {
            details += "Notes:\n";
            details += person.getNotes();
        }

        ((TextView) findViewById(R.id.textViewDetails)).setText(details);
        showPicture();
    }

    /**
     * Load the picture of the person
     */
    private void showPicture() {
        // Load the picture if it exists
        ImageView imageView = (ImageView)findViewById(R.id.imageViewPicture);

        String path = Environment.getExternalStorageDirectory().toString();
        File directory = new File(path + "/Genealogy");
        File imageFile = new File(directory, person.getName() + ".jpg");

        if(imageFile.exists()) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), Uri.fromFile(imageFile));
                imageView.setImageBitmap(bitmap);
            } catch(Exception e) {
                Log.e("showDetails()", e.getMessage());
                Toast.makeText(this, "Oh snap! Developer error.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Show the activity for selecting another name
     */
    public void buttonSearchNameTapped(View view) {
        Intent intent = new Intent(this, SearchNameActivity.class);
        intent.putExtra("genealogy", genealogy);
        startActivityForResult(intent, Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Show the activity for taking camera photo
     */
    public void buttonTakePhotoTapped(View view) {
        // Stop if there are no SD card
        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "There are no external storage where to export data.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize the filename to use where the camera will save the captured photo
        String path = Environment.getExternalStorageDirectory().toString();
        File directory = new File(path + "/Genealogy");
        directory.mkdirs();

        File imageFile = new File(directory, person.getName() + ".jpg");
        imageFile.delete();

        try {
            imageFile.createNewFile();
        } catch(Exception e) {
            Log.e("buttonTakePhotoTapped()", e.getMessage());
            Toast.makeText(this, "Oh snap! Developer error.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(intent, Application.CAMERA_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Show the details of the selected name or process the photo taken
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == Application.SEARCH_NAME_ACTIVITY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                return;
            }

            String name = intent.getExtras().getString("name");
            ((TextView) findViewById(R.id.textViewName)).setText(name);
            person = genealogy.getPerson(name);
            showDetails();
        } else if(requestCode == Application.CAMERA_ACTIVITY_REQUEST_CODE) {
            showPicture();
        }
    }
}
