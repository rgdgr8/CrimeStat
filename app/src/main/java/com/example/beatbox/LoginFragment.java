package com.example.beatbox;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFrag";

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        Button loginButton = v.findViewById(R.id.login_btn);
        EditText emailET = v.findViewById(R.id.email_ET);
        EditText passET = v.findViewById(R.id.pass_ET);

        loginButton.setOnClickListener(v1 -> {
            String email = emailET.getText().toString();
            String pass = passET.getText().toString();

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CrimeListFragment())
                        .commit();
            } else {
                try {
                    auth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        getParentFragmentManager().beginTransaction()
                                                .replace(R.id.fragment_container, new CrimeListFragment())
                                                .commit();
                                    } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        auth.signInWithEmailAndPassword(email, pass)
                                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {
                                                            getParentFragmentManager().beginTransaction()
                                                                    .replace(R.id.fragment_container, new CrimeListFragment())
                                                                    .commit();
                                                        } else {
                                                            Log.w(TAG, "onComplete: ", task.getException());
                                                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Log.w(TAG, "onComplete: ", task.getException());
                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Invalid credentials!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }
}
