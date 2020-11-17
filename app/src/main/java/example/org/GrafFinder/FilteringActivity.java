package example.org.GrafFinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class FilteringActivity extends MapsActivity {

    CheckBox tags;
    CheckBox nearby;
    CheckBox showAll;
    Switch seen;
    SeekBar distance;
    boolean isTags = false;
    boolean isNearby = false;
    boolean isSeen = false;
    boolean isAll = false;
    ArrayList checked;
    TextView seekBarValue;
    Button saveFilters;
    int kmCondition=-1;
    int showCondition = -1;
    EditText tagInput;

    public static ArrayList<String> filteredIDs;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filterting);
        final RelativeLayout filters = (RelativeLayout)findViewById(R.id.chooseFiltersLayout);
        tags = (CheckBox)findViewById(R.id.checkBoxTags);
        nearby= (CheckBox)findViewById(R.id.checkBoxNearby);
        seen =(Switch)findViewById(R.id.switch1);
        distance = (SeekBar) findViewById(R.id.seekBarDistance);
        seekBarValue = (TextView)findViewById(R.id.seekBarValue);
        saveFilters = (Button)findViewById(R.id.buttonSave);
        showAll = (CheckBox)findViewById(R.id.checkSeen);
        tagInput = (EditText) findViewById(R.id.tInput);
        checked = new ArrayList();

        tagInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checked.clear();
                Intent i = new Intent(FilteringActivity.this, TagChoosingActivity.class);
                String selected = tagInput.getText().toString();
                i.putExtra("TAGS", selected);
                startActivityForResult(i, 2);

            }
        });
        showAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAll){
                    seen.setVisibility(View.VISIBLE);
                    showAll.setText("Show only unseen");
                    isSeen = false;
                    showCondition = 0;  // nematyti
                    isAll = true;
                }
                else{
                    showAll.setText("Show only seen/unseen");
                    seen.setVisibility(View.INVISIBLE);
                    showCondition =-1;  //visi
                    isAll= false;
                }
            }
        });
        nearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNearby){
                    seekBarValue.setVisibility(View.VISIBLE);
                distance.setVisibility(View.VISIBLE);
                isNearby=true;
                }
                else{
                    seekBarValue.setVisibility(View.INVISIBLE);
                distance.setVisibility(View.INVISIBLE);
                    isNearby=false;
                }
            }
        });
        distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue.setText(String.valueOf(progress));
                kmCondition = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        tags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tags.isChecked()){
                    tagInput.setEnabled(true);}
                else {
                    tagInput.setEnabled(false);;
                }
            }
        });
        seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSeen){
                    showAll.setText("Show only seen");
                    showCondition = 1;  // matyti
                    isSeen = true;}
                else {
                    showAll.setText("Show only unseen");
                    showCondition = 0;  // nematyti
                    isSeen=false;
                }
            }
        });
        saveFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nearby.isChecked())
                    kmCondition = Integer.parseInt(seekBarValue.getText().toString());
                else
                kmCondition = -1;

                if(!tags.isChecked()) checked.clear();

                LocalDatabase db = new LocalDatabase(FilteringActivity.this, null, null, 1);
                filteredIDs = db.getFilteredIDSet(checked, MapsActivity.currentlatLng,showCondition, kmCondition);
                Toast.makeText(FilteringActivity.this, "saved filters: "+ Arrays.asList(checked)+"\n"+kmCondition+"\n"+showCondition, Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();
                setResult(Activity.RESULT_OK,intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 2){
        if(resultCode == Activity.RESULT_OK){
            String selected = "";
            for(String tag : TagChoosingActivity.allTags){
                checked.add(tag);
                selected+=tag+" ";
            }
            tagInput.setText(selected);
        }
    }
    }
}

