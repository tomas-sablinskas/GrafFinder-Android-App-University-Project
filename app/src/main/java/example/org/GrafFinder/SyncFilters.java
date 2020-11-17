package example.org.GrafFinder;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import example.org.GrafFinder.remoteDatabase.InsertValues;

public class SyncFilters extends FragmentActivity implements View.OnClickListener{

    private CheckBox newest;
    private CheckBox myUploads;
    private CheckBox popular;
    private EditText inputCount;
    private Button btnContinue;
    private EditText inputTop;

    private boolean uploads;

    private Context context;

    public static int newestCount;
    public static String usertoken;
    public static int topNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_filters);
        newest=(CheckBox) findViewById(R.id.btnNewest);
        myUploads=(CheckBox) findViewById(R.id.btnUploads);
        popular =(CheckBox) findViewById(R.id.btnPopular);
        inputCount=(EditText)findViewById(R.id.txtNewest);
        btnContinue=(Button)findViewById(R.id.btnContinue);
        inputTop=(EditText)findViewById(R.id.txtPop);

        newest.setOnClickListener(this);
        myUploads.setOnClickListener(this);
        popular.setOnClickListener(this);
        btnContinue.setOnClickListener(this);
        context = getBaseContext();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnNewest:
                if(newest.isChecked())
                    inputCount.setEnabled(true);
                else
                    inputCount.setEnabled(false);
                break;
            case R.id.btnUploads:
                if(myUploads.isChecked())
                setUploads(true);
                else
                setUploads(false);
                break;
            case R.id.btnPopular:
                if(popular.isChecked())
                inputTop.setEnabled(true);
                else inputTop.setEnabled(false);
                break;
            case R.id.btnContinue:
                getNewestCount();
                getToken();
                getTopx();
                Intent intent = getIntent();
                setResult(1,intent);
                finish();
             //   Toast.makeText(SyncFilters.this, getToken()+" "+getNewestCount()+" "+getTopx(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void getNewestCount() {
        if(newest.isChecked()){
            if(inputCount.getText().toString().equalsIgnoreCase(""))
                newestCount = 0;
            else
            newestCount =  Integer.parseInt(inputCount.getText().toString());
        return;
        }
        newestCount = 0;
    }

    public void getTopx() {
        if(popular.isChecked()){
            if(inputTop.getText().toString().equalsIgnoreCase(""))
                topNum = 0;
            else topNum = Integer.parseInt(inputTop.getText().toString());
            return;
        }
        topNum = 0;
    }

    public void getToken(){
        if(this.uploads){
            SharedPreferences getToken = getSharedPreferences(WelcomeActivity.ACCOUNT_INFO,0);
            usertoken =  getToken.getString(WelcomeActivity.USER_TOKEN,null);
            return;
    }
    usertoken = null;
    }

    public void setUploads(boolean uploads) {
        this.uploads = uploads;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
