package com.example.comterminal;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FragmentTerminal extends Fragment {

    private BluetoothSocket bluetoothSocket;
    private TextView textViewOutput;
    private EditText editTextInput;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflating the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);

        // Getting references to UI elements
        textViewOutput = view.findViewById(R.id.textViewOutput);
        editTextInput = view.findViewById(R.id.editTextInput);
        Button buttonSend = view.findViewById(R.id.buttonSend);

        // Set up the send button
        buttonSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    // Method to set Bluetooth socket
    public void setBluetoothSocket(BluetoothSocket socket) {
        this.bluetoothSocket = socket;
        try {
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            startListeningForData(); // Start listening for incoming data
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to send a message
    private void sendMessage() {
        String message = editTextInput.getText().toString();
        if (!message.isEmpty()) {
            if (outputStream != null) {
                try {
                    outputStream.write(message.getBytes()); // Send data over Bluetooth
                    outputStream.flush();
                    editTextInput.setText(""); // Clear the input field
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("FragmentTerminal", "OutputStream is null. Bluetooth socket not initialized.");
            }
        }
    }

    // Method to listen for incoming data
    private void startListeningForData() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    // Read data from the input stream
                    bytes = inputStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Update the UI on the main thread
                    handler.post(() -> textViewOutput.append(readMessage + "\n"));
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }
}
