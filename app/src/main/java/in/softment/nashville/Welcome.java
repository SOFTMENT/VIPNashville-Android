package in.softment.nashville;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;

import in.softment.nashville.Util.Services;


public class Welcome extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        FirebaseMessaging.getInstance().subscribeToTopic("vipnashville");


        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(Welcome.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }
        else {
            mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(Welcome.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Services.showDialog(Welcome.this,"ERROR",task.getException().getLocalizedMessage());
                        }
                }
            });
        }




//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//
//            FirebaseAuth.getInstance().getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//                @Override
//                public void onComplete(@NonNull Task<GetTokenResult> task) {
//                    if (task.isSuccessful()) {
//                        String providerID = task.getResult().getSignInProvider();
//
//                        if (providerID.equalsIgnoreCase("password")) {
//                            if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
//                                Services.getCurrentUserData(Welcome.this, FirebaseAuth.getInstance().getCurrentUser().getUid(), false);
//                            }
//                            else {
//
//                            }
//                        }
//                        else {
//
//                            Services.getCurrentUserData(Welcome.this, FirebaseAuth.getInstance().getCurrentUser().getUid(), false);
//                        }
//
//                    }
//                }
//            });
//
//        }
//


    }
}
