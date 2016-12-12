package com.tut.qr.qr_reader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class QRActivity extends AppCompatActivity {

    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        iv = (ImageView) findViewById(R.id.qrImageView);
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    public void takePictureClick(View v) {
        takeFromCamera();
    }

    public void deleteImageClick(View v){
        if(mTempCameraPhotoFile != null){
            if(mTempCameraPhotoFile.exists())
                if(mTempCameraPhotoFile.delete()){
                    iv.setImageBitmap(null);
                    File exportDir = new File(Environment.getExternalStorageDirectory(), "tmp");
                    if (exportDir.exists()) {
                        exportDir.delete();
                    }
                }
        }
    }

    private File mTempCameraPhotoFile;

    private void takeFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File exportDir = new File(Environment.getExternalStorageDirectory(), "tmp");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            } else {
                exportDir.delete();
            }
            mTempCameraPhotoFile = new File(exportDir, "/" + UUID.randomUUID().toString().replaceAll("-", "") + ".jpg");
            Log.d("takeFromCamera", "/" + UUID.randomUUID().toString().replaceAll("-", "") + ".jpg");
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempCameraPhotoFile));
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                String filePath = mTempCameraPhotoFile.getPath();
                Log.d("onActivityResult", filePath);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
                bitmap = rotateBitmap(bitmap, 90);
                iv.setImageBitmap(bitmap);
                /*
                // extracted file path can be load into imageview for example with Picasso loader
                Uri uri = Uri.fromFile(new File(mReportImageList.get(position).getUrl()));
                Picasso.with(mContext)
                        .load(uri)
                        .fit()
                        .centerCrop()
                        .into(ivPhoto);*/
            }
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


}





