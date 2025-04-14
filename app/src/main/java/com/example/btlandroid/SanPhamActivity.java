package com.example.btlandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.btlandroid.Book.Book;
import com.example.btlandroid.Cart.Cart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SanPhamActivity extends AppCompatActivity {
    private TextView tvName, tvPrice, tvMoTa, tvminus, amount, tvplus, tvAdd;
    private ImageView idIVSSImage, ivBack, ivHome, ivShop;
    private String bookId;
    private Book currentBook;
    private int soLuong = 1; // số lượng mặc định

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sanpham);

        // Ánh xạ view
        tvName = findViewById(R.id.idTVName);
        tvPrice = findViewById(R.id.idTVPrice);
        tvMoTa = findViewById(R.id.tvMoTa);
        ivBack = findViewById(R.id.ivBack);
        ivHome = findViewById(R.id.ivHome);
        ivShop = findViewById(R.id.ivShop);
        idIVSSImage = findViewById(R.id.idIVSSImage);
        tvminus = findViewById(R.id.tvminus);
        amount = findViewById(R.id.amount);
        tvplus = findViewById(R.id.tvplus);
        tvAdd = findViewById(R.id.tvAdd);

        amount.setText(String.valueOf(soLuong));

        // Lấy bookId từ intent
        bookId = getIntent().getStringExtra("bookId");
        if (bookId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tải dữ liệu sách
        loadBookData(bookId);

        // Quay lại


        ivHome.setOnClickListener(v -> {
            startActivity(new Intent(SanPhamActivity.this, HomeActivity.class));
            finish();
        });

        ivShop.setOnClickListener(v -> {
            startActivity(new Intent(SanPhamActivity.this, GiohangActivity.class));
            finish();
        });

        ivBack.setOnClickListener(v -> finish());

        // Xử lý tăng/giảm số lượng
        tvplus.setOnClickListener(v -> {
            soLuong++;
            amount.setText(String.valueOf(soLuong));
        });

        tvminus.setOnClickListener(v -> {
            if (soLuong > 1) {
                soLuong--;
                amount.setText(String.valueOf(soLuong));
            }
        });

        // Thêm vào giỏ hàng
        tvAdd.setOnClickListener(v -> {
            if (currentBook == null) {
                Toast.makeText(this, "Chưa có thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Bạn cần đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = currentUser.getUid();
            String productId = String.valueOf(currentBook.getId());

            DatabaseReference cartRef = FirebaseDatabase.getInstance()
                    .getReference("giohang")
                    .child(userId)
                    .child(productId);

            cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Cart cart;
                    if (snapshot.exists()) {
                        cart = snapshot.getValue(Cart.class);
                        if (cart != null) {
                            cart.setSoLuong(cart.getSoLuong() + soLuong);
                        }
                    } else {
                        cart = new Cart(productId, currentBook.getName(), currentBook.getPrice(), soLuong, currentBook.getImage());
                    }

                    cartRef.setValue(cart)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(SanPhamActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                                tvAdd.setText("✓");
                                new Handler().postDelayed(() -> tvAdd.setText("Thêm"), 1000);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(SanPhamActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SanPhamActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadBookData(String bookId) {
        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("book").child(bookId);
        bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Book book = snapshot.getValue(Book.class);
                if (book != null) {
                    currentBook = book;

                    tvName.setText(book.getName());
                    tvPrice.setText(book.getPrice() + " đ");
                    tvMoTa.setText(book.getNote());

                    String imageBase64 = book.getImage();
                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        try {
                            String base64String = imageBase64.replace("data:image/jpeg;base64,", "");
                            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            idIVSSImage.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            idIVSSImage.setImageResource(R.drawable.sgk);
                        }
                    } else {
                        idIVSSImage.setImageResource(R.drawable.sgk);
                    }
                } else {
                    Toast.makeText(SanPhamActivity.this, "Không tìm thấy sách", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SanPhamActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadBookData(bookId);
        }
    }
}
