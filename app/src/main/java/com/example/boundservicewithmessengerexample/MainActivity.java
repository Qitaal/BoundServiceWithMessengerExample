package com.example.boundservicewithmessengerexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements HandlerCallback {

    static final String ACTIVITY_MESSENGER = "ACTIVITY_MESSENGER";
    private Boolean serviceBounded = false;
    private Messenger myService; // MESSENGER BUAT REMOTE SERVICE
    static final String KEY_DATA_INPUT = "KEY DATA INPUT";
    static final int UPDATE_RESULT = 5;

    TextView tvResult;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //NGAMBIL MESSENGER YANG DIKIRIM DARI SERVICE
            myService = new Messenger(service);
            serviceBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBounded = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btn_start);
        Button btnGenerateNumber = findViewById(R.id.btn_generate_number);
        Button btnProssesInput = findViewById(R.id.btn_prosses_input);
        EditText etInput = findViewById(R.id.et_input);
        tvResult = findViewById(R.id.tv_result);

        btnStart.setOnClickListener(v -> {
            //MESSENGER YANG DIKIRIM KE SERVICE BIAR SERVICE BISA AKSES ACTIVITY LEWAT MainIncomingHandler
            Messenger activityMessenger = new Messenger(new MainIncomingHandler(this));
            bindService(new Intent(MainActivity.this, MyService.class).putExtra(ACTIVITY_MESSENGER, activityMessenger), serviceConnection, BIND_AUTO_CREATE);
        });

        btnGenerateNumber.setOnClickListener(v -> {
            if (serviceBounded){
                Message message = Message.obtain(null, MyService.GENERATE_NUMBER);
                try {
                    myService.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(this, "You need to start Service first", Toast.LENGTH_SHORT).show();
            }
        });

        btnProssesInput.setOnClickListener(v -> {
            if (serviceBounded){
                if (!etInput.getText().toString().equals("")){
                    Message message = Message.obtain(null, MyService.PROSSES_INPUT);
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_DATA_INPUT, etInput.getText().toString());
                    message.setData(bundle);
                    try {
                        myService.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    etInput.setError("Please input some word");
                }
            }
            else {
                Toast.makeText(this, "You need to start Service first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void updateResult(String result) {
        tvResult.setText(result);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBounded)
            unbindService(serviceConnection);
    }

    static class MainIncomingHandler extends Handler{
        final WeakReference<HandlerCallback> callback;

        MainIncomingHandler(HandlerCallback callback) {
            this.callback = new WeakReference<>(callback);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case UPDATE_RESULT :
                    callback.get().updateResult(msg.getData().getString(MyService.KEY_RESULT));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
interface HandlerCallback{
    void updateResult(String result);
}