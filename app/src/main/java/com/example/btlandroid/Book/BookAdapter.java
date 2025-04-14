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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btlandroid.Cart.Cart;
import com.example.btlandroid.R;
import com.example.btlandroid.SanPhamActivity;
import com.example.btlandroid.SanphamAdminActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookviewHolder> {
    private List<Book> mListBook;
    private List<String> mListBookKeys;
    private Context context;
    private boolean isAdminMode;

    public BookAdapter(Context context, List<Book> mListBook, List<String> mListBookKeys, boolean isAdminMode) {
        this.context = context;
        this.mListBook = mListBook;
        this.mListBookKeys = mListBookKeys;
        this.isAdminMode = isAdminMode;
    }

    @NonNull
    @Override
    public BookviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new BookviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookviewHolder holder, int position) {
        Book book = mListBook.get(position);
        if (book == null) {
            holder.name.setText("Sách không hợp lệ");
            holder.image.setImageResource(R.drawable.sgk);
            return;
        }

        holder.bind(book, isAdminMode);
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
                holder.image.setImageResource(R.drawable.sgk); // Ảnh mặc định nếu lỗi
            }
        } else {
            holder.image.setImageResource(R.drawable.sgk);
        }
        // Xử lý nhấp vào sách
        holder.itemView.setOnClickListener(v -> {
            if (isAdminMode) {
                Intent intent = new Intent(context, SanphamAdminActivity.class);
                intent.putExtra("bookId", mListBookKeys.get(position)); // Truyền khóa sách
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, SanPhamActivity.class);
                intent.putExtra("name", book.getName());
                intent.putExtra("price", book.getPrice());
                intent.putExtra("note", book.getNote());
                intent.putExtra("image", book.getImage());
                context.startActivity(intent);
            }
        });

        // Nút thêm (chỉ cho người dùng)
        if (!isAdminMode) {
            holder.add.setOnClickListener(v -> addButtonClick(book));
        } else {
            holder.add.setOnClickListener(null);
        }
    }

    private void addButtonClick(Book book) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Bạn cần đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String productId = String.valueOf(book.getId());

        // Tham chiếu đến node giỏ hàng của sản phẩm
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("giohang")
                .child(userId)
                .child(productId);

        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Cart cart;
                if (snapshot.exists()) {
                    // Sản phẩm đã có trong giỏ hàng, tăng số lượng
                    cart = snapshot.getValue(Cart.class);
                    if (cart != null) {
                        cart.setSoLuong(cart.getSoLuong() + 1);
                    }
                } else {
                    // Sản phẩm chưa có, tạo mới với số lượng 1
                    cart = new Cart(productId, book.getName(), book.getPrice(), 1, book.getImage());
                }

                // Cập nhật hoặc thêm sản phẩm vào Firebase
                cartRef.setValue(cart)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Đã thêm " + book.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Lỗi khi thêm vào giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Lỗi khi kiểm tra giỏ hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
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

    public class BookviewHolder extends RecyclerView.ViewHolder {
        private TextView name, price, add;
        private ImageView image;

        public BookviewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            add = itemView.findViewById(R.id.add);
            image = itemView.findViewById(R.id.image);
        }

        public void bind(Book book, boolean isAdminMode) {
            name.setText(book.getName());
            price.setText(book.getPrice() + " đ");
            // Hiển thị ảnh từ Base64
            String imageBase64 = book.getImage();
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                try {
                    String base64String = imageBase64.replace("data:image/jpeg;base64,", "");
                    byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    image.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    image.setImageResource(R.drawable.sgk);
                }
            } else {
                image.setImageResource(R.drawable.sgk);
            }

            if (isAdminMode) {
                // Hiển thị số lượng cho admin
                add.setText("x"+String.valueOf(book.getQuantity()));
            } else {
                // Hiển thị "+" hoặc giữ nguyên cho người dùng
                add.setText("+");
            }
        }
    }
}