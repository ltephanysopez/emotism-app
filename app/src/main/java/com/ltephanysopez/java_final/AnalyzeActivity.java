package com.ltephanysopez.java_final;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.support.annotation.Nullable;
import android.view.*;
import android.graphics.*;
import android.widget.*;
import android.provider.*;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class AnalyzeActivity extends Activity {

    private final int PICK_IMAGE = 1;
    private ProgressDialog detectionProgressDialog;


    // Replace `<API endpoint>` with the Azure region associated with
    // your subscription key. For example,
    // apiEndpoint = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0"
    private final String apiEndpoint = "https://centralus.api.cognitive.microsoft.com/face/v1.0";

    // Replace `<Subscription Key>` with your subscription key.
    // For example, subscriptionKey = "0123456789abcdef0123456789ABCDEF"
    private final String subscriptionKey = "a1632a68568743608a2cb113c860e12a";

    private final FaceServiceClient faceServiceClient =
            new FaceServiceRestClient(apiEndpoint, subscriptionKey);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);
        Button button1 = findViewById(R.id.btnBrowseForImage);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(
                        intent, "Select a Picture"), PICK_IMAGE);
            }
        });

        detectionProgressDialog = new ProgressDialog(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), uri);
                ImageView imageView = findViewById(R.id.imageView1);
                imageView.setImageBitmap(bitmap);
                detectAndFrame(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void detectAndFrame(final Bitmap imageBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        @SuppressLint("StaticFieldLeak")
        AsyncTask<InputStream, String, Face[]> detectTask = new AsyncTask<InputStream, String, Face[]>() {

            String exceptionMessage = "";

//            String[] faceAttributes = {"age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise"};

            FaceServiceClient.FaceAttributeType[] faceAttributes = new FaceServiceClient.FaceAttributeType[] {
                    FaceServiceClient.FaceAttributeType.Age,
                    FaceServiceClient.FaceAttributeType.Gender,
                    FaceServiceClient.FaceAttributeType.HeadPose,
                    FaceServiceClient.FaceAttributeType.Smile,
                    FaceServiceClient.FaceAttributeType.FacialHair,
                    FaceServiceClient.FaceAttributeType.Glasses,
                    FaceServiceClient.FaceAttributeType.Emotion,
                    FaceServiceClient.FaceAttributeType.Hair,
                    FaceServiceClient.FaceAttributeType.Makeup,
                    FaceServiceClient.FaceAttributeType.Occlusion,
                    FaceServiceClient.FaceAttributeType.Accessories,
                    FaceServiceClient.FaceAttributeType.Blur,
                    FaceServiceClient.FaceAttributeType.Exposure,
                    FaceServiceClient.FaceAttributeType.Noise
            };

            @Override
            protected Face[] doInBackground(InputStream... params) {

                try {
                    publishProgress("Detecting...");
                    Face[] result = faceServiceClient.detect(params[0], true, false, faceAttributes);
                    if (result == null) {
                        publishProgress("Detection Finished. Nothing detected");
                        return null;
                    }
                    publishProgress(String.format("Detection Finished. %d face(s) detected", result.length));
                    return result;
                } catch (Exception e) {
                    exceptionMessage = String.format("Detection failed: %s", e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                detectionProgressDialog.show();
            }

            @Override
            protected void onPostExecute(Face[] faces) {
                detectionProgressDialog.dismiss();
                if(!exceptionMessage.equals("")) {
                    showError(exceptionMessage);
                }
                if(faces == null) return;
                ImageView imgPhoto = findViewById(R.id.imageView1);
                imgPhoto.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, faces));
                imageBitmap.recycle();
                showResult(faces);
            }

            @Override
            protected void onProgressUpdate(String... values) {
                detectionProgressDialog.setMessage(values[0]);
            }
        };
        detectTask.execute(inputStream);
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }})
                .create().show();
    }

    private static Bitmap drawFaceRectanglesOnBitmap(
            Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
    }

    private void showResult(Face[] faces) {
        TextView txtResult = this.findViewById(R.id.txtResult);
        StringBuilder result = new StringBuilder();
        if (faces != null) {
            for (Face face : faces) {
                result.append("\nanger : ").append(face.faceAttributes.emotion.anger);
                result.append("\ncontempt : ").append(face.faceAttributes.emotion.contempt);
                result.append("\ndisgust : ").append(face.faceAttributes.emotion.disgust);
                result.append("\nfear : ").append(face.faceAttributes.emotion.fear);
                result.append("\nhappiness : ").append(face.faceAttributes.emotion.happiness);
                result.append("\nneutral : ").append(face.faceAttributes.emotion.neutral);
                result.append("\nsadness : ").append(face.faceAttributes.emotion.sadness);
                result.append("\nsurprise : ").append(face. faceAttributes.emotion.surprise);
                result.append("\n\n");
            }
            txtResult.setText(result.toString());
        }
    }
}
