package com.example.matrixphotoeditor.double_exp;

import static java.lang.Math.min;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.matrixphotoeditor.BitmapEffectActivity;
import com.example.matrixphotoeditor.R;

import java.io.IOException;

public class DoubleExpActivity extends BitmapEffectActivity {
    private final int SELECT_PICTURE = 200;

    private RadioButton defaultRadio;
    private Bitmap newBitmap;
    private DoubleExpEffect thisEffect;
    private int lastValue = 0;
    private boolean clearRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_exp);

        actionBar.setTitle("Double Exposure");
        initializeElements(findViewById(R.id.user_image), findViewById(R.id.seek_bar));

        RadioGroup firstGroup = findViewById(R.id.first_group);
        RadioGroup secondGroup = findViewById(R.id.second_group);
        defaultRadio = findViewById(R.id.radio_default);

        firstGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == -1 || clearRequest) {
                    clearRequest = false;
                    return;
                }
                clearRequest = true;
                secondGroup.clearCheck();

                switch (checkedId) {
                    case R.id.radio_default:
                        thisEffect = new DefaultEffect();
                        break;
                    case R.id.radio_light:
                        thisEffect = new LightenEffect();
                        break;
                    case R.id.radio_dark:
                        thisEffect = new DarkenEffect();
                        break;
                }
                previewEffect(lastValue);
            }
        });

        secondGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == -1 || clearRequest) {
                    clearRequest = false;
                    return;
                }

                clearRequest = true;
                firstGroup.clearCheck();

                switch (checkedId) {
                    case R.id.radio_add:
                        thisEffect = new AddingEffect();
                        break;
                    case R.id.radio_sub:
                        thisEffect = new SubtractEffect();
                        break;
                }
                previewEffect(lastValue);
            }
        });
        imageChooser();
    }

    @Override
    protected void previewEffect(int i) {
        int width = initialBitmap.getWidth();
        int height = initialBitmap.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 60 && y == 100) {
                    x = x;
                }
                int first = initialBitmap.getPixel(x, y);
                int second = newBitmap.getPixel(x, y);
                resultBitmap.setPixel(x, y, Color.rgb(
                        thisEffect.combinePixels(Color.red(first), Color.red(second), i),
                        thisEffect.combinePixels(Color.green(first), Color.green(second), i),
                        thisEffect.combinePixels(Color.blue(first), Color.blue(second), i)
                ));
            }
        }
        matrixImage.setBitmap(resultBitmap);
        lastValue = i;
    }

    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    private Bitmap scaleBitmap(Bitmap bitmap) {
        int initWidth = initialBitmap.getWidth();
        int initHeight = initialBitmap.getHeight();
        /*
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (initWidth > initHeight) {
            height = (initWidth * height) / width;
            width = initWidth;
        } else {
            width = (initHeight * width) / height;
            height = initHeight;
        }*/
        return Bitmap.createScaledBitmap(bitmap, initWidth, initHeight, true);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        newBitmap = scaleBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri));
                        defaultRadio.setChecked(true);

                    } catch (IOException e) {
                        Toast.makeText(this, "Cannot open this image", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        }
    }
}