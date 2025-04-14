package com.example.btlandroid;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class ThanhToanOnline extends AppCompatActivity {
    private TextView txtTenNguoiDung,price,xacnhan;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pthuc_thanh_toan);

        txtTenNguoiDung = findViewById(R.id.txtTenNguoiDung);
        price = findViewById(R.id.price);
        xacnhan = findViewById(R.id.xacnhan);

        // Nhận dữ liệu kiểu float
        float tongTien = getIntent().getFloatExtra("tongTien", 0.0f);
        String tenNguoiDung = getIntent().getStringExtra("tenNguoiDung");

        // Format lại nếu muốn hiển thị đẹp
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedPrice = formatter.format(tongTien);

        txtTenNguoiDung.setText(tenNguoiDung);
        price.setText(formattedPrice);

        xacnhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ThanhToanOnline.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
