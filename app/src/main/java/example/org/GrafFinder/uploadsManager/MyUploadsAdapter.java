package example.org.GrafFinder.uploadsManager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import example.org.GrafFinder.R;

/**
 * Created by Tomas on 2016-12-02.
 */

public class MyUploadsAdapter extends ArrayAdapter<SingleUpload> {

    public MyUploadsAdapter(Context context, int recource){
        super(context, recource);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater c = LayoutInflater.from(getContext());
        View view = c.inflate(R.layout.uploads_item, parent, false);
        ImageView upload = (ImageView) view.findViewById(R.id.upload);
        SingleUpload singleUpload = getItem(position);
        upload.setImageBitmap(singleUpload.getImg());
        if (!singleUpload.isStatus())
            upload.setColorFilter(Color.argb(200,158, 158 ,158));
        return view;
    }
}
