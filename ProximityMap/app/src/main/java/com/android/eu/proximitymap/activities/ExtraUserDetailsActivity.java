package com.android.eu.proximitymap.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import com.android.eu.proximitymap.R;
import com.android.eu.proximitymap.models.User;
import com.android.eu.proximitymap.models.UserHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class ExtraUserDetailsActivity extends AppCompatActivity implements
        View.OnClickListener,
        OnCompleteListener {

    private final Calendar myCalendar = Calendar.getInstance();

    private TextInputLayout mLayoutProfession;
    private TextInputLayout mLayoutDob;
    private RadioButton mRadioMale;
    private RadioButton mRadioFemale;
    private Switch mSwitchStudent;

    /**
     * // TODO: Add textwatcher for validation as shown here: http://stackoverflow.com/questions/33072569/best-practice-input-validation-android
     *
     * // TODO: Make uploading the data async.
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
        mSwitchStudent = (Switch) findViewById(R.id.switch_student);
        final Button btnNext = (Button) findViewById(R.id.button_next);

        btnNext.setOnClickListener(this);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        mLayoutDob.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ExtraUserDetailsActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        mLayoutDob.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                new DatePickerDialog(ExtraUserDetailsActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
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

        // Compose a user object.
        String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String prof = mLayoutProfession.getEditText().getText().toString();
        String dob = mLayoutDob.getEditText().getText().toString();
        String gender = mRadioMale.isChecked() ? "m" : "f";
        Boolean student = mSwitchStudent.isChecked();
        User user = new User(name, prof, dob, gender, student);

        // Store the user object in the user helper to later be able to get it easily.
        UserHelper.setUser(user);

        // Upload the user.
        UserHelper.uploadUser(user, this);
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

    /**
     * Changes the date of birth EditText to the corresponding selected date.
     */
    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        EditText et = mLayoutDob.getEditText();
        if (et != null) {
            et.setText(sdf.format(myCalendar.getTime()));
        }
    }
}
