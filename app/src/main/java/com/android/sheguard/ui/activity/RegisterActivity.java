package com.android.sheguard.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.sheguard.common.Constants;
import com.android.sheguard.databinding.ActivityRegisterBinding;
import com.android.sheguard.model.UserModel;
import com.android.sheguard.ui.view.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this);

        binding.btnRegister.setOnClickListener(v -> {
            if (!isInformationValid()) {
                return;
            }

            loadingDialog.show(null);
            firebaseAuth.createUserWithEmailAndPassword(
                            Objects.requireNonNull(binding.etEmail.getText()).toString().trim(),
                            Objects.requireNonNull(binding.etPassword.getText()).toString().trim()
                    )
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Objects.requireNonNull(firebaseAuth.getCurrentUser()).sendEmailVerification()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            saveUserInDatabase();

                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            intent.putExtra("showEmailVerification", true);
                                            intent.putExtra("email", binding.etEmail.getText().toString().trim());
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            loadingDialog.hide();
                                            Toast.makeText(getApplicationContext(), Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                            task1.getException().printStackTrace();
                                        }
                                    });
                        } else {
                            loadingDialog.hide();

                            Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            task.getException().printStackTrace();
                        }
                    });
        });

        binding.btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAffinity();
        });
    }

    private boolean isInformationValid() {
        boolean isValid = true;

        if (Objects.requireNonNull(binding.etFullName.getText()).toString().trim().isEmpty()) {
            binding.etFullNameLayout.setError("Please enter your full name");
            isValid = false;
        } else {
            binding.etFullNameLayout.setErrorEnabled(false);
            binding.etFullNameLayout.setError(null);
        }

        if (Objects.requireNonNull(binding.etEmail.getText()).toString().trim().isEmpty()) {
            binding.etEmailLayout.setError("Please enter your email");
            isValid = false;
        } else {
            binding.etEmailLayout.setErrorEnabled(false);
            binding.etEmailLayout.setError(null);
        }

        if (!Objects.requireNonNull(binding.etPhone.getText()).toString().trim().isEmpty()) {
            if (binding.etPhone.getText().toString().trim().length() < 10) {
                binding.etPhoneLayout.setError("Please enter a valid phone number");
                isValid = false;
            } else {
                binding.etPhoneLayout.setErrorEnabled(false);
                binding.etPhoneLayout.setError(null);
            }
        }

        if (Objects.requireNonNull(binding.etPassword.getText()).toString().trim().isEmpty()) {
            binding.etPasswordLayout.setError("Please enter your password");
            isValid = false;
        } else {
            binding.etPasswordLayout.setErrorEnabled(false);
            binding.etPasswordLayout.setError(null);
        }

        if (Objects.requireNonNull(binding.etConfirmPassword.getText()).toString().trim().isEmpty()) {
            binding.etConfirmPasswordLayout.setError("Please enter your password again");
            isValid = false;
        } else {
            binding.etConfirmPasswordLayout.setErrorEnabled(false);
            binding.etConfirmPasswordLayout.setError(null);
        }

        if (!binding.etPassword.getText().toString().trim().isEmpty() && !binding.etConfirmPassword.getText().toString().trim().isEmpty()) {
            if (!binding.etPassword.getText().toString().trim().equals(binding.etConfirmPassword.getText().toString().trim())) {
                binding.etConfirmPasswordLayout.setError("Password does not match");
                isValid = false;
            } else {
                if (!isValidPassword(binding.etPassword.getText().toString().trim())) {
                    binding.etConfirmPasswordLayout.setError("Password must contain at least 8 characters, 1 digit, 1 lower case letter, 1 upper case letter and no white spaces");
                    isValid = false;
                } else {
                    binding.etConfirmPasswordLayout.setErrorEnabled(false);
                    binding.etConfirmPasswordLayout.setError(null);
                }
            }
        }

        return isValid;
    }

    private boolean isValidPassword(final String password) {
        final String PASSWORD_PATTERN = "^" +
                "(?=.*[0-9])" +         // at least 1 digit
                "(?=.*[a-z])" +         // at least 1 lower case letter
                "(?=.*[A-Z])" +         // at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      // any letter
                "(?=\\S+$)" +           // no white spaces
                ".{8,}" +               // at least 8 characters
                "$";

        return Pattern.compile(PASSWORD_PATTERN).matcher(password).matches();
    }

    private void saveUserInDatabase() {
        UserModel user = new UserModel(
                Objects.requireNonNull(binding.etFullName.getText()).toString().trim(),
                Objects.requireNonNull(binding.etEmail.getText()).toString().trim(),
                Objects.requireNonNull(binding.etPhone.getText()).toString().trim()
        );

        CollectionReference UserList = firestore.collection(Constants.FIRESTORE_COLLECTION_USERLIST);
        CollectionReference PhoneToUid = firestore.collection(Constants.FIRESTORE_COLLECTION_PHONE2UID);

        UserList.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .set(user)
                .addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                        PhoneToUid.document(binding.etPhone.getText().toString().trim())
                                .set(map)
                                .addOnCompleteListener(task3 -> loadingDialog.hide());
                    } else {
                        loadingDialog.hide();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loadingDialog.hide();
    }
}