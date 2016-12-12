package com.tut.qr.qr_reader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class QRActivity extends AppCompatActivity {

    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    private File mTempCameraPhotoFile;
    private ImageView iv;
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        iv = (ImageView) findViewById(R.id.qrImageView);
        tv = (TextView)findViewById(R.id.qrTextView);
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
        tv.setText("Data : ");
    }

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
                //create bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
                bitmap = rotateBitmap(bitmap, 90);
                //display image
                iv.setImageBitmap(bitmap);
                //decode barcode
                decodeBarcode(bitmap);
                //new barcodeTask(bitmap).execute();
            }
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void decodeBarcode(Bitmap bitmap){
        BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                //.setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
        if(!detector.isOperational()){
            Toast.makeText(getApplicationContext(), "Could not set up the detector!", Toast.LENGTH_SHORT).show();
            //return;
        }else{
            Toast.makeText(getApplicationContext(), "Detector is operating", Toast.LENGTH_SHORT).show();
        }
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);
        String data = "Data : ";
        if(barcodes.size() > 0) {
            Barcode thisCode = barcodes.valueAt(0);
            data += thisCode.rawValue;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(thisCode.rawValue)));
        }else{
            data+="Error";
        }
        tv.setText(data);

    }

    private class barcodeTask extends AsyncTask<Void,Void,Void>{

        Bitmap bitmap;
        String uri = "";

        public barcodeTask(Bitmap bitmap){
            this.bitmap=bitmap;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                    //.setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                    .build();
            if(!detector.isOperational()){
                //Toast.makeText(getApplicationContext(), "Could not set up the detector!", Toast.LENGTH_SHORT).show();
                //return;
            }else{
                //Toast.makeText(getApplicationContext(), "Detector is operating", Toast.LENGTH_SHORT).show();
            }
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Barcode> barcodes = detector.detect(frame);
            String data = "Data : ";
            if(barcodes.size() > 0) {
                Barcode thisCode = barcodes.valueAt(0);
                data += thisCode.rawValue;
                uri = thisCode.rawValue;

            }else{
                data+="Error";
            }
            //tv.setText(data);
            final String finalData = data;
            tv.post(new Runnable() {
                @Override
                public void run() {
                    tv.setText(finalData);
                }
            });
           return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            }catch (Exception e){}
            super.onPostExecute(aVoid);
        }
    }

}





