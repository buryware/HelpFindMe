/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.buryware.firebase.geofirebase;

import com.firebase.geofire.GeoLocation;

public class FriendlyMessage {

    private String id;
    private String text;
    private String status;
    private String username;
    private String password;
    private Long minutesleft;
    private GeoLocation latlong;

    public FriendlyMessage(String mUid, String mUsername, String mPassword, String s, String mMinutes, String findyou, String timeStamp) {
    }

    public FriendlyMessage(String status, String text, String username, String password, Long minutesleft, GeoLocation latlong) {
        this.status = status;
        this.username = username;
        this.password = password;
        this.minutesleft = minutesleft;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getusername() {
        return username;
    }

    public void setusername(String username) {
        this.username = username;
    }

    public String getpassword() {
        return password;
    }

    public void setpassword(String photoUrl) {
        this.password = password;
    }

    public Long getminutesleft() {
        return minutesleft;
    }

    public void setminutesleft(Long imageUrl) {
        this.minutesleft = minutesleft;
    }

    public GeoLocation getlatlong() {
        return latlong;
    }

    public void setlatlong(GeoLocation latlong) {
        this.latlong = latlong;
    }
}
