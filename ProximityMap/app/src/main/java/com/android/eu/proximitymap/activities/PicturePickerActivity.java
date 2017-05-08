package com.android.eu.proximitymap.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.eu.proximitymap.R;
import com.android.eu.proximitymap.Utils.PermissionHelper;
import com.android.eu.proximitymap.models.User;
import com.android.eu.proximitymap.models.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This activity gives the user the option to select a profile picture. This is not mandatory.
 * When the user clicks the choose picture button it opens a gallery intent and processes it's
 * result in onActivityResult.
 * <p>
 * When the user clicks the next button it checks if the user choose an image or not. If he choose
 * an image, it uploads this image to the firebase storage. When this action is completed it updates
 * the user object with the newly added picture download url. When this action is successfully
 * completed it goes to the MapsActivity.
 */
public class PicturePickerActivity extends AppCompatActivity implements
        View.OnClickListener,
        OnCompleteListener {

    private static final int GET_FROM_GALLERY = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 1;

    private CircleImageView mImageView;
    private Bitmap mBitmap;
    private ProgressBar mProgressBar;
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_picker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mImageView = (CircleImageView) findViewById(R.id.image_view_profile);
        mProgressBar = (ProgressBar) findViewById(R.id.simpleProgressBar);

        findViewById(R.id.button_pick_image).setOnClickListener(this);
        findViewById(R.id.button_next).setOnClickListener(this);

        permissionHelper = new PermissionHelper(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_pick_image:
                if (!permissionHelper.hasExternalStoragePermission()) {
                    permissionHelper.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                            MY_PERMISSIONS_REQUEST_READ_STORAGE);
                } else {
                    startActivityForResult(new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                            GET_FROM_GALLERY);
                }
                break;

            case R.id.button_next:
                goToMapActivity();
                break;
        }
    }


    /**
     * Response of asking for storage read permission. Start the gallery activity if we
     * got the permission.
     *
     * @param requestCode  The request code passed in requestPermissions(android.app.Activity, String[], int)
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either
     *                     PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startActivityForResult(new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                            GET_FROM_GALLERY);
                }
            }
        }
    }

    /**
     * If the result is from the Gallery, try to load the picture
     * and set it as the profile picture view.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode  The integer result code returned by the child activity.
     * @param data        An Intent, which can return result data to the caller.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the response is from a a successful image pick.
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            mBitmap = null;
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // If the image was successfully loaded, set it.
            if (mBitmap != null) {
                mImageView.setImageBitmap(mBitmap);
            }
        }
    }

    /**
     * Called when the action of inserting the new user object is completed.
     *
     * @param task information about the completed task.
     */
    @Override
    public void onComplete(@NonNull Task task) {
        if (task.isSuccessful()) {
            startMapActivity();
        } else {
            Toast.makeText(this, "Couldn't update your information, please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks if an image is chosen. If not,
     * ask the user if he/she is sure he want's to continue.
     */
    private void goToMapActivity() {
        if (getImageViewBitmap() == null) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        startMapActivity();
                    }
                }
            };

            // No picture selected, sure you want to continue?
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to continue without " +
                    "uploading a profile picture?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        } else {
            // MapActivity is started by a method called by uploadPicture()
            if (mBitmap != null) {
                uploadPicture();
            }
        }
    }

    /**
     * Uploads the selected picture to the firebase storage.
     * If the upload failed show a toast saying it failed.
     * If the upload was successful, add the picture download link to the user profile by
     * calling addPictureLink().
     */
    private void uploadPicture() {
        String uid = UserHelper.getUid();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(uid).child("profile.jpg");

        byte[] data = bitmapToByteArray();

        final UploadTask uploadTask = storageRef.putBytes(data);
        mProgressBar.setVisibility(View.VISIBLE);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("STORAGE", exception.getCause().toString());
                Toast.makeText(PicturePickerActivity.this, "Couldn't upload the image.",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests")
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                User user = UserHelper.getInstance();
                assert downloadUrl != null;
                user.picture = downloadUrl.toString();

                /*
                  Upload the picture url to the real-time database.
                  This calls onComplete when done.
                 */
                UserHelper.uploadUser(user, PicturePickerActivity.this);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests")
                Double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                ObjectAnimator animation = ObjectAnimator.ofInt(mProgressBar, "progress", progress.intValue());
                animation.setDuration(500); // 0.5 second
                animation.setInterpolator(new DecelerateInterpolator());
                animation.start();
            }
        });
    }

    /**
     * Start the MapsActivity.
     */
    private void startMapActivity() {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        startActivity(mapIntent);
        finish();
    }

    /**
     * Returns the bitmap that is currently in the imageView.
     * Can be null if imageView is empty.
     *
     * @return bitmap from imageView.
     */
    private Bitmap getImageViewBitmap() {
        mImageView.setDrawingCacheEnabled(true);
        mImageView.buildDrawingCache();
        return mImageView.getDrawingCache();
    }

    /**
     * Converts a bitmap to JPEG and then to a byte array.
     *
     * @return bitmap in byte[].
     */
    private byte[] bitmapToByteArray() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 20, bytes);
        return bytes.toByteArray();
    }
}
