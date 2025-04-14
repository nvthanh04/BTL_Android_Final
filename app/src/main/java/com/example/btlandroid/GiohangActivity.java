package com.example.btlandroid;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlandroid.Cart.Cart;
import com.example.btlandroid.Cart.CartAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GiohangActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<Cart> cartList;
    private TextView tongTien;
    private ImageView ivHome;
    private TextView tvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giohang);

        recyclerView = findViewById(R.id.rvproduct);
        tongTien = findViewById(R.id.tvtien);
        ivHome = findViewById(R.id.ivHome);
        tvBack = findViewById(R.id.tvBack);

        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartList, this::updateTotal);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cartAdapter);

        // Xử lý nút back
        tvBack.setOnClickListener(v -> finish());

        // Xử lý nút home
        ivHome.setOnClickListener(v -> {
            // Quay về HomeActivity
            finish();
        });

        loadCartFromFirebase();
    }

    private void loadCartFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("giohang")
                .child(userId);

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Cart cart = data.getValue(Cart.class);
                    if (cart != null) {
                        cart.setId(data.getKey());
                        cartList.add(cart);
                    }
                }
                cartAdapter.notifyDataSetChanged();
                updateTotal();
                if (cartList.isEmpty()) {
                    Toast.makeText(GiohangActivity.this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GiohangActivity.this, "Lỗi khi tải giỏ hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotal() {
        double tong = 0;
        for (Cart cart : cartList) {
            tong += cart.getGia() * cart.getSoLuong();
        }
        tongTien.setText(String.format("%.0f đ", tong));
    }
}