package example.org.GrafFinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import example.org.GrafFinder.SharedPrefs.PrefUtils;
import example.org.GrafFinder.SharedPrefs.User;
import example.org.GrafFinder.remoteDatabase.InsertValues;

public class WelcomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    public static int RC_SIGN_IN = 2;
    public static GoogleApiClient mGoogleApiClient;
    private SignInButton signInButton;
   // private GoogleSignInAccount acct;
    public static boolean isGuest = false;
    public static boolean googleSignedIn;
    private static final String TAG = "Login";
    SharedPreferences sharedpreferences;

    public static final String ACCOUNT_INFO = "accountInfoPrefFile";
    public static final String USER_TOKEN = "userToken";
    public static final String USER_NAME = "userName";
    public static final String IS_ARTIST = "isArtist";
    public static final String EMAIL = "email";
    User user;

    public static GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public static void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        WelcomeActivity.mGoogleApiClient = mGoogleApiClient;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        setmGoogleApiClient(new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build());
        setContentView(R.layout.activity_welcome);
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.loginButton);
        Button gButton = (Button) findViewById(R.id.guestButton);
        gButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                isGuest = true;
                goMainScreen();
            }
        });
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        loginButton.setReadPermissions("public_profile", "email", "user_friends");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            public void onSuccess(LoginResult loginResult) {

                AccessToken accessToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();

                Log.i("USER", "loged:"+ profile);
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                sharedpreferences = getSharedPreferences(ACCOUNT_INFO, Context.MODE_PRIVATE);
                                Log.e("response: ", response + "");
                                try {
                                    user = new User();
                                    user.facebookID = object.getString("id");
                                    user.email = object.getString("email");
                                    user.name = object.getString("name");

                                    addUser(object.getString("name"),object.getString("email"),object.getString("id"));

                                    PrefUtils.setCurrentUser(user,WelcomeActivity.this);



                                    isGuest = false;

                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                Intent intent=new Intent(WelcomeActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();

                            }

                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }
            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), R.string.cancel_login, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), R.string.error_login, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        WelcomeActivity.this.finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else
            callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void addUser(String name, String email, String token){
        InsertValues newUser = new InsertValues(this);
        if(newUser.isConnected)
            newUser.addUser(name,email,token);
        else
            Toast.makeText(WelcomeActivity.this, "Cannot connect to remote database", Toast.LENGTH_SHORT).show();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(getmGoogleApiClient());
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    public void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            isGuest = false;
            googleSignedIn = true;
            GoogleSignInAccount acct =result.getSignInAccount();
            addUser(acct.getDisplayName(),acct.getEmail(),acct.getId());
            goMainScreen();
        } else {
            googleSignedIn = false;
            Toast.makeText(WelcomeActivity.this, R.string.error_login, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}