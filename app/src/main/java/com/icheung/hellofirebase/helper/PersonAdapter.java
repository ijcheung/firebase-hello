package com.icheung.hellofirebase.helper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.icheung.hellofirebase.R;
import com.icheung.hellofirebase.model.Person;

public class PersonAdapter extends FirebaseAdapter<Person, PersonAdapter.ViewHolder> {
    OnPersonClickedListener mListener;

    public PersonAdapter(Query ref, OnPersonClickedListener listener) {
        super(ref, Person.class);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.person, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Person person = getItems().get(position);

        holder.first.setText(person.getFirstName());
        holder.last.setText(person.getLastName());
        holder.dob.setText(person.getDateOfBirth());
        holder.zip.setText(person.getZipCode());
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPersonClicked(person, getFirebaseReference(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return getItems().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView first;
        public final TextView last;
        public final TextView dob;
        public final TextView zip;

        public ViewHolder(View view) {
            super(view);

            this.view = view;
            this.first = (TextView) view.findViewById(R.id.first);
            this.last = (TextView) view.findViewById(R.id.last);
            this.dob = (TextView) view.findViewById(R.id.dob);
            this.zip = (TextView) view.findViewById(R.id.zip);
        }
    }

    public interface OnPersonClickedListener {
        void onPersonClicked(Person person, Firebase personRef);
    }
}