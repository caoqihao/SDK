package com.shafa.market.sdk;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

//import com.shafa.market.library.AppUpdate;
import com.shafa.market.library.ShafaMarket;
import com.shafa.update.ShafaUpdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ShafaMarket.start(MainActivity.this.getApplicationContext() , "com.xiaoji.tvbox");
                String appKey = "58353653358e1fa22d8b4568";
                String secretKey = "jN0SCem72hOA56jGg5A5nQP6fGT89Gh1";

//                ShafaMarket.checkUpdate(MainActivity.this.getApplicationContext() , null);

                ShafaUpdate.update(getApplicationContext(),appKey,secretKey);

            }
        });
    }


    private byte[] getBytes(){
        Log.i("myLog" ,"---:  " + Environment.getExternalStorageDirectory() + "/esShare/bg.jpg");
        File file = new File(Environment.getExternalStorageDirectory() + "/esShare/bg.jpg");
        int size = (int) file.length();
        Log.i("myLog" , "size :  " + size);
        byte[] bytes = new byte[size];
        try {
            new FileInputStream(file).read(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }





}
