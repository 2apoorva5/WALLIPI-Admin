package com.developerdepository.wallipiadmin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private Button chooseImgBtn, saveImgBtn;
    private ImageView image;
    private TextView imageName;

    Uri imageUri = null;

    AlertDialog progressDialog;

    CollectionReference wallpaperRef;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        wallpaperRef = FirebaseFirestore.getInstance().collection("Travel");
        storageReference = FirebaseStorage.getInstance().getReference("TravelWallpapers/");

        progressDialog = new SpotsDialog.Builder().setContext(MainActivity.this)
                .setMessage("Saving image..")
                .setCancelable(false)
                .setTheme(R.style.SpotsDialog)
                .build();

        chooseImgBtn = findViewById(R.id.choose_image_btn);
        saveImgBtn = findViewById(R.id.save_image_btn);
        image = findViewById(R.id.image);
        imageName = findViewById(R.id.image_name);

        chooseImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        saveImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                uploadImage();
            }
        });
    }

    private void selectImage() {
        ImagePicker.Companion.with(MainActivity.this)
                .galleryOnly()
                .crop()
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK)
        {
            imageUri = data.getData();

            Glide.with(MainActivity.this).load(imageUri).centerCrop().into(image);
        }
        else if(resultCode == ImagePicker.RESULT_ERROR)
        {
            Toast.makeText(MainActivity.this, "That ran into some error!", Toast.LENGTH_SHORT).show();
            return;
        }
        else
        {
            return;
        }
    }

    private void uploadImage() {
        final String nameValue = imageName.getText().toString().trim();

        if(imageUri != null)
        {
            final StorageReference fileRef = storageReference.child(nameValue + "_wallpaper.img");

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String imageValue = uri.toString();

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("name", nameValue);
                                    map.put("image", imageValue);
                                    map.put("thumbnail", imageValue);

                                    wallpaperRef.document(nameValue).set(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(MainActivity.this, "SUCCESS!", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(MainActivity.this, "FAILURE", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "FAILURE", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "FAILURE", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}