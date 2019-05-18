package com.thomas.communitysocialapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thomas.communitysocialapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    CircleImageView imgUserPhoto;
    static int PReqCode = 1;
    static int REQUESCODE = 1;
    Uri pickedImgUri;

    private EditText userName, userEmail, userPassword, userConfirmPass;
    private ProgressBar loadingProgressBar;
    private Button regBtn;
    Button logBtn;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userName = findViewById(R.id.regName);
        userEmail = findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPassword);
        userConfirmPass = findViewById(R.id.regPassword2);
        loadingProgressBar = findViewById(R.id.progressBar);
        regBtn = findViewById(R.id.regBtn);

        logBtn = findViewById(R.id.login_Btn);

        loadingProgressBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();


        logBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regBtn.setVisibility(View.INVISIBLE);
                loadingProgressBar.setVisibility(View.VISIBLE);

                final String name = userName.getText().toString();
                final String email = userEmail.getText().toString();
                final String pass = userPassword.getText().toString();
                final String confirmPass = userConfirmPass.getText().toString();

                if (email.isEmpty() || name.isEmpty() || pass.isEmpty() || !pass.equals(confirmPass)) {


                    // something goes wrong : all fields must be filled
                    // we need to display an error message
                    Toast.makeText(RegisterActivity.this, "Please Verify all fields", Toast.LENGTH_SHORT).show();
                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgressBar.setVisibility(View.INVISIBLE);

                } else {
                    // everything is ok and all fields are filled now we can start creating user account
                    // CreateUserAccount method will try to create the user if the email is valid

                    CreateUserAccount(email, name, pass);
                }

            }
        });

        imgUserPhoto = findViewById(R.id.regUserPhoto);
        imgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= 22) {
                    checkAndRequestForPermission();
                } else {
                    openGallery();
                }
            }
        });
    }

    private void CreateUserAccount(String email, final String name, String pass) {
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // user account created successfully
                            Toast.makeText(RegisterActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                            // after we created user account we need to update his profile picture and name
                            updateUserInfo(name, pickedImgUri, mAuth.getCurrentUser());

                        } else {

                            // account creation failed
                            Toast.makeText(RegisterActivity.this, "account creation failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            regBtn.setVisibility(View.VISIBLE);
                            loadingProgressBar.setVisibility(View.INVISIBLE);

                        }
                    }
                });
    }

    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {

        // first we need to upload user photo to firebase storage and get url
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // image uploaded succesfully
                // now we can get our image url
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // uri contain user image url
                        UserProfileChangeRequest profleUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profleUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            // user info updated successfully
                                            Toast.makeText(RegisterActivity.this, "Register Complete", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegisterActivity.this, Home.class));
                                            finish();
                                        }
                                    }
                                });
                    }
                });
            }
        });
    }

    private void openGallery() {
        //TODO: open gallery intent to pick up and image
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);
    }

//    @Override
//    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
//        super.onActivityResult(reqCode, resultCode, data);
//
//
//        if (resultCode == RESULT_OK) {
//            try {
//                final Uri imageUri = data.getData();
//                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                imgUserPhoto.setImageBitmap(selectedImage);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                Toast.makeText(getApplication().getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
//            }
//
//        } else {
//            Toast.makeText(RegisterActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
//        }
//    }

    private void checkAndRequestForPermission() {

        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(RegisterActivity.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }
        } else
            openGallery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.getData();
            imgUserPhoto.setImageURI(pickedImgUri);

        }
    }

    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            //user is already connected  so we need to redirect him to home page
            startActivity(new Intent(this, Home.class));
            finish();
        }
    }
}