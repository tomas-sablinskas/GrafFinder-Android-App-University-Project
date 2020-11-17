package example.org.GrafFinder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import example.org.GrafFinder.remoteDatabase.InsertValues;

public class DescriptionActivity extends MainActivity {

    private ImageView selected;
    private TextView sAuthor;
    private TextView sTime;
    private Button like;
    private Button dislike;
    private TextView sTags;
    private int currentId=0;
  //  private TextView sDescription;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //add back button

        Intent intent = getIntent();
        final String id =intent.getStringExtra("chosenID");   //get chosen graffiti id
        final InsertValues getHistory = new InsertValues(this);
        getHistory.getHistory(id);  //find chosen id history

        if(getHistory.icons.size() > 0) {
            final Gallery gallery = (Gallery) findViewById(R.id.gallery);
            gallery.setSpacing(1);  //space between images at the bottom
            LocalDatabase db = new LocalDatabase(this, null, null, 1);
            db.changeSeen(id);

            selected = (ImageView) findViewById(R.id.imageView4);
            sAuthor = (TextView)findViewById(R.id.txtAuthor);
            sTime = (TextView)findViewById(R.id.txtTime);
            sTags=(TextView)findViewById(R.id.txtTags);
         //   sDescription =(TextView)findViewById(R.id.txtDescription);
            like=(Button)findViewById(R.id.btnLike);
            dislike=(Button)findViewById(R.id.btnDislike);

            //information about history pictures
            final ArrayList<String> ids = getHistory.ids;
            final ArrayList<Bitmap> imageIcons = getHistory.icons;
            final ArrayList<String> imageAuthors = getHistory.authors;
            final ArrayList<String> imageTime = getHistory.dates;
            final ArrayList<String> imageDesc = getHistory.desciptions;
            final ArrayList<String> tags = getHistory.tags;

            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int isInserted = getHistory.addLike(ids.get(currentId));
                    if(isInserted == InsertValues.UNKNOWN_USER)
                        Toast.makeText(DescriptionActivity.this, "Unregistered users cannot rate graffities!", Toast.LENGTH_SHORT).show();
                    else if (isInserted == InsertValues.NOT_INSERTED)
                        Toast.makeText(DescriptionActivity.this, "Photo was already liked", Toast.LENGTH_SHORT).show();
                    else if(isInserted == InsertValues.INSERTED)
                    Toast.makeText(DescriptionActivity.this, "You've liked this photo!", Toast.LENGTH_SHORT).show();
                }
            });

            dislike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int isInserted = getHistory.addReport(ids.get(currentId));
                    if(isInserted == InsertValues.UNKNOWN_USER)
                        Toast.makeText(DescriptionActivity.this, "Unregistered users cannot report graffities!", Toast.LENGTH_SHORT).show();
                    else if(isInserted == InsertValues.NOT_INSERTED)
                        Toast.makeText(DescriptionActivity.this, "You've already reported this graffiti!", Toast.LENGTH_SHORT).show();
                    else if(isInserted == InsertValues.INSERTED)
                        Toast.makeText(DescriptionActivity.this, "You've reported this graffiti!", Toast.LENGTH_SHORT).show();
                }
            });


            final GalleryImageAdapter galleryImageAdapter = new GalleryImageAdapter(this, imageIcons);
            gallery.setAdapter(galleryImageAdapter);

            //when gallery is open for the 1st time, show newest photo's information
            selected.setImageBitmap(imageIcons.get(0));
            sAuthor.setText(imageAuthors.get(0));
            sTime.setText(imageTime.get(0));
            sTags.setText(tags.get(0));
       //     sDescription.setText(imageDesc.get(0));

            //when mini icon at the bottom is clicked, open that icon and show all the information of that photo
            gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    selected.setImageBitmap(imageIcons.get(position));
                    sAuthor.setText(imageAuthors.get(position));
                    sTime.setText(imageTime.get(position));
                    currentId = position;
                    sTags.setText(tags.get(position));
         //           sDescription.setText(imageDesc.get(position));
                }
            });
        }
        else {
            Toast.makeText(DescriptionActivity.this, "Graffiti was not found in remote database!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    class GalleryImageAdapter extends BaseAdapter {
        private Context mContext;
        ArrayList<Bitmap> miniIcons;

        public GalleryImageAdapter(Context context, ArrayList<Bitmap> images) {
            mContext = context;
            miniIcons = images;
        }

        public int getCount() { //count of all pictures in a list
            return miniIcons.size();}

        public Object getItem(int position) {
            return position;}

        public long getItemId(int position) {
            return position;}

        public View getView(int index, View view, ViewGroup viewGroup) {
            ImageView imageView = new ImageView(mContext);  //initialize ImageView
            imageView.setImageBitmap(miniIcons.get(index)); //add miniIcon at the bottom listview
            imageView.setLayoutParams(new Gallery.LayoutParams(200, 200));  //apacioje esanciu nuotrauku dydis
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);     //apacioje matomu nuotrauku dydis(isplesti ir suvienodinti)
            return imageView;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
