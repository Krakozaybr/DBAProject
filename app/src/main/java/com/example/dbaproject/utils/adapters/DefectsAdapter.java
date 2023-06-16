package com.example.dbaproject.utils.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dbaproject.R;
import com.example.dbaproject.api.models.processed_data.AbstractProcessedData;
import com.example.dbaproject.databinding.DefectCardBinding;

import java.util.List;

public class DefectsAdapter extends RecyclerView.Adapter<DefectsAdapter.ViewHolder> {

    private List<AbstractProcessedData.Shape.Defect> defects;
    private AppCompatActivity activity;
    private LayoutInflater inflater;

    public DefectsAdapter(AppCompatActivity activity, List<AbstractProcessedData.Shape.Defect> defects) {
        this.defects = defects;
        this.activity = activity;
        this.inflater = LayoutInflater.from(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DefectCardBinding binding = DefectCardBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setDefect(defects.get(position));
    }

    @Override
    public int getItemCount() {
        return defects.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        DefectCardBinding binding;
        AbstractProcessedData.Shape.Defect defect;

        public ViewHolder(DefectCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setDefect(AbstractProcessedData.Shape.Defect defect){
            this.defect = defect;
            fillData();
        }

        private void fillData(){
            binding.defectName.setText(
                    String.format(
                            "%s: %s",
                            activity.getString(R.string.name),
                            defect.name
                    )
            );
            binding.defectProbability.setText(
                    String.format(
                            "%s: %s%%",
                            activity.getString(R.string.probability),
                            ((int)(defect.score * 1000)) / 10.0 + ""
                    )
            );
        }
    }
}
