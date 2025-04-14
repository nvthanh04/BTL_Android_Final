package com.example.btlandroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.btlandroid.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText mail,pass;
    private Button btnlogin;
    private FirebaseAuth mAuth;
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        mail = findViewById(R.id.mail);
        pass = findViewById(R.id.pass);
        btnlogin = findViewById(R.id.btnlogin);

        btnlogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                login();
            }
        });
    }

    private void login() {
        String email, password;
        email = mail.getText().toString();
        password = pass.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Vui lòng nhập email!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Vui lòng nhập mật khẩu!!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    checkUserRole();
                }
                else{
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void checkUserRole() {
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getRole() != null) {
                        Intent intent;
                        if (user.getRole().equals("ADMIN")) {
                            intent = new Intent(LoginActivity.this, HomeAdminActivity.class);
                        } else if (user.getRole().equals("CUSTOMER")) {
                            intent = new Intent(LoginActivity.this, HomeActivity.class);
                        } else {
                            Toast.makeText(LoginActivity.this, "Vai trò không hợp lệ!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!!", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Dữ liệu người dùng không tồn tại!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
