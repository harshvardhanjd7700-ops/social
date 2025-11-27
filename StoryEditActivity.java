package finix.social.finixapp;

import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import finix.social.finixapp.story.TextEditorDialog;
import finix.social.finixapp.story.TextConfig;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.EditText;
import android.view.Gravity;


import android.view.inputmethod.InputMethodManager;
import android.text.TextWatcher;
import android.text.Editable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.OptIn;
import androidx.media3.common.Effect;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.effect.RgbFilter;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.Effects;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.Transformer;
import androidx.media3.transformer.Composition;


import finix.social.finixapp.app.App;
import finix.social.finixapp.constants.Constants;
import finix.social.finixapp.util.MultipartRequest;
import finix.social.finixapp.model.StoryInteractive;

@OptIn(markerClass = UnstableApi.class)
public class StoryEditActivity extends AppCompatActivity {

    private static final String TAG = "StoryEditActivity";

    private ImageView imageView;
    private VideoView videoView;
    private ImageView btnClose;
    private TextView btnPost;
    private ProgressBar progressBar;

    // Interactive story element (poll/question)
    private StoryInteractive mInteractiveDraft = null;

    // Overlay container + sticker view for interactive elements
    private FrameLayout overlayContainer;
    private ImageView deleteBin;
    private boolean isOverDeleteArea = false;

    private View interactiveStickerView;

    // Centered text preview overlay for interactive sticker (fallback text)
    private TextView interactivePreviewText;

    // Bottom filter strip
    private RecyclerView filterRecyclerView;
    private FilterAdapter filterAdapter;

    private Uri mediaUri;
    private boolean isVideo;
    private boolean isFrontCamera = false;

    private GestureDetectorCompat gestureDetector;

    // Zoom & pan support for image stories
    private ScaleGestureDetector scaleGestureDetector;
    private final Matrix imageMatrix = new Matrix();
    private float currentScale = 1f;
    private float minScale = 1f;
    private float maxScale = 4f;
    private float lastTouchX;
    private float lastTouchY;
    private boolean isDragging = false;


    private ColorMatrixColorFilter[] filters;
    private String[] filterNames;
    private int currentFilterIndex = 0;
    private Bitmap originalBitmap;

    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_edit);

        imageView = findViewById(R.id.story_edit_image);
        videoView = findViewById(R.id.story_edit_video);
        btnClose = findViewById(R.id.story_edit_close);
        btnPost = findViewById(R.id.story_edit_post);
        progressBar = findViewById(R.id.story_edit_progress);
        filterRecyclerView = findViewById(R.id.story_filter_list);
        overlayContainer = findViewById(R.id.story_edit_overlay_container);
        deleteBin = findViewById(R.id.delete_bin);
        interactivePreviewText = findViewById(R.id.story_edit_interactive_preview);
        ImageView btnInteractive = findViewById(R.id.story_edit_interactive);
        ImageView btnText = findViewById(R.id.story_edit_text);

        if (btnText != null) {
            btnText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTextEditDialog();
                }
            });
        }

        mediaUri = getIntent().getData();
        isVideo = getIntent().getBooleanExtra("isVideo", false);
        isFrontCamera = getIntent().getBooleanExtra("isFrontCamera", false);

        if (mediaUri == null) {
            Toast.makeText(this, "No media", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupFilters();
        setupGestures();
        setupFilterList();

        if (isVideo) {
            showVideo();
        } else {
            showImage();
        }

        maybeAttachAmaReplySticker();

        btnClose.setOnClickListener(v -> onBackPressed());
        btnPost.setOnClickListener(v -> safeUploadWrapper());

        if (btnInteractive != null) {
            btnInteractive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showInteractiveDialog();
                }
            });
        }
    }






    /**
     * If this StoryEditActivity was opened as a reply to an AMA question,
     * attach a pre-built sticker showing the question + the user's answer,
     * using the same gradient as the Ask Me sticker.
     */
    private void maybeAttachAmaReplySticker() {

        if (overlayContainer == null) return;

        Intent intent = getIntent();
        if (intent == null) return;

        String amaQuestion = intent.getStringExtra("AMA_QUESTION");
        String amaAnswer = intent.getStringExtra("AMA_ANSWER");

        if (amaQuestion == null || amaAnswer == null) {
            return; // not in AMA reply mode
        }

        View sticker = getLayoutInflater().inflate(
                R.layout.view_ama_reply_sticker,
                overlayContainer,
                false
        );

        TextView q = sticker.findViewById(R.id.text_ama_sticker_question);
        TextView a = sticker.findViewById(R.id.text_ama_sticker_answer);

        if (q != null) q.setText(amaQuestion);
        if (a != null) a.setText(amaAnswer);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER;

        overlayContainer.addView(sticker, lp);

        // Make the sticker draggable/transformable like other overlays
        makeStickerMovableAndScalable(sticker);
    }

    private void setupFilters() {
        // 25 Instagram-style presets (names only; looks are approximations using ColorMatrix)
        // 0: Original
        // 1: Clarendon
        // 2: Gingham
        // 3: Juno
        // 4: Lark
        // 5: Valencia
        // 6: Aden
        // 7: Crema
        // 8: Slumber
        // 9: Reyes
        // 10: Rise
        // 11: Amaro
        // 12: Mayfair
        // 13: Nashville
        // 14: Lo-Fi
        // 15: Toaster
        // 16: Hefe
        // 17: Sierra
        // 18: Hudson
        // 19: X-Pro II
        // 20: Willow
        // 21: Earlybird
        // 22: Brannan
        // 23: Sutro
        // 24: Vesper
        filterNames = new String[]{
                "Original",   // 0
                "Clarendon",  // 1
                "Gingham",    // 2
                "Juno",       // 3
                "Lark",       // 4
                "Valencia",   // 5
                "Aden",       // 6
                "Crema",      // 7
                "Slumber",    // 8
                "Reyes",      // 9
                "Rise",       // 10
                "Amaro",      // 11
                "Mayfair",    // 12
                "Nashville",  // 13
                "Lo-Fi",      // 14
                "Toaster",    // 15
                "Hefe",       // 16
                "Sierra",     // 17
                "Hudson",     // 18
                "X-Pro II",   // 19
                "Willow",     // 20
                "Earlybird",  // 21
                "Brannan",    // 22
                "Sutro",      // 23
                "Vesper"      // 24
        };

        filters = new ColorMatrixColorFilter[filterNames.length];

        // 0: Original (no filter)
        filters[0] = null;

        // Helper matrices we reuse to approximate different looks
        // Base: slightly increased contrast
        ColorMatrix baseContrast = new ColorMatrix(new float[]{
                1.1f, 0f,   0f,   0f, -5f,
                0f,   1.1f, 0f,   0f, -5f,
                0f,   0f,   1.1f, 0f, -5f,
                0f,   0f,   0f,   1f,  0f
        });

        // Base warm and cool tints
        ColorMatrix warmTint = new ColorMatrix(new float[]{
                1.08f, 0f,    0f,    0f, 6f,
                0f,    1.02f, 0f,    0f, 2f,
                0f,    0f,    0.95f, 0f,-4f,
                0f,    0f,    0f,    1f, 0f
        });

        ColorMatrix coolTint = new ColorMatrix(new float[]{
                0.96f, 0f,    0f,    0f,-4f,
                0f,    1.02f, 0f,    0f, 2f,
                0f,    0f,    1.08f, 0f, 5f,
                0f,    0f,    0f,    1f, 0f
        });

        // 1: Clarendon – cool shadows, bright highlights
        ColorMatrix clarendon = new ColorMatrix();
        clarendon.setSaturation(1.1f);
        clarendon.postConcat(baseContrast);
        clarendon.postConcat(coolTint);
        filters[1] = new ColorMatrixColorFilter(clarendon);

        // 2: Gingham – faded, warm
        ColorMatrix gingham = new ColorMatrix();
        gingham.setSaturation(0.9f);
        ColorMatrix fadeLift = new ColorMatrix(new float[]{
                1.02f,0f,   0f,   0f, 8f,
                0f,   1.02f,0f,   0f, 8f,
                0f,   0f,   1.0f,0f, 4f,
                0f,   0f,   0f,   1f, 0f
        });
        gingham.postConcat(fadeLift);
        gingham.postConcat(warmTint);
        filters[2] = new ColorMatrixColorFilter(gingham);

        // 3: Juno – warm and vibrant
        ColorMatrix juno = new ColorMatrix();
        juno.setSaturation(1.25f);
        ColorMatrix junoScale = new ColorMatrix();
        junoScale.setScale(1.1f, 1.05f, 0.98f, 1f);
        juno.postConcat(junoScale);
        juno.postConcat(warmTint);
        filters[3] = new ColorMatrixColorFilter(juno);

        // 4: Lark – bright, soft
        ColorMatrix lark = new ColorMatrix();
        lark.setSaturation(1.05f);
        ColorMatrix larkLift = new ColorMatrix(new float[]{
                1.05f,0f,   0f,   0f, 6f,
                0f,   1.05f,0f,   0f, 6f,
                0f,   0f,   1.02f,0f, 2f,
                0f,   0f,   0f,   1f, 0f
        });
        lark.postConcat(larkLift);
        filters[4] = new ColorMatrixColorFilter(lark);

        // 5: Valencia – warm and slightly faded
        ColorMatrix valencia = new ColorMatrix();
        valencia.setSaturation(1.02f);
        valencia.postConcat(warmTint);
        ColorMatrix valenciaFade = new ColorMatrix(new float[]{
                1.0f, 0f,   0f,   0f, 4f,
                0f,   1.0f, 0f,   0f, 4f,
                0f,   0f,   0.98f,0f, 0f,
                0f,   0f,   0f,   1f, 0f
        });
        valencia.postConcat(valenciaFade);
        filters[5] = new ColorMatrixColorFilter(valencia);

        // 6: Aden – soft pastel
        ColorMatrix aden = new ColorMatrix();
        aden.setSaturation(0.9f);
        ColorMatrix adenScale = new ColorMatrix();
        adenScale.setScale(1.02f, 0.98f, 1.05f, 1f);
        aden.postConcat(adenScale);
        aden.postConcat(fadeLift);
        filters[6] = new ColorMatrixColorFilter(aden);

        // 7: Crema – creamy warm
        ColorMatrix crema = new ColorMatrix();
        crema.setSaturation(0.95f);
        ColorMatrix cremaScale = new ColorMatrix();
        cremaScale.setScale(1.06f, 1.02f, 0.96f, 1f);
        crema.postConcat(cremaScale);
        crema.postConcat(warmTint);
        filters[7] = new ColorMatrixColorFilter(crema);

        // 8: Slumber – low contrast, dreamy
        ColorMatrix slumber = new ColorMatrix();
        slumber.setSaturation(0.85f);
        ColorMatrix slumberLift = new ColorMatrix(new float[]{
                1.0f, 0f,  0f,  0f, 10f,
                0f,   1.0f,0f,  0f, 10f,
                0f,   0f,  0.98f,0f, 6f,
                0f,   0f,  0f,  1f, 0f
        });
        slumber.postConcat(slumberLift);
        filters[8] = new ColorMatrixColorFilter(slumber);

        // 9: Reyes – faded and bright
        ColorMatrix reyes = new ColorMatrix();
        reyes.setSaturation(0.9f);
        ColorMatrix reyesLift = new ColorMatrix(new float[]{
                1.02f,0f,   0f,   0f, 12f,
                0f,   1.02f,0f,   0f, 12f,
                0f,   0f,   1.0f, 0f, 4f,
                0f,   0f,   0f,   1f, 0f
        });
        reyes.postConcat(reyesLift);
        filters[9] = new ColorMatrixColorFilter(reyes);

        // 10: Rise – warm glow
        ColorMatrix rise = new ColorMatrix();
        rise.setSaturation(1.05f);
        ColorMatrix riseScale = new ColorMatrix();
        riseScale.setScale(1.08f, 1.02f, 0.96f, 1f);
        rise.postConcat(riseScale);
        rise.postConcat(warmTint);
        filters[10] = new ColorMatrixColorFilter(rise);

        // 11: Amaro – bright center, cool shadows (approx)
        ColorMatrix amaro = new ColorMatrix();
        amaro.setSaturation(1.05f);
        amaro.postConcat(coolTint);
        amaro.postConcat(fadeLift);
        filters[11] = new ColorMatrixColorFilter(amaro);

        // 12: Mayfair – bright with slight vignette feel (approx)
        ColorMatrix mayfair = new ColorMatrix();
        mayfair.setSaturation(1.1f);
        ColorMatrix mayfairScale = new ColorMatrix();
        mayfairScale.setScale(1.08f, 1.03f, 0.98f, 1f);
        mayfair.postConcat(mayfairScale);
        filters[12] = new ColorMatrixColorFilter(mayfair);

        // 13: Nashville – pastel vintage
        ColorMatrix nashville = new ColorMatrix();
        nashville.setSaturation(0.9f);
        ColorMatrix nashvilleScale = new ColorMatrix();
        nashvilleScale.setScale(1.06f, 0.98f, 1.02f, 1f);
        nashville.postConcat(nashvilleScale);
        nashville.postConcat(fadeLift);
        filters[13] = new ColorMatrixColorFilter(nashville);

        // 14: Lo-Fi – high contrast & saturation
        ColorMatrix loFi = new ColorMatrix();
        loFi.setSaturation(1.25f);
        loFi.postConcat(baseContrast);
        filters[14] = new ColorMatrixColorFilter(loFi);

        // 15: Toaster – strong warm cast
        ColorMatrix toaster = new ColorMatrix();
        toaster.setSaturation(1.1f);
        ColorMatrix toasterScale = new ColorMatrix();
        toasterScale.setScale(1.15f, 1.05f, 0.9f, 1f);
        toaster.postConcat(toasterScale);
        toaster.postConcat(warmTint);
        filters[15] = new ColorMatrixColorFilter(toaster);

        // 16: Hefe – deep, warm contrast
        ColorMatrix hefe = new ColorMatrix();
        hefe.setSaturation(1.1f);
        ColorMatrix hefeScale = new ColorMatrix();
        hefeScale.setScale(1.12f, 1.05f, 0.95f, 1f);
        hefe.postConcat(hefeScale);
        hefe.postConcat(baseContrast);
        filters[16] = new ColorMatrixColorFilter(hefe);

        // 17: Sierra – soft fade
        ColorMatrix sierra = new ColorMatrix();
        sierra.setSaturation(0.9f);
        ColorMatrix sierraLift = new ColorMatrix(new float[]{
                1.02f,0f,   0f,   0f, 10f,
                0f,   1.02f,0f,   0f, 10f,
                0f,   0f,   0.98f,0f, 4f,
                0f,   0f,   0f,   1f, 0f
        });
        sierra.postConcat(sierraLift);
        filters[17] = new ColorMatrixColorFilter(sierra);

        // 18: Hudson – cool and bright
        ColorMatrix hudson = new ColorMatrix();
        hudson.setSaturation(1.05f);
        hudson.postConcat(coolTint);
        filters[18] = new ColorMatrixColorFilter(hudson);

        // 19: X-Pro II – strong contrast & color
        ColorMatrix xpro = new ColorMatrix();
        xpro.setSaturation(1.2f);
        xpro.postConcat(baseContrast);
        ColorMatrix xproScale = new ColorMatrix();
        xproScale.setScale(1.1f, 1.05f, 0.95f, 1f);
        xpro.postConcat(xproScale);
        filters[19] = new ColorMatrixColorFilter(xpro);

        // 20: Willow – soft mono
        ColorMatrix willow = new ColorMatrix();
        willow.setSaturation(0f);
        ColorMatrix willowLift = new ColorMatrix(new float[]{
                1.02f,0f,   0f,   0f, 8f,
                0f,   1.02f,0f,   0f, 8f,
                0f,   0f,   1.02f,0f, 8f,
                0f,   0f,   0f,   1f, 0f
        });
        willow.postConcat(willowLift);
        filters[20] = new ColorMatrixColorFilter(willow);

        // 21: Earlybird – warm, muted
        ColorMatrix earlybird = new ColorMatrix();
        earlybird.setSaturation(0.9f);
        earlybird.postConcat(warmTint);
        earlybird.postConcat(fadeLift);
        filters[21] = new ColorMatrixColorFilter(earlybird);

        // 22: Brannan – high contrast with cool tone
        ColorMatrix brannan = new ColorMatrix();
        brannan.setSaturation(1.05f);
        brannan.postConcat(baseContrast);
        brannan.postConcat(coolTint);
        filters[22] = new ColorMatrixColorFilter(brannan);

        // 23: Sutro – dark, moody
        ColorMatrix sutro = new ColorMatrix();
        sutro.setSaturation(0.85f);
        ColorMatrix sutroDark = new ColorMatrix(new float[]{
                0.95f,0f,   0f,   0f,-10f,
                0f,   0.95f,0f,   0f,-10f,
                0f,   0f,   0.9f, 0f,-12f,
                0f,   0f,   0f,   1f, 0f
        });
        sutro.postConcat(sutroDark);
        sutro.postConcat(warmTint);
        filters[23] = new ColorMatrixColorFilter(sutro);

        // 24: Vesper – warm with lifted blacks
        ColorMatrix vesper = new ColorMatrix();
        vesper.setSaturation(0.95f);
        ColorMatrix vesperScale = new ColorMatrix();
        vesperScale.setScale(1.06f, 1.02f, 0.96f, 1f);
        vesper.postConcat(vesperScale);
        vesper.postConcat(fadeLift);
        filters[24] = new ColorMatrixColorFilter(vesper);

        currentFilterIndex = 0;
    }


    private void setupFilterList() {
        if (filterRecyclerView == null) return;

        if (isVideo) {
            // For videos we just show original; hide filter strip
            filterRecyclerView.setVisibility(View.GONE);
            return;
        }

        filterRecyclerView.setVisibility(View.VISIBLE);

        if (filterAdapter == null) {
            filterAdapter = new FilterAdapter();
            LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            filterRecyclerView.setLayoutManager(lm);
            filterRecyclerView.setAdapter(filterAdapter);
        } else {
            filterAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Simple inline adapter to show Instagram‑style filter names in a horizontal strip.
     * Tapping a filter applies it and highlights the selected item.
     */
    private class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

        @Override
        public FilterViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_story_filter, parent, false);
            return new FilterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FilterViewHolder holder, int position) {
            if (filterNames == null || position < 0 || position >= filterNames.length) return;

            String name = filterNames[position];
            holder.nameView.setText(name);

            boolean selected = (position == currentFilterIndex);
            holder.nameView.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
            holder.nameView.setTextColor(selected ? Color.WHITE : Color.LTGRAY);
            holder.nameView.setAlpha(selected ? 1.0f : 0.7f);

            holder.itemView.setOnClickListener(v -> {
                currentFilterIndex = position;
                applyCurrentFilter();
            });
        }

        @Override
        public int getItemCount() {
            return filterNames != null ? filterNames.length : 0;
        }

        class FilterViewHolder extends RecyclerView.ViewHolder {

            final android.widget.TextView nameView;

            FilterViewHolder(View itemView) {
                super(itemView);
                nameView = itemView.findViewById(R.id.story_filter_name);
            }
        }
    }


    private void setupGestures() {
        // Swipe left/right to change filters
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                showTextEditDialog();
                return true;
            }

            private static final int SWIPE_THRESHOLD = 80;
            private static final int SWIPE_VELOCITY_THRESHOLD = 80;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY)
                        && Math.abs(diffX) > SWIPE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                    if (diffX > 0) {
                        previousFilter();
                    } else {
                        nextFilter();
                    }
                    return true;
                }
                return false;
            }
        });

        // Pinch to zoom
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (isVideo || originalBitmap == null) return false;

                float scaleFactor = detector.getScaleFactor();
                float newScale = currentScale * scaleFactor;

                if (newScale < minScale) {
                    scaleFactor = minScale / currentScale;
                    newScale = minScale;
                } else if (newScale > maxScale) {
                    scaleFactor = maxScale / currentScale;
                    newScale = maxScale;
                }

                imageMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                imageView.setImageMatrix(imageMatrix);
                currentScale = newScale;
                return true;
            }
        });

        View target = (overlayContainer != null ? overlayContainer : imageView);
        target.setOnTouchListener((v, event) -> {
            if (!isVideo && scaleGestureDetector != null) {
                scaleGestureDetector.onTouchEvent(event);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        // Let single finger DOWN start gestureDetector (for swipe),
                        // but do not start dragging here. Dragging is for 2+ fingers.
                        lastTouchX = event.getX();
                        lastTouchY = event.getY();
                        isDragging = false;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        // Second finger touches: start potential drag
                        lastTouchX = event.getX();
                        lastTouchY = event.getY();
                        isDragging = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // Only pan when:
                        // - We are zoomed in beyond minScale
                        // - We have 2 or more fingers on screen
                        if (isDragging && event.getPointerCount() >= 2 && currentScale > minScale) {
                            float dx = event.getX() - lastTouchX;
                            float dy = event.getY() - lastTouchY;
                            imageMatrix.postTranslate(dx, dy);
                            imageView.setImageMatrix(imageMatrix);
                            lastTouchX = event.getX();
                            lastTouchY = event.getY();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (event.getPointerCount() <= 2) {
                            isDragging = false;
                        }
                        break;
                }
            }

            // Still allow swipe gestures for filters with a single finger
            if (gestureDetector != null) {
                if(event.getPointerCount()==1) gestureDetector.onTouchEvent(event);
            }
            return true;
        });
    }

    private void showImage() {
        try {
            InputStream is = getContentResolver().openInputStream(mediaUri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();

            // If captured with front camera, mirror horizontally so it matches the preview
            if (isFrontCamera && bitmap != null) {
                Matrix mirrorMatrix = new Matrix();
                mirrorMatrix.preScale(-1f, 1f);
                bitmap = Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.getWidth(),
                        bitmap.getHeight(),
                        mirrorMatrix,
                        true
                );
            }

            originalBitmap = bitmap;

            imageView.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);

            // Initialize matrix so the image fits inside the view (default behaviour)
            imageView.post(() -> {
                if (originalBitmap == null) return;

                int viewWidth = imageView.getWidth();
                int viewHeight = imageView.getHeight();
                int bmWidth = originalBitmap.getWidth();
                int bmHeight = originalBitmap.getHeight();

                if (viewWidth == 0 || viewHeight == 0 || bmWidth == 0 || bmHeight == 0) {
                    imageView.setImageBitmap(originalBitmap);
                    applyCurrentFilter();
                    return;
                }

                imageMatrix.reset();

                float scale = Math.min((float) viewWidth / bmWidth,
                        (float) viewHeight / bmHeight);
                if (scale <= 0f) {
                    scale = 1f;
                }
                minScale = scale;
                currentScale = scale;
                maxScale = scale * 4f;

                float dx = (viewWidth - bmWidth * scale) / 2f;
                float dy = (viewHeight - bmHeight * scale) / 2f;

                imageMatrix.postScale(scale, scale);
                imageMatrix.postTranslate(dx, dy);

                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                imageView.setImageBitmap(originalBitmap);
                imageView.setImageMatrix(imageMatrix);

                applyCurrentFilter();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showVideo() {
        imageView.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoURI(mediaUri);
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            videoView.start();
        });
    }

    private void previousFilter() {
        currentFilterIndex--;
        if (currentFilterIndex < 0) {
            currentFilterIndex = filters.length - 1;
        }
        applyCurrentFilter();
    }

    private void nextFilter() {
        currentFilterIndex++;
        if (currentFilterIndex >= filters.length) {
            currentFilterIndex = 0;
        }
        applyCurrentFilter();
    }

    private void applyCurrentFilter() {
        if (isVideo) {
            Toast.makeText(this, filterNames[currentFilterIndex], Toast.LENGTH_SHORT).show();
            return;
        }

        if (originalBitmap == null) return;

        if (filters != null
                && currentFilterIndex >= 0
                && currentFilterIndex < filters.length
                && filters[currentFilterIndex] != null) {
            imageView.setColorFilter(filters[currentFilterIndex]);
        } else {
            imageView.clearColorFilter();
        }

        // Update bottom strip highlight, if present
        if (filterAdapter != null) {
            filterAdapter.notifyDataSetChanged();
        }

        Toast.makeText(this, filterNames[currentFilterIndex], Toast.LENGTH_SHORT).show();
    }

    private void setUploadingUi(boolean uploading) {
        isUploading = uploading;
        if (btnPost != null) {
            btnPost.setEnabled(!uploading);
            btnPost.setAlpha(uploading ? 0.4f : 1.0f);
        }
        if (progressBar != null) {
            progressBar.setVisibility(uploading ? View.VISIBLE : View.GONE);
        }
    }


    /**
     * Build the final bitmap for upload using current zoom, pan and filter state.
     */
    private Bitmap createUploadBitmap() {
        // If we don't even have a base bitmap or image view, we cannot build a story frame.
        if (originalBitmap == null || imageView == null) {
            return null;
        }

        int viewWidth = imageView.getWidth();
        int viewHeight = imageView.getHeight();

        // Try overlay container dimensions if imageView has not been laid out yet.
        if ((viewWidth <= 0 || viewHeight <= 0) && overlayContainer != null) {
            int overlayW = overlayContainer.getWidth();
            int overlayH = overlayContainer.getHeight();
            if (overlayW > 0 && overlayH > 0) {
                viewWidth = overlayW;
                viewHeight = overlayH;
            }
        }

        // As a last resort, fall back to the original bitmap size.
        // We still go through the composition pipeline so overlays are not lost.
        if (viewWidth <= 0 || viewHeight <= 0) {
            viewWidth = originalBitmap.getWidth();
            viewHeight = originalBitmap.getHeight();
        }

        Bitmap result;
        try {
            result = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return originalBitmap;
        }

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        // Apply current filter to the paint, if any
        if (filters != null
                && filters.length > 0
                && currentFilterIndex >= 0
                && currentFilterIndex < filters.length) {
            ColorMatrixColorFilter cf = filters[currentFilterIndex];
            if (cf != null) {
                paint.setColorFilter(cf);
            }
        }

        // Draw the base image with the current zoom/pan matrix
        canvas.drawBitmap(originalBitmap, imageMatrix, paint);

        // Now draw exactly what the user sees in the overlayContainer on top of the base image,
        // but without re‑drawing the imageView itself or the inline text editor panel.
        if (overlayContainer != null) {
            int saveCount = canvas.save();

            // Temporarily hide the underlying image view so it is not drawn twice.
            int originalVis = imageView.getVisibility();
            imageView.setVisibility(View.INVISIBLE);

            // Hide the inline editor panel if present (tagged as TEXT_EDITOR_PANEL),
            // because the user expects only the final sticker text to be flattened.
            View editorPanel = overlayContainer.findViewWithTag("TEXT_EDITOR_PANEL");
            int editorVis = -1;
            if (editorPanel != null) {
                editorVis = editorPanel.getVisibility();
                editorPanel.setVisibility(View.GONE);
            }

            // Draw the full overlay hierarchy (stickers, text, interactive views, etc.)
            overlayContainer.draw(canvas);

            // Restore visibility state
            if (editorPanel != null && editorVis != -1) {
                editorPanel.setVisibility(editorVis);
            }
            imageView.setVisibility(originalVis);

            canvas.restoreToCount(saveCount);
        }

        return result;
    }



    /**
     * Wrapper for uploads that ensures the image view (and overlay container) are laid out
     * before we build the final bitmap. This avoids cases where createUploadBitmap()
     * falls back to the raw original bitmap and drops text/stickers.
     */
    private void safeUploadWrapper() {
        // For videos we don't need layout information for bitmap composition,
        // so we can directly delegate to the normal flow.
        if (isVideo) {
            uploadStory();
            return;
        }

        if (imageView == null) {
            uploadStory();
            return;
        }

        int w = imageView.getWidth();
        int h = imageView.getHeight();
        if (w <= 0 || h <= 0) {
            // Defer until after layout; this keeps the UX the same but ensures we don't
            // accidentally skip overlays due to a 0x0 view size.
            imageView.post(this::safeUploadWrapper);
            return;
        }

        uploadStory();
    }

    private void uploadStory() {
        if (isUploading) {
            Toast.makeText(this, "Already uploading…", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mediaUri == null) {
            Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // If this is a video, first export with the selected filter using Media3, then upload.
        if (isVideo) {
            setUploadingUi(true);
            isUploading = true;
            Toast.makeText(this, "Applying video filter…", Toast.LENGTH_SHORT).show();

            VideoFilterExporter exporter = new VideoFilterExporter(new VideoFilterExporter.Listener() {
                @Override
                public void onExportCompleted(Uri outputUri) {
                    try {
                        byte[] mediaBytes = readBytesFromUri(outputUri);
                        uploadStoryWithBytes(mediaBytes, true);
                    } catch (Exception e) {
                        setUploadingUi(false);
                        isUploading = false;
                        Log.e(TAG, "Failed to prepare exported video for upload", e);
                        Toast.makeText(StoryEditActivity.this, "Video export failed", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onExportError(Exception exception) {
                    setUploadingUi(false);
                    isUploading = false;
                    Log.e(TAG, "Video export error", exception);
                    Toast.makeText(StoryEditActivity.this, "Video filter failed. Please try again.", Toast.LENGTH_LONG).show();
                }
            });

            exporter.export(mediaUri, currentFilterIndex);
            return;
        }

        // Image flow: build bitmap from current zoom + filter, then upload.
        try {
            if (originalBitmap == null) {
                Toast.makeText(this, "Image not ready", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap uploadBitmap = createUploadBitmap();
            if (uploadBitmap == null) {
                Toast.makeText(this, "Failed to prepare image", Toast.LENGTH_SHORT).show();
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            uploadBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] mediaBytes = baos.toByteArray();

            uploadStoryWithBytes(mediaBytes, false);
        } catch (Exception e) {
            setUploadingUi(false);
            isUploading = false;
            e.printStackTrace();
            Toast.makeText(this, "Upload preparation failed", Toast.LENGTH_LONG).show();
        }
    }


    private void showInteractiveDialog() {

        final CharSequence[] items = {"Ask a Question", "Create Poll", "Remove Interactive"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Story interactive");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0: // Ask a Question
                        mInteractiveDraft = new StoryInteractive();
                        mInteractiveDraft.setType("question");
                        mInteractiveDraft.setPrompt("Ask me anything");
                        Toast.makeText(StoryEditActivity.this, "Question sticker added", Toast.LENGTH_SHORT).show();
                        break;

                    case 1: // Create Poll
                        mInteractiveDraft = new StoryInteractive();
                        mInteractiveDraft.setType("poll");
                        mInteractiveDraft.setQuestion("Your question?");
                        mInteractiveDraft.setOptions(Arrays.asList("Option 1", "Option 2"));
                        Toast.makeText(StoryEditActivity.this, "Poll created", Toast.LENGTH_SHORT).show();
                        break;

                    case 2: // Remove
                        mInteractiveDraft = null;
                        Toast.makeText(StoryEditActivity.this, "Interactive removed", Toast.LENGTH_SHORT).show();
                        break;
                }
                updateInteractivePreview();
            }
        });

        builder.show();
    }


    /**
     * Updates the small centered text preview for the interactive sticker (question/poll).
     * This does not modify the underlying media, only shows what will be attached as metadata.
     */

    private void clearInteractiveSticker() {

        if (overlayContainer != null && interactiveStickerView != null) {
            overlayContainer.removeView(interactiveStickerView);
            interactiveStickerView = null;
        }
    }

    private void makeStickerDraggable(final View view) {

        if (view == null) return;

        view.setOnTouchListener(new View.OnTouchListener() {

            float dX;
            float dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getActionMasked()) {

                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;
                        v.setX(newX);
                        v.setY(newY);
                        return true;
                }

                return false;
            }
        });
    }


    /**
     * Makes a sticker both draggable (one finger) and scalable (pinch with two fingers).
     * This is used for text stickers coming from the IG-style text editor.
     */

    /**
     * Drag + pinch-zoom for stickers/text, with optional drag-to-delete when dragged over deleteBin.
     * This version is very defensive: if deleteBin is null or missing, it simply ignores delete logic.
     */
    private void makeStickerMovableAndScalable(final View view) {

        if (view == null) return;

        view.setOnTouchListener(new View.OnTouchListener() {

            float dX, dY;
            float initialSpacing = 0f;
            float initialScale = 1f;

            float downRawX, downRawY;
            boolean dragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getActionMasked()) {

                    case MotionEvent.ACTION_DOWN:
                        downRawX = event.getRawX();
                        downRawY = event.getRawY();
                        dX = v.getX() - downRawX;
                        dY = v.getY() - downRawY;
                        initialSpacing = 0f;
                        initialScale = (v.getScaleX() == 0f ? 1f : v.getScaleX());
                        dragging = false;

                        // show delete bin if present
                        if (deleteBin != null) {
                            deleteBin.setVisibility(View.VISIBLE);
                            deleteBin.setScaleX(1f);
                            deleteBin.setScaleY(1f);
                        }
                        isOverDeleteArea = false;
                        return true;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (event.getPointerCount() >= 2) {
                            initialSpacing = spacing(event);
                            initialScale = (v.getScaleX() == 0f ? 1f : v.getScaleX());
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() == 1) {
                            float moveRawX = event.getRawX();
                            float moveRawY = event.getRawY();

                            float deltaX = moveRawX - downRawX;
                            float deltaY = moveRawY - downRawY;
                            float touchSlop = 8 * v.getResources().getDisplayMetrics().density;
                            if (!dragging && (Math.abs(deltaX) > touchSlop || Math.abs(deltaY) > touchSlop)) {
                                dragging = true;
                            }

                            if (dragging) {
                                float newX = moveRawX + dX;
                                float newY = moveRawY + dY;
                                v.setX(newX);
                                v.setY(newY);

                                // delete area check (safe: all guarded by null checks)
                                if (deleteBin != null) {
                                    int[] binLoc = new int[2];
                                    deleteBin.getLocationOnScreen(binLoc);
                                    int binX = binLoc[0];
                                    int binY = binLoc[1];
                                    int binW = deleteBin.getWidth();
                                    int binH = deleteBin.getHeight();

                                    int[] viewLoc = new int[2];
                                    v.getLocationOnScreen(viewLoc);
                                    float centerX = viewLoc[0] + v.getWidth() / 2f;
                                    float centerY = viewLoc[1] + v.getHeight() / 2f;

                                    boolean overBin = centerX > binX && centerX < binX + binW
                                            && centerY > binY && centerY < binY + binH;

                                    if (overBin && !isOverDeleteArea) {
                                        // visually indicate delete zone
                                        deleteBin.animate().scaleX(1.2f).scaleY(1.2f).setDuration(120).start();
                                        v.animate().scaleX(initialScale * 0.7f).scaleY(initialScale * 0.7f).setDuration(120).start();
                                        isOverDeleteArea = true;
                                    } else if (!overBin && isOverDeleteArea) {
                                        deleteBin.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                                        v.animate().scaleX(initialScale).scaleY(initialScale).setDuration(120).start();
                                        isOverDeleteArea = false;
                                    }
                                }
                            }
                            return true;

                        } else if (event.getPointerCount() >= 2) {
                            float newSpacing = spacing(event);
                            if (initialSpacing > 0f && newSpacing > 0f) {
                                float factor = newSpacing / initialSpacing;
                                float newScale = initialScale * factor;
                                if (newScale < 0.5f) newScale = 0.5f;
                                if (newScale > 4.0f) newScale = 4.0f;
                                v.setScaleX(newScale);
                                v.setScaleY(newScale);
                            }
                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // hide delete bin if present
                        if (deleteBin != null) {
                            deleteBin.setVisibility(View.GONE);
                            deleteBin.setScaleX(1f);
                            deleteBin.setScaleY(1f);
                        }

                        if (isOverDeleteArea) {
                            // Safest possible delete: just hide the view
                            v.setVisibility(View.GONE);
                        } else {
                            // restore scale
                            v.animate().scaleX(initialScale).scaleY(initialScale).setDuration(120).start();
                        }
                        return true;
                }

                // always consume
                return true;
            }

            private float spacing(MotionEvent event) {
                if (event.getPointerCount() < 2) return 0f;
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return (float) Math.sqrt(x * x + y * y);
            }
        });
    }





    /**
     * Opens a dialog to add a text sticker with selectable font and background color.
     */

    private void showTextEditDialog() {

        TextEditorDialog dialog = new TextEditorDialog();
        dialog.setOnTextEditCompleteListener(new TextEditorDialog.OnTextEditCompleteListener() {
            @Override
            public void onTextEditComplete(TextConfig config) {
                if (config == null) return;
                if (config.text == null) return;
                String text = config.text.trim();
                if (text.length() == 0) return;
                createTextStickerFromConfig(config);
            }
        });
        dialog.show(getSupportFragmentManager(), "TEXT_EDITOR");
    }


    /**
     * Creates a draggable text sticker on top of the story media.
     */
    private void createTextSticker(String text, Typeface typeface, int bgColor, int textColor) {

        if (overlayContainer == null) return;

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(20f);
        tv.setTextColor(textColor);
        tv.setTypeface(typeface);
        int pad = (int) (10 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad, pad, pad);

        if (bgColor == Color.TRANSPARENT) {
            tv.setBackground(null);
        } else {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(bgColor);
            float radius = 16 * getResources().getDisplayMetrics().density;
            bg.setCornerRadius(radius);
            tv.setBackground(bg);
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER;

        overlayContainer.addView(tv, lp);

        // Reuse existing drag logic
        makeStickerDraggable(tv);
    }


    /**
     * Creates a text sticker from a TextConfig returned by the editor dialog.
     * This version wires font, colors, and size, and makes the sticker movable + scalable.
     */
    private void createTextStickerFromConfig(TextConfig config) {

        if (overlayContainer == null || config == null) return;

        TextView tv = new TextView(this);
        tv.setText(config.text);

        // Size: TextView#setTextSize(float) interprets this as "sp"
        if (config.textSizeSp > 0f) {
            tv.setTextSize(config.textSizeSp);
        } else {
            tv.setTextSize(20f);
        }

        // Colors
        int textColor = config.textColor != 0 ? config.textColor : Color.WHITE;
        tv.setTextColor(textColor);

        // Background
        if (config.bgColor == Color.TRANSPARENT) {
            tv.setBackground(null);
        } else {
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(config.bgColor);
            float radius = 16 * getResources().getDisplayMetrics().density;
            bg.setCornerRadius(radius);
            tv.setBackground(bg);
        }

        // Typeface mapping
        Typeface tf = Typeface.SANS_SERIF;
        if ("Serif".equals(config.fontName)) {
            tf = Typeface.SERIF;
        } else if ("Mono".equals(config.fontName)) {
            tf = Typeface.MONOSPACE;
        } else if ("Bold".equals(config.fontName)) {
            tf = Typeface.DEFAULT_BOLD;
        } else if ("Cursive".equals(config.fontName)) {
            tf = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC);
        }
        tv.setTypeface(tf);

        int pad = (int) (10 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad, pad, pad);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER;

        overlayContainer.addView(tv, lp);

        // New behavior: movable + pinch-to-zoom, instead of simple drag only
        makeStickerMovableAndScalable(tv);
    }

    private void ensureQuestionSticker(final String prompt) {

        if (overlayContainer == null) return;

        clearInteractiveSticker();

        // Inflate dedicated editable bubble layout for "Ask me a question"
        final View v = getLayoutInflater().inflate(R.layout.view_askme_question_bubble, overlayContainer, false);

        final EditText editPrompt = v.findViewById(R.id.edit_ask_prompt);

        if (editPrompt != null) {

            String currentPrompt = prompt;

            if (mInteractiveDraft != null && mInteractiveDraft.getPrompt() != null && mInteractiveDraft.getPrompt().trim().length() > 0) {
                currentPrompt = mInteractiveDraft.getPrompt();
            }

            if (currentPrompt == null || currentPrompt.trim().isEmpty()) {
                currentPrompt = "Ask me anything";
            }

            editPrompt.setText(currentPrompt);
            editPrompt.setSelection(editPrompt.getText().length());

            // Keep backing model in sync while user types
            editPrompt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (mInteractiveDraft != null) {
                        mInteractiveDraft.setPrompt(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });

            // Focus + keyboard like Instagram
            editPrompt.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editPrompt, InputMethodManager.SHOW_IMPLICIT);
            }
        }

        interactiveStickerView = v;

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER;

        overlayContainer.addView(v, lp);

        // Reuse generic drag logic so user can move the bubble
        makeStickerDraggable(v);
    }

    private void ensurePollSticker(final String question, final java.util.List<String> options) {

        if (overlayContainer == null) return;

        clearInteractiveSticker();

        // Inflate dedicated editable layout for poll creation
        final View v = getLayoutInflater().inflate(R.layout.view_poll_sticker, overlayContainer, false);
        // Mark this whole layout as an editor-only panel so it is NOT flattened into the final story bitmap.
        // StoryViewerActivity will render the actual interactive poll UI for viewers.
        v.setTag("TEXT_EDITOR_PANEL");

        final EditText editQuestion = v.findViewById(R.id.edit_poll_question);
        final EditText editOption1 = v.findViewById(R.id.edit_poll_option_1);
        final EditText editOption2 = v.findViewById(R.id.edit_poll_option_2);

        // Prepare a mutable options list with at least two entries
        java.util.List<String> draftOptions = (mInteractiveDraft != null ? mInteractiveDraft.getOptions() : null);

        if (draftOptions == null || draftOptions.size() < 2) {

            draftOptions = new java.util.ArrayList<>();

            if (draftOptions.size() == 0) draftOptions.add("Option 1");
            if (draftOptions.size() == 1) draftOptions.add("Option 2");

            if (mInteractiveDraft != null) {
                mInteractiveDraft.setOptions(draftOptions);
            }

        } else {

            // Ensure the list is mutable
            if (!(draftOptions instanceof java.util.ArrayList)) {
                draftOptions = new java.util.ArrayList<>(draftOptions);
                if (mInteractiveDraft != null) {
                    mInteractiveDraft.setOptions(draftOptions);
                }
            }

            // Pad to size 2 if needed
            while (draftOptions.size() < 2) {
                draftOptions.add("Option " + (draftOptions.size() + 1));
            }
        }

        String currentQuestion = question;
        if (mInteractiveDraft != null && mInteractiveDraft.getQuestion() != null && mInteractiveDraft.getQuestion().trim().length() > 0) {
            currentQuestion = mInteractiveDraft.getQuestion();
        }
        if (currentQuestion == null || currentQuestion.trim().isEmpty()) {
            currentQuestion = "Your question?";
        }

        if (editQuestion != null) {
            editQuestion.setText(currentQuestion);
            editQuestion.setSelection(editQuestion.getText().length());

            editQuestion.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (mInteractiveDraft != null) {
                        mInteractiveDraft.setQuestion(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

        if (editOption1 != null) {
            editOption1.setText(draftOptions.get(0));
            editOption1.setSelection(editOption1.getText().length());

            final java.util.List<String> finalDraftOptions = draftOptions;
            editOption1.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    finalDraftOptions.set(0, s.toString());
                    if (mInteractiveDraft != null) {
                        mInteractiveDraft.setOptions(finalDraftOptions);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

        if (editOption2 != null) {
            editOption2.setText(draftOptions.get(1));
            editOption2.setSelection(editOption2.getText().length());

            final java.util.List<String> finalDraftOptions2 = draftOptions;
            editOption2.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    finalDraftOptions2.set(1, s.toString());
                    if (mInteractiveDraft != null) {
                        mInteractiveDraft.setOptions(finalDraftOptions2);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

        interactiveStickerView = v;

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER;

        overlayContainer.addView(v, lp);

        // Allow the whole poll sticker to be dragged around like Instagram
        makeStickerDraggable(v);
    }

    private void updateInteractivePreview() {

        // Hide the plain text preview by default; we rely on the sticker views instead.
        if (interactivePreviewText != null) {
            interactivePreviewText.setVisibility(View.GONE);
        }

        if (mInteractiveDraft == null || mInteractiveDraft.getType() == null) {
            clearInteractiveSticker();
            return;
        }

        String t = mInteractiveDraft.getType();

        if ("question".equals(t)) {

            String prompt = mInteractiveDraft.getPrompt();
            if (prompt == null || prompt.trim().isEmpty()) {
                prompt = "Ask me anything";
            }

            ensureQuestionSticker(prompt);

        } else if ("poll".equals(t)) {

            String q = mInteractiveDraft.getQuestion();
            if (q == null || q.trim().isEmpty()) {
                q = "Poll";
            }

            java.util.List<String> opts = mInteractiveDraft.getOptions();
            ensurePollSticker(q, opts != null ? opts : java.util.Collections.<String>emptyList());

        } else {

            clearInteractiveSticker();
        }
    }

    /**
     * Shared upload logic for both images and videos once we have the final bytes.
     */
    private void uploadStoryWithBytes(final byte[] mediaBytes, final boolean isVideoFinal) {

        final String mimeType = isVideoFinal ? "video/mp4" : "image/jpeg";
        final String fileName = isVideoFinal ? "story_video.mp4" : "story_image.jpg";

        Map<String, String> params = new HashMap<>();
        params.put("accountId", Long.toString(App.getInstance().getId()));
        params.put("accessToken", App.getInstance().getAccessToken());
        params.put("type", isVideoFinal ? "1" : "0"); // 0 = image, 1 = video

        // Attach interactive story element if present (poll/question)
        if (mInteractiveDraft != null && mInteractiveDraft.getType() != null) {

            String t = mInteractiveDraft.getType();

            params.put("interactive_type", t);

            if ("poll".equals(t)) {

                String q = mInteractiveDraft.getQuestion();
                if (q != null && q.trim().length() > 0) {
                    params.put("interactive_question", q.trim());
                }

                java.util.List<String> opts = mInteractiveDraft.getOptions();
                if (opts != null && !opts.isEmpty()) {
                    JSONArray arr = new JSONArray();
                    for (String opt : opts) {
                        if (opt != null) {
                            String val = opt.trim();
                            if (!val.isEmpty()) {
                                arr.put(val);
                            }
                        }
                    }
                    if (arr.length() > 0) {
                        params.put("interactive_options", arr.toString());
                    }
                }

            } else if ("question".equals(t)) {

                String p = mInteractiveDraft.getPrompt();
                if (p != null && p.trim().length() > 0) {
                    params.put("interactive_question", p.trim());
                }
            }
        }

        Map<String, MultipartRequest.DataPart> media = new HashMap<>();
        media.put("file", new MultipartRequest.DataPart(fileName, mediaBytes, mimeType));

        Map<String, String> headers = new HashMap<>();

        setUploadingUi(true);
        isUploading = true;

        MultipartRequest req = new MultipartRequest(
                Constants.METHOD_STORY_UPLOAD,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        setUploadingUi(false);
                        isUploading = false;
                        try {
                            String json = new String(response);
                            Log.d(TAG, "Upload response: " + json);

                            JSONObject obj = new JSONObject(json);

                            boolean isError = false;
                            if (obj.has("error")) {
                                Object errVal = obj.get("error");
                                if (errVal instanceof Boolean) {
                                    isError = (Boolean) errVal;
                                } else if (errVal instanceof Integer) {
                                    isError = ((Integer) errVal) != 0;
                                } else if (errVal instanceof String) {
                                    String s = ((String) errVal).trim().toLowerCase();
                                    isError = s.equals("true") || s.equals("1");
                                }
                            }

                            StringBuilder msgBuilder = new StringBuilder();
                            if (obj.has("error_code")) {
                                msgBuilder.append("code=").append(obj.get("error_code")).append(" ");
                            }
                            if (obj.has("error_description")) {
                                msgBuilder.append(obj.get("error_description"));
                            }

                            if (msgBuilder.length() == 0) {
                                msgBuilder.append(json);
                            }

                            if (!isError) {
                                Toast.makeText(StoryEditActivity.this, "Story posted!", Toast.LENGTH_LONG).show();

                                try {
                                    Intent viewer = new Intent(StoryEditActivity.this, StoryViewerActivity.class);
                                    viewer.putExtra("story_user_id", App.getInstance().getId());
                                    viewer.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(viewer);
                                } catch (Exception e) {
                                    Log.e(TAG, "Failed to open StoryViewerActivity", e);
                                }

                                finish();
                            } else {
                                Toast.makeText(StoryEditActivity.this, "Upload failed: " + msgBuilder.toString(), Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(StoryEditActivity.this, "Upload failed (parse error)", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setUploadingUi(false);
                        isUploading = false;
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null) {
                            String data = new String(networkResponse.data);
                            Log.e(TAG, "Upload error " + networkResponse.statusCode + ": " + data, error);
                            Toast.makeText(StoryEditActivity.this, "Error " + networkResponse.statusCode + ": " + data, Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "Upload error (no networkResponse)", error);
                            Toast.makeText(StoryEditActivity.this, "Upload error", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                headers,
                params,
                media
        );

        App.getInstance().addToRequestQueue(req);
    }





    /**
     * Helper around Media3 Transformer to apply a simple video color filter
     * (based on the current filter index) and export an H.264 MP4 to a temp file.
     */
    @OptIn(markerClass = UnstableApi.class)
    private class VideoFilterExporter {

        interface Listener {
            void onExportCompleted(Uri outputUri);
            void onExportError(Exception exception);
        }

        private final Listener listener;

        VideoFilterExporter(Listener listener) {
            this.listener = listener;
        }

        void export(Uri inputUri, int filterId) {
            try {
                File outFile = File.createTempFile("story_filtered_", ".mp4", getCacheDir());
                String outputPath = outFile.getAbsolutePath();
                Uri outputUri = Uri.fromFile(outFile);

                List<Effect> videoEffects = buildEffectsForFilter(filterId);

                Effects effects = new Effects(
                        Collections.emptyList(), // no audio effects
                        videoEffects
                );

                EditedMediaItem editedMediaItem =
                        new EditedMediaItem.Builder(MediaItem.fromUri(inputUri))
                                .setEffects(effects)
                                .build();

                Transformer transformer =
                        new Transformer.Builder(StoryEditActivity.this)
                                .setVideoMimeType(MimeTypes.VIDEO_H264)
                                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                                .addListener(new Transformer.Listener() {
                                    @Override
                                    public void onCompleted(Composition composition, ExportResult exportResult) {
                                        if (listener != null) {
                                            listener.onExportCompleted(outputUri);
                                        }
                                    }

                                    @Override
                                    public void onError(Composition composition, ExportResult exportResult, ExportException exportException) {
                                        if (listener != null) {
                                            listener.onExportError(exportException);
                                        }
                                    }
                                })
                                .build();

                transformer.start(editedMediaItem, outputPath);

            } catch (Exception e) {
                if (listener != null) {
                    listener.onExportError(e);
                }
            }
        }

        private List<Effect> buildEffectsForFilter(int filterId) {
            List<Effect> effects = new ArrayList<>();

            switch (filterId) {
                case 1:
                    // Mono-style grayscale
                    effects.add(RgbFilter.createGrayscaleFilter());
                    break;
                case 2:
                    // Inverted colors (example/debug)
                    effects.add(RgbFilter.createInvertedFilter());
                    break;
                default:
                    // 0 or unknown: original (no effect)
                    break;
            }

            return effects;
        }
    }


    private byte[] readBytesFromUri(Uri uri) throws Exception {
        InputStream is = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        if (is != null) is.close();
        return buffer.toByteArray();
    }

    @Override
    public void onBackPressed() {
        // If inline text editor is open, treat BACK as confirming the text (Instagram-style)
        if (overlayContainer != null) {
            View panel = overlayContainer.findViewWithTag("TEXT_EDITOR_PANEL");
            if (panel != null) {
                TextView btnDone = panel.findViewById(R.id.text_editor_done);
                if (btnDone != null) {
                    btnDone.performClick(); // reuse existing logic to create the text sticker
                    return;
                }
            }
        }

        if (isUploading) {
            Toast.makeText(this, "Uploading in progress, please wait…", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Discard story?")
                .setMessage("If you go back now, your edited story will be lost.")
                .setPositiveButton("Discard", (dialog, which) -> {
                    dialog.dismiss();
                    StoryEditActivity.super.onBackPressed();
                })
                .setNegativeButton("Keep editing", (dialog, which) -> dialog.dismiss())
                .show();
    }
}