package com.example.btlandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.btlandroid.Book.Book;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SanphamAdminActivity extends AppCompatActivity {
    private TextView tvName, tvPrice, tvCategory, tvQuantity, tvMoTa;
    private ImageView idIVSSImage, ivHome, ivBack, ivPerson, ivEdit, ivDelete;
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sanpham_admin);

        // Khởi tạo view
        tvName = findViewById(R.id.idTVName);
        tvPrice = findViewById(R.id.idTVPrice);
        tvCategory = findViewById(R.id.tvCategory);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvMoTa = findViewById(R.id.tvMoTa);
        ivBack = findViewById(R.id.tvBack);
        idIVSSImage = findViewById(R.id.idIVSSImage);
        ivHome = findViewById(R.id.ivHome);
        ivPerson = findViewById(R.id.ivPerson);
        ivEdit = findViewById(R.id.ivEdit);
        ivDelete = findViewById(R.id.ivDelete);

        // Lấy bookId từ intent
        bookId = getIntent().getStringExtra("bookId");
        if (bookId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tải dữ liệu sách
        loadBookData(bookId);

        // Xử lý nút quay lại
        ivBack.setOnClickListener(v -> finish());

        // Xử lý nút home
        ivHome.setOnClickListener(v -> {
            Intent intent = new Intent(SanphamAdminActivity.this, HomeAdminActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        ivPerson.setOnClickListener(v -> {
            Intent intent = new Intent(SanphamAdminActivity.this, TaikhoanAdminActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Xử lý nút chỉnh sửa
        ivEdit.setOnClickListener(v -> {
            Intent intent = new Intent(SanphamAdminActivity.this, EditAdminActivity.class);
            intent.putExtra("bookId", bookId);
            startActivityForResult(intent, 100);
        });

        // Xử lý nút xóa
        ivDelete.setOnClickListener(v -> {
            DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("book").child(bookId);
            bookRef.removeValue()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(SanphamAdminActivity.this, "Xóa sách thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SanphamAdminActivity.this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    // Hiển thị thông tin sách
                    tvName.setText(book.getName());
                    tvPrice.setText(book.getPrice() + " đ");
                    tvCategory.setText("Danh mục: " + book.getCategory());
                    tvQuantity.setText("Số lượng: " + book.getQuantity());
                    tvMoTa.setText(book.getNote());

                    // Hiển thị ảnh
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
                    Toast.makeText(SanphamAdminActivity.this, "Không tìm thấy sách", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SanphamAdminActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Làm mới dữ liệu
            loadBookData(bookId);
        }
    }
}