package com.carracer.presentation.ui.leaderboard;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.carracer.databinding.ItemLeaderboardBinding;
import com.carracer.domain.models.LeaderboardItem;
import com.carracer.domain.utils.MeasurementType; // <-- DODAJ TEN IMPORT
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Consumer;

public class LeaderboardAdapter extends ListAdapter<LeaderboardItem, LeaderboardAdapter.LeaderboardViewHolder> {

    private final Consumer<String> onDeleteClick;

    public LeaderboardAdapter(Consumer<String> onDeleteClick) {
        super(DIFF_CALLBACK);
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLeaderboardBinding binding = ItemLeaderboardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LeaderboardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardItem item = getItem(position);
        holder.bind(item, position + 1, onDeleteClick);
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        private final ItemLeaderboardBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        LeaderboardViewHolder(ItemLeaderboardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LeaderboardItem item, int position, Consumer<String> onDeleteClick) {
            binding.positionText.setText(position + ".");
            binding.loginText.setText(item.getUserLogin());

            // NOWA LINIA: Ustawianie typu pomiaru
            // Zakładamy, że item.getType() zwraca String, który pasuje do nazw enum MeasurementType
            try {
                MeasurementType measurementType = MeasurementType.valueOf(item.getType());
                binding.measurementTypeText.setText(measurementType.getDisplayName());
            } catch (IllegalArgumentException e) {
                // Obsługa błędu, jeśli typ nie pasuje do enum (np. stary pomiar, nowe typy)
                binding.measurementTypeText.setText("Typ: " + item.getType()); // Pokaż surową nazwę
            }


            binding.dateText.setText(dateFormat.format(new Date(item.getMeasuredAt())));

            if (item.getType().equals("TOP_SPEED")) { // Zmienione na String
                binding.resultText.setText(String.format(Locale.US, "%.1f km/h", item.getPeakSpeedKmh()));
            } else if (item.getType().equals("QUARTER_MILE")) { // Dodaj obsługę dla QUARTER_MILE (dystans)
                binding.resultText.setText(String.format(Locale.US, "%.2f m", item.getDistanceMeters()));
            }
            else {
                binding.resultText.setText(String.format(Locale.US, "%.2f s", item.getDurationS()));
            }

            if (item.isDeletable()) {
                binding.deleteButton.setVisibility(ViewGroup.VISIBLE);
                binding.deleteButton.setOnClickListener(v -> onDeleteClick.accept(item.getId()));
            } else {
                binding.deleteButton.setVisibility(ViewGroup.GONE);
                binding.deleteButton.setOnClickListener(null);
            }
        }
    }

    private static final DiffUtil.ItemCallback<LeaderboardItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<LeaderboardItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull LeaderboardItem oldItem, @NonNull LeaderboardItem newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }
                @Override
                public boolean areContentsTheSame(@NonNull LeaderboardItem oldItem, @NonNull LeaderboardItem newItem) {
                    // UWAGA: Ta metoda powinna porównywać WSZYSTKIE istotne pola, nie tylko ID,
                    // aby DiffUtil wiedział, czy element się zmienił.
                    // Dla prostoty, porównujemy wszystkie pola.
                    return oldItem.equals(newItem); // Upewnij się, że LeaderboardItem ma poprawny equals()
                }
            };
}