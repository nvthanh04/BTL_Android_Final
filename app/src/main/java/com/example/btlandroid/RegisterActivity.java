package com.example.btlandroid;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.btlandroid.Model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, nameEditText, phoneEditText, passwordEditText;
    private Button registerButton;
    private TextView backTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Ánh xạ các view từ XML
        emailEditText = findViewById(R.id.email);
        nameEditText = findViewById(R.id.name);
        phoneEditText = findViewById(R.id.phone);
        passwordEditText = findViewById(R.id.password);
        registerButton = findViewById(R.id.btnregister);
        backTextView = findViewById(R.id.textView2);

        // Xử lý sự kiện nút Đăng ký
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Xử lý sự kiện nút Back
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quay lại màn hình trước
            }
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String role = "user"; // Vai trò mặc định, bạn có thể thay đổi

        // Kiểm tra dữ liệu đầu vào
        if (email.isEmpty() || name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email không hợp lệ");
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        // Đăng ký với Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Lấy ID người dùng từ Firebase
                        String userId = mAuth.getCurrentUser().getUid();

                        // Tạo đối tượng User
                        User user = new User(userId, password, name, email, phone, role);

                        // Lưu thông tin người dùng vào Realtime Database
                        mDatabase.child(userId).setValue(user)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                        finish(); // Quay lại màn hình trước hoặc chuyển sang màn hình khác
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Lỗi khi lưu thông tin: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}