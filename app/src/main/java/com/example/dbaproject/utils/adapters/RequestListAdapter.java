package com.example.dbaproject.utils.adapters;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dbaproject.R;
import com.example.dbaproject.databinding.NewRequestCardBinding;
import com.example.dbaproject.utils.ExpandableUtils;
import com.example.dbaproject.utils.ImageUtils;
import com.example.dbaproject.utils.dialogs.CreateRequestDialog;
import com.example.dbaproject.utils.dialogs.ImageDialog;
import com.example.dbaproject.view_models.NewRequestItem;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.IOException;
import java.util.List;

public class RequestListAdapter extends RecyclerView.Adapter<RequestListAdapter.ViewHolder> {

    private AppCompatActivity activity;
    private List<NewRequestItem> items;
    private LayoutInflater inflater;
    private ItemDelete listener;

    public RequestListAdapter(AppCompatActivity activity, List<NewRequestItem> items, ItemDelete listener) {
        this.activity = activity;
        this.items = items;
        this.inflater = LayoutInflater.from(activity);
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        NewRequestCardBinding binding = NewRequestCardBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setItem(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static interface ItemDelete {
        void delete(NewRequestItem item);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        NewRequestCardBinding binding;
        boolean imageShowed = false;
        NewRequestItem item;

        public ViewHolder(NewRequestCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void setItem(NewRequestItem item){
            this.item = item;
            fillData();
            addListeners();
        }

        private void fillData(){
            binding.requestName.setText(item.getRealName());
            binding.requestNameInput.setHint(item.getDefaultName());
        }

        private void addListeners() {
            binding.requestNameInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    item.setName(charSequence.toString());
                    binding.requestName.setText(item.getRealName());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            binding.requestDeleteBtn.setOnClickListener((view) -> listener.delete(item));
            binding.requestLinearLayout.setOnClickListener((view) -> {
                if (!imageShowed) {
                    imageShowed = true;

                    Bitmap bitmap = ImageUtils.loadBitmap(item.getUri(), activity, R.drawable.placeholder);

                    binding.requestImage.setImageBitmap(
                            ImageUtils.getScaledBitmap(
                                    bitmap,
                                    ImageUtils.getImageScale(
                                            bitmap,
                                            binding.requestImage.getMaxWidth(),
                                            binding.requestImage.getMaxHeight()
                                    )
                            )
                    );
                }
                ExpandableUtils.toggleWithIndicator(binding.requestExpandableLayout, binding.requestExpandableIndicator);
            });
            binding.requestImage.setOnClickListener((view) -> {
                Bitmap bitmap = ImageUtils.loadBitmap(item.getUri(), activity, R.drawable.placeholder);
                ImageDialog d = new ImageDialog(activity, bitmap);
                d.setOnDismissListener(dialogInterface -> bitmap.recycle());
                d.show();
            });
        }
    }
}
