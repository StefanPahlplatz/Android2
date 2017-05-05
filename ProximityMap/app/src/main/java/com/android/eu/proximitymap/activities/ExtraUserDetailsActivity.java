package com.android.eu.proximitymap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import com.android.eu.proximitymap.R;
import com.android.eu.proximitymap.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class ExtraUserDetailsActivity extends AppCompatActivity implements View.OnClickListener, OnCompleteListener {

    private TextInputLayout mLayoutProfession;
    private TextInputLayout mLayoutDob;
    private RadioButton mRadioMale;
    private RadioButton mRadioFemale;
    private Switch swStudent;

    /**
     * // TODO: Add textwatcher for validation as shown here: http://stackoverflow.com/questions/33072569/best-practice-input-validation-android
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_user_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLayoutProfession = (TextInputLayout) findViewById(R.id.input_layout_profession);
        mLayoutDob = (TextInputLayout) findViewById(R.id.input_layout_dob);
        mRadioMale = (RadioButton) findViewById(R.id.rb_male);
        mRadioFemale = (RadioButton) findViewById(R.id.rb_female);
        swStudent = (Switch) findViewById(R.id.switch_student);
        Button btnNext = (Button) findViewById(R.id.button_next);

        btnNext.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked. If the view is the button, validate the controls
     * and try to upload the details to the database. The result of the upload will call
     * the onComplete method.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.button_next) {
            return;
        }
        if (!validateControls()) {
            return;
        }

        String prof = mLayoutProfession.getEditText().getText().toString();
        String dob = mLayoutDob.getEditText().getText().toString();
        String gender = mRadioMale.isChecked() ? "m" : "f";
        Boolean student = swStudent.isChecked();
        User user = new User(prof, dob, gender, student);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");
        //noinspection unchecked
        database.child(uid)
                .setValue(user)
                .addOnCompleteListener(this);
    }

    /**
     * If the task is successful, meaning the upload succeeded. Start the profile picture
     * picker activity and finish this one.
     *
     * @param task that completed.
     */
    @Override
    public void onComplete(@NonNull Task task) {
        if (task.isSuccessful()) {
            Intent profilePictureIntent = new Intent(
                    ExtraUserDetailsActivity.this, PicturePickerActivity.class);
            startActivity(profilePictureIntent);
            finish();
        } else {
            Log.e("DATABASE", "Couldn't upload extra user information to" +
                    "the database.");
            Toast.makeText(ExtraUserDetailsActivity.this,
                    "Couldn't communicate with the database, please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * TODO: Unit test for input validation.
     * <p>
     * Validates all inputs on the screen and
     * handles the appropriate errors for them
     *
     * @return whether the input is valid or not.
     */
    private boolean validateControls() {
        // Validate text input.
        for (TextInputLayout e : Arrays.asList(mLayoutProfession, mLayoutDob)) {
            EditText et = e.getEditText();
            if (et != null) {
                if (et.getText().toString().isEmpty()) {
                    e.setError("Can't be empty");
                    return false;
                } else {
                    e.setError(null);
                }
            }
        }
        if (!mRadioMale.isChecked() && !mRadioFemale.isChecked()) {
            mRadioMale.setError("Must choose one.");
            return false;
        }
        return true;
    }
}
