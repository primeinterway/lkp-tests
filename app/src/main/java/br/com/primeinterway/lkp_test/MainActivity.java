package br.com.primeinterway.lkp_test;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sewoo.jpos.command.ESCPOSConst;
import com.sewoo.jpos.printer.ESCPOSPrinter;
import com.sewoo.port.android.BluetoothPort;
import com.sewoo.request.android.RequestHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import br.com.primeinterway.lkp_test.utils.CheckPrinterStatus;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "ASFDev";

    private String address = "40:19:20:11:13:27";

    ESCPOSPrinter printer;
    CheckPrinterStatus checkPrinterStatus;
    BluetoothPort bluetoothPort = new BluetoothPort();

    TextView textView;
    Button button;

    private Thread btThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        printer = new ESCPOSPrinter();
        checkPrinterStatus = new CheckPrinterStatus();

        textView = findViewById(R.id.text);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    printBarcode();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        new Thread(this::init).start();
    }

    @Override
    protected void onDestroy() {
        if (printer != null && bluetoothPort.isConnected()) {
            try {
                bluetoothPort.disconnect();
                btThread.interrupt();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        try {
            if (bluetoothPort.isValidAddress(address)) {
                setText("Loading connection...");
                bluetoothPort.connect(address);
                setText("Connected");

                RequestHandler rh = new RequestHandler();
                btThread = new Thread(rh);
                btThread.start();

                printer = new ESCPOSPrinter();
            }
        } catch (IOException e) {
            setText("Failed to connect");
            e.printStackTrace();
        }
    }

    public void printBarcode() throws UnsupportedEncodingException {
        String data = "www.primeinterway.com.br";

        int sts = checkPrinterStatus.PrinterStatus(printer);
        if (sts != ESCPOSConst.LK_SUCCESS) {
            Toast.makeText(MainActivity.this, "Fail: " + printer.printerCheck(), Toast.LENGTH_SHORT).show();
            return;
        }

        printer.printQRCode(data, data.length(), 10, ESCPOSConst.LK_QRCODE_EC_LEVEL_L, ESCPOSConst.LK_ALIGNMENT_CENTER);
        printer.lineFeed(4);
    }

    private void setText(String text) {
        runOnUiThread(() -> textView.setText(text));
    }
}