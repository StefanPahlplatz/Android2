package com.android.eu.proximitymap.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.android.eu.proximitymap.R;
import com.android.eu.proximitymap.models.User;
import com.android.eu.proximitymap.models.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
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

    private CircleImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_picker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mImageView = (CircleImageView) findViewById(R.id.image_view_profile);

        findViewById(R.id.button_pick_image).setOnClickListener(this);
        findViewById(R.id.button_next).setOnClickListener(this);
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
                startActivityForResult(new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                        GET_FROM_GALLERY);
                break;

            case R.id.button_next:
                goToMapActivity();
                break;
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
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // If the image was successfully loaded, set it.
            if (bitmap != null) {
                mImageView.setImageBitmap(bitmap);
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
            Toast.makeText(this, "Coulnd't update your information, please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks if an image is chosen. If not,
     * ask the user if he/she is sure he want's to continue.
     */
    private void goToMapActivity() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        startMapActivity();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        if (getImageViewBitmap() == null) {
            // No picture selected, sure you want to continue?
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to continue without " +
                    "uploading a profile picture?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        } else {
            // MapActivity is started by a method called by uploadPicture()
            uploadPicture();
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

        byte[] data = bitmapToByteArray(getImageViewBitmap());

        final UploadTask uploadTask = storageRef.putBytes(data);
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
                //noinspection VisibleForTests
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
     * @param bitmap to convert.
     * @return bitmap in byte[].
     */
    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        return bytes.toByteArray();
    }
}
