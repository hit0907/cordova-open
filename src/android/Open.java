package com.disusered;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * This class starts an activity for an intent to view files
 */
public class Open extends CordovaPlugin {

  public static final String OPEN_ACTION = "open";

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals(OPEN_ACTION)) {
      String path = args.getString(0);
      this.chooseIntent(path, callbackContext);
      return true;
    }
    return false;
  }

  /**
   * Returns the MIME type of the file.
   *
   * @param path
   * @return
   */
  private static String getMimeType(String path) {
    String mimeType = null;

    String extension = MimeTypeMap.getFileExtensionFromUrl(path);
    if (extension != null) {
      MimeTypeMap mime = MimeTypeMap.getSingleton();
      mimeType = mime.getMimeTypeFromExtension(extension.toLowerCase());
    }

    return mimeType;
  }

  /**
   * Creates an intent for the data of mime type
   *
   * @param path
   * @param callbackContext
   */
  private void chooseIntent(String path, CallbackContext callbackContext) {
    if (path != null && path.length() > 0) {
      try {

        String mime = getMimeType(path);

        path = java.net.URLDecoder.decode(path, "UTF-8");
        Uri uri = Uri.parse(path);

        Intent fileIntent = new Intent(Intent.ACTION_VIEW);

        // Android O above
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {

          // Android 24 above not support path file:///
          path = path.replace("file:/","");


          File newFile = new File(path);

          Uri contentUri = FileProvider.getUriForFile(cordova.getActivity().getApplicationContext(),
                  cordova.getActivity().getPackageName() + ".fileProvider",
                  newFile);

          fileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

          fileIntent.setDataAndType(contentUri, mime);

          List<ResolveInfo> infoList = cordova.getActivity().getApplicationContext().getPackageManager().queryIntentActivities(fileIntent, PackageManager.MATCH_DEFAULT_ONLY);
          for (ResolveInfo resolveInfo : infoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            cordova.getActivity().getApplicationContext().grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
          }

        } else {
          fileIntent.setDataAndType(uri, mime);
          fileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        cordova.getActivity().startActivity(fileIntent);
//        cordova.getActivity().startActivity(Intent.createChooser(fileIntent, "Open File in..."));
        callbackContext.success();
      } catch (ActivityNotFoundException e) {
        e.printStackTrace();
        callbackContext.error(1);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    } else {
      callbackContext.error(2);
    }
  }
}
