package com.mayokun.bookscout.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mayokun.bookscout.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import model.Book;
import utils.Constants;
import utils.Prefs;

public class BookDetailActivity extends AppCompatActivity {
    private Book book;
    private ImageView bookImage;
    private TextView bookTitle;
    private TextView bookAuthor;
    private TextView bookPublisher;
    private TextView bookPages;
    private FloatingActionButton shareButton;
    private FloatingActionButton viewButton;


    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        queue = Volley.newRequestQueue(this);

        bookImage = (ImageView) findViewById(R.id.bookImg);
        bookTitle = (TextView) findViewById(R.id.titleID);
        bookAuthor = (TextView) findViewById(R.id.authorID);
        bookPublisher = (TextView) findViewById(R.id.publishedByID);
        bookPages = (TextView) findViewById(R.id.pagesID);


        //Floating Action buttons
        shareButton = (FloatingActionButton) findViewById(R.id.shareID);
        viewButton = (FloatingActionButton) findViewById(R.id.viewID);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareIntent();
            }
        });

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewIntent(book.getBookIMDB());
            }
        });


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



    }

    public void getBookDetails(String id) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constants.BASE_LEFT_URL + id + Constants.BASE_RIGHT_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (response.has("publishers")) {
                            try {
                                JSONArray publishersArray = response.getJSONArray("publishers");
                                int arrayLength = publishersArray.length();
                                String[] publishers = new String[arrayLength];
                                for (int i = 0; i < arrayLength; i++) {
                                    publishers[i] = publishersArray.getString(i);
                                }
                                bookPublisher.setText(TextUtils.join(",", publishers));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            bookPublisher.setText(getString(R.string.publisher_na));
                        }

                        if (response.has("number_of_pages")) {
                            try {
                                bookPages.setText(String.format("%s Pages", Integer.toString(response.getInt("number_of_pages"))));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            bookPages.setText(getString(R.string.pages_na));
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", error.getMessage());
            }
        });
        queue.add(jsonObjectRequest);

    }

    private void shareIntent() {
        ImageView shareImage = (ImageView) findViewById(R.id.bookImg);
        TextView shareTitle = (TextView) findViewById(R.id.titleID);

        Uri bmpUri = getLocalBitmapUri(shareImage);
        // Construct a ShareIntent with link to image
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, (String) shareTitle.getText());
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        //Launch share menu
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }

    private Uri getLocalBitmapUri(ImageView shareImage) {
        //Extract Bitmap from ImageView drawable
        Drawable drawable = shareImage.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) shareImage.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream outputStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
            outputStream.close();
            bmpUri = Uri.fromFile(file);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public void viewIntent(final String id){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constants.BASE_LEFT_URL + id));
        startActivity(intent);
    }


}
