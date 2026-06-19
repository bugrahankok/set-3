package com.example.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private TextView tvModeIndicator;
    private TextView tvBaseIndicator;
    private TextView tvFormula;
    private TextView tvDisplay;

    private boolean isSeqMode = true;
    private String currentBase = "DEC";

    private double previousResult = 0.0;
    private boolean hasPreviousResult = false;
    private String currentOperator = null;
    private StringBuilder currentInput = new StringBuilder();
    private boolean isResultDisplayed = false;
    private boolean isError = false;

    private final List<String> expressionTokens = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvModeIndicator = findViewById(R.id.tv_mode_indicator);
        tvBaseIndicator = findViewById(R.id.tv_base_indicator);
        tvFormula = findViewById(R.id.tv_formula);
        tvDisplay = findViewById(R.id.tv_display);

        setupDigitButtons();
        setupOperatorButtons();
        setupActionButtons();
        setupBaseButtons();
    }

    private void setupDigitButtons() {
        int[] digitIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        };

        for (int i = 0; i < digitIds.length; i++) {
            final String digitStr = String.valueOf(i);
            findViewById(digitIds[i]).setOnClickListener(v -> onDigitPressed(digitStr));
        }

        findViewById(R.id.btn_dot).setOnClickListener(v -> onDigitPressed("."));
    }

    private void setupOperatorButtons() {
        findViewById(R.id.btn_add).setOnClickListener(v -> onOperatorPressed("+"));
        findViewById(R.id.btn_subtract).setOnClickListener(v -> onOperatorPressed("-"));
        findViewById(R.id.btn_multiply).setOnClickListener(v -> onOperatorPressed("*"));
        findViewById(R.id.btn_divide).setOnClickListener(v -> onOperatorPressed("/"));
        findViewById(R.id.btn_power).setOnClickListener(v -> onOperatorPressed("^"));
    }

    private void setupActionButtons() {
        findViewById(R.id.btn_clear).setOnClickListener(v -> onClearPressed());
        findViewById(R.id.btn_equals).setOnClickListener(v -> onEqualsPressed());
        findViewById(R.id.btn_toggle_mode).setOnClickListener(v -> toggleMode());
        findViewById(R.id.btn_history).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    private void setupBaseButtons() {
        findViewById(R.id.btn_dec).setOnClickListener(v -> setBase("DEC"));
        findViewById(R.id.btn_bin).setOnClickListener(v -> setBase("BIN"));
        findViewById(R.id.btn_oct).setOnClickListener(v -> setBase("OCT"));
        findViewById(R.id.btn_hex).setOnClickListener(v -> setBase("HEX"));
    }

    private void onDigitPressed(String val) {
        if (isError) {
            return;
        }

        if (!currentBase.equals("DEC")) {
            setBase("DEC");
        }

        if (isResultDisplayed) {
            clearState();
            isResultDisplayed = false;
        }

        if (val.equals(".")) {
            if (currentInput.indexOf(".") != -1) {
                return;
            }
            if (currentInput.length() == 0) {
                currentInput.append("0");
            }
        } else if (val.equals("0")) {
            if (currentInput.toString().equals("0")) {
                return;
            }
        } else {
            if (currentInput.toString().equals("0")) {
                currentInput.setLength(0);
            }
        }

        currentInput.append(val);
        tvDisplay.setText(currentInput.toString());

        if (!isSeqMode) {
            updateFormulaDisplayExpr();
        }
    }

    private void onOperatorPressed(String op) {
        if (isError) {
            return;
        }

        if (!currentBase.equals("DEC")) {
            setBase("DEC");
        }

        if (isSeqMode) {
            onOperatorPressedSeq(op);
        } else {
            onOperatorPressedExpr(op);
        }
    }

    private void onOperatorPressedSeq(String op) {
        if (currentInput.length() > 0) {
            double value = Double.parseDouble(currentInput.toString());
            if (hasPreviousResult) {
                try {
                    double result = evaluateSeq(previousResult, currentOperator, value);
                    String entry = formatDecimal(previousResult) + " " + currentOperator + " " + formatDecimal(value) + " = " + formatDecimal(result);
                    HistoryManager.addEntry(entry);
                    previousResult = result;
                    tvDisplay.setText(formatDecimal(previousResult));
                } catch (ArithmeticException e) {
                    isError = true;
                    tvDisplay.setText("Error");
                    tvFormula.setText("");
                    return;
                }
            } else {
                previousResult = value;
                hasPreviousResult = true;
            }
            currentInput.setLength(0);
        } else {
            if (!hasPreviousResult) {
                previousResult = 0.0;
                hasPreviousResult = true;
            }
        }
        currentOperator = op;
        isResultDisplayed = false;
        String formulaText = formatDecimal(previousResult) + " " + currentOperator;
        tvFormula.setText(formulaText);
    }

    private void onOperatorPressedExpr(String op) {
        if (isResultDisplayed) {
            expressionTokens.clear();
            expressionTokens.add(formatDecimal(previousResult));
            isResultDisplayed = false;
        }

        if (currentInput.length() > 0) {
            expressionTokens.add(currentInput.toString());
            currentInput.setLength(0);
        } else {
            if (expressionTokens.isEmpty()) {
                expressionTokens.add("0");
            } else {
                String lastToken = expressionTokens.get(expressionTokens.size() - 1);
                if (isOperator(lastToken)) {
                    expressionTokens.remove(expressionTokens.size() - 1);
                }
            }
        }
        expressionTokens.add(op);
        updateFormulaDisplayExpr();
    }

    private void onEqualsPressed() {
        if (isError) {
            return;
        }

        if (!currentBase.equals("DEC")) {
            setBase("DEC");
        }

        if (isSeqMode) {
            onEqualsPressedSeq();
        } else {
            onEqualsPressedExpr();
        }
    }

    private void onEqualsPressedSeq() {
        if (currentOperator == null || !hasPreviousResult) {
            if (currentInput.length() > 0) {
                previousResult = Double.parseDouble(currentInput.toString());
                hasPreviousResult = true;
                currentInput.setLength(0);
                tvDisplay.setText(formatDecimal(previousResult));
                tvFormula.setText(formatDecimal(previousResult) + " = " + formatDecimal(previousResult));
                isResultDisplayed = true;
            }
            return;
        }

        double operand2;
        if (currentInput.length() > 0) {
            operand2 = Double.parseDouble(currentInput.toString());
            currentInput.setLength(0);
        } else {
            operand2 = previousResult;
        }

        try {
            double result = evaluateSeq(previousResult, currentOperator, operand2);
            String entry = formatDecimal(previousResult) + " " + currentOperator + " " + formatDecimal(operand2) + " = " + formatDecimal(result);
            HistoryManager.addEntry(entry);
            tvFormula.setText(entry);
            previousResult = result;
            tvDisplay.setText(formatDecimal(previousResult));
            currentOperator = null;
            isResultDisplayed = true;
        } catch (ArithmeticException e) {
            isError = true;
            tvDisplay.setText("Error");
            tvFormula.setText("");
        }
    }

    private void onEqualsPressedExpr() {
        if (currentInput.length() > 0) {
            expressionTokens.add(currentInput.toString());
            currentInput.setLength(0);
        }

        if (expressionTokens.isEmpty()) {
            return;
        }

        String lastToken = expressionTokens.get(expressionTokens.size() - 1);
        if (isOperator(lastToken)) {
            expressionTokens.remove(expressionTokens.size() - 1);
        }

        if (expressionTokens.isEmpty()) {
            return;
        }

        StringBuilder formulaBuilder = new StringBuilder();
        for (int i = 0; i < expressionTokens.size(); i++) {
            formulaBuilder.append(expressionTokens.get(i));
            if (i < expressionTokens.size() - 1) {
                formulaBuilder.append(" ");
            }
        }
        String formulaText = formulaBuilder.toString();

        try {
            double result = evaluateExpr(expressionTokens);
            previousResult = result;
            String resultStr = formatDecimal(result);
            tvDisplay.setText(resultStr);
            String entry = formulaText + " = " + resultStr;
            tvFormula.setText(entry);
            HistoryManager.addEntry(entry);
            expressionTokens.clear();
            isResultDisplayed = true;
        } catch (ArithmeticException e) {
            isError = true;
            tvDisplay.setText("Error");
            tvFormula.setText("");
        }
    }

    private void onClearPressed() {
        clearState();
        tvDisplay.setText("0");
        tvFormula.setText("");
    }

    private void clearState() {
        previousResult = 0.0;
        hasPreviousResult = false;
        currentOperator = null;
        currentInput.setLength(0);
        isResultDisplayed = false;
        isError = false;
        expressionTokens.clear();
        setBase("DEC");
    }

    private void toggleMode() {
        isSeqMode = !isSeqMode;
        clearState();
        tvDisplay.setText("0");
        tvFormula.setText("");
        if (isSeqMode) {
            tvModeIndicator.setText("Mode: Seq");
        } else {
            tvModeIndicator.setText("Mode: Expr");
        }
    }

    private void setBase(String base) {
        if (isError) {
            return;
        }
        currentBase = base;
        tvBaseIndicator.setText("Base: " + base);

        double valToConvert = 0.0;
        if (currentInput.length() > 0) {
            valToConvert = Double.parseDouble(currentInput.toString());
        } else if (hasPreviousResult || isResultDisplayed) {
            valToConvert = previousResult;
        }

        tvDisplay.setText(formatValueInBase(valToConvert, base));
    }

    private double evaluateSeq(double a, String op, double b) {
        if (op.equals("+")) {
            return a + b;
        } else if (op.equals("-")) {
            return a - b;
        } else if (op.equals("*")) {
            return a * b;
        } else if (op.equals("/")) {
            if (b == 0) {
                throw new ArithmeticException("Division by zero");
            }
            return a / b;
        } else if (op.equals("^")) {
            return Math.pow(a, b);
        }
        return 0;
    }

    private double evaluateExpr(List<String> tokens) {
        List<String> rpn = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        for (String token : tokens) {
            if (isNumber(token)) {
                rpn.add(token);
            } else if (isOperator(token)) {
                while (!stack.isEmpty() && isOperator(stack.peek()) &&
                        ((isLeftAssociative(token) && precedence(token) <= precedence(stack.peek())) ||
                                (!isLeftAssociative(token) && precedence(token) < precedence(stack.peek())))) {
                    rpn.add(stack.pop());
                }
                stack.push(token);
            }
        }
        while (!stack.isEmpty()) {
            rpn.add(stack.pop());
        }

        Stack<Double> valStack = new Stack<>();
        for (String token : rpn) {
            if (isNumber(token)) {
                valStack.push(Double.parseDouble(token));
            } else {
                if (valStack.size() < 2) {
                    throw new ArithmeticException("Invalid expression");
                }
                double b = valStack.pop();
                double a = valStack.pop();
                double res = 0;
                if (token.equals("+")) {
                    res = a + b;
                } else if (token.equals("-")) {
                    res = a - b;
                } else if (token.equals("*")) {
                    res = a * b;
                } else if (token.equals("/")) {
                    if (b == 0) {
                        throw new ArithmeticException("Division by zero");
                    }
                    res = a / b;
                } else if (token.equals("^")) {
                    res = Math.pow(a, b);
                }
                valStack.push(res);
            }
        }
        if (valStack.size() != 1) {
            throw new ArithmeticException("Invalid expression");
        }
        return valStack.pop();
    }

    private boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("^");
    }

    private int precedence(String op) {
        if (op.equals("^")) return 3;
        if (op.equals("*") || op.equals("/")) return 2;
        if (op.equals("+") || op.equals("-")) return 1;
        return 0;
    }

    private boolean isLeftAssociative(String op) {
        return !op.equals("^");
    }

    private String formatDecimal(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) {
            return "Error";
        }
        if (val == (long) val) {
            return String.valueOf((long) val);
        }
        return String.valueOf(val);
    }

    private String formatValueInBase(double value, String base) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "Error";
        }
        long intPart = (long) value;
        if (base.equals("BIN")) {
            return Long.toBinaryString(intPart);
        } else if (base.equals("OCT")) {
            return Long.toOctalString(intPart);
        } else if (base.equals("HEX")) {
            return Long.toHexString(intPart).toUpperCase();
        } else {
            return formatDecimal(value);
        }
    }

    private void updateFormulaDisplayExpr() {
        StringBuilder sb = new StringBuilder();
        for (String t : expressionTokens) {
            sb.append(t).append(" ");
        }
        if (currentInput.length() > 0) {
            sb.append(currentInput.toString());
        }
        tvFormula.setText(sb.toString().trim());
    }
}
