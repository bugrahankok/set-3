package com.example.calculator;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private static final List<String> historyList = new ArrayList<>();

    public static void addEntry(String entry) {
        historyList.add(entry);
    }

    public static List<String> getHistory() {
        return new ArrayList<>(historyList);
    }

    public static void clearHistory() {
        historyList.clear();
    }
}
