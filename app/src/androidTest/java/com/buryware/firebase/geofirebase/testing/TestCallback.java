/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.buryware.firebase.geofirebase.testing;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.firebase.database.DatabaseError;

import org.junit.Assert;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Locale.US;

/**
 * This is a test callback for the LocationCallback interface.
 * It allows you to verify that a certain value was set on the given location.
 */
public final class TestCallback implements LocationCallback {
    public static String location(String key, double latitude, double longitude) {
        return String.format(US, "LOCATION(%s,%f,%f)", key, latitude, longitude);
    }

    public static String noLocation(String key) {
        return String.format("NO_LOCATION(%s)", key);
    }

    private final SimpleFuture<String> future = new SimpleFuture<>();

    /** Timeout in seconds. */
    public final long timeout;

    public TestCallback() {
        this(GeoFireTestingRule.DEFAULT_TIMEOUT_SECONDS);
    }

    public TestCallback(final long timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the callback value as a string in a blocking fashion.
     * It's one of the values that are returned by the static factory methods above.
     */
    public String getCallbackValue() throws InterruptedException, TimeoutException {
        return future.get(timeout, TimeUnit.SECONDS);
    }

    @Override
    public void onLocationResult(String key, GeoLocation location) {
        if (future.isDone()) {
            throw new IllegalStateException("Already received callback");
        }

        if (location != null) {
            future.put(location(key, location.latitude, location.longitude));
        } else {
            future.put(noLocation(key));
        }
    }

    @Override
    public void onCancelled(DatabaseError firebaseError) {
        Assert.fail("Firebase synchronization failed: " + firebaseError);
    }
}
