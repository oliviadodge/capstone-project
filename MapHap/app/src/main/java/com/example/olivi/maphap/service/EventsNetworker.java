package com.example.olivi.maphap.service;

import android.net.Uri;
import android.util.Log;

import com.example.olivi.maphap.utils.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

/**
 * Created by olivi on 1/4/2016.
 *
 * Must be called from a background thread (e.g. MapHapService.onHandleIntent()) so that
 * network operation may remain off the main (UI) thread.
 */
public class EventsNetworker {

    public static final String LOG_TAG = EventsNetworker.class.getSimpleName();

    private static final String EXPANSIONS = "logo,venue,category";

    private static EventsNetworker sInstance;

    private HttpRequest mRequest;
    private Callback mCallback;
//    private final StethoURLConnectionManager stethoManager;


    public static synchronized EventsNetworker getsInstance(HttpRequest request, Callback callback) {
        if (sInstance == null) {
            sInstance = new EventsNetworker(request, callback);
        } else {
            sInstance.setRequest(request);
            sInstance.setCallback(callback);
        }
        return sInstance;
    }

    private EventsNetworker(HttpRequest request, Callback callback) {
        mRequest = request;
        mCallback = callback;
//        stethoManager = new StethoURLConnectionManager(request.friendlyName);
    }

    private void setRequest(HttpRequest request) {
        Log.i(LOG_TAG, "setting up new request");
        mRequest = request;
    }

    private void setCallback(Callback callback) {
        Log.i(LOG_TAG, "setting up new callback");
        mCallback = callback;
    }

    public void execute() {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String eventsJsonStr = null;

        StringBuilder sb = new StringBuilder();

        for (String category : mRequest.categories) {
            sb.append(category).append(",");
        }
        //Delete the last comma
        sb.deleteCharAt(sb.length() - 1);


        try {
            // Construct the URL for the Eventbrite query
            // Possible parameters are avaiable at
            // https://www.eventbrite.com/developer/v3/endpoints/events/
            final String EVENTS_BASE_URL =
                    "https://www.eventbriteapi.com/v3/events/search/";
            final String SEARCH_QUERY_PARAM = "q";
            final String LATITUDE_PARAM = "location.latitude";
            final String LONGITUDE_PARAM = "location.longitude";
            final String WITHIN_PARAM = "location.within";
            final String CATEGORIES_PARAM = "categories";
            final String POPULAR_PARAM = "popular";
            final String EXPAND_PARAM = "expand";
            final String OAUTH_TOKEN = "token";

            Uri builtUri = Uri.parse(EVENTS_BASE_URL).buildUpon()
                    .appendQueryParameter(LATITUDE_PARAM, Double.toString(mRequest.latitude))
                    .appendQueryParameter(LONGITUDE_PARAM, Double.toString(mRequest.longitude))
                    .appendQueryParameter(WITHIN_PARAM, Integer.toString(mRequest.radius) + "mi")
                    .appendQueryParameter(CATEGORIES_PARAM, sb.toString())
                    .appendQueryParameter(POPULAR_PARAM, Boolean.toString(Constants.RETURN_POPULAR))
                    .appendQueryParameter(EXPAND_PARAM, EXPANSIONS)
                    .appendQueryParameter(OAUTH_TOKEN, mRequest.authToken)
                    .build();

            URL url = new URL(builtUri.toString());

            Log.i(LOG_TAG, "URL to access eventbrite: " + builtUri);

            // Create the request to Eventbrite, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(mRequest.method.toString());
//            stethoManager.preConnect(urlConnection, null);

            urlConnection.connect();
//            stethoManager.postConnect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();

            //Let stetho see this stream
//            inputStream = stethoManager.interpretResponseStream(inputStream);

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            eventsJsonStr = buffer.toString();

            HttpResponse result = new HttpResponse(urlConnection.getResponseCode(), eventsJsonStr);

            mCallback.onResponse(result);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point in attempting
            // to parse it.
//            stethoManager.httpExchangeFailed(e);

            mCallback.onFailure(e);

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

    }


    public static class HttpRequest {
        public final String friendlyName;
        public final double latitude;
        public final double longitude;
        private Set<String> categories;
        public final int radius;
        public final HttpMethod method;
        public final String authToken;
        public final String body;

        public static Builder newBuilder() {
            return new Builder();
        }

        HttpRequest(Builder b) {
            if (b.method != HttpMethod.GET) {
                throw new IllegalArgumentException("GET is the only method allowed for this operation");
            } else if (b.body != null) {
                throw new IllegalArgumentException("GET cannot have a body");
            }

            this.friendlyName = b.friendlyName;
            this.latitude = b.latitude;
            this.longitude = b.longitude;
            this.categories = b.categories;
            this.radius = b.radius;
            this.method = b.method;
            this.authToken = b.authToken;
            this.body = b.body;
        }

        public static class Builder {
            private String friendlyName;
            private double latitude;
            private double longitude;
            private Set<String> categories;
            private int radius;
            private EventsNetworker.HttpMethod method;
            private String authToken;
            private String body = null;

            Builder() {
            }
            public Builder friendlyName(String friendlyName) {
                this.friendlyName = friendlyName;
                return this;
            }
            public Builder latitude(double latitude) {
                this.latitude = latitude;
                return this;
            }
            public Builder longitude(double longitude) {
                this.longitude = longitude;
                return this;
            }
            public Builder categories(Set<String> categories) {
                this.categories = categories;
                return this;
            }
            public Builder radius(int radius) {
                this.radius = radius;
                return this;
            }
            public Builder method(EventsNetworker.HttpMethod method) {
                this.method = method;
                return this;
            }
            public Builder authToken(String token) {
                this.authToken = token;
                return this;
            }
            public HttpRequest build() {
                return new HttpRequest(this);
            }
        }
    }

    public enum HttpMethod {
        GET
    }

    public static class HttpResponse {
        public final int statusCode;
        public final String body;

        HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }

    public interface Callback {
        void onResponse(HttpResponse result);
        void onFailure(IOException e);
    }
}
