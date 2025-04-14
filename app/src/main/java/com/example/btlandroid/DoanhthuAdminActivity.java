package com.example.btlandroid;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoanhthuAdminActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText etDateBD, etDateKT;
    private Button btnDoanhthu;
    private TextView tvTongdoanhthu;
    private ImageView ivBack;
    private SimpleDateFormat dateFormat;
    private ListHoaDonAdapter hoadonAdapter;
    private List<HoaDon> danhSachHoaDon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doanhthu_admin);

        etDateBD = findViewById(R.id.etDateBD);
        etDateKT = findViewById(R.id.etDateKT);
        btnDoanhthu = findViewById(R.id.btnDoanhthu);
        tvTongdoanhthu = findViewById(R.id.tvTongdoanhthu);
        recyclerView = findViewById(R.id.recyclerDoanhthu);
        ivBack = findViewById(R.id.ivBack);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        danhSachHoaDon = new ArrayList<>();
        hoadonAdapter = new ListHoaDonAdapter(this, danhSachHoaDon);
        recyclerView.setAdapter(hoadonAdapter);

        // Nhận orderId từ Intent (nếu có)
        Intent intent = getIntent();
        String orderId = intent.getStringExtra("order_id");
        if (orderId != null) {
            Log.d("DoanhthuAdminActivity", "Received orderId: " + orderId);
            loadNewOrderFromFirebase(orderId);
        } else {
            Log.d("DoanhthuAdminActivity", "No orderId received from Intent");
        }

        // Tải danh sách đơn hàng từ Firebase
        loadOrdersFromFirebase();

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        etDateBD.setOnClickListener(v -> showDatePickerDialog(etDateBD));
        etDateKT.setOnClickListener(v -> showDatePickerDialog(etDateKT));

        ivBack.setOnClickListener(v -> {
            Intent backIntent = new Intent(DoanhthuAdminActivity.this, HomeAdminActivity.class);
            startActivity(backIntent);
            overridePendingTransition(0, 0);
            finish();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.thanhdieuhuong);
        bottomNavigationView.setSelectedItemId(R.id.nav_doanhthu);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent homeIntent = new Intent(DoanhthuAdminActivity.this, HomeAdminActivity.class);
                startActivity(homeIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_doanhthu) {
                return true;
            } else if (itemId == R.id.nav_person) {
                Intent personIntent = new Intent(DoanhthuAdminActivity.this, TaikhoanAdminActivity.class);
                startActivity(personIntent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        btnDoanhthu.setOnClickListener(v -> {
            String dateBD = etDateBD.getText().toString();
            String dateKT = etDateKT.getText().toString();
            if (dateBD.isEmpty() || dateKT.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn đầy đủ ngày", Toast.LENGTH_SHORT).show();
            } else {
                calculateTotalRevenue(dateBD, dateKT);
            }
        });
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    editText.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void calculateTotalRevenue(String dateBD, String dateKT) {
        try {
            Date startDate = dateFormat.parse(dateBD);
            Date endDate = dateFormat.parse(dateKT);

            if (endDate.before(startDate)) {
                Toast.makeText(this, "Ngày kết thúc phải sau ngày bắt đầu", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference hoaDonRef = FirebaseDatabase.getInstance().getReference("orders");
            hoaDonRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    float totalRevenue = 0;
                    danhSachHoaDon.clear(); // Xóa danh sách cũ

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        HoaDon hoaDon = snapshot.getValue(HoaDon.class);
                        if (hoaDon != null && hoaDon.getNgayDat() != null) {
                            hoaDon.setId(snapshot.getKey());
                            try {
                                Date ngayDat = dateFormat.parse(hoaDon.getNgayDat());
                                if ((ngayDat.equals(startDate) || ngayDat.after(startDate)) &&
                                        (ngayDat.equals(endDate) || ngayDat.before(endDate))) {
                                    totalRevenue += hoaDon.getTotalMoney();
                                    danhSachHoaDon.add(hoaDon); // Chỉ thêm vào danh sách nếu hợp lệ
                                }
                            } catch (ParseException ignored) {}
                        }
                    }

                    tvTongdoanhthu.setText(String.format("Doanh thu: %.0f VNĐ", totalRevenue));
                    sortHoaDonByDate(); // Sắp xếp lại danh sách
                    hoadonAdapter.notifyDataSetChanged(); // Cập nhật giao diện
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(DoanhthuAdminActivity.this, "Lỗi khi lấy dữ liệu: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ParseException e) {
            Toast.makeText(this, "Định dạng ngày không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadNewOrderFromFirebase(String orderId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        orderRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                HoaDon newOrder = task.getResult().getValue(HoaDon.class);
                if (newOrder != null) {
                    newOrder.setId(orderId);
                    Log.d("DoanhthuAdminActivity", "Loaded new order: " + newOrder.toString());
                    if (!isOrderAlreadyInList(newOrder)) {
                        danhSachHoaDon.add(0, newOrder);
                        sortHoaDonByDate();
                        hoadonAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("DoanhthuAdminActivity", "New order already in list, orderId: " + orderId);
                    }
                } else {
                    Log.e("DoanhthuAdminActivity", "New order is null for orderId: " + orderId);
                    Toast.makeText(this, "Lỗi: Không lấy được đơn hàng mới", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("DoanhthuAdminActivity", "Failed to fetch order with ID: " + orderId);
                Toast.makeText(this, "Lỗi: Không lấy được đơn hàng mới", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrdersFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("DoanhthuAdminActivity", "Current user is null, cannot load orders");
            Toast.makeText(this, "Bạn cần đăng nhập để xem đơn hàng", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        String emailHienTai = currentUser.getEmail();
        if (emailHienTai == null) {
            Log.e("DoanhthuAdminActivity", "Current user's email is null");
            Toast.makeText(this, "Lỗi: Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }
        Log.d("DoanhthuAdminActivity", "Loading orders for email: " + emailHienTai);
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String orderIdFromIntent = getIntent().getStringExtra("order_id");
                if (orderIdFromIntent == null) {
                    danhSachHoaDon.clear();
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String orderId = snapshot.getKey();
                    HoaDon order = snapshot.getValue(HoaDon.class);
                    if (order != null) {
                        if (order.getEmail() == null) {
                            Log.w("DoanhthuAdminActivity", "Order has null email, orderId: " + orderId);
                            continue;
                        }
                        // Không lọc theo email để admin thấy tất cả đơn hàng
                        order.setId(orderId);
                        if (!isOrderAlreadyInList(order)) {
                            danhSachHoaDon.add(order);
                            Log.d("DoanhthuAdminActivity", "Added order, orderId: " + orderId + ", details: " + order.toString());
                        } else {
                            Log.d("DoanhthuAdminActivity", "Skipped duplicate order, orderId: " + orderId);
                        }
                    } else {
                        Log.w("DoanhthuAdminActivity", "Order is null for orderId: " + orderId);
                    }
                }

                if (danhSachHoaDon.isEmpty()) {
                    Log.d("DoanhthuAdminActivity", "No orders found");
                    Toast.makeText(DoanhthuAdminActivity.this, "Chưa có đơn hàng nào", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("DoanhthuAdminActivity", "Loaded " + danhSachHoaDon.size() + " orders");
                    sortHoaDonByDate();
                }

                hoadonAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DoanhthuAdminActivity", "Error loading orders: " + databaseError.getMessage());
                Toast.makeText(DoanhthuAdminActivity.this, "Lỗi khi tải đơn hàng: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortHoaDonByDate() {
        Collections.sort(danhSachHoaDon, new Comparator<HoaDon>() {
            @Override
            public int compare(HoaDon o1, HoaDon o2) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                try {
                    Date date1 = sdf.parse(o1.getNgayDat());
                    Date date2 = sdf.parse(o2.getNgayDat());
                    return date2.compareTo(date1); // Sắp xếp giảm dần
                } catch (ParseException e) {
                    Log.e("DoanhthuAdminActivity", "Error parsing date: " + e.getMessage());
                    return 0;
                }
            }
        });
    }

    private boolean isOrderAlreadyInList(HoaDon newOrder) {
        if (newOrder.getId() == null) {
            return false;
        }
        for (HoaDon existingOrder : danhSachHoaDon) {
            if (existingOrder.getId() != null && existingOrder.getId().equals(newOrder.getId())) {
                return true;
            }
        }
        return false;
    }
}