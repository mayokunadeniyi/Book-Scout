package com.mayokun.bookscout.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.agrawalsuneet.loaderspack.loaders.SearchLoader;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        searchLoader = (SearchLoader) findViewById(R.id.searchStuff);

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


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPopUp();

            }
        });
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
                }
                alertDialog.dismiss();


            }
        });


    }


    public List<Book> getBookList(String searchItem) {


        //clear the book list
        bookList.clear();

        //Make Search Loader visible
        searchLoader.setVisibility(View.VISIBLE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constants.LEFT_BASE_URL + searchItem.trim() + Constants.RIGHT_BASE_URL,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                searchLoader.setVisibility(View.GONE);

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
                VolleyLog.d("Error", error.getMessage());
            }
        });

        queue.add(jsonObjectRequest);

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
            }else {
                return "N/A";
            }

        } catch (JSONException e) {

            return "";
        }

    }




}
