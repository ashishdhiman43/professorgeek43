package com.dhiman.android.professorgeek;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Signup extends AppCompatActivity {
    FirebaseAuth mAuth;
    EditText name, email, datebirth, phonenumber, otp, password;
    Button btnReg, generateotp, btnselect, btnupload;
    Spinner gender;
    ImageView image;
    CheckBox terms;
    String verificationId;
    private Uri filepath;
    private final int Pickimagerequest = 22;
    FirebaseStorage cloudstorage;
    StorageReference storageReference;
    private ImageView imageView;
    private int result_ok;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        // initialise views
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        image = findViewById(R.id.image);
        datebirth = findViewById(R.id.DOB);
        phonenumber = findViewById(R.id.phonenumber);
        password = findViewById(R.id.password);
        gender = findViewById(R.id.spinner);
        generateotp = findViewById(R.id.generateotp);
        otp = findViewById(R.id.otp);
        btnReg = findViewById(R.id.btnRegister);
        btnselect = findViewById(R.id.btnselect);
        btnupload = findViewById(R.id.btnupload);
        mAuth = FirebaseAuth.getInstance();
        ArrayAdapter<CharSequence>adapter=ArrayAdapter.createFromResource(this, R.array.spinner, android.R.layout.simple_spinner_item);
        gender.setAdapter(adapter);
        generateotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(phonenumber.getText().toString()))
            {
                Toast.makeText(Signup.this, "Enter an Valid Number", Toast.LENGTH_SHORT).show();
            }
                else {
                    String mobilenumber = phonenumber.getText().toString();
                sendverificationcode(mobilenumber);
                }
            }
        });
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    if (TextUtils.isEmpty(password.getText().toString()))
                    {
                        Toast.makeText(Signup.this, "Enter an Valid otp", Toast.LENGTH_SHORT).show();
                    }
                }
                verify(password.getText().toString());
            }
        });
    }
    private void sendverificationcode(String phonenumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phonenumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
    mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            final String code = credential.getSmsCode();
            if (code!=null){
                verify(code);
            }
        }
        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(Signup.this,"Verification Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull String s,
            @NonNull PhoneAuthProvider.ForceResendingToken token)
        {
        super.onCodeSent(s,token);
        verificationId = s;
        }
    };

    private void verify(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
        siginbycredential(credential);
    }

    private void siginbycredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                            startActivity(new Intent(Signup.this, MainActivity.class));
                    }
                });

        // get firebase storage reference
        cloudstorage = FirebaseStorage.getInstance();
        storageReference = cloudstorage.getReference();
        // on pressing btnselect selectimage() is called
        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
        // on pressing btnupload uploadImage() is called
        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }
    // create selectimage method
    private void SelectImage() {
// implicit intent to the mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "select image from here......"),
                Pickimagerequest);
    }
    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent data){
        super.onActivityResult(requestcode,resultcode, data);
        // checking request code and result code
        // if request code is pickimagerequest and result code is ok
        // then set image in the imageview
        if (requestcode == Pickimagerequest && resultcode == result_ok && data != null && data.getData() != null) {
            // get uri of data
            filepath = data.getData();
            try {
                // setting image on image view using bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(getContentResolver(), filepath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e){
               // log the exception
                e.printStackTrace();
            }
        }
    }
    // upload an image
    private void uploadImage() {
        if (filepath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            ref.putFile(filepath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot) {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(Signup.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(Signup.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int) progress + "%");
                                }
                            });
        }
    }
}