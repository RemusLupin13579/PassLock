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

import java.util.List;

public class CategoriesAdapter extends ArrayAdapter<Category> {

    Context context;
    List<Category> objects;
    public CategoriesAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Category> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.categories_layout,parent,false);

        TextView tvTitle = (TextView)view.findViewById(R.id.tvTitle);
        ImageView ivCategory =(ImageView)view.findViewById(R.id.ivCategory);
        ImageView ivArrow =(ImageView)view.findViewById(R.id.ivArrow);
        Category temp = objects.get(position);


        tvTitle.setText(temp.getTitle());



        return view;
    }
}