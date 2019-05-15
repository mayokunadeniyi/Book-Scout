package adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mayokun.bookscout.R;
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
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookRecyclerViewAdapter.ViewHolder viewHolder, int i) {
        Book book = bookList.get(i);
        String posterLink = book.getPoster();

        viewHolder.bookName.setText(book.getBookTitle());
        viewHolder.bookAuthor.setText(book.getBookAuthor());

        Picasso.get()
                .load(posterLink)
                .placeholder(R.drawable.bookdefault)
                .into(viewHolder.bookCover);


    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView bookCover;
        public TextView bookName;
        public TextView bookAuthor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            bookCover = (ImageView) itemView.findViewById(R.id.bookImageID);
            bookAuthor = (TextView) itemView.findViewById(R.id.bookAuthor);
            bookName = (TextView) itemView.findViewById(R.id.bookTitleID);
        }
    }
}
