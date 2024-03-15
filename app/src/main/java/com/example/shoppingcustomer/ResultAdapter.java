package com.example.shoppingcustomer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppingcustomer.databinding.ProductAddingListBinding;
import com.example.shoppingcustomer.model.Product;

import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultViewHolder> {
    private List<Product> resultList;

    public ResultAdapter(List<Product> resultList) {
        this.resultList = resultList;
    }

    public void setProductList(List<Product> resultList) {
        this.resultList = resultList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ProductAddingListBinding binding = ProductAddingListBinding.inflate(layoutInflater, parent, false);
        return new ResultViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        Product product = resultList.get(position);
        holder.binding.setProduct(product);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return resultList.size();
     }

    public static class ResultViewHolder extends RecyclerView.ViewHolder {
        private final ProductAddingListBinding binding;

        public ResultViewHolder(@NonNull ProductAddingListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }


    }
}
