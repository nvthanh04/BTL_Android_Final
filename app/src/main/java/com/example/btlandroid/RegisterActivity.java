package com.example.btlandroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.btlandroid.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText email,password, name, phone;
    private Button btnregister;
    private FirebaseAuth mAuth;
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        btnregister = findViewById(R.id.btnregister);

        btnregister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                register();
            }
        });
    }

    private void register() {
        String mail,pass, Name, Phone;
        mail = email.getText().toString();
        pass = password.getText().toString();
        Name = name.getText().toString();
        Phone = phone.getText().toString();

        // Kiểm tra nếu trường nào bị trống
        if (TextUtils.isEmpty(mail)) {
            Toast.makeText(this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Name)) {
            Toast.makeText(this, "Vui lòng nhập họ và tên!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Phone)) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đăng ký tài khoản
        mAuth.createUserWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Cập nhật tên hiển thị trong Firebase Authentication
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(Name) // Cập nhật họ tên
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Log.d("FirebaseAuth", "Tên người dùng đã được cập nhật.");
                                        }
                                    });

                            // Lưu thêm thông tin vào Firebase Database
                            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");
                            String userId = user.getUid();

                            User userInfo = new User(userId, Name, mail, Phone);
                            databaseRef.child(userId).setValue(userInfo)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                            Log.d("FirebaseAuth", "Thông tin người dùng đã được lưu vào Database.");
                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                        } else {
                                            Log.e("FirebaseAuth", "Lỗi lưu thông tin: " + dbTask.getException().getMessage());
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
