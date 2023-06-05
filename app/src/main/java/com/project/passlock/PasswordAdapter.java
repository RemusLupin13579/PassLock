package com.project.passlock;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.passlock.Category;
import com.project.passlock.Password;
import com.project.passlock.R;

import java.util.List;

public class PasswordAdapter extends ArrayAdapter<Password>{
        Context context;
        List<Password> objects;

    public PasswordAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Password> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.passwords_layout,parent,false);

        TextView tvTitle = (TextView)view.findViewById(R.id.tvPassTitle);
        TextView tvPassword = (TextView)view.findViewById(R.id.tvPassword);

        Password temp = objects.get(position);


        tvTitle.setText(temp.getTitle());
        tvPassword.setText(temp.getPassword());



        return view;
    }
}