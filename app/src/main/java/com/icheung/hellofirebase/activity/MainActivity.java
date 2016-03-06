package com.icheung.hellofirebase.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.client.Firebase;
import com.icheung.hellofirebase.R;
import com.icheung.hellofirebase.helper.PersonAdapter;
import com.icheung.hellofirebase.model.Person;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PersonAdapter.OnPersonClickedListener {
    private Firebase mFirebase;

    private RecyclerView mRecyclerView;
    private PersonAdapter mAdapter;

    private FloatingActionButton mAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebase = new Firebase(getString(R.string.firebase_endpoint)).child("person");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdd = (FloatingActionButton) findViewById(R.id.fab);
        mAdapter = new PersonAdapter(mFirebase.limit(50), this);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).color(getResources().getColor(R.color.divider)).size(1).build());

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePersonDialog(new PersonUpdateListener() {
                    @Override
                    public void onPersonUpdated(Person person) {
                        mFirebase.push().setValue(person);
                    }
                })
                .setTitle(R.string.new_person)
                .create().show();
            }
        });
    }

    @Override
    public void onPersonClicked(Person person, final Firebase personRef) {
        generatePersonDialog(new PersonUpdateListener() {
            @Override
            public void onPersonUpdated(Person person) {
                personRef.setValue(person);
            }
        }, person)
                .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog)
                                .setTitle(R.string.confirm_delete)
                                .setMessage(R.string.confirm_delete_message)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        personRef.removeValue();
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .create().show();
                    }
                })
                .create().show();
    }

    private AlertDialog.Builder generatePersonDialog(PersonUpdateListener listener) {
        return generatePersonDialog(listener, null);
    }

    private AlertDialog.Builder generatePersonDialog(final PersonUpdateListener listener, Person base) {
        View view = getLayoutInflater().inflate(R.layout.new_person, null, false);
        final EditText firstName = (EditText) view.findViewById(R.id.first);
        final EditText lastName = (EditText) view.findViewById(R.id.last);
        final EditText dob = (EditText) view.findViewById(R.id.dob);
        final EditText zipCode = (EditText) view.findViewById(R.id.zip);

        if(base != null) {
            firstName.setText(base.getFirstName());
            lastName.setText(base.getLastName());
            dob.setText(base.getDateOfBirth());
            zipCode.setText(base.getZipCode());
        }

        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        ImageView pickDate = (ImageView) view.findViewById(R.id.pick_date);
        pickDate.setColorFilter(getResources().getColor(R.color.colorAccent));
        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    calendar.setTime(sdf.parse(dob.getText().toString()));
                } catch (ParseException e) {
                    calendar.setTimeInMillis(System.currentTimeMillis());
                }
                new DatePickerDialog(MainActivity.this, R.style.AlertDialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        dob.setText(sdf.format(calendar.getTime()));
                    }
                },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });
        return new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog)
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Person p = new Person(firstName.getText().toString(),
                                lastName.getText().toString(),
                                dob.getText().toString(),
                                zipCode.getText().toString());
                        listener.onPersonUpdated(p);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
    }

    private interface PersonUpdateListener {
        void onPersonUpdated(Person person);
    }
}