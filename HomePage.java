package com.example.plant_leaf;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.plant_leaf.ml.DeepLeafGuard;
import com.example.plant_leaf.ml.LeafDetector;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HomePage extends AppCompatActivity {
    FloatingActionButton camera, folder, add;
    Animation open,close,forward,backward;
    boolean isOpen = false;
    TextView disease,txtView,appdesc;
    ImageView imageView4;
    MaterialButton btnProcess;

    int imageSize = 224;
    Uri dat;
    Bitmap image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        camera =(FloatingActionButton) findViewById(R.id.camera);
        folder =(FloatingActionButton) findViewById(R.id.folder);
        add = (FloatingActionButton) findViewById(R.id.add);

        open = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        close = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        forward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        backward = AnimationUtils.loadAnimation(this, R.anim.rotate_backword);

        disease = findViewById(R.id.disease);
        txtView = findViewById(R.id.txtView);
        appdesc=findViewById(R.id.appdesc);
        btnProcess = findViewById(R.id.btnProcess);
        appdesc.setText("Upload an image by clicking on the \n\n\"+\"  icon below");
        imageView4 = findViewById(R.id.imageView4);

        //floating action
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateAdd();

            }
        });


        camera.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent,3);
                }
                else{
                    requestPermissions(new String[]{Manifest.permission.CAMERA},100);
                }
            }
        });

        folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to pick an image from any available app
                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                // Create a chooser intent to show the user all possible apps to pick an image
                Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");

                // Start the activity for result with the chooser intent
                startActivityForResult(chooserIntent, 1);
            }
        });
    }

    public void modelPredict(Bitmap image){
        Bitmap image1=image;
        try {
            LeafDetector model = LeafDetector.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature = TensorBuffer.createFixedSize(new int[]{1, 299, 299, 3}, org.tensorflow.lite.DataType.FLOAT32);
            ByteBuffer byteBuffer = preprocessImageForNewModel(image);
            inputFeature.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            LeafDetector.Outputs outputs = model.process(inputFeature);
            TensorBuffer outputFeature = outputs.getOutputFeature0AsTensorBuffer();


            // Post-process the result as needed
            // Get the array of predicted probabilities
            float[] probabilities = outputFeature.getFloatArray();

            if(probabilities[0]<0.5)
            {
                try {
                    DeepLeafGuard model1 = DeepLeafGuard.newInstance(getApplicationContext());

                    // Prepare the input buffer
                    TensorBuffer inputFeature1 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, org.tensorflow.lite.DataType.FLOAT32);
                    ByteBuffer byteBuffer1 = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
                    byteBuffer1.order(ByteOrder.nativeOrder());

                    // Preprocess the image
                    image1 = Bitmap.createScaledBitmap(image1, 224, 224, false);
                    int[] intValues = new int[224 * 224];
                    image1.getPixels(intValues, 0, image1.getWidth(), 0, 0, image1.getWidth(), image1.getHeight());

                    int pixel = 0;
                    for (int i = 0; i < 224; i++) {
                        for (int j = 0; j < 224; j++) {
                            int val = intValues[pixel++];
                            byteBuffer1.putFloat(((val >> 16) & 0xFF) * (1.f / 255));
                            byteBuffer1.putFloat(((val >> 8) & 0xFF) * (1.f / 255));
                            byteBuffer1.putFloat((val & 0xFF) * (1.f / 255));
                        }
                    }

                    inputFeature1.loadBuffer(byteBuffer1);

                    // Run model inference and get the result
                    DeepLeafGuard.Outputs outputs1 = model1.process(inputFeature1);
                    TensorBuffer outputFeature1 = outputs1.getOutputFeature0AsTensorBuffer();

                    // Post-process the result as needed
                    // Get the array of predicted probabilities
                    float[] probabilities1 = outputFeature1.getFloatArray();

                    // Find the index of the maximum probability
                    int maxIndex = 0;
                    float maxProbability = 0;
                    for (int i = 0; i < probabilities1.length; i++) {
                        if (probabilities1[i] > maxProbability) {
                            maxProbability = probabilities1[i];
                            maxIndex = i;
                        }
                    }

//            String[] classNames = {"Apple___Apple_scab", "Apple___Black_rot", "Apple___Cedar_apple_rust", "Apple___healthy", "Blueberry___healthy", "Cherry_(including_sour)___Powdery_mildew", "Cherry_(including_sour)___healthy", "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot",
//                    "Corn_(maize)___Common_rust_", "Corn_(maize)___Northern_Leaf_Blight", "Corn_(maize)___healthy", "Grape___Black_rot", "Grape___Esca_(Black_Measles)",
//                    "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)", "Grape___healthy", "Orange___Haunglongbing_(Citrus_greening)", "Peach___Bacterial_spot",
//                    "Peach___healthy", "Pepper,_bell___Bacterial_spot", "Pepper,_bell___healthy", "Potato___Early_blight", "Potato___Late_blight", "Potato___healthy",
//                    "Raspberry___healthy", "Soybean___healthy", "Squash___Powdery_mildew", "Strawberry___Leaf_scorch", "Strawberry___healthy", "Tomato___Bacterial_spot",
//                    "Tomato___Early_blight", "Tomato___Late_blight", "Tomato___Leaf_Mold", "Tomato___Septoria_leaf_spot", "Tomato___Spider_mites Two-spotted_spider_mite",
//                    "Tomato___Target_Spot", "Tomato___Tomato_Yellow_Leaf_Curl_Virus", "Tomato___Tomato_mosaic_virus", "Tomato___healthy"};



                    String[] classNames = {"Apple Scab", "Apple Black Rot","Apple Cedar Apple Rust","Apple Healthy","Blueberry Healthy", "Cherry (including Sour) Powdery Mildew","Cherry (including Sour) Healthy","Corn(Maize) Cercospora Gray Leaf Spot",
                            "Corn(Maize) Common Rust", "Corn(Maize) Northern Leaf Blight", "Corn(Maize) Healthy","Grape Black Rot", "Grape Esca (Black Measles)",
                            "Grape Leaf Blight (Isariopsis Leaf Spot)","Grape Healthy","Orange Haunglongbing(Citrus Greening)","Peach Bacterial Spot",
                            "Peach Healthy", "Pepper Bell Bacterial Spot", "Pepper Bell Healthy", "Potato Early Blight", "Potato Late Blight", "Potato Healthy",
                            "Raspberry Healthy","Soybean Healthy","Squash Powdery Mildew", "Strawberry Leaf Scorch","Strawberry Healthy","Tomato Bacterial Spot",
                            "Tomato Early Blight","Tomato Late Blight","Tomato Leaf Mold","Tomato Septoria Leaf Spot", "Tomato Spider Mites Two Spotted Spider Mite",
                            "Tomato Target Spot","Tomato Yellow Leaf Curl Virus", "Tomato Mosaic Virus","Tomato Healthy"};



                    String disease= classNames[maxIndex];
                    Float accuracy=maxProbability;
                    updateUI(disease, accuracy);


                    btnProcess.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Assuming you have the predicted class and confidence available
                            String predictedClass = disease.toString();
                            String accuracyString = txtView.getText().toString(); // Remove non-numeric characters from the string
                            accuracyString = accuracyString.replaceAll("[^0-9.]", "");
                            float accuracy = Float.parseFloat(accuracyString);

                            // Create an Intent to navigate to the RemediesActivity
                            Intent intent = new Intent(HomePage.this, RemedyActivity.class);

                            // Pass necessary information to the RemediesActivity
                            intent.putExtra("disease", disease);
                            intent.putExtra("accuracy", accuracy);
                            intent.putExtra("image", image);
                            // Start the RemediesActivity
                            startActivity(intent);
                        }
                    });


                    // Release model resources if no longer used
                    model.close();

                } catch (IOException e) {
                    // Handle the exception
                    e.printStackTrace();
                }
            }
            else{
                String result = "Leaf not detected!!!";
                disease.setTextColor(Color.RED);
                disease.setText("Please upload the image of a Leaf!");
                txtView.setText("");
                //btnProcess.setVisibility(View.GONE);
                btnProcess.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(HomePage.this, HomePage.class);
                        startActivity(intent);
                    }
                });
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            }

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
            e.printStackTrace();
        }

    }

    private ByteBuffer preprocessImageForNewModel(Bitmap image) {
        // Resize the image to the required input size (299x299)
        Bitmap resizedImage = Bitmap.createScaledBitmap(image, 299, 299, false);

        // Initialize a ByteBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 299 * 299 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        // Convert the Bitmap to ByteBuffer
        int[] intValues = new int[299 * 299];
        resizedImage.getPixels(intValues, 0, resizedImage.getWidth(), 0, 0, resizedImage.getWidth(), resizedImage.getHeight());

        int pixel = 0;
        for (int i = 0; i < 299; i++) {
            for (int j = 0; j < 299; j++) {
                int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255));
                byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));
                byteBuffer.putFloat((val & 0xFF) * (1.f / 255));
            }
        }

        return byteBuffer;
    }

    // Add this method to update UI based on the prediction
    private void updateUI(String predictedClass,Float accuracy) {
        disease.setText(String.format("Disease/Class:  %s",predictedClass));
        txtView.setText(String.format("Confidence:  %.2f%%", accuracy*100));
        // Add any other UI updates you need
    }

   @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
       appdesc.setText("Uploaded Image:");
        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {
                image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView4.setImageBitmap(image);
                imageView4.setVisibility(View.VISIBLE);
                image = Bitmap.createScaledBitmap(image, 224, 224, false);
                modelPredict(image);
                btnProcess.setVisibility(View.VISIBLE);

            }
            else{
                dat = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                }
                catch(IOException e){
                    e.printStackTrace();
                }
                imageView4.setImageBitmap(image);
                imageView4.setVisibility(View.VISIBLE);
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                modelPredict(image);
                btnProcess.setVisibility(View.VISIBLE);

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void animateAdd(){
        if (isOpen){
            add.startAnimation(forward);
            camera.startAnimation(close);
            folder.startAnimation(close);
            camera.setClickable(false);
            folder.setClickable(false);
            isOpen=false;
        }
        else{
            add.startAnimation(backward);
            camera.startAnimation(open);
            folder.startAnimation(open);
            camera.setClickable(true);
            folder.setClickable(true);
            isOpen = true;
        }
    }
}