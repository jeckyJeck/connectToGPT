package com.yakov.connecttogpt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WLNewListActivity extends AppCompatActivity {

    EditText mEditText;
    Button mCreateListBtn;
    TextView mWarningTv;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;

    String EnglishHebrewSentences, paragraph, date, metaData;
    SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wlnew_list);
        mEditText = findViewById(R.id.editText);
        mCreateListBtn = findViewById(R.id.create_list_btn);
        mWarningTv = findViewById(R.id.warning_tv);
        databaseHelper = new DatabaseHelper(getApplicationContext());
        setmEditTextListener();
    }

    public void setmEditTextListener(){

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    mCreateListBtn.setEnabled(false);
                } else {
                    mCreateListBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void onClickCreateListBtn(View view){
        String inputText;
        inputText = mEditText.getText().toString();
        if (validateInput(inputText)) {
            //TODO sent words to gpt()
            dataToVariables();
            addToWordsList();
            finish();
        } else {
            mWarningTv.setVisibility(View.VISIBLE);
        }
    }
    private boolean validateInput(String input) {
        String[] pairs = input.split("\n");

        for (String pair : pairs) {
            String[] words = pair.split("#");
            if (words.length != 2) {
                return false;
            }
        }
        return true;
    }

    public void dataToVariables(){
        EnglishHebrewSentences = "example#דוגמה#sentence that contain the word example;"
                                +"another#אחרת#sentence that contain the word another";
        paragraph = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut non pretium purus. In hac habitasse platea dictumst. Vestibulum condimentum commodo nisl, id posuere nisi iaculis ac. Donec tristique porttitor dolor sed dictum. Sed in sapien eu diam venenatis molestie. Donec non vulputate diam, sed molestie nisl. Maecenas mattis varius urna, ut cursus mauris ultrices sed. Vivamus mi nibh, ullamcorper nec sagittis et, venenatis at justo. Phasellus cursus tristique massa eu pretium. Ut pulvinar nulla nulla. Aliquam posuere odio et est euismod, in aliquet massa tempus. Integer at sodales magna. Sed id velit risus.";
        date = dateFormat.format(new Date());

        String[] words = EnglishHebrewSentences.split(";");
        metaData = String.valueOf(words.length)+";"+
                words[0].split("#")[0] + " - " +
                words[words.length-1].split("#")[0] ;
    }

    public void addToWordsList(){
        // Inserting data into the database
        db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pairs", EnglishHebrewSentences);
        values.put("paragraph", paragraph);
        values.put("metadata", metaData);
        values.put("creation_date", date);
        long insertedRowId = db.insert("word_packets", null, values);
        db.close();
    }

}