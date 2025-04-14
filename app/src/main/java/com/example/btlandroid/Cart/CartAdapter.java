package com.example.btlandroid.Cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btlandroid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<Cart> cartList;
    private Context context;
    private OnCartChangeListener cartChangeListener;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    public CartAdapter(Context context, List<Cart> cartList, OnCartChangeListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.cartChangeListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_giohang, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart cart = cartList.get(position);
        if (cart == null) return;

        holder.name.setText(cart.getTen());
        holder.price.setText(cart.getGia() + " đ");
        holder.amount.setText(String.valueOf(cart.getSoLuong()));
        Glide.with(context).load(cart.getImage()).into(holder.image);

        // Xử lý nút tăng số lượng
        holder.tvPlus.setOnClickListener(v -> {
            int newAmount = cart.getSoLuong() + 1;
            updateCartQuantity(cart, newAmount);
        });

        // Xử lý nút giảm số lượng
        holder.tvMinus.setOnClickListener(v -> {
            int newAmount = cart.getSoLuong() - 1;
            if (newAmount < 1) {
                // Xóa sản phẩm nếu số lượng nhỏ hơn 1
                deleteCartItem(cart);
            } else {
                updateCartQuantity(cart, newAmount);
            }
        });

        // Xử lý nút xóa
        holder.delete.setOnClickListener(v -> deleteCartItem(cart));
    }

    private void updateCartQuantity(Cart cart, int newAmount) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("giohang")
                .child(userId)
                .child(cart.getId());

        cart.setSoLuong(newAmount);
        cartRef.setValue(cart)
                .addOnSuccessListener(unused -> {
                    cartChangeListener.onCartChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi cập nhật số lượng", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteCartItem(Cart cart) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(context, "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("giohang")
                .child(userId)
                .child(cart.getId());

        cartRef.removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Đã xóa " + cart.getTen() + " khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                    cartChangeListener.onCartChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return cartList != null ? cartList.size() : 0;
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        private TextView name, price, tvMinus, tvPlus;
        private ImageView image, delete;
        private EditText amount;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            tvMinus = itemView.findViewById(R.id.tvminus);
            tvPlus = itemView.findViewById(R.id.tvplus);
            delete = itemView.findViewById(R.id.idDelete);
            image = itemView.findViewById(R.id.image);
            amount = itemView.findViewById(R.id.amount);
        }
    }
}