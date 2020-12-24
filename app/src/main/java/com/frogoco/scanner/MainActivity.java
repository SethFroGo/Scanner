package com.frogoco.scanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity{
    TextView mainText;
    Button startButton, compButton;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap curBitmap;
    ImageView mImageView;
    private String photoPath;
    FirebaseVisionText ftext;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainText = findViewById(R.id.mainText);
        startButton = findViewById(R.id.cameraLaunchButton);
        compButton = findViewById(R.id.computeButton);
        mImageView = findViewById(R.id.imageView);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButtonFunction();
            }
        });
        compButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                computeImage();
            }
        });
    }

    private void startButtonFunction() {
        mainText.setText(getString(R.string.newText));
        System.out.println("clicked");
        selectImage(this);
    }

    private void cleanText(FirebaseVisionText ftext) {
        String raw = ftext.getText();
        int numblock = ftext.getTextBlocks().size();
        ArrayList<FirebaseVisionText.TextBlock> blocks = new ArrayList<>();
        ArrayList<Integer> numlines = new ArrayList<>();
        for (FirebaseVisionText.TextBlock block : ftext.getTextBlocks()) {
            blocks.add(block);
            numlines.add(block.getLines().size());
        }


    }

    private void computeImage() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    System.out.println("start ftext");
                    ftext = fireText();
                    System.out.println("finish ftext");
                    System.out.println(ftext.getTextBlocks().size());
                    System.out.println(ftext.getText());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    System.out.println("exbroke");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.out.println("intbroke");;
                }
            }}).start();
    }

    private FirebaseVisionText fireText() throws ExecutionException, InterruptedException {
        FirebaseVisionImage fImg = FirebaseVisionImage.fromBitmap(curBitmap);
        FirebaseVisionTextRecognizer tscan = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        System.out.println("before task");

        Task<FirebaseVisionText> result =
                tscan.processImage(fImg)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                System.out.println("task success");

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println("task fail");
                                    }
                                });
        Tasks.await(result);
        return result.getResult();

    }

    private void gottenText(FirebaseVisionText fireText) {
        System.out.println(fireText.getText());
    }

    private void selectImage(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        curBitmap = selectedImage;
                        mImageView.setImageBitmap(selectedImage);
                        System.out.println("loaded pic");
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                photoPath = picturePath;
                                Bitmap tempBitmap = BitmapFactory.decodeFile(picturePath);
                                curBitmap = tempBitmap;
                                int rotate = 0;
                                Matrix mat = new Matrix();
                                try {
                                    ExifInterface exi = new ExifInterface(photoPath);
                                    int orient = exi.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                            ExifInterface.ORIENTATION_NORMAL);
                                    switch (orient) {
                                        case ExifInterface.ORIENTATION_ROTATE_270:
                                            rotate = 270;
                                            mat.postRotate(270);
                                            System.out.println("rotate 270");
                                            curBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(),
                                                    tempBitmap.getHeight(), mat, true);
                                            break;
                                        case ExifInterface.ORIENTATION_ROTATE_180:
                                            rotate = 180;
                                            mat.postRotate(180);
                                            System.out.println("rotate 180");
                                            curBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(),
                                                    tempBitmap.getHeight(), mat, true);
                                            break;
                                        case ExifInterface.ORIENTATION_ROTATE_90:
                                            rotate = 90;
                                            mat.postRotate(90);
                                            System.out.println("rotate 90");
                                            curBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(),
                                                    tempBitmap.getHeight(), mat, true);
                                            break;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("took pic");
                                mImageView.setImageBitmap(curBitmap);
                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }



}