package com.cse110.eventlit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cse110.eventlit.db.Event;
import com.cse110.eventlit.db.Organization;
import com.cse110.eventlit.db.User;
import com.cse110.utils.EventUtils;
import com.cse110.utils.FileStorageUtils;
import com.cse110.utils.OrganizationUtils;
import com.cse110.utils.UserUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.enums.EPickType;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class EditEventActivity extends AppCompatActivity implements IPickResult{

    // Fields for Organizer User to fill in
    private TextInputLayout mTitle;
    private TextInputLayout mLocation;
    private TextInputLayout mDescription;
    private TextInputLayout mTag;
    private TextInputLayout mCapacity;

    // Event Pic
    private ImageView mEventPic;

    // Orgs that the Organizer User manages
    private List<String> orgsManaging;

    private HashMap<String, Calendar> calendars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // TODO: FAB for adding event picture
        mEventPic = (ImageView) findViewById(R.id.event);
        mEventPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               PickImageDialog.build(new PickSetup()
                .setTitle("Please select a photo for the event")
                .setPickTypes(EPickType.GALLERY))
                .setOnPickResult(EditEventActivity.this)
                .show(getSupportFragmentManager());

            }
        });

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        // Add edit_profilephoto xml
        final View profView = factory.inflate(R.layout.edit_profilephoto, null);
        dialog.setView(profView);

        calendars = new HashMap<>();


        // Gets the Orgs that the Organization User is managing
        User user = UserUtils.getCurrentUser();
        ArrayList<Organization> orgs = UserUtils.getOrgsManaging();
        orgsManaging = new ArrayList<>();
        for (Organization org: orgs) {
            orgsManaging.add(org.getName());
        }

        // Cancel and Create button functionality
        Button cancelBut = (Button) findViewById(R.id.cancelButton);
        Button createBut = (Button) findViewById(R.id.createButton);

        cancelBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Go back to organizer feed
                startActivity(new Intent(EditEventActivity.this, OrganizerFeedActivity.class));
                finish();
            }
        });

        createBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Create event (Add event to database)
                addEventToDB();

            }
        });

        // Populate spinner for selecting org that the event is for
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setPrompt("Organization holding event");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, android.R.id.text1, orgsManaging);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        // Input Fields
        mTitle = (TextInputLayout) findViewById(R.id.title);
        mLocation = (TextInputLayout) findViewById(R.id.locationtext);
        mDescription = (TextInputLayout) findViewById(R.id.descriptiontext);
        mTag = (TextInputLayout) findViewById(R.id.tagtext);
        mCapacity = (TextInputLayout) findViewById(R.id.peopletext);

        // Check for validity of input
        mTitle.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    checkTitle();
                }
            }
        });
        mLocation.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    checkLocation();
                }
            }
        });
        mDescription.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    checkDescription();
                }
            }
        });
        mTag.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    checkTag();
                }
            }
        });
        mCapacity.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    checkCapacity();
                }
            }
        });
    }

    /* Show the start time picker dialog */
    public void onStartTimeClicked(View v){
        TimePickerFragment newFragment = new TimePickerFragment();
        TextView startTimeText = (TextView) findViewById(R.id.starttimetext);
        startTimeText.setText("Start Time: ");

        calendars.put("startTime", newFragment.getCalendar());

        // Pass in start time textview
        Bundle args = new Bundle();
        args.putInt("timetext", R.id.starttimetext);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(),"TimePicker");
    }

    /* Show the start date picker dialog */
    public void onStartDateClicked(View v){
        DatePickerFragment newFragment = new DatePickerFragment();
        TextView startDateText = (TextView) findViewById(R.id.startdatetext);
        startDateText.setText("Start Date: ");

        calendars.put("startDate", newFragment.getCalendar());

        // Pass in start time textview
        Bundle args = new Bundle();
        args.putInt("datetext", R.id.startdatetext);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(),"DatePicker");
    }

    /* Show the end time picker dialog */
    public void onEndTimeClicked(View v){
        TimePickerFragment newFragment = new TimePickerFragment();
        TextView endTimeText = (TextView) findViewById(R.id.endtimetext);
        endTimeText.setText("End Time: ");

        calendars.put("endTime", newFragment.getCalendar());

        // Pass in the end time textview
        Bundle args = new Bundle();
        args.putInt("timetext", R.id.endtimetext);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(),"TimePicker");
    }

    /* Show the end date picker dialog */
    public void onEndDateClicked(View v){
        DatePickerFragment newFragment = new DatePickerFragment();
        TextView startDateText = (TextView) findViewById(R.id.enddatetext);
        startDateText.setText("End Date: ");

        calendars.put("endDate", newFragment.getCalendar());

        // Pass in start time textview
        Bundle args = new Bundle();
        args.putInt("datetext", R.id.enddatetext);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(),"DatePicker");
    }

    @Override
    public void onBackPressed() {
        // TODO: Go back to organizer feed
        startActivity(new Intent(this, OrganizerFeedActivity.class));
        finish();

    }

    /* Gets the text fields to make event in database */
    private void addEventToDB() {
        String title = mTitle.getEditText().getText().toString();
        String location = mLocation.getEditText().getText().toString();
        String description = mDescription.getEditText().getText().toString();
        String capacity = mCapacity.getEditText().getText().toString();

        // Get the organization name
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        String orgName = spinner.getSelectedItem().toString();
        String orgId = OrganizationUtils.getOrgId(orgName);


        Calendar startTime = calendars.get("startTime");
        Calendar startDate = calendars.get("startDate");
        Calendar endTime = calendars.get("endTime");
        Calendar endDate = calendars.get("endDate");

        if (startTime == null) {
            startTime = Calendar.getInstance();
        }
        if (startDate == null) {
            startDate = Calendar.getInstance();
        }
        if (endTime == null) {
            endTime = Calendar.getInstance();
        }
        if (endDate == null) {
            endDate = Calendar.getInstance();
        }

        Log.w("Start Time", startTime.getTimeInMillis() + "");
        Log.w("Start Date", startDate.getTimeInMillis() + "");
        Log.w("End Time", endTime.getTimeInMillis() + "");
        Log.w("End Date", endDate.getTimeInMillis() + "");

        if (capacity.equals("")) {
            capacity = "0";
        }

        Event event = new Event(title, description, orgId, "0", location, "Uncateogorized", Integer.parseInt(capacity));
        event.putStartTime(startDate.get(Calendar.YEAR),
                           startDate.get(Calendar.MONTH),
                           startDate.get(Calendar.DAY_OF_MONTH),
                            startTime.get(Calendar.HOUR),
                            startTime.get(Calendar.MINUTE));
        event.putEndTime(endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH),
                endTime.get(Calendar.HOUR),
                endTime.get(Calendar.MINUTE));
        EventUtils.createEvent(event, new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    // TODO dismiss activity, Event has been created at this point
                    String eventId = task.getResult();

                    try {
                        // Store in db
                        FileStorageUtils.uploadImageFromLocalFile(eventId,
                                ((BitmapDrawable)mEventPic.getDrawable()).getBitmap());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }

    /* Check validity of title */
    protected boolean checkTitle() {
        EditText titleEditText = mTitle.getEditText();

        if (titleEditText != null && titleEditText.getError() != null) return false;

        String titleText = null;
        if (titleEditText != null) {
            titleText = titleEditText.getText().toString();
        }

        // Title Criteria
        if (titleText != null && titleText.length() < 1) {
            titleEditText.setError("Title must contain at least 2 characters");
        }

        return true;
    }

    // TODO write validity checks
    protected boolean checkLocation() {
        return true;
    }
    protected boolean checkDescription() {

        EditText editText = mDescription.getEditText();

        if (editText != null && editText.getError() != null) return false;

        String text = null;
        if (editText != null) {
            text = editText.getText().toString();
        }

        // Criteria
        if (text != null && text.length() > 100) {
            editText.setError("Title can contain at most 100 characters");
        }

        return true;
    }
    protected boolean checkCapacity() {
        return true;
    }
    protected boolean checkTag() {
        return true;
    }

    @Override
    public void onPickResult(PickResult r) {

        // Check for errors with event picture selection
        if (r.getError() == null) {

            // Store image Organizer User selected in global var (will store in db later)
            Bitmap imageSelected = r.getBitmap();
            mEventPic.setImageBitmap(imageSelected);

        } else {
            //Handle possible errors
            //TODO: do what you have to do with r.getError();
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}