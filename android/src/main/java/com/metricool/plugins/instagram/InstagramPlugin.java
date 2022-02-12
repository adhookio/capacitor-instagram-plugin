package com.metricool.plugins.instagram;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.getcapacitor.JSArray;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

@CapacitorPlugin(name = "Instagram")
public class InstagramPlugin extends Plugin {

    public static final String SOURCE_APPLICATION = "io.adhook";
    private static final String LOGTAG = "InstagramPlugin";

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
            Intent shareIntent;
            if (TARGET_STORY.equalsIgnoreCase(target)) {
                shareIntent = createStoryIntent(mediaType, medias);
            } else {
                shareIntent = createFeedIntent(mediaType, medias);
            }

            shareIntent.setPackage("com.instagram.android");
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Log.v(LOGTAG, "Sharing to Instagram', '" + mediaType + "', '" + medias + "'");
            startActivityForResult(call, shareIntent, "shareResultCallback");

        } catch (Exception e) {
            call.reject("Internal error: " + e.getMessage());
        }
    }


    private Intent createStoryIntent(String mediaType, JSArray medias) throws JSONException {
        String action = "com.instagram.share.ADD_TO_STORY";

        Intent shareIntent = new Intent(action);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        File file = new File(medias.getString(0));
        Uri uri = getAuthorizedUriForFile(file);
        shareIntent.setDataAndType(uri, "image/*");

        return shareIntent;
    }

    private Uri getAuthorizedUriForFile(File file) {
        AppCompatActivity activity = this.getActivity();

        // Really not clean, as it depends on the package in our MainActivity program.
        String authority = "io.adhook.fileprovider";

        Uri uri = FileProvider.getUriForFile(activity, authority, file);
        return uri;
    }

    private Intent createFeedIntent(String mediaType, JSArray medias) throws JSONException {
        String action = "com.instagram.share.ADD_TO_FEED";
        Intent shareIntent = new Intent(action);
        if (mediaType.toLowerCase().contains("mp4") || mediaType.toLowerCase().contains("mov")) {
            shareIntent.setType("video/" + mediaType);
        } else {
            shareIntent.setType("image/" + mediaType);
        }
        File file = new File(medias.getString(0));
        Uri uri = Uri.fromFile(file);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        return shareIntent;
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
