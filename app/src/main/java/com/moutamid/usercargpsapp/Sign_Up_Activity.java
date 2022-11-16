package com.moutamid.usercargpsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Sign_Up_Activity extends AppCompatActivity {

    Button sign_btn;
    LinearLayout goto_signin;
    EditText fnameTxt,emailTxt,passTxt,carTxt;
    String name,email,password,car;
    private FirebaseAuth mAuth;
    private DatabaseReference db;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        sign_btn = findViewById(R.id.register_btn);
        goto_signin = findViewById(R.id.goto_signin);
        carTxt = findViewById(R.id.car_name);
        fnameTxt = findViewById(R.id.fname);
        emailTxt = findViewById(R.id.email);
        passTxt = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference().child("Car");
        sign_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validInfo()){
                    dialog = new ProgressDialog(Sign_Up_Activity.this);
                    dialog.setMessage("Creating your account....");
                    dialog.show();
                    createAccount();
                }

            }
        });

        goto_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Sign_Up_Activity.this,Login_Activity.class);
                startActivity(intent);
            }
        });
    }

    private void createAccount() {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    CarDetails model = new CarDetails(user.getUid(),car,name,email,password,"","",0.0,0.0,
                            "","","","parked");
                    db.child(user.getUid()).setValue(model);
                    Intent intent = new Intent(Sign_Up_Activity.this , MainActivity.class);
                    startActivity(intent);
                    finish();
                    dialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Sign_Up_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean validInfo() {

        car = carTxt.getText().toString();
        name = fnameTxt.getText().toString();
        email = emailTxt.getText().toString();
        password = passTxt.getText().toString();

        if(car.isEmpty()){
            carTxt.setText("Input Car");
            carTxt.requestFocus();
            return false;
        }

        if(name.isEmpty()){
            fnameTxt.setText("Input Email");
            fnameTxt.requestFocus();
            return false;
        }

        if(email.isEmpty()){
            emailTxt.setText("Input Email");
            emailTxt.requestFocus();
            return false;
        }


        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTxt.setError("Please input valid email!");
            emailTxt.requestFocus();
            return false;
        }

        if(password.isEmpty()){
            passTxt.setText("Input Password");
            passTxt.requestFocus();
            return false;
        }

        return true;
    }

}