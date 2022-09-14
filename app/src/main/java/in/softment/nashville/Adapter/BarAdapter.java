package in.softment.nashville.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import in.softment.nashville.BarDetailsActivity;
import in.softment.nashville.R;

public class BarAdapter extends RecyclerView.Adapter<BarAdapter.MyViewHolder> {

    private Context context;


    public BarAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public BarAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bar_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull BarAdapter.MyViewHolder holder, int position) {
            holder.setIsRecyclable(false);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, BarDetailsActivity.class));
                }
            });
    }

    @Override
    public int getItemCount() {
        return 50;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private View view;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }
    }
}
