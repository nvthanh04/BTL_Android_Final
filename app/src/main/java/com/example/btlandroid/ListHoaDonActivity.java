package com.example.btlandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlandroid.HoaDon.HoaDon;
import com.example.btlandroid.HoaDon.ListHoaDonAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListHoaDonActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ListHoaDonAdapter hoadonAdapter;
    private List<HoaDon> danhSachHoaDon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listhoadon);

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.rvhoadon);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Khởi tạo danh sách đơn hàng và adapter
        danhSachHoaDon = new ArrayList<>();
        hoadonAdapter = new ListHoaDonAdapter(this, danhSachHoaDon);
        recyclerView.setAdapter(hoadonAdapter);

        // Nhận orderId từ Intent (nếu có)
        Intent intent = getIntent();
        String orderId = intent.getStringExtra("order_id");
        if (orderId != null) {
            Log.d("ListHoaDonActivity", "Received orderId: " + orderId);
            loadNewOrderFromFirebase(orderId);
        } else {
            Log.d("ListHoaDonActivity", "No orderId received from Intent");
        }

        // Tải danh sách đơn hàng từ Firebase
        loadOrdersFromFirebase();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Đặt Home là mục mặc định

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intentHome = new Intent(ListHoaDonActivity.this, HomeActivity.class);
                startActivity(intentHome);
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_doanhthu) {
                // Chuyển sang DoanhthuAdminActivity
                return true;
            } else if (itemId == R.id.nav_person) {
                Intent intentPerson = new Intent(ListHoaDonActivity.this, TaikhoanActivity.class);
                startActivity(intentPerson);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadNewOrderFromFirebase(String orderId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        orderRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                HoaDon newOrder = task.getResult().getValue(HoaDon.class);
                if (newOrder != null) {
                    newOrder.setId(orderId);
                    Log.d("ListHoaDonActivity", "Loaded new order: " + newOrder.toString());
                    if (!isOrderAlreadyInList(newOrder)) {
                        danhSachHoaDon.add(0, newOrder);
                        sortHoaDonByDate();
                        hoadonAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("ListHoaDonActivity", "New order already in list, orderId: " + orderId);
                    }
                } else {
                    Log.e("ListHoaDonActivity", "New order is null for orderId: " + orderId);
                    Toast.makeText(this, "Lỗi: Không lấy được đơn hàng mới", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("ListHoaDonActivity", "Failed to fetch order with ID: " + orderId);
                Toast.makeText(this, "Lỗi: Không lấy được đơn hàng mới", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrdersFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("ListHoaDonActivity", "Current user is null, cannot load orders");
            Toast.makeText(this, "Bạn cần đăng nhập để xem đơn hàng", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        String emailHienTai = currentUser.getEmail();
        if (emailHienTai == null) {
            Log.e("ListHoaDonActivity", "Current user's email is null");
            Toast.makeText(this, "Lỗi: Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        Log.d("ListHoaDonActivity", "Loading orders for email: " + emailHienTai);
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Intent intent = getIntent();
                if (intent.getStringExtra("order_id") == null) {
                    danhSachHoaDon.clear();
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String orderId = snapshot.getKey();
                    HoaDon order = snapshot.getValue(HoaDon.class);
                    if (order != null) {
                        if (order.getEmail() == null) {
                            Log.w("ListHoaDonActivity", "Order has null email, orderId: " + orderId);
                            continue;
                        }
                        if (order.getEmail().equals(emailHienTai)) {
                            order.setId(orderId);
                            if (!isOrderAlreadyInList(order)) {
                                danhSachHoaDon.add(order);
                                Log.d("ListHoaDonActivity", "Added order, orderId: " + orderId + ", details: " + order.toString());
                            } else {
                                Log.d("ListHoaDonActivity", "Skipped duplicate order, orderId: " + orderId);
                            }
                        } else {
                            Log.d("ListHoaDonActivity", "Skipped order with different email: " + order.getEmail() + ", orderId: " + orderId);
                        }
                    } else {
                        Log.w("ListHoaDonActivity", "Order is null for orderId: " + orderId);
                    }
                }

                if (danhSachHoaDon.isEmpty()) {
                    Log.d("ListHoaDonActivity", "No orders found for user: " + emailHienTai);
                    Toast.makeText(ListHoaDonActivity.this, "Bạn chưa có đơn hàng nào", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("ListHoaDonActivity", "Loaded " + danhSachHoaDon.size() + " orders");
                    sortHoaDonByDate();
                }

                hoadonAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ListHoaDonActivity", "Error loading orders: " + databaseError.getMessage());
                Toast.makeText(ListHoaDonActivity.this, "Lỗi khi tải đơn hàng: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isOrderAlreadyInList(HoaDon order) {
        if (order.getId() == null) {
            return false;
        }
        for (HoaDon existingOrder : danhSachHoaDon) {
            if (existingOrder.getId() != null && existingOrder.getId().equals(order.getId())) {
                return true;
            }
        }
        return false;
    }

    private void sortHoaDonByDate() {
        Collections.sort(danhSachHoaDon, new Comparator<HoaDon>() {
            @Override
            public int compare(HoaDon o1, HoaDon o2) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                try {
                    Date date1 = sdf.parse(o1.getNgayDat());
                    Date date2 = sdf.parse(o2.getNgayDat());
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    Log.e("ListHoaDonActivity", "Error parsing date: " + e.getMessage());
                    return 0;
                }
            }
        });
    }
}