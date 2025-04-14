package com.example.btlandroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlandroid.Book.Book;
import com.example.btlandroid.Book.SearchBookAdminAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchAdminActivity extends AppCompatActivity {
    private AutoCompleteTextView searchAdmin;
    private RecyclerView rvSearchAdmin;
    private SearchBookAdminAdapter bookAdapter;
    private List<Book> mListBook;
    private List<String> mListBookKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_admin);

        // Khởi tạo các view
        searchAdmin = findViewById(R.id.searchAdmin);
        rvSearchAdmin = findViewById(R.id.rvSearchAdmin);
        TextView back = findViewById(R.id.back);
        ImageView ivHome = findViewById(R.id.ivHome);
        ImageView ivAdd = findViewById(R.id.ivAdd);

        // Thiết lập RecyclerView với LinearLayoutManager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvSearchAdmin.setLayoutManager(linearLayoutManager);

        // Khởi tạo danh sách và adapter
        mListBook = new ArrayList<>();
        mListBookKeys = new ArrayList<>();
        bookAdapter = new SearchBookAdminAdapter(this, mListBook, mListBookKeys);
        rvSearchAdmin.setAdapter(bookAdapter);

        // Lấy danh sách sách từ Firebase
        getListBook();

        // Sự kiện nhấn nút Back
        back.setOnClickListener(v -> finish());

        // Sự kiện nhấn nút Home
        ivHome.setOnClickListener(v -> finish());

        // Sự kiện nhấn nút Add
        ivAdd.setOnClickListener(v -> {
            Intent intent = new Intent(SearchAdminActivity.this, AddAdminActivity.class);
            startActivity(intent);
        });

        // Tìm kiếm động
        searchAdmin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterBooks(s.toString().trim().toLowerCase());
            }
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
                    if (book != null) {
                        mListBook.add(book);
                        mListBookKeys.add(key);
                    }
                }
                bookAdapter.setBooks(mListBook, mListBookKeys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchAdminActivity.this, "Lỗi khi lấy danh sách sách: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterBooks(String query) {
        List<Book> filteredList = new ArrayList<>();
        List<String> filteredKeys = new ArrayList<>();

        for (int i = 0; i < mListBook.size(); i++) {
            Book book = mListBook.get(i);
            if (book.getName().toLowerCase().contains(query)) {
                filteredList.add(book);
                filteredKeys.add(mListBookKeys.get(i));
            }
        }

        bookAdapter.setBooks(filteredList, filteredKeys);
    }
}