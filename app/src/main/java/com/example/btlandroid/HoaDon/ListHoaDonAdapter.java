package com.example.btlandroid.HoaDon;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btlandroid.HoadonActivity;
import com.example.btlandroid.R;

import java.text.DecimalFormat;
import java.util.List;

public class ListHoaDonAdapter extends RecyclerView.Adapter<ListHoaDonAdapter.HoaDonViewHolder> {

    private Context context;
    private List<HoaDon> orderList;

    public ListHoaDonAdapter(Context context, List<HoaDon> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public HoaDonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_listhoadon, parent, false);
        return new HoaDonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoaDonViewHolder holder, int position) {
        HoaDon order = orderList.get(position);

        // Gán giá trị cho các TextView
        holder.tenNguoiDung.setText(order.getTenNguoiDung());
        holder.tvAddress.setText(order.getAddress());
        holder.tvSDTKH.setText(order.getSoDienThoai());
        holder.tvValueNgayDat.setText(order.getNgayDat());
        holder.tvValueNgayGiao.setText(order.getNgayGiao());
        holder.tvValueTrangThai.setText(order.getTrangThai());

        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedPrice = formatter.format(order.getTotalMoney());
        holder.tvValueTongTien.setText(formattedPrice + " đ");

        // Thiết lập RecyclerView sản phẩm
        com.example.btlandroid.HoaDon.HoaDonAdapter hoadonAdapter = new com.example.btlandroid.HoaDon.HoaDonAdapter(context, order.getDanhSachSanPham());
        holder.recyclerViewSanPham.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerViewSanPham.setAdapter(hoadonAdapter);

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HoadonActivity.class); // Sửa thành HoadonActivity
            intent.putExtra("order_id", order.getId());
            intent.putExtra("tenNguoiDung", order.getTenNguoiDung());
            intent.putExtra("address", order.getAddress());
            intent.putExtra("soDienThoai", order.getSoDienThoai());
            intent.putExtra("totalMoney", order.getTotalMoney());
            intent.putExtra("trangThai", order.getTrangThai());
            intent.putExtra("ngayDat", order.getNgayDat());
            intent.putExtra("ngayGiao", order.getNgayGiao());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        Log.d("ListHoaDonAdapter", "Item count: " + orderList.size());
        return orderList.size();
    }

    static class HoaDonViewHolder extends RecyclerView.ViewHolder {
        private TextView tenNguoiDung, tvAddress, tvSDTKH, tvValueTongTien, tvValueTrangThai, tvValueNgayDat, tvValueNgayGiao;
        private RecyclerView recyclerViewSanPham;

        public HoaDonViewHolder(@NonNull View itemView) {
            super(itemView);
            tenNguoiDung = itemView.findViewById(R.id.tvTenKH);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvSDTKH = itemView.findViewById(R.id.tvSDTKH);
            tvValueTongTien = itemView.findViewById(R.id.tvValueTongTien);
            tvValueTrangThai = itemView.findViewById(R.id.tvValueTrangThai);
            tvValueNgayDat = itemView.findViewById(R.id.tvValueNgayDat);
            tvValueNgayGiao = itemView.findViewById(R.id.tvValueNgayGiao);
            recyclerViewSanPham = itemView.findViewById(R.id.recyclerViewSanPham);
        }
    }
}