package com.galihekasyaputra.studentsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.galihekasyaputra.studentsapp.model.Student;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<Student> {

    public StudentAdapter(Context context, List<Student> students) {
        super(context, 0, students);
    }

    @NonNull
    @Override
    public View getView(
            int position,
            @Nullable View convertView,
            @NonNull ViewGroup parent
    ) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_student, parent, false);
        }

        Student student = getItem(position);

        TextView tvNis     = convertView.findViewById(R.id.tvNis);
        TextView tvName    = convertView.findViewById(R.id.tvName);
        TextView tvAddress = convertView.findViewById(R.id.tvAddress);

        if (student != null) {
            tvNis.setText("NIS: " + student.getNis());
            tvName.setText(student.getName());
            tvAddress.setText(student.getAddress());
        }

        return convertView;
    }
}