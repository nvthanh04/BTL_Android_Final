package com.example.btlandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlandroid.Cart.Cart;
import com.example.btlandroid.HoaDon.HoaDon;
import com.example.btlandroid.HoaDon.HoaDonAdapter;
import com.example.btlandroid.Model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HoadonActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HoaDonAdapter hoaDonAdapter;
    private List<Cart> cartItems;

    private TextView tvAccountName, tvPhoneNumber, tongtien, tvNgayDat, tvNgayGiao, tvEmail;
    private EditText tvAddress;
    private LinearLayout onlinepay;
    private Button btnDatHang;
    private CheckBox cbCOD;
    private ImageView ivBack, ivHome;

    private boolean isPaidOnline = false;
    private static final int REQUEST_CODE_PAYMENT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hoadon);

        // Khởi tạo các view
        recyclerView = findViewById(R.id.recyclerViewItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvAccountName = findViewById(R.id.tvAccountName);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tongtien = findViewById(R.id.tongtien);
        tvNgayDat = findViewById(R.id.tvNgayDat);
        tvNgayGiao = findViewById(R.id.tvNgayGiao);
        onlinepay = findViewById(R.id.onlinepay);
        btnDatHang = findViewById(R.id.btnDathang);
        cbCOD = findViewById(R.id.cbCOD);
        tvEmail = findViewById(R.id.tvEmail);
        ivBack = findViewById(R.id.ivBack);
        ivHome = findViewById(R.id.ivHome);

        // Khởi tạo danh sách giỏ hàng
        cartItems = new ArrayList<>();
        hoaDonAdapter = new HoaDonAdapter(this, cartItems);
        recyclerView.setAdapter(hoaDonAdapter);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String userId = intent.getStringExtra("user_id");
        ArrayList<String> cartListIds = intent.getStringArrayListExtra("cart_list");
        double totalPrice = intent.getDoubleExtra("total_price", 0.0);

        // Kiểm tra dữ liệu hợp lệ
        if (userId == null || userId.isEmpty()) {
            Log.e("HoadonActivity", "userId is null or empty");
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (cartListIds != null && !cartListIds.isEmpty()) {
            Log.d("HoadonActivity", "Received cartListIds: " + cartListIds.toString());
            getCartItemsFromIds(userId, cartListIds);
        } else {
            Log.e("HoadonActivity", "cartListIds is null or empty");
            Toast.makeText(this, "Không có sản phẩm trong giỏ hàng", Toast.LENGTH_SHORT).show();
        }

        ivBack.setOnClickListener(v->finish());

        ivHome.setOnClickListener(v -> {
            startActivity(new Intent(HoadonActivity.this, HomeActivity.class));
            finish();
        });


        // Lấy thông tin người dùng từ userId
        getUserInfoFromId(userId);

        // Lấy ngày hiện tại
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());
        tvNgayDat.setText(currentDate);

        // Tính ngày giao dự kiến (thêm 3 ngày)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 3);
        String deliveryDate = sdf.format(calendar.getTime());
        tvNgayGiao.setText(deliveryDate);

        // Cập nhật tổng tiền ban đầu
        float tongTienValue = (float) totalPrice + 20000;
        updateTotalPrice(tongTienValue);

        // Sự kiện thanh toán online
        onlinepay.setOnClickListener(v -> {
            float tongTienValueForPayment = 0.0f;
            try {
                String tongTienRaw = tongtien.getText().toString().replaceAll("[^0-9]", "");
                tongTienValueForPayment = Float.parseFloat(tongTienRaw);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            String tenNguoiDung = tvAccountName.getText().toString();
            Intent paymentIntent = new Intent(HoadonActivity.this, ThanhToanOnline.class);
            paymentIntent.putExtra("tenNguoiDung", tenNguoiDung);
            paymentIntent.putExtra("tongTien", tongTienValueForPayment);
            startActivityForResult(paymentIntent, REQUEST_CODE_PAYMENT);
        });

        // Sự kiện đặt hàng
        btnDatHang.setOnClickListener(v -> {
            String tenTaiKhoan = tvAccountName.getText().toString();
            String diaChi = tvAddress.getText().toString();
            String soDienThoai = tvPhoneNumber.getText().toString();
            String ngayDat = tvNgayDat.getText().toString();
            String ngayGiao = tvNgayGiao.getText().toString();
            String email = tvEmail.getText().toString();

            if (diaChi.isEmpty()) {
                Toast.makeText(HoadonActivity.this, "Vui lòng nhập địa chỉ giao hàng.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cartItems.isEmpty()) {
                Toast.makeText(HoadonActivity.this, "Giỏ hàng rỗng, không thể đặt hàng.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy giá trị tổng tiền
            String tongTienString = tongtien.getText().toString();
            String numericString = tongTienString.replaceAll("[^0-9]", "");
            float tongTien = 0.0f;
            try {
                tongTien = Float.parseFloat(numericString);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            // Xác định trạng thái thanh toán
            String trangThaiThanhToan;
            if (isPaidOnline) {
                trangThaiThanhToan = "Đã thanh toán trực tuyến";
            } else if (cbCOD.isChecked()) {
                trangThaiThanhToan = "Chưa thanh toán tiền";
            } else {
                trangThaiThanhToan = "Đã thanh toán";
            }

            // Tạo đơn hàng
            HoaDon order = new HoaDon(diaChi, tongTien, trangThaiThanhToan,
                    tenTaiKhoan, soDienThoai, ngayDat, ngayGiao, email, cartItems);

            saveOrderToDatabase(order);
        });
    }

    private void getUserInfoFromId(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    tvAccountName.setText(user.getName());
                    tvPhoneNumber.setText(user.getPhone());
                    tvEmail.setText(user.getEmail());
                } else {
                    Log.e("HoadonActivity", "User data is null for userId: " + userId);
                    Toast.makeText(this, "Lỗi: Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("HoadonActivity", "Failed to fetch user data for userId: " + userId);
                Toast.makeText(this, "Lỗi: Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCartItemsFromIds(String userId, ArrayList<String> cartListIds) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("giohang").child(userId);
        AtomicInteger itemsToFetch = new AtomicInteger(cartListIds.size());

        if (itemsToFetch.get() == 0) {
            Log.e("HoadonActivity", "No items to fetch, cartListIds is empty");
            Toast.makeText(this, "Không có sản phẩm để hiển thị", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String productId : cartListIds) {
            if (productId != null && !productId.isEmpty()) {
                Log.d("HoadonActivity", "Fetching product with ID: " + productId + " for user: " + userId);
                cartRef.child(productId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Cart cartItem = task.getResult().getValue(Cart.class);
                        if (cartItem != null) {
                            cartItem.setId(productId); // Đảm bảo ID được gán
                            cartItems.add(cartItem);
                            Log.d("HoadonActivity", "Added product: " + cartItem.toString());
                        } else {
                            Log.e("HoadonActivity", "Cart item is null for productId: " + productId);
                        }
                    } else {
                        Log.e("HoadonActivity", "Failed to fetch product with ID: " + productId + " for user: " + userId);
                        Toast.makeText(this, "Không lấy được sản phẩm ID: " + productId, Toast.LENGTH_SHORT).show();
                    }

                    // Kiểm tra khi tất cả sản phẩm đã được tải
                    if (itemsToFetch.decrementAndGet() == 0) {
                        if (cartItems.isEmpty()) {
                            Log.e("HoadonActivity", "No products were fetched");
                            Toast.makeText(this, "Không tìm thấy sản phẩm nào trong giỏ hàng", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("HoadonActivity", "All products fetched, updating adapter");
                            hoaDonAdapter.notifyDataSetChanged();
                        }
                    }
                });
            } else {
                Log.e("HoadonActivity", "Invalid productId: null or empty");
                if (itemsToFetch.decrementAndGet() == 0) {
                    if (cartItems.isEmpty()) {
                        Log.e("HoadonActivity", "No products were fetched");
                        Toast.makeText(this, "Không tìm thấy sản phẩm nào trong giỏ hàng", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("HoadonActivity", "All products fetched, updating adapter");
                        hoaDonAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    // Xử lý kết quả từ ThanhToanOnline
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PAYMENT && resultCode == RESULT_OK) {
                isPaidOnline = true; // Đánh dấu đã thanh toán online
                updateTotalPrice(0.0f); // Cập nhật tổng tiền về 0
                cbCOD.setChecked(false); // Bỏ chọn COD nếu có
            } else {
                Log.e("HoadonActivity", "Payment result data is null Suppl");
                Toast.makeText(this, "Lỗi: Không nhận được dữ liệu thanh toán", Toast.LENGTH_SHORT).show();
            }
    }

    // Hàm cập nhật tổng tiền
    private void updateTotalPrice(float tongTien) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedPrice = formatter.format(tongTien);
        tongtien.setText("Tổng tiền: " + formattedPrice + " đ");
    }

    // Lưu đơn hàng vào Firebase
    private void saveOrderToDatabase(HoaDon order) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        String orderId = ordersRef.push().getKey(); // Tạo ID mới
        ordersRef.child(orderId).setValue(order)
                .addOnSuccessListener(aVoid -> {
                    Log.d("HoadonActivity", "Order saved successfully: " + orderId);
                    Toast.makeText(getApplicationContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

                    // Gửi order sang ListHoaDonActivity
                    Intent intent = new Intent(HoadonActivity.this, ListHoaDonActivity.class);
                    intent.putExtra("order_id", orderId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("HoadonActivity", "Failed to save order: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Lỗi khi lưu đơn hàng", Toast.LENGTH_SHORT).show();
                });
    }
}