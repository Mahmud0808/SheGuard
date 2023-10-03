package com.android.sheguard.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.sheguard.R;
import com.android.sheguard.databinding.FragmentLoginBinding;
import com.android.sheguard.ui.activity.MainActivity;
import com.android.sheguard.ui.view.LoadingDialog;
import com.android.sheguard.ui.view.VerifyEmailDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private LoadingDialog loadingDialog;
    private VerifyEmailDialog verifyEmailDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(getContext());
        verifyEmailDialog = new VerifyEmailDialog(getContext());

        View.OnClickListener resend_verification_email = v -> Objects.requireNonNull(firebaseAuth.getCurrentUser()).sendEmailVerification()
                .addOnCompleteListener(task -> {
                    verifyEmailDialog.hide();

                    if (task.isSuccessful()) {
                        Snackbar.make(view, getString(R.string.verification_email_sent), Snackbar.LENGTH_SHORT).show();
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        Intent intent = requireActivity().getIntent();
        if (intent != null) {
            boolean showEmailVerification = intent.getBooleanExtra("showEmailVerification", false);
            String email = intent.getStringExtra("email");

            if (showEmailVerification) {
                verifyEmailDialog.show(R.drawable.ic_tick, R.string.email_verification, getString(R.string.email_verification_description, email), resend_verification_email);
            }
        }

        binding.btnRegister.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_registerFragment));

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
                                Intent intent1 = new Intent(getActivity(), MainActivity.class);
                                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent1);
                            } else {
                                verifyEmailDialog.show(R.drawable.ic_warning, R.string.email_verification, getString(R.string.email_verification_description, binding.etEmail.getText().toString().trim()), resend_verification_email);
                            }
                        } else {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        View.OnClickListener resend_reset_email = v1 -> Objects.requireNonNull(firebaseAuth.sendPasswordResetEmail(Objects.requireNonNull(binding.etEmail.getText()).toString().trim())
                .addOnCompleteListener(task -> {
                    verifyEmailDialog.hide();

                    if (task.isSuccessful()) {
                        Snackbar.make(view, getString(R.string.password_reset_email_sent), Snackbar.LENGTH_SHORT).show();
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }));

        binding.tvForgotPassword.setOnClickListener(v -> {
            if (Objects.requireNonNull(binding.etEmail.getText()).toString().trim().isEmpty()) {
                Snackbar.make(view, getString(R.string.enter_your_email_address), Snackbar.LENGTH_SHORT).show();
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

        return view;
    }

    private boolean isInformationValid() {
        boolean isValid = true;

        if (Objects.requireNonNull(binding.etEmail.getText()).toString().trim().isEmpty()) {
            binding.etEmailLayout.setError(getString(R.string.email_is_required));
            isValid = false;
        } else {
            binding.etEmailLayout.setErrorEnabled(false);
            binding.etEmailLayout.setError(null);
        }

        if (Objects.requireNonNull(binding.etPassword.getText()).toString().trim().isEmpty()) {
            binding.etPasswordLayout.setError(getString(R.string.password_is_required));
            isValid = false;
        } else {
            binding.etPasswordLayout.setErrorEnabled(false);
            binding.etPasswordLayout.setError(null);
        }

        return isValid;
    }

    @Override
    public void onDestroyView() {
        loadingDialog.hide();
        verifyEmailDialog.hide();
        super.onDestroyView();
    }
}