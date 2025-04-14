package com.example.btlandroid.HoaDon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btlandroid.Cart.Cart;
import com.example.btlandroid.R;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HoaDonAdapter extends RecyclerView.Adapter<HoaDonAdapter.HoaDonViewHolder> {

    private Context context;
    private List<Cart> cartList;

    public HoaDonAdapter(Context context, List<Cart> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @NonNull
    @Override
    public HoaDonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hoadon, parent, false);
        return new HoaDonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoaDonViewHolder holder, int position) {
        Cart cart = cartList.get(position);
        if (cart == null) return;

        holder.name.setText(cart.getTen());
        holder.quantity.setText("x" + cart.getSoLuong());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedPrice = format.format(cart.getGia() * cart.getSoLuong());
        holder.price.setText(formattedPrice);

        Glide.with(context).load(cart.getImage()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return (cartList != null) ? cartList.size() : 0;
    }

    public class HoaDonViewHolder extends RecyclerView.ViewHolder {
        private TextView name, price, quantity;
        private ImageView image;

        public HoaDonViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            quantity = itemView.findViewById(R.id.quantity);
            image = itemView.findViewById(R.id.image);
        }
    }
}
