package com.example.dbaproject.utils.adapters;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dbaproject.R;
import com.example.dbaproject.api.APIHelper;
import com.example.dbaproject.api.APIWrapper;
import com.example.dbaproject.api.models.processed_data.ProcessedData;
import com.example.dbaproject.databinding.ProcessedDataCardBinding;
import com.example.dbaproject.databinding.ProcessedDataDialogBinding;
import com.example.dbaproject.databinding.ProcessingDataCardBinding;
import com.example.dbaproject.utils.DateTimeUtils;
import com.example.dbaproject.utils.ExpandableUtils;
import com.example.dbaproject.utils.PreferenceManager;
import com.example.dbaproject.utils.dialogs.Dialogs;
import com.example.dbaproject.utils.dialogs.ImageDialog;
import com.example.dbaproject.utils.ImageUtils;
import com.example.dbaproject.view_models.DeleteStopSelectData;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

// List of ProcessedData in MainActivity
public class ProcessedDataListAdapter extends RecyclerView.Adapter<ProcessedDataListAdapter.ViewHolder> {

    private List<ProcessedData> dataList;
    private LayoutInflater inflater;
    private AppCompatActivity activity;
    private DeleteStopSelectData listener;

    public ProcessedDataListAdapter(List<ProcessedData> dataList, AppCompatActivity activity, DeleteStopSelectData listener) {
        this.dataList = dataList;
        this.inflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.listener = listener;
    }

    // We have two types of view holder: ProcessingViewHolder and ProcessedViewHolder
    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).getState();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ProcessedData.DONE){
            return new ProcessedViewHolder(ProcessedDataCardBinding.inflate(inflater, parent, false));
        }
        return new ProcessingViewHolder(ProcessingDataCardBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ProcessedData data = dataList.get(position);
        holder.setData(data);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void setData(ProcessedData data);
    }

    // Contains ProcessedData in any intermediate state (PROCESSING, UPLOADING, CANCELLED)
    class ProcessingViewHolder extends ViewHolder {
        ProcessingDataCardBinding binding;
        ProcessedData data;

        public ProcessingViewHolder(ProcessingDataCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        void setData(ProcessedData data) {
            this.data = data;
            fillData();
            addListeners();
        }

        private void fillData(){
            binding.processingDataName.setText(data.name);
        }

        private void addListeners(){
            binding.processingDataCancelBtn.setOnClickListener((view) -> {
                // We cannot do that because ProcessedData is now processed and uploading
                // or already cancelled
                if (data.getState() != ProcessedData.PROCESSING){
                    return;
                }
                Dialogs.confirmationDialog(
                        activity,
                        R.string.cancel_suppress,
                        (dialogInterface, i) -> {
                            // Pay attention that .setState notifies LiveData<Integer> state
                            data.setState(ProcessedData.CANCELLED);
                        }
                );
            });
            data.getStateLiveData().observe(activity, state -> {
                // Change text and text color of state indicator depending on state
                int text;
                int color;
                if (state == ProcessedData.PROCESSING){
                    text = R.string.processing;
                    color = activity.getColor(R.color.yellow);
                } else if (state == ProcessedData.UPLOADING){
                    text = R.string.uploading;
                    color = activity.getColor(R.color.light_green);

                    // We cannot cancel uploading ProcessedData
                    binding.processingDataProgressBar.setVisibility(View.GONE);
                    binding.processingDataCancelBtn.setVisibility(View.GONE);

                } else {
                    text = R.string.cancelled;
                    color = activity.getColor(R.color.danger);
                }
                binding.processingDataState.setText(text);
                binding.processingDataState.setTextColor(color);
            });

            // Show image of ProcessedData
            binding.processedDataCard.setOnClickListener((view) -> {
                new ImageDialog(activity, data.getBitmap()).show();
            });
        }
    }

    // Contains ProcessedData in DONE state
    class ProcessedViewHolder extends ViewHolder {
        ProcessedDataCardBinding binding;
        ProcessedData data;

        public ProcessedViewHolder(ProcessedDataCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        void setData(ProcessedData data) {
            this.data = data;
            // init methods
            fillData();
            addListeners();
        }

        private void fillData(){
            binding.processedDataName.setText(data.name);
            binding.processedDataDate.setText(DateTimeUtils.format(data.dateCreation));
            binding.processedDataState.setText(activity.getString(R.string.done));
            updateSelected();
        }

        private void addListeners() {
            binding.processedDataCard.setOnClickListener((view) -> {
                new ProcessedDataBottomDialog().show();
            });
            binding.deleteProcessedDataBtn.setOnClickListener((view) -> attemptToDelete());
            binding.processedDataSelectedBtn.setOnClickListener((view) -> {
                listener.select(data);
                updateSelected();
            });
        }

        // Changes ProcessedData selected state
        private void updateSelected(){
            binding.processedDataSelectedBtn.setImageDrawable(
                    ResourcesCompat.getDrawable(
                            activity.getResources(),
                            data.selected ? R.drawable.ic_selected : R.drawable.ic_unselected,
                            null
                    )
            );
        }

        // Confirmation for deleting ProcessedData
        private void attemptToDelete() {
            attemptToDelete(null);
        }

        private void attemptToDelete(Runnable r) {
            Dialogs.confirmationDialog(activity, R.string.delete_suppress, (dialogInterface, i) -> {
                listener.delete(data);
                if (r != null) {
                    r.run();
                }
            });
        }

        // Dialog containing full information
        class ProcessedDataBottomDialog extends BottomSheetDialog {

            ProcessedDataDialogBinding binding;
            private boolean isImageClear = false;
            private final MutableLiveData<Bitmap> source = new MutableLiveData<>();
            private Bitmap smallImage = null;
            private InsulatorListAdapter adapter;

            public ProcessedDataBottomDialog() {
                super(activity);

                binding = ProcessedDataDialogBinding.inflate(inflater);

                initViews();
                addListeners();
                setContentView(binding.getRoot());

                // Clear memory from bitmaps when dialog is dismissed
                setOnDismissListener(dialogInterface -> {
                    if (smallImage != null){
                        smallImage.recycle();
                        smallImage = null;
                    }
                    Bitmap src = source.getValue();
                    if (src != null){
                        src.recycle();
                        source.removeObservers(activity);
                        source.setValue(null);
                    }
                });
            }

            private void initViews() {
                binding.procesedDataName.setText(data.name);
                binding.imageTimeDialog.setText(DateTimeUtils.format(data.dateCreation));
                // Initiating insulators list
                adapter = new InsulatorListAdapter(
                        data.shapes,
                        activity,
                        () -> {
                            if (!isImageClear) {
                                updateImage();
                            }
                        }
                );
                binding.insulatorsList.setAdapter(adapter);
                // Downloading Bitmap in new thread
                new Thread(() -> {
                    try {
                        Bitmap bmp = Picasso.with(activity).load(data.source).get();
                        if (data.rotated){
                            bmp = ImageUtils.rotate90(bmp);
                        }
                        source.postValue(bmp);
                    } catch (IOException e) {}
                }).start();
                source.observe(activity, bitmap -> updateImage());
            }

            private void addListeners() {
                binding.deleteProcessedImageBtn.setOnClickListener((view) -> attemptToDelete(this::cancel));
                // Shows insulators list
                binding.insulatorsExpandableButton.setOnClickListener((view) -> {
                    ExpandableUtils.toggleWithIndicator(binding.insulatorsExpandableLayout, binding.expandableIndicator);
                });
                // Toggles insulators view
                binding.changeImageBtn.setOnClickListener((view) -> {
                    isImageClear = !isImageClear;
                    binding.processedImage.animate().setDuration(400).alpha(0);
                    updateImage();
                    binding.processedImage.animate().setDuration(400).alpha(1);
                });
                binding.processedImage.setOnClickListener((view) -> showImageInDialog());
            }

            // Show high resolution image in special dialog
            private void showImageInDialog() {
                new ImageDialog(activity, getCurrentBitmap()).show();
            }

            private Bitmap getCurrentBitmap(){
                return ImageUtils.getProcessedImage(source.getValue(), adapter.getSelectedShapes(), 1);
            }

            // Small bitmap is used for rendering in ProcessedDataBottomDialog
            private Bitmap getCurrentSmallBitmap() {
                Bitmap img = source.getValue();
                float scale = ImageUtils.getImageScale(
                        img,
                        binding.processedImage.getMaxWidth(),
                        binding.processedImage.getMaxHeight()
                );

                if (smallImage == null){
                    smallImage = ImageUtils.getScaledBitmap(
                            source.getValue(),
                            scale
                    );
                }

                if (isImageClear) {
                    return smallImage;
                }

                return ImageUtils.getProcessedImage(smallImage, adapter.getSelectedShapes(), scale);
            }

            // Updates ProcessedDataBottomDialog image
            private void updateImage() {
                binding.processedImage.setImageBitmap(
                        getCurrentSmallBitmap()
                );
            }


        }
    }
}
