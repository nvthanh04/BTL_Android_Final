package com.example.btlandroid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlandroid.Book.Book;
import com.example.btlandroid.Book.BookAdapter;
import com.example.btlandroid.Book.BookAdminAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeAdminActivity extends AppCompatActivity {
    private Button btnall, btnTieuHoc, btnTHCS, btnTHPT, btnToic, btnIelts;
    private AutoCompleteTextView searchAdmin;
    private RecyclerView rcvbook;
    private BookAdminAdapter mbookAdapter;
    private List<Book> mListBook;
    private List<String> mListBookKeys; // Danh sách khóa
    private List<Button> buttonList; // Danh sách để quản lý các nút
    private String currentCategory = "All"; // Lưu danh mục hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_admin);

        // Khởi tạo các view
        btnall = findViewById(R.id.btnall);
        btnTieuHoc = findViewById(R.id.btnTieuHoc);
        btnTHCS = findViewById(R.id.btnTHCS);
        btnTHPT = findViewById(R.id.btnTHPT);
        btnToic = findViewById(R.id.btnToic);
        btnIelts = findViewById(R.id.btnIelts);
        rcvbook = findViewById(R.id.rcvbook);
        ImageView ivAdd = findViewById(R.id.ivAdd);
        searchAdmin = findViewById(R.id.searchAdmin);

        searchAdmin.setKeyListener(null);
        searchAdmin.setKeyListener(null);
        searchAdmin.setFocusableInTouchMode(false);

        // Thêm các nút vào danh sách
        buttonList = new ArrayList<>();
        buttonList.add(btnall);
        buttonList.add(btnTieuHoc);
        buttonList.add(btnTHCS);
        buttonList.add(btnTHPT);
        buttonList.add(btnToic);
        buttonList.add(btnIelts);

        // Thiết lập RecyclerView với GridLayoutManager (2 cột)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 cột
        rcvbook.setLayoutManager(gridLayoutManager);

        // Thêm phân cách (nếu muốn giữ, nhưng có thể bỏ trong lưới)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rcvbook.addItemDecoration(dividerItemDecoration);

        mListBook = new ArrayList<>();
        mListBookKeys = new ArrayList<>();
        mbookAdapter = new BookAdminAdapter(this, mListBook, mListBookKeys,true);
        rcvbook.setAdapter(mbookAdapter);

        // Đặt trạng thái ban đầu: nút "Tất cả" màu xanh
        setButtonSelected(btnall);

        // Lấy danh sách sách và hiển thị "Tất cả" khi dữ liệu sẵn sàng
        getListBook();

        // Sự kiện nhấn nút
        btnall.setOnClickListener(v -> {
            loadBooksByCategory("All");
            setButtonSelected(btnall);
        });
        btnTieuHoc.setOnClickListener(v -> {
            loadBooksByCategory("Tiểu học");
            setButtonSelected(btnTieuHoc);
        });
        btnTHCS.setOnClickListener(v -> {
            loadBooksByCategory("THCS");
            setButtonSelected(btnTHCS);
        });
        btnTHPT.setOnClickListener(v -> {
            loadBooksByCategory("THPT");
            setButtonSelected(btnTHPT);
        });
        btnToic.setOnClickListener(v -> {
            loadBooksByCategory("Toeic");
            setButtonSelected(btnToic);
        });
        btnIelts.setOnClickListener(v -> {
            loadBooksByCategory("Ielts");
            setButtonSelected(btnIelts);
        });

        // Sự kiện tìm kiếm
        searchAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, SearchAdminActivity.class);
            startActivity(intent);
        });

        // Thêm sự kiện nhấn cho ivAdd
        ivAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, AddAdminActivity.class);
            startActivity(intent);
        });

        // Thiết lập BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Đặt Home là mục mặc định
//
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Đã ở Home, không làm gì
                return true;
            } else if (itemId == R.id.nav_doanhthu) {
                // Chuyển sang DoanhthuAdminActivity
                Intent intent = new Intent(HomeAdminActivity.this, DoanhthuAdminActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Tắt animation
                return true;
            } else if (itemId == R.id.nav_person) {
                Intent intent = new Intent(HomeAdminActivity.this, TaikhoanAdminActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Tắt animation
                return true;
            }
            return false;
        });
}

    private void getListBook() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("book");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mListBook.clear();
                mListBookKeys.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Book book = dataSnapshot.getValue(Book.class);
                    String key = dataSnapshot.getKey();
                    if (book != null) { // Kiểm tra null để tránh lỗi
                        mListBook.add(book);
                        mListBookKeys.add(key);
                    }
                }
                // Sau khi dữ liệu được tải, hiển thị danh sách theo danh mục hiện tại
                loadBooksByCategory(currentCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeAdminActivity.this, "Lỗi khi lấy danh sách sách: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBooksByCategory(String category) {
        currentCategory = category;
        List<Book> filteredList = new ArrayList<>();
        List<String> filteredKeys = new ArrayList<>();
        if (mListBook.isEmpty()) {
            mbookAdapter.setBooks(filteredList, filteredKeys);
            return;
        }

        // Tạo danh sách chỉ số (indices) của các phần tử phù hợp với danh mục
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < mListBook.size(); i++) {
            Book book = mListBook.get(i);
            if (category.equals("All") || book.getCategory().equalsIgnoreCase(category)) {
                indices.add(i);
            }
        }
        Collections.sort(indices, (i1, i2) -> Integer.compare(mListBook.get(i1).getQuantity(), mListBook.get(i2).getQuantity()));
        // Dùng danh sách chỉ số đã sắp xếp để tạo filteredList và filteredKeys
        for (int index : indices) {
            filteredList.add(mListBook.get(index));
            filteredKeys.add(mListBookKeys.get(index));
        }

        mbookAdapter.setBooks(filteredList, filteredKeys);
    }

    private void setButtonSelected(Button selectedButton) {
        // Đặt lại màu cho tất cả các nút
        for (Button button : buttonList) {
            button.setTextColor(Color.parseColor("#666666")); // Màu mặc định
        }
        // Đặt màu xanh cho nút được chọn
        selectedButton.setTextColor(Color.parseColor("#06A5FF"));
    }
}