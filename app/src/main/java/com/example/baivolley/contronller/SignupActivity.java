package com.example.baivolley.contronller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.baivolley.R;
import com.example.baivolley.api.SignupApi;
import com.example.baivolley.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 11;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 12;
    EditText edtName, edtEmail;
    Intent intentSignup;
    ImageButton imvRegister;
    TextInputLayout textInputLayout;
    TextInputEditText textInputEditText;
    String password;
    ImageView imgAvatar, imgCam, imgGallery;
    Uri imageUri;
    StorageReference storageReference;
    String imageUrl;
    Handler handler = new Handler();
    double progress;
    private Intent cameraIntent, galleryIntent;
    private ActivityResultLauncher<Intent> cameraLauncher, galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        setupUI();

        // get image from camera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            imageUri = data.getData();
                            Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras())
                                                           .get("data");
                            imgAvatar.setImageBitmap(photo);
                        }
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            Toast.makeText(this, "Khong chon anh", Toast.LENGTH_SHORT)
                                 .show();
                            return;
                        }
                        imageUri = data.getData();
                        imgAvatar.setImageURI(imageUri);
                    }
                }
        );
        setupProcess();
    }

    private void setupUI() {
        imgAvatar = findViewById(R.id.image_avatar);
        imgCam = findViewById(R.id.image_camera);
        imgGallery = findViewById(R.id.image_folder);
        edtName = findViewById(R.id.editText_name);
        edtEmail = findViewById(R.id.editText_email1);
        textInputLayout = findViewById(R.id.editText_password1);
        textInputEditText = findViewById(R.id.textinput_password1);
        imvRegister = findViewById(R.id.imageView_register);
    }

    private void setupProcess() {
        setupCreateAccount();
    }

    public void setupCreateAccount() {
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                password = s.toString();

                if (password.length() >= 8) {
                    Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
                    Matcher matcher = pattern.matcher(password);
                    boolean passwordsMatch = matcher.find();
                    if (passwordsMatch) {
                        textInputLayout.setHelperText("Your password are strong");
                        textInputLayout.setError("");
                    } else {
                        textInputLayout.setError(
                                "Mix of letters(upper and lower case), number and symbols");
                    }
                } else {
                    textInputLayout.setHelperText("Password must 8 characters Long");
                    textInputLayout.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        imgCam.setOnClickListener(v -> dispatchTakePhoto());

        imgGallery.setOnClickListener(v -> dispatchTakePhotoGallery());

        imvRegister.setOnClickListener(v -> {
            try {
                String name = edtName.getText()
                                     .toString(),
                        email = edtEmail.getText()
                                        .toString();

                if (name.length() == 0 || email.length() == 0) {
                    Toast.makeText(this, "Nhap lai", Toast.LENGTH_SHORT)
                         .show();

                } else {

                    uploadFile();

                    try {
                        Toast.makeText(this, "Upload success", Toast.LENGTH_SHORT)
                             .show();
                        // add signup api here
                        SignupApi.signUp(
                                this, name, email, password, imageUrl,
                                new SignupApi.SignupListener() {
                                    @Override
                                    public void onSignupSuccess(User user) {
                                        Toast.makeText(
                                                     SignupActivity.this,
                                                     "Signup success",
                                                     Toast.LENGTH_SHORT
                                             )
                                             .show();
                                    }

                                    @Override
                                    public void onSignupError(String message) {
                                        Toast.makeText(
                                                     SignupActivity.this,
                                                     message,
                                                     Toast.LENGTH_SHORT
                                             )
                                             .show();
                                    }
                                }
                        );
                        loadRegister();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }

                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT)
                         .show();

                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    public void loadRegister() {
        intentSignup = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intentSignup);
    }

    private void dispatchTakePhotoGallery() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    GALLERY_PERMISSION_REQUEST_CODE
            );
        } else {
            // Permission already granted, launch gallery intent
            launchGallery();
        }
    }

    private void dispatchTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE
            );
        } else {
            // Permission already granted, launch camera intent
            launchCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, launch camera intent
                launchCamera();
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(this, "Permission deny", Toast.LENGTH_SHORT)
                     .show();
            }
        }
        if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, launch gallery intent
                launchGallery();
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(this, "Permission deny", Toast.LENGTH_SHORT)
                     .show();
            }
        }
    }

    private void launchGallery() {
        galleryIntent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        galleryLauncher.launch(galleryIntent);
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void launchCamera() {
        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        }
    }

    public void uploadFile() {
        // Upload file to Firebase Storage
        FirebaseApp.initializeApp(SignupActivity.this);
        storageReference = FirebaseStorage.getInstance()
                                          .getReference();

        StorageReference ref = storageReference.child("images/" + UUID.randomUUID()
                                                                      .toString());

        ref.putFile(imageUri)
           .addOnSuccessListener(taskSnapshot -> {
               // Get the download URL
               ref.getDownloadUrl()
                  .addOnSuccessListener(uri -> {
                      // Save the download URL to the database
                      imageUrl = uri.toString();
                  });
           })
           .addOnFailureListener(e -> {
               // Handle unsuccessful uploads
               Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
           })
           .addOnProgressListener(snapshot -> {
               // Observe state change events such as progress, pause, and resume
               progress =
                       (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
           });
    }
}