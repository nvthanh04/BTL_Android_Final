package com.example.btlandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlandroid.Cart.Cart;
import com.example.btlandroid.Cart.CartAdapter;
import com.example.btlandroid.Model.User;
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
    private TextView tongTien, tvmua;
    private ImageView ivHome, ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giohang);

        recyclerView = findViewById(R.id.rvproduct);
        tongTien = findViewById(R.id.tvtien);
        ivHome = findViewById(R.id.ivHome);
        ivBack = findViewById(R.id.ivBack);
        tvmua = findViewById(R.id.tvmua);

        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartList, this::updateTotal);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cartAdapter);

        ivBack.setOnClickListener(v -> finish());

        ivHome.setOnClickListener(v -> {
            startActivity(new Intent(GiohangActivity.this, HomeActivity.class));
            finish();
        });

        loadCartFromFirebase();

        tvmua.setOnClickListener(v -> {
            getCurrentUserFromFirebase(user -> {
                if (user != null) {
                    double totalPrice = calculateTotal();
                    Intent intent = new Intent(GiohangActivity.this, HoadonActivity.class);
                    intent.putExtra("user_id", user.getId());
                    intent.putExtra("cart_list", getCartIds(cartList));
                    intent.putExtra("total_price", totalPrice);
                    startActivity(intent);
                } else {
                    Toast.makeText(GiohangActivity.this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        });
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
                    tvmua.setEnabled(false);
                    tvmua.setAlpha(0.5f);
                } else {
                    tvmua.setEnabled(true);
                    tvmua.setAlpha(1.0f);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GiohangActivity.this, "Lỗi khi tải giỏ hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCurrentUserFromFirebase(UserCallback callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String id = snapshot.child("id").getValue(String.class);
                        String name = snapshot.child("name").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        User currentUser = new User(id, "", name, email, phone,"");
                        callback.onUserLoaded(currentUser);
                    } else {
                        callback.onUserLoaded(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(GiohangActivity.this, "Lỗi khi lấy thông tin người dùng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.onUserLoaded(null);
                }
            });
        } else {
            callback.onUserLoaded(null);
        }
    }

    public interface UserCallback {
        void onUserLoaded(User user);
    }

    private double calculateTotal() {
        double tong = 0;
        for (Cart cart : cartList) {
            Double gia = cart.getGia();
            Integer soLuong = cart.getSoLuong();
            if (gia != null && soLuong != null) {
                tong += gia * soLuong;
            }
        }
        return tong;
    }


    private void updateTotal() {
        double tong = calculateTotal();
        tongTien.setText(String.format("%.0f đ", tong));
    }

    private ArrayList<String> getCartIds(List<Cart> cartList) {
        ArrayList<String> cartIds = new ArrayList<>();
        for (Cart item : cartList) {
            if (item.getId() != null) {
                cartIds.add(item.getId());
            }
        }
        return cartIds;
    }
}
