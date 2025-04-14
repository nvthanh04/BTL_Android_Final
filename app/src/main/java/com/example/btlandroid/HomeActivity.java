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

public class HomeActivity extends AppCompatActivity {
    private Button btnall, btnTieuHoc, btnTHCS, btnTHPT, btnToic, btnIelts;
    private AutoCompleteTextView search;
    private RecyclerView rcvbook;
    private BookAdapter mbookAdapter;
    private List<Book> mListBook;
    private List<String> mListBookKeys; // Danh sách khóa
    private List<Button> buttonList; // Danh sách để quản lý các nút
    private String currentCategory = "All"; // Lưu danh mục hiện tại
    private ImageView giohang, ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        btnall = findViewById(R.id.btnall);
        btnTieuHoc = findViewById(R.id.btnTieuHoc);
        btnTHCS = findViewById(R.id.btnTHCS);
        btnTHPT = findViewById(R.id.btnTHPT);
        btnToic = findViewById(R.id.btnToic);
        btnIelts = findViewById(R.id.btnIelts);
        search = findViewById(R.id.search);
        giohang = findViewById(R.id.giohang);
        rcvbook = findViewById(R.id.rcvbook);
        ivBack = findViewById(R.id.ivback);

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
        mbookAdapter = new BookAdapter(this, mListBook, mListBookKeys);
        rcvbook.setAdapter(mbookAdapter);

        // Đặt trạng thái ban đầu: nút "Tất cả" màu xanh
        setButtonSelected(btnall);

        // Lấy danh sách sách và hiển thị "Tất cả" khi dữ liệu sẵn sàng
        getListBook();

        ivBack.setOnClickListener(v->finish());

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
        search.setFocusable(false);
        search.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });



        giohang.setOnClickListener(view ->{
            Intent intent = new Intent(HomeActivity.this, GiohangActivity.class);
            startActivity(intent);
        });

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
                Toast.makeText(HomeActivity.this, "Lỗi khi lấy danh sách sách: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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