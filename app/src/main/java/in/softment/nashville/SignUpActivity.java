package in.softment.nashville;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;

import in.softment.nashville.Model.UserModel;
import in.softment.nashville.Util.ProgressHud;
import in.softment.nashville.Util.Services;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        EditText name = findViewById(R.id.fullName);
        EditText emailAddress = findViewById(R.id.emailaddress);
        EditText password = findViewById(R.id.password);

        //BACK
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sName = name.getText().toString().trim();
                String sEmail = emailAddress.getText().toString().trim();
                String sPassword = password.getText().toString().trim();

                if (sName.isEmpty()) {
                    Services.showCenterToast(SignUpActivity.this,"Enter Full Name");
                }
                else if (sEmail.isEmpty()) {
                    Services.showCenterToast(SignUpActivity.this,"Enter Email Address");
                }
                else if (sPassword.isEmpty()) {
                    Services.showCenterToast(SignUpActivity.this,"Enter Password");
                }
                else {
                    ProgressHud.show(SignUpActivity.this,"Creating Account...");
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(sEmail, sPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            ProgressHud.dialog.dismiss();
                            if (task.isSuccessful()) {
                                UserModel userModel = new UserModel();
                                userModel.email = sEmail;
                                userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                userModel.fullName = sName;
                                userModel.registredAt = new Date();
                                userModel.regiType = "custom";
                                Services.addUserDataOnServer(SignUpActivity.this,userModel);
                            }
                            else {
                                Services.showDialog(SignUpActivity.this,"ERROR",task.getException().getLocalizedMessage());
                            }
                        }
                    });
                }
            }
        });

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}