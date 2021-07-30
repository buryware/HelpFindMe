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
 *  *
 *  * Help Find Me! is written by Steve Stansbury, for the Buryware Company
 *  * Created September 2, 2020 by Buryware.
 *  * All rights reservered.
 *  *
 */
package com.buryware.firebase.geofirebase;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;

public class FriendlyMessage {

    private String id;
    private String helpid;
    private String minutesleft;
    private String msgType;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String lat;
    private String longi;
    private String fromto;
    private String timeStamp;

    private String device_id;
    private String timestamp;

    public FriendlyMessage() {
    }

    public FriendlyMessage(String mUid, String username, String password, String email, String phone, String status, String minutesleft, String lat, String longi, String timeStamp, String helpid, String fromto) {
    }

    public FriendlyMessage(String username, String password, String email, String phone, String msgType, String minutesleft, String lat, String longi, String timeStamp, String helpid, String fromtoString) {
        this.msgType = msgType;
        this.helpid = helpid;
        this.minutesleft = minutesleft;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.lat = lat;
        this.longi = longi;
        this.fromto = fromto;
        this.timeStamp = timeStamp;
        this.device_id = device_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String gethelpid() {
        return helpid;
    }

    public void sethelpid(String helpid) {
        this.helpid = helpid;
    }

    public String getmsgType() {
        return msgType;
    }

    public void setmsgType(String text) {
        this.msgType = text;
    }

    public String getFromTo() {
        return fromto;
    }

    public void setFromTo(String visible) {
        this.fromto = visible;
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

    public void setpassword(String password) {
        this.password = password;
    }

    public String getemail() {
        return email;
    }

    public void setemail(String email) {
        this.email = email;
    }

    public String getphone() {
        return phone;
    }

    public void setphone(String phone) {
        this.phone = phone;
    }

    public String getminutesleft() {
        return minutesleft;
    }

    public void setminutesleft(String minutesleft) {
        this.minutesleft = minutesleft;
    }

    public String gettimeStamp() {
        return timeStamp;
    }

    public String  getlat() {
        return lat;
    }

    public String getlongi() {
        return longi;
    }

    public LatLng getLatLng() {

        return new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));
    }

    public String getDeviceId() {
        return device_id;
    }

    public void setDeviceId(String device_id) {
        this.id = device_id;
    }

    //public void setlatlong(GeoLocation latlong) {
    //    this.latlong = latlong;
    //}
}
