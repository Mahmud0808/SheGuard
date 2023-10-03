package com.android.sheguard.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.android.sheguard.R;
import com.android.sheguard.common.Constants;
import com.android.sheguard.databinding.FragmentEditProfileBinding;
import com.android.sheguard.model.UserModel;
import com.android.sheguard.ui.view.LoadingDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.header.toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            binding.header.collapsingToolbar.setTitle(getString(R.string.activity_edit_profile_title));
            binding.header.collapsingToolbar.setSubtitle(getString(R.string.activity_edit_profile_desc));
        }

        loadingDialog = new LoadingDialog(getContext());

        binding.btnSave.setOnClickListener(v -> {
            loadingDialog.show(null);
            saveDetailsInDatabase();
        });

        return view;
    }

    private void saveDetailsInDatabase() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference UserList = firestore.collection(Constants.FIRESTORE_COLLECTION_USERLIST);

        FirebaseFirestore.getInstance()
                .collection(Constants.FIRESTORE_COLLECTION_USERLIST)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists()) {
                            String name = binding.etNewName.getText() != null ? binding.etNewName.getText().toString().trim() : "";
                            String phone = binding.etNewPhone.getText() != null ? binding.etNewPhone.getText().toString().trim() : "";

                            UserModel user = new UserModel(
                                    name.isEmpty() ? document.getString("name") : name,
                                    document.getString("email"),
                                    phone.isEmpty() ? document.getString("phone") : phone
                            );

                            UserList.document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                    .set(user)
                                    .addOnCompleteListener(task2 -> {
                                        loadingDialog.hide();

                                        if (task2.isSuccessful()) {
                                            binding.etNewName.setText("");
                                            binding.etNewPhone.setText("");

                                            Snackbar.make(binding.getRoot(), getString(R.string.details_saved_successfully), Snackbar.LENGTH_SHORT).show();
                                        } else {
                                            if (getContext() != null) {
                                                Toast.makeText(getContext(), Objects.requireNonNull(task2.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    } else {
                        loadingDialog.hide();

                        if (getContext() != null) {
                            Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        loadingDialog.hide();
        super.onDestroyView();
    }
}