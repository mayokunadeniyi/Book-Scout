package com.mayokun.bookscout.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mayokun.bookscout.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import model.Book;
import utils.Constants;

public class BookDetailActivity extends AppCompatActivity {
    private Book book;
    private ImageView bookImage;
    private TextView bookTitle;
    private TextView bookAuthor;
    private TextView bookPublisher;
    private TextView bookPages;

    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        queue = Volley.newRequestQueue(this);

        bookImage = (ImageView) findViewById(R.id.bookImg);
        bookTitle = (TextView) findViewById(R.id.titleID);
        bookAuthor = (TextView) findViewById(R.id.authorID);
        bookPublisher = (TextView) findViewById(R.id.publishedByID);
        bookPages = (TextView) findViewById(R.id.pagesID);

        //Get the Book intent
        book = (Book) getIntent().getSerializableExtra("Book");
        //Get the book IMDB_ID
        String bookID = book.getBookIMDB();

        //Setting the book Image;
        Picasso.get()
                .load(book.getLargePoster())
                .placeholder(R.drawable.bookdefault)
                .into(bookImage);

        //Setting the book title and author
        bookTitle.setText(book.getBookTitle());
        bookAuthor.setText(book.getBookAuthor());

        //Setting up book publisher and pages by calling getBookDetails
        getBookDetails(bookID);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void getBookDetails(String id){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constants.BASE_LEFT_URL + id + Constants.BASE_RIGHT_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.has("publishers")){
                            try {
                                JSONArray publishersArray = response.getJSONArray("publishers");
                                int arrayLength = publishersArray.length();
                                String[] publishers = new String[arrayLength];
                                for (int i = 0; i < arrayLength; i++) {
                                    publishers[i] = publishersArray.getString(i);
                                }
                                bookPublisher.setText(TextUtils.join(",",publishers));

                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }else {
                            bookPublisher.setText(getString(R.string.publisher_na));
                        }

                        if (response.has("number_of_pages")){
                            try {
                                bookPages.setText(String.format("%s Pages", Integer.toString(response.getInt("number_of_pages"))));
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }else {
                            bookPages.setText(getString(R.string.pages_na));
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error",error.getMessage());
            }
        });
        queue.add(jsonObjectRequest);

    }


}
