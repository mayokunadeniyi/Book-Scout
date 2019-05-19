package com.mayokun.bookscout.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.agrawalsuneet.loaderspack.loaders.SearchLoader;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mayokun.bookscout.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import adapter.BookRecyclerViewAdapter;
import model.Book;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import utils.Constants;
import utils.Prefs;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BookRecyclerViewAdapter bookRecyclerViewAdapter;
    private List<Book> bookList;
    private RequestQueue queue;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private SearchLoader searchLoader;
    private TextView waitingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        searchLoader = (SearchLoader) findViewById(R.id.searchStuff);
        waitingText = (TextView) findViewById(R.id.waitingID);

        recyclerView = (RecyclerView) findViewById(R.id.RecyclerViewID);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Prefs prefs = new Prefs(MainActivity.this);
        String search = prefs.getSearch();

        bookList = new ArrayList<>();

        bookList = getBookList(search);

        //Passing the bookList into the recyclerViewAdapter
        bookRecyclerViewAdapter = new BookRecyclerViewAdapter(this, bookList);
        recyclerView.setAdapter(bookRecyclerViewAdapter);
        bookRecyclerViewAdapter.notifyDataSetChanged();

        //Search Floating action button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchLoader.setVisibility(View.GONE);
                waitingText.setVisibility(View.GONE);
                createPopUp();

            }
        });


        //Focus design for search floating action button
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean("FirstTime", false)) {

            //Cancel volley request from shared preference
            queue.cancelAll("DOCS");

            //Disable searchloader
            searchLoader.setVisibility(View.GONE);
            waitingText.setVisibility(View.GONE);
            new MaterialTapTargetPrompt.Builder(MainActivity.this)
                    .setTarget(R.id.fab)
                    .setFocalColour(Color.parseColor("#ffffff"))
                    .setPrimaryText("Make your first search!")
                    .setBackgroundColour(Color.parseColor("#5f4339"))
                    .setSecondaryText("Tap the search icon to get started with Book Scout")
                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                        @Override
                        public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                                createPopUp();
                            }

                        }
                    }).show();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("FirstTime", true);
            editor.commit();

        }
    }

    public void createPopUp() {
        builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup, null);
        final EditText searchItem = (EditText) view.findViewById(R.id.searchID);
        Button searchButton = (Button) view.findViewById(R.id.searchbuttonID);

        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs prefs = new Prefs(MainActivity.this);

                if (!searchItem.getText().toString().isEmpty()) {

                    prefs.setSearch(searchItem.getText().toString());
                    //Clear the book list
                    bookList.clear();
                    //get new book list
                    getBookList(searchItem.getText().toString());
                    alertDialog.dismiss();
                }else if (searchItem.getText().toString().isEmpty()){
                    Snackbar.make(v,"Search request is empty!",Snackbar.LENGTH_LONG).show();
                }

            }
        });
    }

    public List<Book> getBookList(String searchItem) {


        //clear the book list
        bookList.clear();

        //Make Search Loader visible
        searchLoader.setVisibility(View.VISIBLE);
        waitingText.setText(getString(R.string.searching));
        waitingText.setVisibility(View.VISIBLE);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constants.LEFT_BASE_URL + searchItem.trim() + Constants.RIGHT_BASE_URL,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                searchLoader.setVisibility(View.GONE);
                waitingText.setVisibility(View.GONE);

                try {
                    JSONArray bookArray = response.getJSONArray("docs");


                    for (int i = 0; i < bookArray.length(); i++) {

                        JSONObject bookObj = bookArray.getJSONObject(i);
                        //Create a new book instance
                        Book book = new Book();

                        //Check if bookObj has title

                        if (bookObj.has("title_suggest")) {

                            book.setBookTitle(bookObj.getString("title_suggest"));

                        } else {
                            book.setBookAuthor("");
                        }

                        //Calling the getAuthors method
                        book.setBookAuthor(getAuthors(bookObj));

                        //For the book image: Checking if it has cover_edition_key or edition_key
                        if (bookObj.has("cover_edition_key")) {
                            book.setBookIMDB(bookObj.getString("cover_edition_key"));
                        } else if (bookObj.has("edition_key")) {
                            JSONArray bookImdbArray = bookObj.getJSONArray("edition_key");
                            book.setBookIMDB(bookImdbArray.getString(0));
                        }
                        if (bookObj.has("isbn")){
                            JSONArray bookIsbn = bookObj.getJSONArray("isbn");
                            book.setIsbn(bookIsbn.getString(0));
                        }

                        bookList.add(book);
                    }
                    bookRecyclerViewAdapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleVolleyError(error);
            }
        });

        queue.add(jsonObjectRequest);
        jsonObjectRequest.setTag("DOCS");

        return bookList;

    }

    //This method is to get the authors
    private static String getAuthors(JSONObject jsonObject) {

        try {
            if (jsonObject.has("author_name")) {

                JSONArray authors = jsonObject.getJSONArray("author_name");
                //Get the length of the author array
                int numAuthors = authors.length();

                String[] authorStrings = new String[numAuthors];

                for (int i = 0; i < numAuthors; i++) {
                    authorStrings[i] = authors.getString(i);
                }
                return TextUtils.join(",", authorStrings);
            } else {
                return "N/A";
            }

        } catch (JSONException e) {

            return "";
        }

    }

    //Error methods for Volley request
    private void handleVolleyError(VolleyError volleyError) {

        if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError) {
            builder = new AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.error_popup, null);

            TextView errorText = (TextView) view.findViewById(R.id.errorMessageID);
            Button reloadButton = (Button) view.findViewById(R.id.reloadBtn);

            reloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    Prefs prefs = new Prefs(MainActivity.this);
                    getBookList(prefs.getSearch());
                }
            });

            errorText.setText(getString(R.string.network_error));
            builder.setView(view);
            alertDialog = builder.create();
            alertDialog.show();
            waitingText.setText(getString(R.string.waiting_string));
            waitingText.setVisibility(View.VISIBLE);
            searchLoader.setVisibility(View.GONE);
        } else if (volleyError instanceof NetworkError) {
            builder = new AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.error_popup, null);

            TextView errorText = (TextView) view.findViewById(R.id.errorMessageID);
            Button reloadButton = (Button) view.findViewById(R.id.reloadBtn);

            reloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    Prefs prefs = new Prefs(MainActivity.this);
                    getBookList(prefs.getSearch());
                }
            });

            errorText.setText(getString(R.string.networkError));
            builder.setView(view);
            alertDialog = builder.create();
            alertDialog.show();
            waitingText.setText(getString(R.string.waiting_string));
            waitingText.setVisibility(View.VISIBLE);
            searchLoader.setVisibility(View.GONE);
        } else if (volleyError instanceof ParseError) {
            builder = new AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.error_popup, null);

            TextView errorText = (TextView) view.findViewById(R.id.errorMessageID);
            Button reloadButton = (Button) view.findViewById(R.id.reloadBtn);

            reloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    Prefs prefs = new Prefs(MainActivity.this);
                    getBookList(prefs.getSearch());
                }
            });

            errorText.setText(getString(R.string.parse_error));
            builder.setView(view);
            alertDialog = builder.create();
            alertDialog.show();
            waitingText.setText(getString(R.string.waiting_string));
            waitingText.setVisibility(View.VISIBLE);
            searchLoader.setVisibility(View.GONE);
        } else if (volleyError instanceof ServerError) {
            builder = new AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.error_popup, null);

            TextView errorText = (TextView) view.findViewById(R.id.errorMessageID);
            Button reloadButton = (Button) view.findViewById(R.id.reloadBtn);

            reloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    Prefs prefs = new Prefs(MainActivity.this);
                    getBookList(prefs.getSearch());
                }
            });

            errorText.setText(getString(R.string.server_error));
            builder.setView(view);
            alertDialog = builder.create();
            alertDialog.show();
            waitingText.setText(getString(R.string.waiting_string));
            waitingText.setVisibility(View.VISIBLE);
            searchLoader.setVisibility(View.GONE);
        }

    }


}
