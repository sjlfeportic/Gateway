package com.viatom.a20ftest;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LivehistoryActivity extends AppCompatActivity {

    Button insertButton;
    GraphView graphView;

    DatabaseHandler dbh;
    SQLiteDatabase sqLiteDatabase;

    LineGraphSeries<DataPoint> dataseries = new LineGraphSeries<>(new DataPoint[0]);

    @SuppressLint("SimpleDataFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.livehistory);

        insertButton = (Button) findViewById(R.id.button);
        // inputTextY = (EditText) findViewById(R.id.InputTextY);
        graphView = (GraphView) findViewById(R.id.graph);

        dbh= new DatabaseHandler(this);
        sqLiteDatabase = dbh.getWritableDatabase();

        graphView.addSeries(dataseries);
        graphView.getGridLabelRenderer().setNumHorizontalLabels(4);
        //por os dados logo de inicio
        dataseries.resetData(grabData());




        insertData();



    }


    public void insertData()
    {
        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long xValue = new Date().getTime();
                TextView spo2Value = ConnectFragment.ok;
                String value = spo2Value.getText().toString();

                int yValue= Integer.parseInt(value);


                dbh.insertToData(xValue, yValue);

                dataseries.resetData(grabData());

                graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if (isValueX) {
                            // show normal x values
                            return sdf.format(new Date((long)value));
                        } else {
                            // show currency for y values
                            return super.formatLabel(value, isValueX) ;
                        }
                    }
                });



            }
        });

    }


    private DataPoint[] grabData ()
    {
        String [] column = {"xValue", "yValue"};
        @SuppressLint("Recycle") Cursor cursor =sqLiteDatabase.query("Table1", column, null, null, null, null, null);

        DataPoint[] dataPoints = new DataPoint[cursor.getCount()];

        for (int i =0 ; i <cursor.getCount(); i++)
        {
            cursor.moveToNext();
            dataPoints[i]=new DataPoint(cursor.getLong(0),cursor.getInt(1));
        }

        return dataPoints;
    }
}



