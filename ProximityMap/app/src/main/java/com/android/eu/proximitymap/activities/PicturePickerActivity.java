package com.android.eu.proximitymap.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.eu.proximitymap.R;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class PicturePickerActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final int GET_FROM_GALLERY = 0;

    private CircleImageView mImageView;
    private Bitmap mBitmap;

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
     * Checks if an image is chosen. If not,
     * ask the user if he/she is sure he want's to continue.
     */
    private void goToMapActivity() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        startMapActivity();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        if (mBitmap == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to continue without " +
                    "uploading a profile picture?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        } else {
            startMapActivity();
        }
    }

    /**
     * Start the MapsActivity.
     */
    private void startMapActivity() {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        startActivity(mapIntent);
        finish();
    }
}
