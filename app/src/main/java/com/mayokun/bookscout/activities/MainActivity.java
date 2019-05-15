package com.mayokun.bookscout.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);

//        recyclerView = (RecyclerView) findViewById(R.id.RecyclerViewID);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Prefs prefs = new Prefs(MainActivity.this);
        String search = prefs.getSearch();

        bookList = new ArrayList<>();

        bookList = getBookList(search);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPopUp();

            }
        });
    }

    public void createPopUp(){
        builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup,null);
        final EditText searchItem = (EditText) view.findViewById(R.id.searchID);
        Button searchButton = (Button) view.findViewById(R.id.searchbuttonID);

        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs prefs = new Prefs(MainActivity.this);

                if (!searchItem.getText().toString().isEmpty()){

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

    public List<Book> getBookList(String searchItem){

        //clear the book list
        bookList.clear();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constants.LEFT_BASE_URL + searchItem.trim() + Constants.RIGHT_BASE_URL,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try{
                    JSONArray bookArray = response.getJSONArray("docs");


                    for (int i = 0; i < bookArray.length(); i++) {

                        JSONObject bookObj = bookArray.getJSONObject(i);
                        //Create a new book instance
                        Book book = new Book();
                        book.setBookTitle(bookObj.getString("title_suggest"));
                        book.setBookAuthor(bookObj.getString("author_name"));

                        //book.setPoster(bookObj.getString("")); //TODO: Follow guide in already built example http://covers.openlibrary.org/b/olid/" + openLibraryId + "-M.jpg?default=false";
                        /**
                         * Note that the object may have just a poster or cover or edition key
                         */
                        bookList.add(book);
                    }
//                    bookRecyclerViewAdapter.notifyDataSetChanged();


                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error",error.getMessage());
            }
        });

        queue.add(jsonObjectRequest);
        return bookList;


    }


}
