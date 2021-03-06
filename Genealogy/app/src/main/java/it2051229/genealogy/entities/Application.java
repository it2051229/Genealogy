package it2051229.genealogy.entities;

import android.os.Environment;
import java.io.File;

public class Application {

    /**
     * Global request codes for activities
     */
    public static final int ADD_NAME_ACTIVITY_REQUEST_CODE = 1;
    public static final int SEARCH_NAME_ACTIVITY_REQUEST_CODE = 2;
    public static final int UPDATE_NAME_ACTIVITY_REQUEST_CODE = 3;
    public static final int QUESTION_AND_ANSWER_ACTIVITY_REQUEST_CODE = 4;
    public static final int CAMERA_ACTIVITY_REQUEST_CODE = 5;

    /**
     * Global location of genealogy related files
     */
    public static final File DIRECTORY = new File(Environment.getExternalStorageDirectory().toString() + "/Genealogy");
}
