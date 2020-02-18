package com.tripplanner.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

/*sara*/
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tripplanner.R;
import android.content.Intent;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
/*sara*/
/*omnia*/
/*omnia*/
public class HomeActivity extends AppCompatActivity {
    /*sara*/
    private static final int RC_SIGN_IN = 123;
    LoginViewModel loginViewModel;
    FirebaseUser user=null;
    /*sara*/
    /*omnia*/
    /*omnia*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*omnia*/
        // TODO create view model
        // TODO create recyclerview
        // TODO create home layout
        /*omnia*/
        /*sara*/
        user = FirebaseAuth.getInstance().getCurrentUser();
        loginViewModel= ViewModelProviders.of(this).get(LoginViewModel.class);
        if(user==null)
        {
            createSignInIntent();
        //    setContentView(R.layout.activity_main);

        }
        else
        {
            setContentView(R.layout.activity_main);

        }

        /*sara*/
    }

    /*omnia*/
    /*omnia*/
    /*sara*/
    //TODO create login with firebase
    public void createSignInIntent() {

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.LoginTheme)
                        .build(),
                RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {

                user = loginViewModel.getCurrentUser();
                Log.i("hh", "onActivityResult: "+user);
                setContentView(R.layout.activity_main);
            } else {

finish();
            }
        }
    }
    /*sara*/
}
