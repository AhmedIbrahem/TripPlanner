package com.tripplanner.add_trip;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.tripplanner.Constants;
import com.tripplanner.R;
import com.tripplanner.add_trip.place.PlaceAutoSuggestAdapter;
import com.tripplanner.alarm.NotificationActivity;
import com.tripplanner.data_layer.local_data.entity.Note;
import com.tripplanner.data_layer.local_data.entity.Trip;
import com.tripplanner.databinding.FragmentAddTripBinding;
import com.tripplanner.home.HomeFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.content.Context.ALARM_SERVICE;


public class AddTripFragment extends Fragment {
    private AddTripViewModel tripViewModel;
    private FragmentAddTripBinding fragmentAddTripBinding;
    private NoteAdapter noteAdapter;
    public static final int ALARM_ID = 200;
    private static final String TAG = "AddTripFragment";
    private Trip trip;
    PlaceAutoSuggestAdapter Fromadapter;
    PlaceAutoSuggestAdapter toAdapter;

    long time;

    public AddTripFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentAddTripBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_add_trip, container, false);
        View root = fragmentAddTripBinding.getRoot();
        tripViewModel = ViewModelProviders.of(getActivity()).get(AddTripViewModel.class);
       Fromadapter = new PlaceAutoSuggestAdapter(getContext(), R.layout.pop_up_item);
       toAdapter = new PlaceAutoSuggestAdapter(getContext(),R.layout.pop_up_item);
        fragmentAddTripBinding.placeView.fromEt.setAdapter(Fromadapter);
        fragmentAddTripBinding.placeView.toEt.setAdapter(toAdapter);
        fragmentAddTripBinding.placeView.fromEt.setOnItemClickListener((adapterView, view, i, l) -> trip.setStartPoint(Fromadapter.getPlace(i)));
        fragmentAddTripBinding.placeView.toEt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                trip.setEndPoint(toAdapter.getPlace(i));
            }
        });
        trip = AddTripFragmentArgs.fromBundle(getArguments()).getTrip();

        if (trip == null) {
            trip = new Trip();
        }
        trip.setOnline(isNetworkConnected());
        fragmentAddTripBinding.setTrip(trip);
        fragmentAddTripBinding.setFragmet(this);
        noteAdapter = new NoteAdapter(new ArrayList<>());
        fragmentAddTripBinding.addNoteCard.noteRv.setAdapter(noteAdapter);
        setupTimeDatePicker();
        return root;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    //...
                    return !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                }
            } else {
                // current code
                return true;
            }
        }
        return false;
    }


    private void setupTimeDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        MaterialDatePicker materialDatePicker = builder.build();
        CalendarConstraints.Builder calendarConstraints = new CalendarConstraints.Builder();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTS"));
        calendarConstraints.setStart(calendar.getTimeInMillis());
        builder.setCalendarConstraints(calendarConstraints.build());
        long today = MaterialDatePicker.todayInUtcMilliseconds();
        builder.setSelection(today);
        fragmentAddTripBinding.dateView.datePicker.setOnClickListener(view -> {

            materialDatePicker.show(getActivity().getSupportFragmentManager(), "a");
            materialDatePicker.addOnPositiveButtonClickListener(selection ->

            {
                if (selection != null) {
                    fragmentAddTripBinding.dateView.datePicker.setText(materialDatePicker.getHeaderText());
                    Date date = getDate(materialDatePicker.getHeaderText());
                    trip.setTripDate(date);
                    Log.d(TAG, "setupTimeDatePicker: "+trip.getTripDate());
                    Log.d(TAG, "setupTimeDatePicker: " + selection.toString());
                }
            });
        });
    }

    private Date getDate(String headerText) {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
        try {
            Date d = format.parse(headerText);
            return d;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void openTimePicker() {
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getContext(), (timePicker, selectedHour, selectedMinute) -> {
            fragmentAddTripBinding.dateView.timePicker.setText(selectedHour + ":" + selectedMinute);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, selectedHour);
            cal.set(Calendar.MINUTE, selectedMinute);
            time = cal.getTime().getTime();
        }, 12, 0, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }


    public void showNoteNameDialog() {
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(getContext());
        alert.setTitle("Note Name");
        // Set an EditText view to get user input
        final TextInputEditText input = new TextInputEditText(getContext());
        alert.setView(input);
        alert.setPositiveButton("Confirm", (dialog, whichButton) -> {
            if (!input.getText().toString().isEmpty()) {
                Note note = new Note();
                note.setNoteName(input.getText().toString());
                noteAdapter.addNote(note);
            }
        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });

        alert.show();
    }

    public void insertTip(View view) {
        if (tripViewModel.validate(fragmentAddTripBinding)) {
            Log.d(TAG, "insertTip: "+trip);
            trip.getTripDate().setTime(time);
            tripViewModel.insertTrip(trip, noteAdapter.getNotes()).observe(getActivity(), aBoolean -> {
                Toast.makeText(getContext(), "Inserted Successfully", Toast.LENGTH_SHORT).show();
                setAlarmManger();
                goback(view);
            });
        }
    }

    private void setAlarmManger() {

        // Set the alarm to start at 8:30 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTime(trip.getTripDate());
        Intent notifyIntent = new Intent(getContext(), NotificationActivity.TripAlarmReciver.class);
        notifyIntent.putExtra(Constants.TRIPS,trip.getId());
        final PendingIntent notifyPendingIntent = PendingIntent.getBroadcast
                (getContext(), ALARM_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    notifyPendingIntent);

        }
    }

    public void goback(View v) {
        Navigation.findNavController(v).popBackStack();
    }
}
