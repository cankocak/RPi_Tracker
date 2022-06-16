package com.cankocak.rpi_tracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class FeedViewModel extends ViewModel {
    private static final String SETTINGS_FILENAME = "rpi_track_settings";
    private static final String WORK_REQUEST_TAG = "rpi_track_periodic_feed_request";

    private Context context;

    public static DateTimeFormatter dateFormatter = DateTimeFormatter.RFC_1123_DATE_TIME; // ToDo: Make this configurable, also add it to saved content

    // Saved content
    private ZonedDateTime latestUpdateDate = null;
    private String urlStr = null;
    private Set<String> filterKeywords = null;
    private int updateInterval = 0; // minutes

    private List<RSS_Item> feed;
    private RSS_Parser parser;

    private Timer timer;
    private boolean periodicWorkerStarted = false;
    private int notification_id;

    private boolean initialized = false;

    private MutableLiveData<Integer> liveFeedUpdatedFlag;

    public void init(Context context) {
        this.context = context;
        // Get preferences
        loadPreferences();
        if (this.urlStr == null) {
            this.urlStr = "https://rpilocator.com/feed/";
        }
        if (this.filterKeywords == null) {
            this.filterKeywords = new HashSet<String>();
        }
        this.parser = new RSS_Parser();
        this.liveFeedUpdatedFlag = new MutableLiveData<>();

        this.notification_id = 100;

        this.initialized = true;
    }

    public MutableLiveData<Integer> getLiveFeedUpdatedFlag() {
        return liveFeedUpdatedFlag;
    }

    public List<RSS_Item> getFeed() {
        return feed;
    }

    public void updateFeed() {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlStr,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            feed = parser.parse(response);
                            Collections.sort(feed, Collections.reverseOrder());
                            if (periodicWorkerStarted) {
                                Log.i("RPI_Tracker", "Sending notifications latestUpdate: " + (latestUpdateDate != null ? latestUpdateDate.format(FeedViewModel.dateFormatter) : "null"));
                                sendNotifications();
                            }
                            latestUpdateDate = feed.get(0).getDate();
                            savePreferences();
                            liveFeedUpdatedFlag.postValue(feed.size());
                        } catch (IOException e) {
                            Toast.makeText(context, "RPI_Tracker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            liveFeedUpdatedFlag.postValue(-1);
                        } catch (XmlPullParserException e) {
                            Toast.makeText(context, "RPI_Tracker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            liveFeedUpdatedFlag.postValue(-1);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "RPI_Tracker: " + error.toString(), Toast.LENGTH_SHORT).show();
                        liveFeedUpdatedFlag.postValue(-1);
                    }
                });
        queue.add(stringRequest);
    }

    private void loadPreferences() {
        SharedPreferences sharedPref = context.getSharedPreferences(SETTINGS_FILENAME, Context.MODE_PRIVATE);
        this.urlStr = sharedPref.getString("RPI_TRACK_URL", null);
        String latestUpdateDateStr = sharedPref.getString("RPI_TRACK_DATE", null);
        this.latestUpdateDate = (latestUpdateDateStr != null) ? ZonedDateTime.parse(latestUpdateDateStr, FeedViewModel.dateFormatter) : null;
        this.filterKeywords = sharedPref.getStringSet("RPI_TRACK_KEYWORDS", null);
        this.updateInterval = sharedPref.getInt("RPI_TRACK_UPDATE", 0);
    }

    public void savePreferences() {
        SharedPreferences sharedPref = context.getSharedPreferences(SETTINGS_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("RPI_TRACK_URL", urlStr);
        if (latestUpdateDate != null) {
            editor.putString("RPI_TRACK_DATE", latestUpdateDate.format(FeedViewModel.dateFormatter));
        }
        editor.putStringSet("RPI_TRACK_KEYWORDS", filterKeywords);
        editor.putInt("RPI_TRACK_UPDATE", updateInterval);
        editor.apply();
    }

    public String getUrlStr() {
        return urlStr;
    }

    public void setUrlStr(String urlStr) {
        this.urlStr = urlStr;
    }

    public Set<String> getFilterKeywords() {
        return filterKeywords;
    }

    public void setFilterKeywords(Set<String> filterKeywords) {
        this.filterKeywords = filterKeywords;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public void setPreferences(String urlStr, Set<String> filterKeywords, int updateInterval) {
        setUrlStr(urlStr);
        setFilterKeywords(filterKeywords);
        setUpdateInterval(updateInterval);
        savePreferences();
        if (isPeriodicWorkerWorking()) {
            stopPeriodicWorker();
            startPeriodicWorker();
        }
    }

    public void startPeriodicWorker() {
        if (this.timer == null && updateInterval != 0) {
            this.periodicWorkerStarted = true;
            //Timer thread
            this.timer = new Timer();
            long updateIntervalMillis = updateInterval * 60000;
            this.timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateFeed();
                }
            }, updateIntervalMillis, updateIntervalMillis);
            Log.i("RPI_Tracker", "Timer scheduled: " + updateIntervalMillis + "ms");
        }
    }

    public void stopPeriodicWorker() {
        if (this.timer != null) {
            this.periodicWorkerStarted = false;
            this.timer.cancel();
            this.timer = null;
            Log.i("RPI_Tracker", "Timer cancelled");
        }
    }

    public boolean isPeriodicWorkerWorking() {
        return this.timer != null;
    }

    public void sendNotifications() {
        for (RSS_Item feedItem : feed) {
            if (latestUpdateDate == null || feedItem.getDate().compareTo(latestUpdateDate) > 0) {
                if (filterKeywords == null || filterKeywords.size() == 0) {
                    sendNotification(feedItem.getDevice() + " (" + feedItem.getCountry() + ")",
                            feedItem.getTitle(),
                            feedItem.getLink());
                } else {
                    for (String keyword : filterKeywords) {
                        if (feedItem.getTitle().contains(keyword) ||
                                feedItem.getDevice().contains(keyword) ||
                                feedItem.getCountry().contains(keyword) ||
                                feedItem.getVendor().contains(keyword)) {
                            sendNotification(feedItem.getDevice() + " (" + feedItem.getCountry() + ")",
                                    feedItem.getTitle(),
                                    feedItem.getLink());
                            break;
                        }
                    }
                }
            }
        }
    }

    public void sendNotification(String title, String text, String link) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0,
                browserIntent,
                PendingIntent.FLAG_IMMUTABLE);
        final String NOTIFICATION_CHANNEL_ID = "NEW_RPi_ALERT";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_rpi)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(false);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                            "My Notifications", NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel
            notificationChannel.setDescription("Sample Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(this.notification_id++, notificationBuilder.build());
    }

    public boolean isInitialized() {
        return initialized;
    }
}
