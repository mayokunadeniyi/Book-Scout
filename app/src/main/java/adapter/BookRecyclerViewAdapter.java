package adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mayokun.bookscout.R;
import com.mayokun.bookscout.activities.BookDetailActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import model.Book;

public class BookRecyclerViewAdapter extends RecyclerView.Adapter<BookRecyclerViewAdapter.ViewHolder> {
    private List<Book> bookList;
    private Context context;

    public BookRecyclerViewAdapter(Context context, List<Book> list) {
        this.bookList = list;
        this.context = context;
    }

    @NonNull
    @Override
    public BookRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull BookRecyclerViewAdapter.ViewHolder viewHolder, int i) {
        Book book = bookList.get(i);
        String posterLink = book.getMediumPoster();

        viewHolder.bookName.setText(book.getBookTitle());
        viewHolder.bookAuthor.setText(book.getBookAuthor());

        Picasso.get()
                .load(posterLink)
                .placeholder(R.drawable.bookdefault).centerCrop()
                .into(viewHolder.bookCover);


    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView bookCover;
        public TextView bookName;
        public TextView bookAuthor;


        public ViewHolder(@NonNull View itemView, final Context ctx) {
            super(itemView);
            context = ctx;

            bookCover = (ImageView) itemView.findViewById(R.id.bookImageID);
            bookAuthor = (TextView) itemView.findViewById(R.id.bookAuthor);
            bookName = (TextView) itemView.findViewById(R.id.bookTitleID);

            //Set Item view onclick listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Book book = bookList.get(getAdapterPosition());
                    Intent intent = new Intent(ctx, BookDetailActivity.class);
                    intent.putExtra("Book",Book.class);
                    ctx.startActivity(intent);

                }
            });
        }

        @Override
        public void onClick(View v) {

        }
    }
}
