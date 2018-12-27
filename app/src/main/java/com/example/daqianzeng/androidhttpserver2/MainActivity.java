package com.example.daqianzeng.androidhttpserver2;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {

    private WebServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);

        server = new WebServer();
        try {
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch(IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");

        String unzipPath = "http://10.4.80.61:8181/"+Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "web" + "/index.html";
        Log.e("***********************" , unzipPath);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(unzipPath));
        startActivity(intent);
    }

    private class WebServer extends NanoHTTPD {

        public WebServer()
        {
            super(8181);
        }

        @Override
        public Response serve(IHTTPSession session) {

            String mimeType = "";
            try {

                List<String> _urlPatternList = Arrays.asList(session.getUri().split("\\?"));

                if (_urlPatternList.get(0).endsWith(".js")) {
                    mimeType = "text/javascript";
                } else if (_urlPatternList.get(0).endsWith(".html")) {
                    mimeType = "text/html";
                } else if (_urlPatternList.get(0).endsWith(".css"))  {
                    mimeType = "text/css";
                } else if (_urlPatternList.get(0).endsWith(".gz"))  {
                    mimeType = "application/x-gzip";
                } else if (_urlPatternList.get(0).endsWith(".json"))  {
                    mimeType = "application/json";
                }

                // Open file from SD Card
                File root = Environment.getExternalStorageDirectory();
                String path = session.getUri();

                FileInputStream fis = new FileInputStream(path);

                Response respone = newChunkedResponse(Response.Status.OK, mimeType, fis);
                if (_urlPatternList.get(0).endsWith(".gz")) {
                    respone.addHeader("Content-Encoding", "gzip");
                }
                return respone;

            } catch(IOException ioe) {
                Log.w("Httpd", ioe.toString());
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found");
        }

    }
}
