package com.example.btlandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btlandroid.Model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TaikhoanAdminActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone;
    private TextInputEditText tvPassword;
    private Button btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taikhoan_admin);

        // Khởi tạo các view
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvPassword = findViewById(R.id.tvPassword);
        btnLogout = findViewById(R.id.btnLogout);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Thiết lập BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.thanhdieuhuong);
        bottomNavigationView.setSelectedItemId(R.id.nav_person);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(TaikhoanAdminActivity.this, HomeAdminActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_doanhthu) {
                startActivity(new Intent(TaikhoanAdminActivity.this, DoanhthuAdminActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_person) {
                return true;
            }
            return false;
        });

        // Lấy thông tin người dùng từ Firebase
        loadUserInfo();

        // Xử lý sự kiện nút đăng xuất
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(TaikhoanAdminActivity.this, LoginActivity.class));
            finish();
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        tvName.setText(user.getName());
                        tvEmail.setText(user.getEmail());
                        tvPhone.setText(user.getPhone());
                        tvPassword.setText(user.getPassword());
                    } else {
                        Toast.makeText(TaikhoanAdminActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(TaikhoanAdminActivity.this, "Lỗi khi lấy dữ liệu: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(TaikhoanAdminActivity.this, LoginActivity.class));
            finish();
        }
    }
}