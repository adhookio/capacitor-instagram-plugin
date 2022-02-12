package com.metricool.plugins.instagram;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.getcapacitor.JSArray;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

@CapacitorPlugin(name = "Instagram")
public class InstagramPlugin extends Plugin {

    private static final String LOGTAG = "InstagramPlugin";

    private static final String TARGET_FEED = "feed";
    private static final String TARGET_STORY = "story";

    @PluginMethod
    public void shareLocalMedia(PluginCall call) {
        JSArray medias = call.getArray("medias");
        if (medias == null || medias.length() == 0) {
            call.reject("Missing 'medias' argument");
            return;
        }

        String mediaType = call.getString("mediaType");
        if (mediaType == null || mediaType.length() == 0) {
            call.reject("Missing 'mediaType' argument");
            return;
        }


        String target = call.getString("target");
        if (target == null || target.length() == 0) {
            call.reject("Missing 'target' argument");
            return;
        }

        Log.d(LOGTAG, "shareLocalMedia: '" + medias + "', '" + mediaType + "', '" + target + "'");
        shareLocalMedia(call, medias, mediaType, target);
    }

    private void shareLocalMedia(PluginCall call, JSArray medias, String mediaType, String target) {
        getBridge().saveCall(call);

        try {
            boolean multipleMedia = medias.length() > 1;

            // choose the right action
            String action = "com.instagram.share.ADD_TO_" + TARGET_FEED.toUpperCase();
            if (multipleMedia || TARGET_STORY.equalsIgnoreCase(target)) {
                // multiple medias are only enable to share in 'stories', so 'target' parameter is discarded.
                action = Intent.ACTION_SEND_MULTIPLE;
                multipleMedia = true;
            }

            Intent shareIntent = new Intent(action);
            if (mediaType.toLowerCase().contains("mp4") || mediaType.toLowerCase().contains("mov")) {
                shareIntent.setType("video/" + mediaType);
            } else {
                shareIntent.setType("image/" + mediaType);
            }

            if (multipleMedia) {
                ArrayList<Uri> uris = new ArrayList<Uri>();

                for (int i = 0; i < medias.length(); i++) {
                    File file = new File(medias.getString(i));
                    Uri uri = Uri.fromFile(file);
                    uris.add(uri);
                }
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            } else {
                File file = new File(medias.getString(0));
                Uri uri = Uri.fromFile(file);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            }

            shareIntent.setPackage("com.instagram.android");
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Log.v(LOGTAG, "Sharing '" + action + "', '" + mediaType + "', '" + medias + "'");
            startActivityForResult(call, shareIntent, "shareResultCallback");

        } catch (Exception e) {
            call.reject("Internal error: " + e.getMessage());
        }
    }

    @ActivityCallback
    private void shareResultCallback(PluginCall call, ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_CANCELED) {
            call.reject("Activity canceled");
        } else {
            call.resolve();
        }
    }
}
