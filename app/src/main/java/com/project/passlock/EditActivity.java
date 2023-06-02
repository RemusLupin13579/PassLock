package com.project.passlock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etTitle;
    Button btnSave,btnCancel;
    ImageView ivCategory;
    Bitmap bitmap;
    Button btnTakePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);


        etTitle=(EditText)findViewById(R.id.etTitle);
        btnSave=(Button)findViewById(R.id.btnSave);
        btnCancel=(Button)findViewById(R.id.btnCancel);
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        ivCategory=(ImageView)findViewById(R.id.ivCategory);

        btnTakePic=(Button)findViewById(R.id.btnPic);
        btnTakePic.setOnClickListener(this);
        //connect to intent if its edit mode
        Intent intent=getIntent();
        if(intent.getExtras()!=null)
        {
            String title = intent.getExtras().getString("title");
            bitmap = Helper.byteArrayToBitmap(intent.getExtras().getByteArray("icon"));
            etTitle.setText(title);
            ivCategory.setImageBitmap(bitmap);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0)
        {
            if(resultCode==RESULT_OK)
            {
                bitmap = (Bitmap)data.getExtras().get("data");
                if(bitmap!=null)
                    ivCategory.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(btnTakePic==v)//option 1 - take new picture
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent,0);
        }
        else if(btnSave==v)//option 2 - save the data and go to first screen
        {
            if(etTitle.getText().toString().length()>0&&bitmap!=null) {
                Intent intent = new Intent();
                intent.putExtra("title", etTitle.getText().toString());
                intent.putExtra("bitmap", Helper.bitmapToByteArray(bitmap));
                setResult(RESULT_OK, intent);
                finish();
            }
            else
                Toast.makeText(this,"Please fill all fields",Toast.LENGTH_LONG).show();

        }
        else if (btnCancel==v)//option 3 - cancel-  and go to first screen
        {
            setResult(RESULT_CANCELED,null);
            finish();

        }
    }
}