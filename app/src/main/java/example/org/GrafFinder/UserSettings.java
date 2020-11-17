package example.org.GrafFinder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import example.org.GrafFinder.remoteDatabase.InsertValues;

/**
 * Created by Tomas on 2016-11-16.
 */

public class UserSettings extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button changeUsername = (Button)findViewById(R.id.changeName);
        changeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserSettings.this);

                builder.setTitle("Type in a new nickname:");
                final EditText text = new EditText(UserSettings.this);
                builder.setView(text);

                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = text.getText().toString();
                        SharedPreferences sharedPreferences = getSharedPreferences(WelcomeActivity.ACCOUNT_INFO,0);
                        sharedPreferences.edit().putString(WelcomeActivity.USER_NAME, newName)
                                .apply();
                        InsertValues values = new InsertValues(UserSettings.this);

                        values.changeUsername();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        Button logout = (Button)findViewById(R.id.logout);

        //final MainActivity  mainActivity = new MainActivity();
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WelcomeActivity.googleSignedIn)
                    signOut();
                if (WelcomeActivity.isGuest)
                    goLoginScreen();
                else
                    logout();
            }
        });

    }
}
