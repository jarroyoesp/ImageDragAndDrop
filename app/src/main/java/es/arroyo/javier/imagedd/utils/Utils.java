package es.arroyo.javier.imagedd.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.arroyo.javier.imagedd.activity.DragAndDropApplication;

/**
 * Created by JARROYO on 25/02/2015.
 *
 * Funciones:
 * - Save Images on disk
 * - GetBase64 from Image
 * - Get md5
 * - Delete image
 * - GetIMEI
 * - Display Image loading
 * - showDialogError
 */
public class Utils {
    //---------------------------------------------------------------------------------------------
    // GET OUTPUT MEDIA FILE URI
    //---------------------------------------------------------------------------------------------

    /**
     * Create a file Uri for saving an image or video
     */
    public static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }


    //---------------------------------------------------------------------------------------------
    // GET OUTPUT MEDIA FILE
    //---------------------------------------------------------------------------------------------

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "ImageDragAndDrop");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == DragAndDropApplication.MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == DragAndDropApplication.MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    //---------------------------------------------------------------------------------------------
    // GET BASE 64 FROM IMAGE
    //---------------------------------------------------------------------------------------------
    public static String getBase64FromImage(String path) {
        try {
            Bitmap bm = BitmapFactory.decodeFile(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 50, baos); //bm is the bitmap object
            byte[] byteArrayImage = baos.toByteArray();

            String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
            encodedImage = URLEncoder.encode(encodedImage, "utf-8");
            bm.recycle();

            return encodedImage;
        } catch (Exception e) {
            return null;
        }
    }

    //-----------------------------------------------------------------------------
    // DISPLAY IMAGES WITH LOADING
    //-----------------------------------------------------------------------------
    public static void displayImageLoading(String path, ImageView imageView, final View progressView) {
        ImageLoader.getInstance().displayImage(path, imageView, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(progressView!=null) {
                    progressView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if(progressView!=null) {
                    progressView.setVisibility(View.GONE);
                }

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(progressView!=null) {
                    progressView.setVisibility(View.GONE);
                }
            }
        });
    }

   //------------------------------------------------------------------------------------------
   // GET REAL PATH FROM URI
   //------------------------------------------------------------------------------------------
   public static String getRealPathFromURI(Context context, Uri contentUri) {
       Cursor cursor = null;
       try {
           String[] proj = { MediaStore.Images.Media.DATA };
           cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
           int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
           cursor.moveToFirst();
           return cursor.getString(column_index);
       } finally {
           if (cursor != null) {
               cursor.close();
           }
       }
   }

    public static Bitmap getResizedBitmap(Bitmap bitmap, float scale){
        int newWidth = (int)(bitmap.getWidth()*0.5);
        int newHeight = (int)(bitmap.getHeight()*0.5);

        return bitmap.createScaledBitmap(bitmap, newWidth,newHeight,false);
        //int width = bitmap.getWidth();
        //int height = bitmap.getHeight();
//
        //float scaleWidth = ((float) newWidth)/width;
        //float scaleHeight = ((float) newHeight)/height;
//
        //Matrix matrix = new Matrix();
        //matrix.postScale(scaleWidth, scaleHeight);
//
        //Bitmap resized = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,false);
//
        //return resized;
    }

}
