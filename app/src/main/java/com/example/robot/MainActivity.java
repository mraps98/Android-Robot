package com.example.robot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    public EditText editTextIp;
    public EditText editTextPort;
    public Button buttonDisconnect;
    public Button buttonConnect;
    public Button buttonUp;
    public Button buttonDown;
    public Button buttonLeft;
    public Button buttonRight;
    public Button buttonStop;
    public TextView textViewTop;
    public Socket socket;
    public String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bind ui elements
        editTextIp = (EditText) findViewById(R.id.editTextIp);
        editTextPort = (EditText) findViewById(R.id.editTextPort);
        buttonDisconnect = (Button) findViewById(R.id.buttonDisconnect);
        buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonDown = (Button) findViewById(R.id.buttonDown);
        buttonUp = (Button) findViewById(R.id.buttonUp);
        buttonLeft = (Button) findViewById(R.id.buttonLeft);
        buttonRight = (Button) findViewById(R.id.buttonRight);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        textViewTop = (TextView) findViewById(R.id.textViewTop);

        TAG = "Robot socket test";

        buttonUp.setEnabled(false);
        buttonLeft.setEnabled(false);
        buttonRight.setEnabled(false);
        buttonDown.setEnabled(false);
        buttonStop.setEnabled(false);
    }

    public void connect(View view){
        Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_SHORT).show();
        String ip = editTextIp.getText().toString().trim();
        ConnectThread thread = new ConnectThread(ip);

        //키보드 자동 내리기
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextIp.getWindowToken(), 0);
        thread.start();
    }

    public void disconnect(View view){
        try {
            socket.close();
            Toast.makeText(getApplicationContext(), "Disconnected Successfully", Toast.LENGTH_SHORT).show();
            buttonDisconnect.setEnabled(false);
            buttonConnect.setEnabled(true);
            buttonUp.setEnabled(false);
            buttonLeft.setEnabled(false);
            buttonRight.setEnabled(false);
            buttonDown.setEnabled(false);
            textViewTop.setText("Not Connected");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Failure to Disconnect", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendSignal(View view) throws IOException {
        String ip = editTextIp.getText().toString();
        int port = Integer.parseInt(editTextPort.getText().toString());
        byte[] data;
        switch(view.getId()) {
            case R.id.buttonUp:
                Toast.makeText(getApplicationContext(), "Up Pressed\nIP:" + ip + "\nPort:" + port, Toast.LENGTH_SHORT).show();
                SignalThread upThread = new SignalThread("w");
                upThread.start();
                Log.d(TAG, "Up signal sent");
                break;
            case R.id.buttonDown:
                Toast.makeText(getApplicationContext(), "Down Pressed\nIP:" + ip + "\nPort:" + port, Toast.LENGTH_SHORT).show();
                SignalThread downThread = new SignalThread("s");
                downThread.start();
                Log.d(TAG, "Down signal sent");
                break;
            case R.id.buttonLeft:
                Toast.makeText(getApplicationContext(), "Left Pressed\nIP:" + ip + "\nPort:" + port, Toast.LENGTH_SHORT).show();
                SignalThread leftThread = new SignalThread("a");
                leftThread.start();
                Log.d(TAG, "Left signal sent");
                break;
            case R.id.buttonRight:
                Toast.makeText(getApplicationContext(), "Right Pressed\nIP:" + ip + "\nPort:" + port, Toast.LENGTH_SHORT).show();
                SignalThread rightThread = new SignalThread("d");
                rightThread.start();
                Log.d(TAG, "Right signal sent");
                break;
            case R.id.buttonStop:
                Toast.makeText(getApplicationContext(), "Stop Pressed\nIP:" + ip + "\nPort:" + port, Toast.LENGTH_SHORT).show();
                SignalThread stopThread = new SignalThread("x");
                stopThread.start();
                Log.d(TAG, "Stop signal sent");
                break;
        }
    }

    class SignalThread extends Thread{

        String signalMessage;

        public SignalThread(String signalMessage){
            this.signalMessage = signalMessage;
        }

        public void run(){
            try {
                byte[] data = signalMessage.getBytes();
                OutputStream output = socket.getOutputStream();
                output.write(data);
                Log.d(TAG, "Signal Sent");

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG,"IO Exception");
            }
        }
    }

    // fixme: Socket Connect.
    class ConnectThread extends Thread {
        String hostname;

        public ConnectThread(String addr) {
            hostname = addr;
        }

        public void run() {
            try {

                int port = Integer.parseInt(editTextPort.getText().toString());
                socket = new Socket(hostname, port);

                textViewTop = findViewById(R.id.textViewTop);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        InetAddress addr = socket.getInetAddress();
                        String tmp = addr.getHostAddress();
                        textViewTop.setText("Connected to " + tmp);
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();

                        buttonDisconnect.setEnabled(true);
                        buttonConnect.setEnabled(false);
                        buttonUp.setEnabled(true);
                        buttonDown.setEnabled(true);
                        buttonLeft.setEnabled(true);
                        buttonRight.setEnabled(true);
                        buttonStop.setEnabled(true);
                    }
                });




            } catch (UnknownHostException uhe) { // 소켓 생성 시 전달되는 호스트(www.unknown-host.com)의 IP를 식별할 수 없음.

//                Log.e(TAG, " 생성 Error : 호스트의 IP 주소를 식별할 수 없음.(잘못된 주소 값 또는 호스트 이름 사용)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 호스트의 IP 주소를 식별할 수 없음.(잘못된 주소 값 또는 호스트 이름 사용)", Toast.LENGTH_SHORT).show();
                        textViewTop.setText("Error : 호스트의 IP 주소를 식별할 수 없음.(잘못된 주소 값 또는 호스트 이름 사용)");
                    }
                });

            } catch (IOException ioe) { // 소켓 생성 과정에서 I/O 에러 발생.

//                Log.e(TAG, " 생성 Error : 네트워크 응답 없음");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 네트워크 응답 없음", Toast.LENGTH_SHORT).show();
                        textViewTop.setText("네트워크 연결 오류");
                    }
                });


            } catch (SecurityException se) { // security manager에서 허용되지 않은 기능 수행.

//                Log.e(TAG, " 생성 Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)", Toast.LENGTH_SHORT).show();
                        textViewTop.setText("Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)");
                    }
                });


            } catch (IllegalArgumentException le) { // 소켓 생성 시 전달되는 포트 번호(65536)이 허용 범위(0~65535)를 벗어남.

//                Log.e(TAG, " 생성 Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생.(0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), " Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생.(0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)", Toast.LENGTH_SHORT).show();
                        textViewTop.setText("Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생.(0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)");
                    }
                });
            }
        }
    }

    @Override
    protected void onStop() {  //앱 종료시
        super.onStop();
        try {
            socket.close(); //소켓을 닫는다.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}