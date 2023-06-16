package com.example.dbaproject.utils.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dbaproject.R;
import com.example.dbaproject.api.models.processed_data.ProcessedDataCreate;
import com.example.dbaproject.databinding.InsulatorShapeBinding;
import com.example.dbaproject.utils.ExpandableUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.List;

// List of insulators in ProcessedDataBottomSheetDialog
public class InsulatorListAdapter extends RecyclerView.Adapter<InsulatorListAdapter.ViewHolder> {

    // List of insulators dataclasses
    List<ProcessedDataCreate.Shape> shapes;
    AppCompatActivity activity;
    LayoutInflater inflater;
    ViewHolder[] holders;
    // Notify ProcessedDataBottomSheetDialog about changing selected insulators to repaint image
    SelectedShapesChangedListener listener;

    public InsulatorListAdapter(List<ProcessedDataCreate.Shape> shapes, AppCompatActivity activity, SelectedShapesChangedListener listener) {
        this.shapes = shapes;
        this.activity = activity;
        this.inflater = LayoutInflater.from(activity);
        this.holders = new ViewHolder[getItemCount()];
        this.listener = listener;
    }

    public static interface SelectedShapesChangedListener {
        void changed();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        InsulatorShapeBinding binding = InsulatorShapeBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holders[position] = holder;
        holder.setShape(shapes.get(position));
    }

    @Override
    public int getItemCount() {
        return shapes.size();
    }

    public List<ProcessedDataCreate.Shape> getSelectedShapes() {
        List<ProcessedDataCreate.Shape> shapes = new ArrayList<>();
        for (int i = 0; i < holders.length; i++) {
            ViewHolder holder = holders[i];
            if (holder == null || holder.isChecked()) {
                shapes.add(this.shapes.get(i));
            }
        }
        return shapes;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ProcessedDataCreate.Shape shape;
        InsulatorShapeBinding binding;

        public ViewHolder(InsulatorShapeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setShape(ProcessedDataCreate.Shape shape) {
            this.shape = shape;
            fillData();
            addListeners();
        }

        private void fillData() {
            binding.probabilityInsulator.setText(
                    String.format("%s: %s%%",
                            activity.getString(R.string.probability),
                            ((int) (shape.score * 1000)) / 10.0
                    )
            );

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            binding.insulatorExtraInformation.setText(
                    gson.toJson(shape)
            );
            // Init defects
            if (!shape.defects.isEmpty()) {
                DefectsAdapter adapter = new DefectsAdapter(activity, shape.defects);
                binding.defectsList.setAdapter(adapter);
            } else {
                // Don`t show expandable layout and its btn if there is no defects
                binding.shapeDefectsLinearLayout.setVisibility(View.GONE);
                binding.insulatorsDefectsExpandableLayout.setVisibility(View.GONE);
            }
        }

        private void addListeners() {
            // Checkbox in header responsible for selected state of insulator
            binding.insulatorCheckbox.setOnCheckedChangeListener((compoundButton, b) -> listener.changed());
            // Shows body of view holder
            binding.shapeLinearLayout.setOnClickListener((view) ->
                    ExpandableUtils.toggleWithIndicator(
                            binding.insulatorsExpandableLayout,
                            binding.shapeExpandableIndicator
                    )
            );
            binding.shapeDefectsLinearLayout.setOnClickListener((view) ->
                    ExpandableUtils.toggleWithIndicator(
                            binding.insulatorsDefectsExpandableLayout,
                            binding.shapeDefectsExpandableIndicator
                    )
            );
            // Shows JSON representing of request for detailed information
            binding.shapeExtraInformationLinearLayout.setOnClickListener((view) ->
                    ExpandableUtils.toggleWithIndicator(
                            binding.insulatorsExtraInformationExpandableLayout,
                            binding.shapeExtraInformationExpandableIndicator
                    )
            );
        }

        boolean isChecked() {
            return binding.insulatorCheckbox.isChecked();
        }

        public ProcessedDataCreate.Shape getShape() {
            return shape;
        }
    }

}
