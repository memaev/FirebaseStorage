package com.example.firebasestorage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.firebasestorage.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Uri filePath;
    private int PICK_IMAGE_REQUEST = 71;
    private FirebaseStorage storage;
    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        binding.loadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.imageNameTxt.getText().toString().isEmpty()){
                    String imageName = binding.imageNameTxt.getText().toString();

                    storageReference.child("image").child(imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(getApplicationContext()).load(uri).into(binding.imageView);
                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(), "Image name field cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select your image from here..."), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==PICK_IMAGE_REQUEST && resultCode==Activity.RESULT_OK && data!=null && data.getData()!=null){
            filePath = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), filePath);
                binding.imageView.setImageBitmap(bitmap);
            }catch(IOException e){
                e.printStackTrace();
            }

            uploadImage();
        }
    }

    private void uploadImage(){
        if (filePath!=null){
            ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Uploading image");
            progressDialog.show();

            String imageName = Double.toString(Math.random());

            storageReference.child("image").child(imageName).putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();

                    Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}