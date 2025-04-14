package com.example.btlandroid.Book;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btlandroid.R;
import com.example.btlandroid.SanPhamActivity;

import java.time.Instant;
import java.util.List;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.BookviewHolder>{
    private List<Book> mListBook;

    private Context context;

    public SearchBookAdapter(Context context,List<Book> mListBook){
        this.context = context;
        this.mListBook = mListBook;
    }

    @NonNull
    @Override
    public BookviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search,parent, false);
        return new BookviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookviewHolder holder, int position) {
        Book book = mListBook.get(position);

        holder.bind(book);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SanPhamActivity.class);
            intent.putExtra("name", book.getName());
            intent.putExtra("price", book.getPrice());
            intent.putExtra("note", book.getNote());
            intent.putExtra("image", book.getImage()); // nếu là URL ảnh
            context.startActivity(intent);
        });

        if(book == null){
            return;
        }

        holder.name.setText(book.getName());
        holder.price.setText(String.valueOf(book.getPrice()));
        Glide.with(holder.itemView.getContext())
                .load(book.getImage())  // Đường dẫn ảnh (URL)
                .into(holder.image);

//        holder.add.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view){
//                addButtonClick(view, book);
//            }
//        });
    }

//    private void addButtonClick(View view, Book book) {
//
//    }

    public void setFilteredList(List<Book> filteredList) {
        this.mListBook = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(mListBook != null){
            return mListBook.size();
        }
        return 0;
    }

    public void setBooks(List<Book> books) {
        this.mListBook = books;
    }

    public class BookviewHolder extends RecyclerView.ViewHolder{
        private TextView name, price, add;
        private ImageView image;

        public BookviewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            add = itemView.findViewById(R.id.add);
            image = itemView.findViewById(R.id.image);
        }

        public void bind(Book book) {
            name.setText(book.getName());
            price.setText(book.getPrice() + " đ");

            // Nếu dùng Glide để load ảnh
            Glide.with(context).load(book.getImage()).into(image);
        }
    }
}
