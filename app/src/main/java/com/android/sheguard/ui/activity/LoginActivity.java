package com.android.sheguard.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.R;
import com.android.sheguard.databinding.ActivityLoginBinding;
import com.android.sheguard.ui.view.LoadingDialog;
import com.android.sheguard.ui.view.VerifyEmailDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    FirebaseAuth firebaseAuth;
    LoadingDialog loadingDialog;
    VerifyEmailDialog verifyEmailDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(this);
        verifyEmailDialog = new VerifyEmailDialog(this);

        View.OnClickListener resend_verification_email = v -> Objects.requireNonNull(firebaseAuth.getCurrentUser()).sendEmailVerification()
                .addOnCompleteListener(task -> {
                    verifyEmailDialog.hide();

                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Verification email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        Intent intent = getIntent();
        if (intent != null) {
            boolean showEmailVerification = intent.getBooleanExtra("showEmailVerification", false);
            String email = intent.getStringExtra("email");

            if (showEmailVerification) {
                verifyEmailDialog.show(R.drawable.ic_tick, R.string.email_verification, getString(R.string.email_verification_description, email), resend_verification_email);
            }
        }

        binding.btnRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        binding.btnLogin.setOnClickListener(v -> {
            if (!isInformationValid()) {
                return;
            }

            loadingDialog.show(null);
            firebaseAuth.signInWithEmailAndPassword(
                            Objects.requireNonNull(binding.etEmail.getText()).toString().trim(),
                            Objects.requireNonNull(binding.etPassword.getText()).toString().trim()
                    )
                    .addOnCompleteListener(task -> {
                        loadingDialog.hide();

                        if (task.isSuccessful()) {
                            if (Objects.requireNonNull(firebaseAuth.getCurrentUser()).isEmailVerified()) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                verifyEmailDialog.show(R.drawable.ic_warning, R.string.email_verification, getString(R.string.email_verification_description, binding.etEmail.getText().toString().trim()), resend_verification_email);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        View.OnClickListener resend_reset_email = v1 -> Objects.requireNonNull(firebaseAuth.sendPasswordResetEmail(Objects.requireNonNull(binding.etEmail.getText()).toString().trim())
                .addOnCompleteListener(task -> {
                    verifyEmailDialog.hide();

                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }));

        binding.tvForgotPassword.setOnClickListener(v -> {
            if (Objects.requireNonNull(binding.etEmail.getText()).toString().trim().isEmpty()) {
                Snackbar.make(binding.getRoot(), "Enter your email address", Snackbar.LENGTH_SHORT).show();
                return;
            }

            loadingDialog.show(null);
            firebaseAuth.sendPasswordResetEmail(binding.etEmail.getText().toString().trim())
                    .addOnCompleteListener(task -> {
                        loadingDialog.hide();

                        if (task.isSuccessful()) {
                            verifyEmailDialog.show(R.drawable.ic_tick, R.string.email_reset, getString(R.string.email_reset_description, binding.etEmail.getText().toString().trim()), resend_reset_email);
                        }
                    });
        });
    }

    private boolean isInformationValid() {
        boolean isValid = true;

        if (Objects.requireNonNull(binding.etEmail.getText()).toString().trim().isEmpty()) {
            binding.etEmailLayout.setError("Email is required");
            isValid = false;
        } else {
            binding.etEmailLayout.setErrorEnabled(false);
            binding.etEmailLayout.setError(null);
        }

        if (Objects.requireNonNull(binding.etPassword.getText()).toString().trim().isEmpty()) {
            binding.etPasswordLayout.setError("Password is required");
            isValid = false;
        } else {
            binding.etPasswordLayout.setErrorEnabled(false);
            binding.etPasswordLayout.setError(null);
        }

        return isValid;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loadingDialog.hide();
        verifyEmailDialog.hide();
    }
}