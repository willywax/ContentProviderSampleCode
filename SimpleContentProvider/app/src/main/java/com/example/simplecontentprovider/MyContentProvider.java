package com.example.simplecontentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyContentProvider extends ContentProvider {
    public MyContentProvider() {
    }

    static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    static final String AUTHORITY = "inno.user.provider";

    static final String API_STRING ="";

    private static final String PATH_DATA = "data";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_DATA);

    private static final int DATA = 1;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static {
        uriMatcher.addURI(AUTHORITY, PATH_DATA, DATA);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        return true;
    }

    @Override
    public Cursor query(Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //implement network call to fetch data and retrive variables from resolver
        String dataString = selectionArgs[0].toString();
        return fetchDataFromNetwork(dataString);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Cursor fetchDataFromNetwork(String json) {
        // Example using OkHttp to fetch data
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(API_STRING)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Process the response and convert it to a Cursor
            return convertResponseToCursor(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private Cursor convertResponseToCursor(String jsonResponse) {
        // Convert the JSON response to a Cursor
        // You can use a library like Gson or org.json to parse the JSON
        // Define the columns to return in the cursor
        String[] columns = {"status", "card_balance", "required_balance","message"};

        // Create a MatrixCursor with the columns
        MatrixCursor matrixCursor = new MatrixCursor(columns);

        try {
                JSONObject jsonObject = new JSONObject(jsonResponse);

                String status,card_balance,required_balance,message = null;

                if(jsonObject.getString("status").equals("error")){
                    status = jsonObject.getString("status");
                    card_balance = "N/A";
                    required_balance = "N/A";
                    message = jsonObject.getString("message");
                }else{
                    status = jsonObject.getString("status");
                    card_balance = jsonObject.getString("card_balance");
                    required_balance = jsonObject.getString("required_balance");
                    message = jsonObject.getString("message");
                }

                // Add a row to the cursor
                matrixCursor.addRow(new Object[]{status,card_balance, required_balance, message});
//            }

        } catch (JSONException e) {
            Log.v("REACHED","Json array ERROR HERE");
            e.printStackTrace();
        }

        // Return the populated MatrixCursor
        return matrixCursor;

    }
}