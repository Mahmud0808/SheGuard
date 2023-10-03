package com.android.sheguard.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.sheguard.R;
import com.android.sheguard.common.Constants;
import com.android.sheguard.databinding.FragmentRegisterBinding;
import com.android.sheguard.model.UserModel;
import com.android.sheguard.ui.view.LoadingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(getContext());

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

                                            Intent intent = requireActivity().getIntent();
                                            intent.putExtra("showEmailVerification", true);
                                            intent.putExtra("email", binding.etEmail.getText().toString().trim());
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            loadingDialog.hide();

                                            if (getContext() != null) {
                                                Toast.makeText(getContext(), Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            loadingDialog.hide();

                            if (getContext() != null) {
                                Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        binding.btnLogin.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private boolean isInformationValid() {
        boolean isValid = true;

        if (Objects.requireNonNull(binding.etFullName.getText()).toString().trim().isEmpty()) {
            binding.etFullNameLayout.setError(getString(R.string.please_enter_your_full_name));
            isValid = false;
        } else {
            binding.etFullNameLayout.setErrorEnabled(false);
            binding.etFullNameLayout.setError(null);
        }

        if (Objects.requireNonNull(binding.etEmail.getText()).toString().trim().isEmpty()) {
            binding.etEmailLayout.setError(getString(R.string.please_enter_your_email));
            isValid = false;
        } else if (!isValidEmail(binding.etEmail.getText().toString().trim())) {
            binding.etEmailLayout.setError(getString(R.string.please_enter_a_valid_email));
            isValid = false;
        } else {
            binding.etEmailLayout.setErrorEnabled(false);
            binding.etEmailLayout.setError(null);
        }

        if (Objects.requireNonNull(binding.etPhone.getText()).toString().trim().isEmpty()) {
            binding.etPhoneLayout.setError(getString(R.string.please_enter_your_phone_number));
            isValid = false;
        } else if (binding.etPhone.getText().toString().trim().length() < 10) {
            binding.etPhoneLayout.setError(getString(R.string.please_enter_a_valid_phone_number));
            isValid = false;
        } else {
            binding.etPhoneLayout.setErrorEnabled(false);
            binding.etPhoneLayout.setError(null);
        }

        if (Objects.requireNonNull(binding.etPassword.getText()).toString().trim().isEmpty()) {
            binding.etPasswordLayout.setError(getString(R.string.please_enter_your_password));
            isValid = false;
        } else {
            binding.etPasswordLayout.setErrorEnabled(false);
            binding.etPasswordLayout.setError(null);
        }

        if (Objects.requireNonNull(binding.etConfirmPassword.getText()).toString().trim().isEmpty()) {
            binding.etConfirmPasswordLayout.setError(getString(R.string.please_enter_your_password_again));
            isValid = false;
        } else {
            binding.etConfirmPasswordLayout.setErrorEnabled(false);
            binding.etConfirmPasswordLayout.setError(null);
        }

        if (!binding.etPassword.getText().toString().trim().isEmpty() && !binding.etConfirmPassword.getText().toString().trim().isEmpty()) {
            if (!binding.etPassword.getText().toString().trim().equals(binding.etConfirmPassword.getText().toString().trim())) {
                binding.etConfirmPasswordLayout.setError(getString(R.string.password_does_not_match));
                isValid = false;
            } else {
                if (!isValidPassword(binding.etPassword.getText().toString().trim())) {
                    binding.etConfirmPasswordLayout.setError(getString(R.string.password_constraints));
                    isValid = false;
                } else {
                    binding.etConfirmPasswordLayout.setErrorEnabled(false);
                    binding.etConfirmPasswordLayout.setError(null);
                }
            }
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        final String PASSWORD_PATTERN = "^" +   // Start of string
                "(?=.*[0-9])" +                 // at least 1 digit
                "(?=.*[a-z])" +                 // at least 1 lower case letter
                "(?=.*[A-Z])" +                 // at least 1 upper case letter
                "(?=.*[a-zA-Z])" +              // any letter
                "(?=\\S+$)" +                   // no white spaces
                ".{8,}" +                       // at least 8 characters
                "$";                            // end of string

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
    public void onDestroyView() {
        loadingDialog.hide();
        super.onDestroyView();
    }
}