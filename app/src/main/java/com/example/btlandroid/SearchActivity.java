package com.example.btlandroid;

import static java.util.Locale.filter;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlandroid.Book.Book;
import com.example.btlandroid.Book.SearchBookAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private AutoCompleteTextView search;
    private RecyclerView rvSearch;
    private List<String> mListBookKeys;
    private SearchBookAdapter mbookAdapter;
    private List<Book> mListBook;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        search = findViewById(R.id.search);
        rvSearch = findViewById(R.id.rvSearch);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvSearch.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rvSearch.addItemDecoration(dividerItemDecoration);

        mListBook = new ArrayList<>();
        mListBookKeys = new ArrayList<>();
        mbookAdapter = new SearchBookAdapter(this,mListBook, mListBookKeys);

        rvSearch.setAdapter(mbookAdapter);

        getListBook();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            private void filter(String text) {
                List<Book> filteredList = new ArrayList<>();
                for (Book book : mListBook) {
                    if (book.getName() != null && book.getName().toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(book);
                    }
                }

                mbookAdapter.setFilteredList(filteredList);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void getListBook(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("book");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Book book = dataSnapshot.getValue(Book.class);
                    mListBook.add(book);
                }

                mbookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchActivity.this, "Get list book fail!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
