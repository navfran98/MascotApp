package com.barkindustries.mascotapp.ui.yourPets.medicalHistory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.barkindustries.mascotapp.R;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

public class MedicalHistoryAdapter extends RecyclerView.Adapter<MedicalHistoryAdapter.MedicalHistoryHolder> {

    private Context context;
    private List<MedicalHistoryItem> historiesItems;

    public MedicalHistoryAdapter(Context context, List<MedicalHistoryItem> historiesItems) {
        this.context = context;
        this.historiesItems = historiesItems;
    }

    @NonNull
    @Override
    public MedicalHistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.medical_history_item, parent, false);
        return new MedicalHistoryAdapter.MedicalHistoryHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicalHistoryHolder holder, int position) {

        MedicalHistoryItem currentItem = historiesItems.get(position);

        holder.historyItemTitle.setText(currentItem.getTitle());
        holder.historyItemDescription.setText(currentItem.getDescription());

        String timestampString = currentItem.getTimestamp();
        timestampString = timestampString.replace("T", "   ").substring(0, 21);

        holder.historyItemTimestamp.setText(timestampString);
        if(currentItem.medicineIsPresent()) {
            holder.medicineIcon.setVisibility(View.VISIBLE);
            holder.medicineName.setVisibility(View.VISIBLE);
            holder.medicineName.setText(currentItem.getMedicine());
        } else {
            holder.medicineIcon.setVisibility(View.GONE);
            holder.medicineName.setVisibility(View.GONE);
        }

        if(currentItem.isImportant())
            holder.importantIcon.setVisibility(View.VISIBLE);
        else
            holder.importantIcon.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return historiesItems.size();
    }

    public static class MedicalHistoryHolder extends RecyclerView.ViewHolder {

        private TextView historyItemTitle;
        private TextView historyItemDescription;
        private TextView historyItemTimestamp;
        private TextView medicineName;
        private ImageView medicineIcon;
        private ImageView importantIcon;

        public MedicalHistoryHolder(@NonNull View itemView) {
            super(itemView);
            historyItemTitle = itemView.findViewById(R.id.history_item_title);
            historyItemDescription = itemView.findViewById(R.id.history_item_description);
            historyItemTimestamp = itemView.findViewById(R.id.history_item_timestamp);
            medicineName = itemView.findViewById(R.id.medicine_name);
            medicineIcon = itemView.findViewById(R.id.medicine_icon);
            importantIcon = itemView.findViewById(R.id.medical_history_item_important_icon);
        }
    }
}
