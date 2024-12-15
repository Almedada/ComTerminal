package com.example.comterminal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentTerminal extends Fragment {

    private EditText etTerminalInput;
    private Button btnClear, btnSend;
    private TextView tvTerminalOutput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);

        etTerminalInput = view.findViewById(R.id.et_terminal_input);
        btnClear = view.findViewById(R.id.btn_clear);
        btnSend = view.findViewById(R.id.btn_send);
        tvTerminalOutput = view.findViewById(R.id.tv_terminal_output);

        // Очистка строки ввода и вывода
        btnClear.setOnClickListener(v -> {
            etTerminalInput.setText(""); // Очистить поле ввода
            tvTerminalOutput.setText(""); // Очистить терминал
        });

        // Отправка текста
        btnSend.setOnClickListener(v -> {
            String textToSend = etTerminalInput.getText().toString().trim();
            if (!textToSend.isEmpty()) {
                sendDataViaSPP(textToSend);
                etTerminalInput.setText(""); // Очистить поле ввода после отправки
            }
        });

        return view;
    }

    private void sendDataViaSPP(String data) {
        // TODO: Реализуйте передачу данных через COM-порт
        tvTerminalOutput.append("Sent: " + data + "\n");
    }
}
