package example.org.GrafFinder;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ListViewActivity extends ArrayAdapter<String> {

    private Activity context;
 //   private Bitmap[] images;
    private ArrayList<String> locations;
    private ArrayList<String> authors;
    ArrayList<LatLng> latsLngs;
    private ArrayList<String> IDs;
    private ArrayList<Bitmap> icons;
    public static final int RECENTS = 0;
    public static final int ROUTE =1;
    public static final int EXISTING = 2;
    private int mode;
    public static int startPos =-1;
    public static int endPos = -1;
    private boolean inStart = true;
    public String selected = "";

    /**
     * Constructor, initializes all class variables.
     * @param context Context to initialize
     * @param locations String array of locations to initialize
     * @param images Bitmap array of images to initialize
     * @param authors String array of authors to initialize
     */
    public ListViewActivity(Activity context, ArrayList<String> locations, ArrayList<Bitmap> images, ArrayList<String> authors, ArrayList<LatLng> latsLngs,ArrayList<String> IDs, int mode) {
        super(context, R.layout.graffiti_item, IDs);

        for(LatLng a:latsLngs){
            Log.i("LAT", a.toString());
        }

        this.context = context;
        this.locations = locations;
        this.icons = images;
        this.authors = authors;
        this.latsLngs = latsLngs;
        this.IDs = IDs;
        this.mode = mode;

   //     Log.d("INFORMACIJA","ids: "+ Arrays.asList(IDs));
    //    Log.d("INFORMACIJA"," latsLngs: "+Arrays.asList(latsLngs));
    }

    public ListViewActivity(Activity context,ArrayList<String> locations, ArrayList<Bitmap> images, ArrayList<String> authors, ArrayList<LatLng> latsLngs,ArrayList<String> IDs, int mode, boolean inStart){
        this(context,locations,images,authors,latsLngs,IDs,mode);
        this.inStart = inStart;
    }
    public ListViewActivity(Activity context, ArrayList gids, ArrayList icons){
        super(context,R.layout.existing_item, icons);
        this.context = context;
        this.mode = this.EXISTING;
        this.IDs = gids;
        this.icons = icons;
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @param view
     * @param parent
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(final int position, final View view, final ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View singleItem = null;

        if (this.mode == ROUTE) {
            View route = inflater.inflate(R.layout.route_item,null,true);
            singleItem = caseRoute(position, route);
        }
        else if(this.mode == RECENTS){
            View graffiti = inflater.inflate(R.layout.graffiti_item,null,true);
            singleItem = caseRecents(position,graffiti);
        }
        else if(this.mode == EXISTING){
            View existing = inflater.inflate(R.layout.existing_item,null,true);
            singleItem = caseExisting(position,existing);
        }
        return singleItem;
    }

    /**
     *  Initialize all widgets for getView method if mode is ROUTE.
     * @param position position of current element.
     * @param singleElement view of current element.
     * @return initialized view.
     */
    private View caseRoute(final int position, final View singleElement){
        final ViewHolderRoute holder = new ViewHolderRoute();
        //initialize each widget in ViewHolderRoute
        holder.btnImage = (ImageButton) singleElement.findViewById(R.id.routeImage);
        holder.btnRemove = (ImageButton)singleElement.findViewById(R.id.btnDelete);
        holder.txtAddress = (TextView)singleElement.findViewById(R.id.routeAddress);
        holder.txtAddress.setText(locations.get(position));

        holder.btnImage.setImageBitmap(icons.get(position));
        holder.btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inStart){
                    startPos = position;
                endPos =-1;}
                else
                    endPos=position;
                notifyDataSetChanged(); //refresh the list view
            }
        });
        if(inStart) {
            if (position == startPos)
                holder.btnImage.clearColorFilter(); // if this position is start or end position, then make it colorful.
            else
                holder.btnImage.setColorFilter(Color.argb(150, 150, 150, 150));    //otherwise make it gray.
        }
        else {
            if (position == endPos)
                holder.btnImage.clearColorFilter();
            else
                holder.btnImage.setColorFilter(Color.argb(150,150,150,150));
        }

        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getCount() > 2){
                    LocalDatabase localDatabase = new LocalDatabase(context,null,null,1);
                    localDatabase.removeFromRoute(IDs.get(position));
                    locations.remove(position);
                    icons.remove(position);
                    authors.remove(position);
                    latsLngs.remove(position);
                    IDs.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "Item was removed!", Toast.LENGTH_SHORT).show();}
                else {
                    Toast.makeText(context, "You must have at least 2 graffiti points in your route!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        singleElement.setTag(holder);
        return singleElement;
    }

    /**
     * Initialize all widgets for getView method if mode is RECENTS.
     * @param position position of current element.
     * @param singleGraffiti view of current Graffiti.
     * @return initialized view.
     */
    private View caseRecents(final int position, final View singleGraffiti){
        final ViewHolderGraffiti holder = new ViewHolderGraffiti();

        //initialize each widget in ViewHoldGraffiti
        holder.image = (ImageView)singleGraffiti.findViewById(R.id.image);
        holder.toRoute = (Button)singleGraffiti.findViewById(R.id.btnToRoute);
        holder.txtAddress=(TextView)singleGraffiti.findViewById(R.id.address);
        holder.txtAuthor =(TextView)singleGraffiti.findViewById(R.id.author);

        //if "ADD TO THE ROUTE" button was clicked, add it to localDB.
        holder.toRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalDatabase db = new LocalDatabase(singleGraffiti.getContext(),null,null,1);
                if(db.addToRoute(IDs.get(position)))
                Toast.makeText(context, "Graffiti was added to the route", Toast.LENGTH_SHORT).show();
            }
        });

        holder.image.setImageBitmap(icons.get(position));
        holder.image.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.txtAuthor.setText(authors.get(position));
        holder.txtAddress.setText(locations.get(position));
        return singleGraffiti;
    }

    private View caseExisting(final int position, final View singleElement){
        final ViewHolderExistingImage holder = new ViewHolderExistingImage();
        holder.gid =(TextView)singleElement.findViewById(R.id.txtID);
        holder.image =(ImageButton)singleElement.findViewById(R.id.imageButton);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = IDs.get(position);
                Button btnContinue = (Button)context.findViewById(R.id.btnAdd);
                btnContinue.setText("Continue");
            }
        });
        holder.image.setImageBitmap(icons.get(position));
        holder.image.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.gid.setText(IDs.get(position));
        return singleElement;
    }

    /**
     * Class that holds information about one element in routeList listview.
     */
    static class ViewHolderRoute {
        TextView txtAddress;
        ImageButton btnImage;
        ImageButton btnRemove;
    }

    /**
     * Class that holds information about one element in graffitiList listview.
     */
    static class ViewHolderGraffiti{
        TextView txtAddress;
        TextView txtAuthor;
        ImageView image;
        Button toRoute;
    }

    static class ViewHolderExistingImage{
        TextView gid;
        ImageButton image;
    }
}
