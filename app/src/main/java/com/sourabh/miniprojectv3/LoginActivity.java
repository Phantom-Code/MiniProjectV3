package com.sourabh.miniprojectv3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText email,password;
    private Button signin,signup;
    private FirebaseAuth mauth;
    String Email,Pass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signin = findViewById(R.id.signin);
        signup = findViewById(R.id.signup);

        mauth = FirebaseAuth.getInstance();

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Email = email.getText().toString();
                Pass = password.getText().toString();

                mauth.signInWithEmailAndPassword(Email,Pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(),"sign in successful",Toast.LENGTH_SHORT).show();
                            email.setText("");
                            password.setText("");
                            startActivity(new Intent(LoginActivity.this,DisplayActivity.class));
                        }
                    }
                });
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Email = email.getText().toString();

                if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches())
                {
                    email.setError("please enter valid email");
                }
                Pass = password.getText().toString();

                mauth.createUserWithEmailAndPassword(Email,Pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(),"sign up successful",Toast.LENGTH_SHORT).show();
                            email.setText("");
                            password.setText("");
                        }
                        else
                            Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }
}

