package com.example.btlandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class EditAdminActivity extends AppCompatActivity {
    private EditText editName, editSl, editPrice, editNote;
    private Spinner editDanhmuc;
    private ImageView editImage, ivHome, ivBack, ivPerson;
    private Button editButton;
    private String bookId;
    private String imageBase64;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_admin);

        // Khởi tạo view
        editName = findViewById(R.id.editName);
        editDanhmuc = findViewById(R.id.editDanhmuc);
        editSl = findViewById(R.id.editSl);
        editPrice = findViewById(R.id.editPrice);
        editNote = findViewById(R.id.editNote);
        editImage = findViewById(R.id.editImage);
        editButton = findViewById(R.id.editButton);
        ivBack = findViewById(R.id.ivBack);
        ivHome = findViewById(R.id.ivHome);
        ivPerson = findViewById(R.id.ivPerson);

        // Thiết lập Spinner cho danh mục
        List<String> categories = Arrays.asList("Tiểu học", "THCS", "THPT", "Toeic", "Ielts");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editDanhmuc.setAdapter(adapter);

        // Lấy bookId từ intent
        bookId = getIntent().getStringExtra("bookId");
        if (bookId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy dữ liệu sách từ Firebase
        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("book").child(bookId);
        bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Book book = snapshot.getValue(Book.class);
                if (book != null) {
                    // Hiển thị thông tin sách
                    editName.setText(book.getName());
                    editSl.setText(String.valueOf(book.getQuantity()));
                    editPrice.setText(String.valueOf(book.getPrice()));
                    editNote.setText(book.getNote());

                    // Thiết lập danh mục trong Spinner
                    String category = book.getCategory();
                    if (category != null && !category.isEmpty()) {
                        int position = categories.indexOf(category);
                        if (position >= 0) {
                            editDanhmuc.setSelection(position);
                        } else {
                            editDanhmuc.setSelection(categories.size() - 1); // Chọn "Khác" nếu không tìm thấy
                        }
                    }

                    // Hiển thị ảnh
                    imageBase64 = book.getImage();
                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        try {
                            String base64String = imageBase64.replace("data:image/jpeg;base64,", "");
                            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            editImage.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            editImage.setImageResource(R.drawable.sgk);
                        }
                    } else {
                        editImage.setImageResource(R.drawable.sgk);
                    }
                } else {
                    Toast.makeText(EditAdminActivity.this, "Không tìm thấy sách", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditAdminActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Xử lý nút quay lại
        ivBack.setOnClickListener(v -> finish());

        // Xử lý nút home
        ivHome.setOnClickListener(v -> {
            Intent intent = new Intent(EditAdminActivity.this, HomeAdminActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        ivPerson.setOnClickListener(v -> {
            Intent intent = new Intent(EditAdminActivity.this, TaikhoanAdminActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Xử lý chọn ảnh khi nhấn vào ImageView editImage
        editImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Xử lý nút "Sửa"
        editButton.setOnClickListener(v -> saveBook());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                editImage.setImageBitmap(bitmap);

                // Chuyển ảnh thành Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveBook() {
        String name = editName.getText().toString().trim();
        String category = editDanhmuc.getSelectedItem().toString();
        String quantityStr = editSl.getText().toString().trim();
        String priceStr = editPrice.getText().toString().trim();
        String note = editNote.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào
        if (name.isEmpty() || category.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển đổi dữ liệu
        int quantity;
        double price;
        try {
            quantity = Integer.parseInt(quantityStr);
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số lượng hoặc giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng Book
        Book updatedBook = new Book();
        updatedBook.setName(name);
        updatedBook.setCategory(category);
        updatedBook.setQuantity(quantity);
        updatedBook.setPrice(price);
        updatedBook.setNote(note);
        updatedBook.setImage(imageBase64 != null ? imageBase64 : "");

        // Lưu vào Firebase
        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("book").child(bookId);
        bookRef.setValue(updatedBook)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(EditAdminActivity.this, "Cập nhật sách thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditAdminActivity.this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}