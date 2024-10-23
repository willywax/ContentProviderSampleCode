package com.example.mymainresolver;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static final String AUTHORITY = "inno.user.provider";
    private static final String PATH_DATA = "data";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_DATA);

    TextView balanceView, requiredView, messageView, statusText;
    EditText volumeText, cardText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusResult);
        balanceView = findViewById(R.id.balanceResult);
        requiredView = findViewById(R.id.requiredResult);
        messageView = findViewById(R.id.messageResult);

        cardText = findViewById(R.id.cardText);
        volumeText = findViewById(R.id.volumeText);
        button = findViewById(R.id.getButton);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a JSON object to pass in the query
                new QueryContentProviderTask().execute();
            }
        });


    }

    private class QueryContentProviderTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... voids) {
            JSONObject queryJson = new JSONObject();
            JSONObject results = new JSONObject();
            try {
                queryJson.put("card_number", cardText.getText().toString());
                queryJson.put("volume", volumeText.getText().toString());
                // Add other key-value pairs as required by the provider
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Convert JSON object to string
            String jsonString = queryJson.toString();

            // Selection (where clause) and arguments (passing JSON as an argument)
            String selection = "json_query";  // Replace with the actual key expected by the ContentProvider
            String[] selectionArgs = new String[]{jsonString};  // Pass JSON string as the selection argument

            // Columns to fetch
            String[] projection = new String[]{"status", "card_balance", "required_balance","message"};


            // Query the ContentProvider using ContentResolver
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(CONTENT_URI, projection, selection, selectionArgs, null);

            // Check if the query returned data
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder builder = new StringBuilder();
//                Map<String, String> results = new HashMap<>();

                do {
//                int id = cursor.getInt(cursor.getColumnIndex("status"));
                    String status = cursor.getString(cursor.getColumnIndex("status"));
                    String card_balance = cursor.getString(cursor.getColumnIndex("card_balance"));
                    String required_balance = cursor.getString(cursor.getColumnIndex("required_balance"));
                    String message = cursor.getString(cursor.getColumnIndex("message"));

                    try {
                        results.put("status",status);
                        results.put("card_balance",card_balance);
                        results.put("required_balance", required_balance);
                        results.put("message",message);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                } while (cursor.moveToNext());

//                Log.v("RESX", results.get("message"));

                cursor.close();
            } else {
//                messageView.setText("No data found.");
            }
            return results.toString();
        }

        protected void onPostExecute(String results) {
            try {
                JSONObject jsonObject = new JSONObject(results);
                messageView.setText(jsonObject.getString("message"));
                balanceView.setText(jsonObject.getString("card_balance"));
                requiredView.setText(jsonObject.getString("required_balance"));
                statusText.setText(jsonObject.getString("status"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            Log.v("RESX", results);

        }
    }
}