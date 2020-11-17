package example.org.GrafFinder;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Arrays;

public class TagChoosingActivity extends FragmentActivity {

    String tags;
    static public String[] allTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_choosing);

        String existing = getIntent().getStringExtra("TAGS");

        tags = existing;

        EditText text = (EditText)findViewById(R.id.tagInput);
        text.append(tags);



        LocalDatabase db = new LocalDatabase(this, null, null, 1);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, db.getTagSet());

        Log.i("INFO", db.getTagSet().toString());


        ListView tagList = (ListView) findViewById(R.id.tagList);
        tagList.setAdapter(adapter);
        final EditText tagInput = (EditText) findViewById(R.id.tagInput);

        tagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedTag = adapter.getItem(position);


                String all = "";
                if(allTags!=null){
                int limit = (tags.endsWith(" "))? allTags.length:allTags.length-1;
                for(int i = 0; i < limit; i++){
                    all+=allTags[i]+" ";
                }}
                if(!all.contains(selectedTag))
                all+=selectedTag+" ";
                tagInput.setText("");
                tagInput.append(all);
            }
        });



        tagInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tags = s.toString();
                allTags = tags.split(" ");
                String currentTag = "";
                if(allTags.length!=0 && !(tags.endsWith(" "))){
                    currentTag = allTags[allTags.length-1];
                }

                adapter.getFilter().filter(currentTag);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Button save = (Button) findViewById(R.id.saveButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allTags = tagInput.getText().toString().split(" ");
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

    }
}
