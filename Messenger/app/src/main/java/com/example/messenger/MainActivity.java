package com.example.messenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button, button2;
    private EditText h, m;
    private ListView lvMain;
    private ListView lvMain1;
    private TextView test;
    private TextView test1;

    ArrayList<String> ar = new ArrayList<String>();
    ArrayList<String> ar1 = new ArrayList<String>();

    final String DIR_SD = "MyFiles";
    final String FILENAME_SD = "fileSD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        h = (EditText) findViewById(R.id.h);
        m = (EditText) findViewById(R.id.m);
        lvMain = (ListView) findViewById(R.id.lvMain);
        lvMain1 = (ListView) findViewById(R.id.lvMain1);
        test = (TextView) findViewById(R.id.textView5);
        test1 = (TextView) findViewById(R.id.textView6);

        button.setOnClickListener(this);
        button2.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // открываем подключение
        MyTask mt = new MyTask();
        mt.execute();

        ArrayAdapter newadapter1 = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ar1);
        lvMain1.setAdapter(newadapter1);

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Закрываем подключение и курсор

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button:
                new Thread(new Runnable() {
                    public void run() {

                        String mes = m.getText().toString();
                        String hos = h.getText().toString();
                        //Тут пишем основной код
                        boolean status = true;
                        DatagramSocket sock = null;
                        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));

                        try {
                            sock = new DatagramSocket();
                            while (status == true) {
                                //Ожидаем ввод сообщения серверу
                                //System.out.println("Введите сообщение серверу: ");
                                String s = mes;
                                byte[] b = s.getBytes();

                                //Отправляем сообщение
                                DatagramPacket dp = new DatagramPacket(b, b.length, InetAddress.getByName(hos), 7000);
                                sock.send(dp);

                                //буфер для получения входящих данных
                                byte[] buffer = new byte[65536];
                                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

                                //Получаем данные
                                sock.receive(reply);
                                byte[] data = reply.getData();
                                s = new String(data, 0, reply.getLength());

                                ar.add("Сервер: " + reply.getAddress().getHostAddress() + ", порт: " + reply.getPort() + ", получил: " + s);
                                status = false;
                            }
                        } catch (IOException e1) {
                            System.err.println("IOException " + e1);
                        }
                    }
                }).start();

                ArrayAdapter newadapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ar);
                lvMain.setAdapter(newadapter);
                test.setText("Inbox:");
                break;

            case R.id.button2:
                writeFileSD();
                test1.setText("Sended:");
                break;

            default:
                break;
        }
    }

    class MyTask extends AsyncTask<Void, Void, Void> {

        String title;//Тут храним значение заголовка сайта
        @Override
        protected Void doInBackground(Void... params) {

            //Тут пишем основной код
            try
            {
                //Создаем сокет
                DatagramSocket sockser = new DatagramSocket(7000);
                //буфер для получения входящих данных
                byte[] bufferser = new byte[65536];
                DatagramPacket incomingser = new DatagramPacket(bufferser, bufferser.length);

                ar1.add("Ожидаем данные...");

                while(true)
                {
                    //Получаем данные
                    sockser.receive(incomingser);
                    byte[] data = incomingser.getData();
                    String s = new String(data, 0, incomingser.getLength());

                    ar1.add("Сервер получил: " + s);
                    //Отправляем данные клиенту
                    DatagramPacket dpser = new DatagramPacket(s.getBytes() , s.getBytes().length , incomingser.getAddress() , incomingser.getPort());
                    sockser.send(dpser);
                }
            }
            catch(IOException e)
            {
                System.err.println("IOException " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            //textView.setText(title);
        }
    }

    void writeFileSD() {
        String querysd=h.getText().toString();
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            //Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
            return;
        }
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
        // создаем каталог
        sdPath.mkdirs();
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, FILENAME_SD);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
            // пишем данные
            bw.write(querysd);
            for(String el : ar){
                bw.write(el);
            }
            bw.write("_____________________");
            for(String el1 : ar1){
                bw.write(el1);
            }
            bw.write("_____________________");
            bw.close();
            //  Log.d(LOG_TAG, "Файл записан на SD: " + sdFile.getAbsolutePath());
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Запись на SD карту")
                            .setContentText("Данные сохранены на /sdcard/MyFiles/");

            Notification notification = builder.build();
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(3, notification);
            //
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}