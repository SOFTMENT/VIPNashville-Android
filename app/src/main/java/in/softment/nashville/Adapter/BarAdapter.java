package in.softment.nashville.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

import in.softment.nashville.BarDetailsActivity;
import in.softment.nashville.MainActivity;
import in.softment.nashville.Model.BarModel;
import in.softment.nashville.R;

public class BarAdapter extends RecyclerView.Adapter<BarAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<BarModel> barModels;
    public BarAdapter(Context context,ArrayList<BarModel> barModels){
        this.context = context;
        this.barModels = barModels;
    }

    @NonNull
    @Override
    public BarAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bar_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull BarAdapter.MyViewHolder holder, int position) {
            holder.setIsRecyclable(false);

             BarModel barModel = barModels.get(position);
             Glide.with(context).load(barModel.image).placeholder(R.drawable.placeholder).into(holder.barImage);
             holder.barName.setText(barModel.name);
             holder.description.setText(barModel.about);
             holder.rating.setText(String.format("%.1f", barModel.rating));
             holder.address.setText(barModel.address);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, BarDetailsActivity.class);
                    intent.putExtra("barmodel",barModel);
                    context.startActivity(intent);
                }
            });
    }

    @Override
    public int getItemCount() {
        if (barModels.size() > 0) {
            ((MainActivity)context).no_bars_ll.setVisibility(View.GONE);
        }
        else {
            ((MainActivity)context).no_bars_ll.setVisibility(View.VISIBLE);
        }
        return barModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private RoundedImageView barImage;
        private TextView barName, address, description, rating;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            this.address = view.findViewById(R.id.address);
            this.barImage = view.findViewById(R.id.barImage);
            this.barName = view.findViewById(R.id.barName);
            this.description = view.findViewById(R.id.description);
            this.rating = view.findViewById(R.id.rating);

        }
    }
}
