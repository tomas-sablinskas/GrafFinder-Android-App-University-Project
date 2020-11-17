package example.org.GrafFinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Class for image processing.
 */

public class ImageProcessing {

    public static Bitmap convertToBitmap(byte[] bytes){
        Bitmap image = null;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        image = BitmapFactory.decodeStream(inputStream);
        return image;
    }

    public static byte[] convertToByteArray(Bitmap image){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
        byte[] bytes = outputStream.toByteArray();
        return bytes;
    }
    public static Uri convertToUri(Context context, Bitmap image){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
        String uri = MediaStore.Images.Media.insertImage(context.getContentResolver(),image, "Title","Description");
        return Uri.parse(uri);
    }
}
