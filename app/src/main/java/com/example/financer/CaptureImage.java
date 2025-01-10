package com.example.financer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaptureImage extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_INTENT_REQUEST_CODE = 101;
    String[] regularMonths = {"SKIP 0", "JAN", "FEB","MAR","APR", "MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
    String[] months = {"JAN", "FEB","MAR","APR", "MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
    private Uri imageUri;

    ImageView imageView;
    Button captureButton, saveButton;
    List<Text.TextBlock> blocks;
    String extractedText = "";
    DatabaseReference firebase;
    FirebaseAuth fbAuth;
    String userId;
    String username;
    User user;


    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "permission success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Save the image to a file
            File photoFile = createImageFile();
            if (photoFile != null) {

                Log.d("user", "image file created");
                imageUri = FileProvider.getUriForFile(this, "com.example.financer.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                Log.d("user", "starting activity");
                startActivityForResult(cameraIntent, CAMERA_INTENT_REQUEST_CODE);
            }
        }
    }

    // Create a file to save the image
    private File createImageFile() {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String fileName = "receipt_" + System.currentTimeMillis() + ".jpg";
        return new File(storageDir, fileName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_INTENT_REQUEST_CODE && resultCode == RESULT_OK) {
            // The image is saved at the URI (imageUri)
            processImage(imageUri);
        }
    }

    private void processImage(Uri imageUri) {
        //reset for next receipt
        extractedText = "";
        blocks = null;
        imageView.setImageDrawable(null);

        try {
            InputImage inputImage = InputImage.fromFilePath(this, imageUri);

            // Get an instance of Firebase's TextRecognizer
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            // Process the image
            recognizer.process(inputImage)
                    .addOnSuccessListener(firebaseVisionText -> {
                        // Extract the text from the receipt
                        extractedText = firebaseVisionText.getText();
                        blocks = firebaseVisionText.getTextBlocks();
                       // Log.d("First block", blocks.get(0).getText());

                        for(int i = 0; i < blocks.size(); i++){
                            Log.d("Block " + i, blocks.get(i).getText());
                        }

                        Log.d("MLKitText", "Extracted text: " + extractedText);
                        Toast.makeText(this, "Extracted success!", Toast.LENGTH_LONG).show();

                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(this, "Text recognition failed", Toast.LENGTH_SHORT).show();
                    });

            imageView.setImageURI(imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String createDateString(int year, int month, int day){
        //this method is used for dates that are created, when a date wasn't found in image
        return months[month] + " " + day + " " + year;
    }
    public String createDateStringReg(int year, int month, int day){
        //this method is used for dates that were parsed from receipt
        //these months wont start with 0
        return regularMonths[month] + " " + day + " " + year;
    }
    public void addToFirebase(Transaction t){
        DatabaseReference transactionsReference = firebase.child(userId).child("transactions");
        DatabaseReference userReference = firebase.child(userId).child("profile");

        userReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                user = task.getResult().getValue(User.class);
                int transactionCount;
                if(user != null){
                    transactionCount = user.getTransactionCount();
                    transactionCount = transactionCount + 1;
                    //add transaction
                    transactionsReference.child("Transaction " + transactionCount).setValue(t);
                    //update user's counts
                    HashMap<String,Integer> categories = user.getCategories();
                    int category_count = 0;
                    if(categories.containsKey("receipt")){
                        category_count = categories.get("receipt");
                        category_count = category_count + 1;
                        categories.put("receipt", category_count);
                    }else{
                        categories.put("receipt", 1);
                    }
                    //update hashmap holding all category counts
                    userReference.child("categories").setValue(categories);
                    //update transactions count
                    userReference.child("transactionCount").setValue(transactionCount);

                }
            }
        });

    }
    public void extractTransactionInfo(){
        //1st block of receipt will usually be name of store
        String description = blocks.get(0).getLines().get(0).getText();
        Log.d("Receipt title", description);

        //will use date of when the picture is taken if pattern is not found
        //String datePattern = "\\b(0[1-9]|1[0-2])/([0-2][0-9]|3[01])/\\d{2}\\b";
        String datePattern = "\\b\\d{1,2}/\\d{1,2}/\\d{2}\\b";

        // Compile the pattern
        Pattern firstPattern = Pattern.compile(datePattern);
        Matcher dateMatcher = firstPattern.matcher(extractedText);
        String date = null;
        //check for match
        if(dateMatcher.find()) {
            date = dateMatcher.group();
            String[] dateData = date.split("/");
            int month = Integer.parseInt(dateData[0]);
            int day = Integer.parseInt(dateData[1]);
            int year = Integer.parseInt(dateData[2]);
            date = createDateStringReg(year,month,day);
            Log.d("Receipt Date", date);
        }

        //if date not found in receipt, use today's date
        if(date == null){
            //Initiate Date picker
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

           date = createDateString(year,month,day);
           Log.d("Today's Date", date);
        }


        String category = "Receipt";

        //iterate through blocks looking for numbers
        //the largest one will be the total
        double maxPrice = 0.0f;

        // Regular expression to match prices
        String pricePattern = "\\b\\$?\\d+(\\.\\d{1,2})\\b";

        // Compile the pattern
        Pattern pattern = Pattern.compile(pricePattern);
        Matcher matcher = pattern.matcher(extractedText);
        // Check for matches
        while (matcher.find()) {
            String price = matcher.group();
            double value = Double.parseDouble(price);
            if(value > maxPrice){
                maxPrice = value;
            }

        }

        Log.d("Receipt Total", "Receipt total = $" + Double.toString(maxPrice));

        //add transaction to firebase
        Transaction t = new Transaction(description, date, category, maxPrice);
        addToFirebase(t);

    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);

        imageView = findViewById(R.id.imagePreview);
        captureButton = findViewById(R.id.captureButton);
        saveButton = findViewById(R.id.save_Button);

        //initialize reference to firebase
        firebase = FirebaseDatabase.getInstance().getReference();
        fbAuth = FirebaseAuth.getInstance();
        userId = fbAuth.getCurrentUser().getUid();
        username = fbAuth.getCurrentUser().getDisplayName();
        Log.d("user", userId + ": " + username);

        requestCameraPermission();


        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("user", "opening camera");
                openCamera();
                
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(blocks == null){
                    Toast.makeText(CaptureImage.this, "Please take picture of receipt", Toast.LENGTH_SHORT).show();
                    return;
                }

                extractTransactionInfo();
                Toast.makeText(CaptureImage.this, "Transaction saved", Toast.LENGTH_SHORT).show();
                
            }
        });

    }
}