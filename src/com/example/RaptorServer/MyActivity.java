package com.example.RaptorServer;

import android.app.Activity;
import android.os.Bundle;

import java.io.*;

import ch.ethz.ssh2.*;

public class MyActivity extends Activity {




    private String remoteDir = "/../var/www/";
    // Look for a file path like "smoke20070128_wkt.txt"
    private String filePatternString = ".*/*.png";
    // Local directory to receive file
    private String localDir = "/sdcard/test";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        MyActivity app = new MyActivity();

        AsyncConnect conn = new AsyncConnect();

        conn.execute();


    }

}
