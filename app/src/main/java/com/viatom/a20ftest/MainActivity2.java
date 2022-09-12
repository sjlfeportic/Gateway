package com.viatom.a20ftest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;



public class MainActivity2 extends AppCompatActivity {

    private EditText e1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        e1 = (EditText)findViewById(R.id.editTextTextPersonName);

    }

    public void send_data(View view) {
    }


    static class BackgroundTask extends AsyncTask<String,Void,Void>
    {
        Socket s;
        PrintWriter writer;

        @Override
        protected Void doInBackground(String... strings) {
            try
            {
                String message = strings[0];
                s = new Socket("192.168.190.12",6000);
                writer = new PrintWriter(s.getOutputStream());
                writer.write(message);
                writer.flush();
                writer.close();

            }catch(IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }
}