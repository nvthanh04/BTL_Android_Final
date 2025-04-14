package com.example.btlandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.btlandroid.R;

public class SanPhamActivity extends AppCompatActivity {
    private TextView idTVName, idTVPrice, tvMoTa;
    private ImageView idIVSSImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sanpham);

        idTVName = findViewById(R.id.idTVName);
        idTVPrice = findViewById(R.id.idTVPrice);
        tvMoTa = findViewById(R.id.tvMoTa);
        idIVSSImage = findViewById(R.id.idIVSSImage);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        float price = getIntent().getFloatExtra("price", 0f);
        String description = intent.getStringExtra("note");
        String imageUrl = intent.getStringExtra("image");

        idTVName.setText(name);
        idTVPrice.setText(price + " đ");
        tvMoTa.setText(description);

        Glide.with(this).load(imageUrl).into(idIVSSImage); // load ảnh từ URL
    }
}
