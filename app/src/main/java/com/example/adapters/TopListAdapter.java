package com.example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.model.User;
import com.example.zenly.R;

import java.util.List;

public class TopListAdapter extends RecyclerView.Adapter<TopListAdapter.MyViewHolder> {
private Context context;
private List<User> list=null;
    public TopListAdapter(Context context,List<User> list) {
        this.context=context;
        this.list=list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_top_list,null,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User user = list.get(position);
        holder.tvIndex.setText(position+1+"");
        holder.tvStep.setText(user.getStep().toString());
        holder.tvUserName.setText(user
                .getUsername());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView tvIndex,tvUserName,tvStep;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex=itemView.findViewById(R.id.tv_index);
            tvUserName=itemView.findViewById(R.id.tv_name);
            tvStep=itemView.findViewById(R.id.tv_step);
        }
    }
}
