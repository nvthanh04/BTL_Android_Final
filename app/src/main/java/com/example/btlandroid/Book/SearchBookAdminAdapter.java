package com.example.btlandroid.Book;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlandroid.R;
import com.example.btlandroid.SanphamAdminActivity;

import java.util.List;

public class SearchBookAdminAdapter extends RecyclerView.Adapter<SearchBookAdminAdapter.BookViewHolder> {
    private List<Book> mListBook;
    private List<String> mListBookKeys;
    private Context context;

    public SearchBookAdminAdapter(Context context, List<Book> mListBook, List<String> mListBookKeys) {
        this.context = context;
        this.mListBook = mListBook;
        this.mListBookKeys = mListBookKeys;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_admin, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = mListBook.get(position);
        String bookKey = mListBookKeys.get(position);

        if (book == null) {
            holder.name.setText("Sách không hợp lệ");
            holder.image.setImageResource(R.drawable.sgk);
            return;
        }

        // Gán dữ liệu cho các view
        holder.name.setText(book.getName());
        holder.price.setText(book.getPrice() + " đ");
        holder.quantity.setText("Số lượng: " + book.getQuantity());

        // Hiển thị ảnh từ Base64
        String imageBase64 = book.getImage();
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                String base64String = imageBase64.replace("data:image/jpeg;base64,", "");
                byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.image.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                holder.image.setImageResource(R.drawable.sgk);
            }
        } else {
            holder.image.setImageResource(R.drawable.sgk);
        }

        // Sự kiện nhấn vào item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SanphamAdminActivity.class);
            intent.putExtra("bookId", bookKey);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mListBook != null ? mListBook.size() : 0;
    }

    public void setBooks(List<Book> books, List<String> bookKeys) {
        this.mListBook = books;
        this.mListBookKeys = bookKeys;
        notifyDataSetChanged();
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        private TextView name, price, quantity;
        private ImageView image;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            quantity = itemView.findViewById(R.id.quantity);
            image = itemView.findViewById(R.id.image);
        }
    }
}