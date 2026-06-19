package com.example.calculator;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView lvHistory;
    private ArrayAdapter<String> adapter;
    private List<String> historyItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Button btnBack = findViewById(R.id.btn_back);
        Button btnClearHistory = findViewById(R.id.btn_clear_history);
        lvHistory = findViewById(R.id.lv_history);

        btnBack.setOnClickListener(v -> finish());

        historyItems = HistoryManager.getHistory();
        adapter = new ArrayAdapter<>(this, R.layout.item_history, R.id.tv_history_item, historyItems);
        lvHistory.setAdapter(adapter);

        btnClearHistory.setOnClickListener(v -> {
            HistoryManager.clearHistory();
            historyItems.clear();
            adapter.notifyDataSetChanged();
        });
    }
}
