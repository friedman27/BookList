package com.example.android.booklist;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * base URL to query the google books api
     */
    private static String BASE_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=";


    Button mSearchButton;
    EditText mSearchField;
    BooksAdapter adapter;
    ListView listView;
    List<Book> books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mSearchButton = (Button) findViewById(R.id.search_button);
        mSearchField = (EditText) findViewById(R.id.search_entry);

        books = new ArrayList<>();

        adapter = new BooksAdapter(this);
        listView = (ListView) findViewById(R.id.list);
        listView.setEmptyView(findViewById(R.id.empty_view_holder));
        listView.setAdapter(adapter);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookAsyncTask task = new BookAsyncTask();
                if (isNetworkAvailable()) {
                    task.execute();
                } else {
                    Toast.makeText(getApplicationContext(), "Network not available", Toast.LENGTH_SHORT).show();
                }


            }
        });

        if(savedInstanceState != null) {
            // restore saved values
            books = (List<Book>) savedInstanceState.getSerializable("myKey");
            adapter.clear();
            adapter.addAll(books);
            adapter.notifyDataSetChanged();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putSerializable("myKey", (Serializable) books);
    }


    /**
     * Update the screen to display information from the given {@link Book}.
     */
    private void updateUi(List<Book> booksList) {
        books.clear();
        books.addAll(booksList);

        adapter.clear();
        adapter.addAll(booksList);
        adapter.notifyDataSetChanged();
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first earthquake in the response.
     */
    private class BookAsyncTask extends AsyncTask<URL, Void, List<Book>> {

        String searchEntry = mSearchField.getText().toString().replaceAll(" ", "+");

        @Override
        protected List<Book> doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(BASE_REQUEST_URL + searchEntry);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }

            // Extract relevant fields from the JSON response and create an {@link Event} object
            return extractFeatureFromJson(jsonResponse);

        }

        /**
         * Update the screen with the given book (which was the result of the
         * {@link BookAsyncTask}).
         */
        @Override
        protected void onPostExecute(List<Book> booksList) {
            if (booksList == null) {
                return;
            }

            updateUi(booksList);
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            //if url is null, then return early
            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                //if the request was successful (response code 200);
                //then read the input stream and parse the response
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);

                } else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem receiving JSON results", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        /**
         * Return an {@link Book} object by parsing out information
         * about the first book from the input bookJSON string.
         */
        private List<Book> extractFeatureFromJson(String bookJSON) {

            //if json string is empty or null return early
            if (TextUtils.isEmpty(bookJSON)) {
                return null;
            }

            List booksList = new ArrayList();

            try {
                JSONObject baseJsonResponse = new JSONObject(bookJSON);
                JSONArray itemsArray = baseJsonResponse.getJSONArray("items");

                for (int i = 0; i < itemsArray.length(); i++) {
                    // Extract out the book results
                    JSONObject firstObjectFeature = itemsArray.getJSONObject(i);
                    JSONObject volumeInfo = firstObjectFeature.getJSONObject("volumeInfo");

                    // Extract out the title and author
                    String title = volumeInfo.getString("title");
                    String authors = "";
                    JSONArray authorJsonArray = volumeInfo.optJSONArray("authors");

                    if (volumeInfo.has("authors")) {
                        if (authorJsonArray.length() > 0) {
                            for (int j = 0; j < authorJsonArray.length(); j++) {
                                authors = authorJsonArray.optString(j) + "";
                            }
                        }
                    }

                    booksList.add(new Book(title, authors));
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
            }
            return booksList;
        }

    }
}