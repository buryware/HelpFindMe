/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Help Find Me! is written by Steve Stansbury, for the Buryware Company
 * Created September 2, 2020 by Buryware.
 * All rights reservered.
 *
 */
package com.buryware.firebase.geofirebase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;


public class SplashScreen extends Activity {

    static private Boolean bEULA = false;  // sos todo hack to work
    private WebView mWebview;
    private Button mOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (bEULA) {
            setContentView(R.layout.activity_eula);

            mWebview = findViewById(R.id.webview);
            mWebview.loadUrl("https://www.iubenda.com/privacy-policy/92163031");
            mWebview.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setContentView(R.layout.activity_about);
                    DoHandleSplash();
                }
            });

            mOk = findViewById(R.id.btn_okay);
            mOk.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    setContentView(R.layout.activity_about);
                    DoHandleSplash();
                }
            });
            bEULA = false;

        } else {
            setContentView(R.layout.activity_about);
            DoHandleSplash();
        }
    }

    public void DoHandleSplash(){

        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(2000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent intent = new Intent(SplashScreen.this, MapsActivity.class);
                    startActivity(intent);
                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}