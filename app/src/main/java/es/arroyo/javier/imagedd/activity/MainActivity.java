package es.arroyo.javier.imagedd.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import es.arroyo.javier.imagedd.R;
import es.arroyo.javier.imagedd.utils.BlurBuilder;
import es.arroyo.javier.imagedd.utils.Utils;


public class MainActivity extends ActionBarActivity {


    //Constants
    //---------
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int MAX_ROTATION = 20;

    //View
    //----
    private ImageView imageViewBackground;
    private ImageView imageViewGallery;
    private ImageView imageViewCamera;
    private ImageView imageViewDelete;
    private ImageView imageViewPhoto;
    private ImageView imageViewExpanded;
    private ImageView imageViewShare;
    private ImageView imageViewCircleGallery;
    private ImageView imageViewCircleCamera;
    private ImageView imageViewCircleDelete;
    private ImageView imageViewCircleShare;
    private ImageView imageViewTakePhoto;
    private View layoutGallery;
    private View layoutCamera;
    private View layoutDelete;
    private View layoutShare;
    private View container;

    //Data
    //----
    private Uri fileUri;
    private Uri selectedImageUri;
    private int mActivePointerId = 0;
    private Bitmap bitmap;
    private RelativeLayout.LayoutParams layoutParamsImage;

    //Animation
    //---------
    private Animation animScale;
    private Animation animRotateCenterToRight;
    private Animation animRotateRightToCenter;
    private Animation animRotateLeftToCenter;
    private Animation animRotateCenterToLeft;
    private Animation animResizeUpCircle;
    private Animation animResizeDownCircle;
    private Animation animReduceToZero;
    private Animation animSizeToOne;
    private Animation animReduceToZeroUp;
    private Animation animSizeToOneUp;
    private Animation animAlphaZeroToOne;
    private Animation animAlphaOneToZero;
    private Animation animMoveViewToTop;
    private Animation animMoveViewToBottom;
    private Animator mCurrentAnimator;

    private int mShortAnimationDuration = 500;
    private int durationZoom = 500;

    //Drag and Drop
    //-------------
    private MyDragShadowBuilder shadowBuilder;
    private boolean isDragOnRightUp = false;
    private boolean isDragOnLefttUp = false;
    private boolean isDragOnCenterUp = false;
    private boolean isDragOnCenterDown = false;

    //ZOOM
    final Rect startBounds = new Rect();
    final Rect finalBounds = new Rect();
    final Point globalOffset = new Point();
    private float startScale;
    //private final float startScaleFinal;




    //-------------------------------------------------------------------------------------
    // ON CREATE
    //-------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //View
        //----
        imageViewBackground = (ImageView) findViewById(R.id.imageViewBackground);
        imageViewGallery = (ImageView) findViewById(R.id.imageViewGallery);
        imageViewCamera = (ImageView) findViewById(R.id.imageViewCamera);
        imageViewDelete = (ImageView) findViewById(R.id.imageViewDelete);
        imageViewPhoto = (ImageView) findViewById(R.id.imageViewPhoto);
        imageViewExpanded = (ImageView) findViewById(R.id.expanded_image);
        imageViewShare = (ImageView) findViewById(R.id.imageViewShare);
        imageViewCircleGallery = (ImageView) findViewById(R.id.imageViewCircleGallery);
        imageViewCircleCamera = (ImageView) findViewById(R.id.imageViewCircleCamera);
        imageViewCircleDelete = (ImageView) findViewById(R.id.imageViewCircleDelete);
        imageViewCircleShare = (ImageView) findViewById(R.id.imageViewCircleShare);
        imageViewTakePhoto = (ImageView) findViewById(R.id.imageViewTakePhoto);
        layoutGallery = findViewById(R.id.layoutGallery);
        layoutCamera = findViewById(R.id.layoutCamera);
        layoutDelete = findViewById(R.id.layoutDelete);
        layoutShare = findViewById(R.id.layoutShare);
        container = findViewById(R.id.container);

        //ANIMATIONS
        //----------
        configAnimationReduceImage();
        loadAnimations();

        //Config Drag and Drop
        //-------------------
        configDragAndDrop();

        //CLICK LISTENER
        //--------------
        imageViewPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //imageViewPhoto.startAnimation(animScale);
                //startDragAndDrop();
                zoomImageFromThumb(imageViewPhoto, selectedImageUri);
                return true;
            }
        });
        configViewBounds(imageViewPhoto);
        imageViewExpanded.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick (View view) {
                zoomOut(imageViewPhoto);
                return true;
            }
        });

        //Params imageVIew
        layoutParamsImage = (RelativeLayout.LayoutParams)imageViewPhoto.getLayoutParams();

    }
    //------------------------------------------------------------------------------------
    // CONFIG ANIMATION REDUCE IMAGE
    //------------------------------------------------------------------------------------
    private void configAnimationReduceImage() {
        animScale = AnimationUtils.loadAnimation(this,
                R.anim.scale_image);
        animScale.setFillAfter(true);
        animScale.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startDragAndDrop();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //------------------------------------------------------------------------------------
    // CONFIG ANIMATION REDUCE IMAGE
    //------------------------------------------------------------------------------------
    private void loadAnimations() {
        animRotateCenterToRight = AnimationUtils.loadAnimation(this,
                R.anim.rotate_center_to_right);
        animRotateCenterToRight.setFillAfter(true);

        animRotateRightToCenter= AnimationUtils.loadAnimation(this,
                R.anim.rotate_right_to_center);
        animRotateRightToCenter.setFillAfter(true);

        animRotateCenterToLeft = AnimationUtils.loadAnimation(this,
                R.anim.rotate_center_to_left);
        animRotateCenterToLeft.setFillAfter(true);

        animRotateLeftToCenter= AnimationUtils.loadAnimation(this,
                R.anim.rotate_left_to_center);
        animRotateCenterToLeft.setFillAfter(true);

        animResizeUpCircle= AnimationUtils.loadAnimation(this,
                R.anim.resize_up_circle);
        animResizeUpCircle.setFillAfter(true);

        animResizeDownCircle= AnimationUtils.loadAnimation(this,
                R.anim.resize_down_circle);
        animResizeDownCircle.setFillAfter(true);

        animReduceToZero= AnimationUtils.loadAnimation(this,
                R.anim.reduce_to_zero_bottom);
        animReduceToZero.setFillAfter(true);

        animSizeToOne= AnimationUtils.loadAnimation(this,
                R.anim.size_to_one_bottom);
        animSizeToOne.setFillAfter(true);

        animReduceToZeroUp= AnimationUtils.loadAnimation(this,
                R.anim.reduce_to_zero_up);
        animReduceToZeroUp.setFillAfter(true);

        animSizeToOneUp= AnimationUtils.loadAnimation(this,
                R.anim.size_to_one_up);
        animSizeToOneUp.setFillAfter(true);

        animAlphaZeroToOne= AnimationUtils.loadAnimation(this,
                R.anim.alpha_zero_to_one);
        animAlphaZeroToOne.setFillAfter(true);

        animAlphaOneToZero= AnimationUtils.loadAnimation(this,
                R.anim.alpha_one_to_zero);
        animAlphaOneToZero.setFillAfter(true);

        animMoveViewToTop= AnimationUtils.loadAnimation(this,
                R.anim.move_view_to_top);
        animMoveViewToTop.setFillAfter(true);

        animMoveViewToBottom= AnimationUtils.loadAnimation(this,
                R.anim.move_view_to_bottom);
        animMoveViewToBottom.setFillAfter(true);
    }

    //------------------------------------------------------------------------------------
    // CONFIG DRAG AND DROP
    //------------------------------------------------------------------------------------
    private void configDragAndDrop(){
        DragAndDropListener dragAndDropListener = new DragAndDropListener();
        //imageViewCamera.setOnDragListener(dragAndDropListener);
        //imageViewDelete.setOnDragListener(dragAndDropListener);
        //imageViewGallery.setOnDragListener(dragAndDropListener);
        //imageViewShare.setOnDragListener(dragAndDropListener);
        imageViewBackground.setOnDragListener(dragAndDropListener);
        layoutCamera.setOnDragListener(dragAndDropListener);
        layoutGallery.setOnDragListener(dragAndDropListener);
        layoutDelete.setOnDragListener(dragAndDropListener);
        layoutShare.setOnDragListener(dragAndDropListener);

    }
    //------------------------------------------------------------------------------------
    // OPEN CAMERA
    //------------------------------------------------------------------------------------
    private void openCamera() {

        fileUri = Utils.getOutputMediaFileUri(DragAndDropApplication.MEDIA_TYPE_IMAGE);
        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            cameraIntents.add(intent);
        }

        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    //------------------------------------------------------------------------------------
    // ON CLICK MAIN ACTIVITY
    //------------------------------------------------------------------------------------
    public void onClickMainActivity(View view) {
        switch (view.getId()) {
            case R.id.imageViewCamera:
            case R.id.imageViewGallery:
            case R.id.imageViewTakePhoto:
                openCamera();
                break;
        }
    }


    //---------------------------------------------------------------------
    // ON ACTIVITY RESULT
    //---------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }


                String path = "";
                if (isCamera) {
                    selectedImageUri = fileUri;
                    path = "file//" + fileUri.getPath();
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                    path = "file://" + Utils.getRealPathFromURI(MainActivity.this, selectedImageUri);
                    selectedImageUri = Uri.parse(path);
                }
                displayBlurImageInBackground(selectedImageUri);
                displayImagePhoto(selectedImageUri);


            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }
    }

    //---------------------------------------------------------------------
    // DISPLAY BUR IMAGE IN BACKGROUND
    //---------------------------------------------------------------------
    private void displayBlurImageInBackground(Uri imageUri) {
        //Utils.displayImageLoading(path, imageViewBackground, null);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            bitmap = Utils.getResizedBitmap(bitmap, 0.5f);
            imageViewBackground.setImageBitmap(BlurBuilder.blur(MainActivity.this, bitmap));

            Utils.displayImageLoading(imageUri.getPath(),imageViewBackground, null);
        } catch (Exception e) {
            //TODO mostrar error
            //Error al obtener bitmap
            e.printStackTrace();
        }
    }


    //---------------------------------------------------------------------
    // DISPLAY BUR IMAGE IN BACKGROUND
    //---------------------------------------------------------------------
    private void displayImagePhoto(Uri imageUri) {
        //Utils.displayImageLoading(path, imageViewBackground, null);
        try {
            mShortAnimationDuration = 0;
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            bitmap = Utils.getResizedBitmap(bitmap, 0.5f);
            imageViewPhoto.setVisibility(View.VISIBLE);
            imageViewPhoto.setImageBitmap(bitmap);
            zoomImageFromThumb(imageViewPhoto, imageUri);
            imageViewExpanded.setImageBitmap(bitmap);
            mShortAnimationDuration = 500;

            //Utils.displayImageLoading(imageUri.getPath(),imageViewPhoto, null);
            imageViewTakePhoto.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, getString(R.string.error_getting_photo), Toast.LENGTH_SHORT).show();
            //TODO mostrar error
            //Error al obtener bitmap
            e.printStackTrace();
        }
    }

    //---------------------------------------------------------------------
    // START DRAG AND DROP
    //---------------------------------------------------------------------
    private void startDragAndDrop() {

        ClipData data = ClipData.newPlainText("", "");
        shadowBuilder = new MyDragShadowBuilder(imageViewPhoto);
        imageViewPhoto.startDrag(data, shadowBuilder, imageViewPhoto, 0);
        //imageViewPhoto.setVisibility(View.GONE);
    }


    //---------------------------------------------------------------------
    // DRAG AND DROP LISTENER
    //---------------------------------------------------------------------
    private class DragAndDropListener implements View.OnDragListener {
        private float currentRotation;
        private float currentScale = 1;
        private float lastPosX, lastPosY;
        private float currentScaleCircleRightUp = 0;
        private float currentScaleCircleLeftUp = 0;
        private float currentScaleCircleCenterUp = 0;
        private float currentScaleCircleCenterDown = 0;


        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    onDragEntered(v);

                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    onDragExited(v);
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    float posXCurrentDrag = 0;
                    float posYCurrentDrag = 0;
                    posXCurrentDrag = moveImageViewX(v, event, posXCurrentDrag);
                    posYCurrentDrag = moveImageViewY(v, event, posYCurrentDrag);

                    //ROTATION
                    //--------
                    rotateOnRightUp(posXCurrentDrag);
                    rotateOnLeftUp(posXCurrentDrag);
                    rotateOnRightDown(v, posXCurrentDrag);
                    rotateOnLeftDown(v, posXCurrentDrag);

                    //SIZE CIRCLES
                    //------------
                    changeSizeCircles(event,posXCurrentDrag, posYCurrentDrag);

                    lastPosX = posXCurrentDrag;
                    lastPosY = posYCurrentDrag;
                    break;
                case DragEvent.ACTION_DROP:
                    onDropView(v);
                    break;

                case DragEvent.ACTION_DRAG_ENDED:

                    break;
                default:
                    break;
            }
            return true;
        }
        //---------------
        //MOVE IMAGE VIEW X
        //---------------
        private float moveImageViewX(View viewOnDrag, DragEvent event, float posXCurrentDrag){

            if(viewOnDrag.getId() == R.id.imageViewBackground) {
                posXCurrentDrag = event.getX() - imageViewPhoto.getWidth()/2;
            }
            else if(isDragOnRightUp){
                posXCurrentDrag = posXCurrentDrag+layoutCamera.getX() + event.getX() -imageViewPhoto.getWidth()/2;

            }
            else if(isDragOnLefttUp){
                posXCurrentDrag = posXCurrentDrag+layoutGallery.getX() + event.getX() -imageViewPhoto.getWidth()/2;
            }

            else if(isDragOnCenterUp){
                posXCurrentDrag = posXCurrentDrag+layoutShare.getX() + event.getX() -imageViewPhoto.getWidth()/2;
            }

            else if(isDragOnCenterDown){
                posXCurrentDrag = posXCurrentDrag+layoutDelete.getX() + event.getX() -imageViewPhoto.getWidth()/2;
            }
            imageViewPhoto.setX(posXCurrentDrag);
            return posXCurrentDrag;
        }

        //---------------
        //MOVE IMAGE VIEW Y
        //---------------
        private float moveImageViewY(View viewOnDrag, DragEvent event, float posYCurrentDrag){

            if(viewOnDrag.getId() == R.id.imageViewBackground) {
                posYCurrentDrag = event.getY() - imageViewPhoto.getHeight()/2;
            }
            else if(isDragOnRightUp){
                posYCurrentDrag = posYCurrentDrag+layoutCamera.getY() + event.getY() -imageViewPhoto.getHeight()/2;

            }

            else if(isDragOnLefttUp){
                posYCurrentDrag = posYCurrentDrag+layoutGallery.getY() + event.getY() - imageViewPhoto.getHeight()/2;
            }

            else if(isDragOnCenterUp){
                posYCurrentDrag = posYCurrentDrag+layoutShare.getY() + event.getY() - imageViewPhoto.getHeight()/2;
            }

            else if(isDragOnCenterDown){
                posYCurrentDrag = posYCurrentDrag+layoutDelete.getY() + event.getY() - imageViewPhoto.getHeight()/2;
            }
            imageViewPhoto.setY(posYCurrentDrag);
            return posYCurrentDrag;
        }


        //------------------
        // ROTATE ON RIGHT UP
        //------------------
        private void rotateOnRightUp(float posXCurrentDrag) {
            Log.d("Rotation RIGHT", currentRotation + "");
            if(isDragOnRightUp && posXCurrentDrag > lastPosX && currentRotation < MAX_ROTATION){
                //currentRotation = currentRotation + posXCurrentDrag * 0.002f;
                currentRotation = currentRotation + Math.abs(posXCurrentDrag-lastPosX) * 0.1f;
                imageViewPhoto.setRotation(currentRotation);
            }else{
                if(isDragOnRightUp &&currentRotation > 0 && posXCurrentDrag < lastPosX){
                    Log.d("Rotation - RIGHT", currentRotation + "");
                    //currentRotation = currentRotation - posXCurrentDrag * 0.002f;
                    currentRotation = currentRotation - Math.abs(posXCurrentDrag-lastPosX) * 0.1f;
                    if(currentRotation < 0){
                        currentRotation = 0;
                    }
                    imageViewPhoto.setRotation(currentRotation);
                }
            }

            //Log.d("Rotation", currentRotation+"");
            //Log.d("POS X CAMERA", layoutCamera.getX()+"");
            if(isDragOnRightUp && posXCurrentDrag > lastPosX && currentScale <= 1 && currentScale > 0.8){
                currentScale = currentScale - 0.005f;
                imageViewPhoto.setScaleX(currentScale);
                imageViewPhoto.setScaleY(currentScale);
            }else{
                if(isDragOnRightUp && currentScale < 1 && posXCurrentDrag < lastPosX){
                    currentScale = currentScale + 0.005f;
                    imageViewPhoto.setScaleX(currentScale);
                    imageViewPhoto.setScaleY(currentScale);
                }
            }
        }

        //-------------------
        //ROTATE ON LEFT UP
        //-------------------
        private void rotateOnLeftUp(float posXCurrentDrag) {
            Log.d("Rotation LEFT", currentRotation + "");
            if( isDragOnLefttUp && posXCurrentDrag > 0 && posXCurrentDrag < lastPosX && currentRotation > - MAX_ROTATION){
                //currentRotation = currentRotation - posXCurrentDrag * 0.02f;
                currentRotation = currentRotation - Math.abs(posXCurrentDrag-lastPosX) * 0.2f;
                imageViewPhoto.setRotation(currentRotation);
            }else{
                if(isDragOnLefttUp && currentRotation < 0&& posXCurrentDrag > 0 && posXCurrentDrag > lastPosX){
                    Log.d("Rotation - LEFT", currentRotation + "");
                    //currentRotation = currentRotation + posXCurrentDrag * 0.02f;
                    currentRotation = currentRotation + Math.abs(posXCurrentDrag-lastPosX) * 0.2f;
                    if(currentRotation > 0){
                        currentRotation = 0;
                    }
                    imageViewPhoto.setRotation(currentRotation);
                }
            }

            //Log.d("Rotation", currentRotation+"");
            //Log.d("POS X CAMERA", layoutCamera.getX()+"");
            if(isDragOnLefttUp && posXCurrentDrag < lastPosX && currentScale <= 1 && currentScale > 0.8){
                currentScale = currentScale - 0.005f;
                imageViewPhoto.setScaleX(currentScale);
                imageViewPhoto.setScaleY(currentScale);
            }else{
                if(isDragOnLefttUp && currentScale < 1 && posXCurrentDrag > lastPosX){
                    currentScale = currentScale + 0.005f;
                    imageViewPhoto.setScaleX(currentScale);
                    imageViewPhoto.setScaleY(currentScale);
                }
            }
        }

        //------------------
        // ROTATE ON RIGHT DOWN
        //------------------
        private void rotateOnRightDown(View view, float posXCurrentDrag) {
            //Log.d("POSX RIGHT DOWN", posXCurrentDrag + "");
            //Log.d(" - LAST X DOWN", lastPosX + "");
            //Log.d(" - Rotation RIGHT DOWN", currentRotation + "");
            //Log.d(" - Width/2", container.getWidth()/2 + "");

            int center = container.getWidth()/2 - imageViewPhoto.getWidth()/2;

            if(isDragOnCenterDown&& posXCurrentDrag > 0 && posXCurrentDrag > lastPosX && currentRotation < MAX_ROTATION && posXCurrentDrag > center){
                //currentRotation = currentRotation + posXCurrentDrag * 0.002f;
                currentRotation = currentRotation + Math.abs(posXCurrentDrag-lastPosX) * 0.1f;
                imageViewPhoto.setRotation(currentRotation);
            }else{
                if(isDragOnCenterDown && currentRotation > 0&& posXCurrentDrag > 0 && posXCurrentDrag < lastPosX){

                    //currentRotation = currentRotation - posXCurrentDrag * 0.002f;
                    currentRotation = currentRotation - Math.abs(posXCurrentDrag-lastPosX) * 0.1f;
                    if(currentRotation < 0){
                        currentRotation = 0;
                    }
                    imageViewPhoto.setRotation(currentRotation);
                }
            }
        }

        //------------------
        // ROTATE ON RIGHT LEFT
        //------------------
        private void rotateOnLeftDown(View view, float posXCurrentDrag) {
            //Log.d("POSX RIGHT DOWN", posXCurrentDrag + "");
            //Log.d(" - LAST X DOWN", lastPosX + "");
            //Log.d(" - Rotation RIGHT DOWN", currentRotation + "");
            //Log.d(" - Width/2", container.getWidth()/2 + "");

            int center = container.getWidth()/2 - imageViewPhoto.getWidth()/2;
            if(isDragOnCenterDown && posXCurrentDrag < lastPosX && currentRotation > - MAX_ROTATION && posXCurrentDrag < center){
                //currentRotation = currentRotation - posXCurrentDrag * 0.004f;
                currentRotation = currentRotation - Math.abs(posXCurrentDrag-lastPosX) * 0.1f;
                imageViewPhoto.setRotation(currentRotation);
            }else{
                if(isDragOnCenterDown && currentRotation < 0 && posXCurrentDrag > lastPosX){

                    //currentRotation = currentRotation + posXCurrentDrag * 0.004f;
                    currentRotation = currentRotation +Math.abs(posXCurrentDrag-lastPosX) * 0.1f;
                    if(currentRotation > 0){
                        currentRotation = 0;
                    }
                    imageViewPhoto.setRotation(currentRotation);
                }
            }
        }

        //------------------
        // Change SIZE CIRCLES
        //------------------
        private void changeSizeCircles(DragEvent event,float posXCurrentDrag, float posYCurrentDrag){

            float difX =  posXCurrentDrag - lastPosX;
            float difY = posYCurrentDrag - lastPosY;

            if(isDragOnRightUp && lastPosY > posYCurrentDrag  && currentScaleCircleRightUp < 1){
                currentScaleCircleRightUp = currentScaleCircleRightUp + (event.getY()+event.getX()) * 0.0001f;
                imageViewCircleCamera.setVisibility(View.VISIBLE);
                imageViewCircleCamera.setScaleX(currentScaleCircleRightUp);
                imageViewCircleCamera.setScaleY(currentScaleCircleRightUp);
            }else if(isDragOnRightUp && currentScaleCircleRightUp > 0 && posYCurrentDrag > lastPosY){
                currentScaleCircleRightUp = currentScaleCircleRightUp - (event.getY()+event.getX()) * 0.0001f;
                imageViewCircleCamera.setVisibility(View.VISIBLE);
                imageViewCircleCamera.setScaleX(currentScaleCircleRightUp);
                imageViewCircleCamera.setScaleY(currentScaleCircleRightUp);
            }
            else{
                if(!isDragOnRightUp) {
                    currentScaleCircleRightUp = 0;
                    imageViewCircleCamera.setVisibility(View.VISIBLE);
                    imageViewCircleCamera.setScaleX(currentScaleCircleRightUp);
                    imageViewCircleCamera.setScaleY(currentScaleCircleRightUp);
                }
            }

            if(isDragOnLefttUp && lastPosY > posYCurrentDrag && currentScaleCircleLeftUp < 1){
                currentScaleCircleLeftUp = currentScaleCircleLeftUp + event.getY() * 0.0001f;
                imageViewCircleGallery.setVisibility(View.VISIBLE);
                imageViewCircleGallery.setScaleX(currentScaleCircleLeftUp);
                imageViewCircleGallery.setScaleY(currentScaleCircleLeftUp);
            }else if(isDragOnLefttUp && currentScaleCircleLeftUp > 0 && posYCurrentDrag > lastPosY){
                currentScaleCircleLeftUp = currentScaleCircleLeftUp - event.getY() * 0.0001f;
                imageViewCircleGallery.setVisibility(View.VISIBLE);
                imageViewCircleGallery.setScaleX(currentScaleCircleLeftUp);
                imageViewCircleGallery.setScaleY(currentScaleCircleLeftUp);
            }else{
                if(!isDragOnLefttUp) {
                    currentScaleCircleLeftUp = 0;
                    imageViewCircleGallery.setVisibility(View.VISIBLE);
                    imageViewCircleGallery.setScaleX(currentScaleCircleLeftUp);
                    imageViewCircleGallery.setScaleY(currentScaleCircleLeftUp);
                }
            }

            if(isDragOnCenterUp && lastPosY > posYCurrentDrag && currentScaleCircleCenterUp < 1){
                currentScaleCircleCenterUp = currentScaleCircleCenterUp + event.getY() * 0.0001f;
                imageViewCircleShare.setVisibility(View.VISIBLE);
                imageViewCircleShare.setScaleX(currentScaleCircleCenterUp);
                imageViewCircleShare.setScaleY(currentScaleCircleCenterUp);
            }else if(isDragOnCenterUp && currentScaleCircleCenterUp > 0 && posYCurrentDrag > lastPosY){
                currentScaleCircleCenterUp = currentScaleCircleCenterUp - event.getY() * 0.0001f;
                imageViewCircleShare.setVisibility(View.VISIBLE);
                imageViewCircleShare.setScaleX(currentScaleCircleCenterUp);
                imageViewCircleShare.setScaleY(currentScaleCircleCenterUp);
            }else{
                if(!isDragOnCenterUp) {
                    currentScaleCircleCenterUp = 0;
                    imageViewCircleShare.setVisibility(View.VISIBLE);
                    imageViewCircleShare.setScaleX(currentScaleCircleCenterUp);
                    imageViewCircleShare.setScaleY(currentScaleCircleCenterUp);
                }
            }

            if(isDragOnCenterDown && lastPosY < posYCurrentDrag && currentScaleCircleCenterDown < 1){
                currentScaleCircleCenterDown = currentScaleCircleCenterDown + event.getY() * 0.0002f;
                imageViewCircleDelete.setVisibility(View.VISIBLE);
                imageViewCircleDelete.setScaleX(currentScaleCircleCenterDown);
                imageViewCircleDelete.setScaleY(currentScaleCircleCenterDown);
            }else if(isDragOnCenterDown && currentScaleCircleCenterDown > 0 && posYCurrentDrag < lastPosY){
                currentScaleCircleCenterDown = currentScaleCircleCenterDown - event.getY() * 0.0002f;
                imageViewCircleDelete.setVisibility(View.VISIBLE);
                imageViewCircleDelete.setScaleX(currentScaleCircleCenterDown);
                imageViewCircleDelete.setScaleY(currentScaleCircleCenterDown);
            }else{
                if(!isDragOnCenterDown) {
                    currentScaleCircleCenterDown = 0;
                    imageViewCircleDelete.setVisibility(View.VISIBLE);
                    imageViewCircleDelete.setScaleX(currentScaleCircleCenterDown);
                    imageViewCircleDelete.setScaleY(currentScaleCircleCenterDown);
                }
            }
        }


        //ON DROP VIEW
        //-----------
        private void onDropView(View v){
            switch (v.getId()){
                case R.id.imageViewCamera:
                    showInfoDrop("Camera");
                    //imageViewPhoto.setLayoutParams(layoutParamsImage);


                    break;
                case R.id.imageViewGallery:
                    showInfoDrop("Gallery");
                    //imageViewPhoto.setLayoutParams(layoutParamsImage);
                    break;
                case R.id.imageViewDelete:
                    showInfoDrop("Delete");
                    //imageViewPhoto.setLayoutParams(layoutParamsImage);
                    break;

                case R.id.imageViewShare:
                    showInfoDrop("Share");
                    //imageViewPhoto.setLayoutParams(layoutParamsImage);
                    break;

                default:

            }

            //Jarroyo22/05
            isDragOnRightUp = false;
            isDragOnCenterUp = false;
            isDragOnLefttUp = false;
            isDragOnCenterDown = false;
            currentRotation = 0;
            hideCircles();

            configViewBounds(imageViewPhoto);
            zoomImageFromThumb(imageViewPhoto, selectedImageUri);
            //JARROYO HOY
            imageViewPhoto.setLayoutParams(layoutParamsImage);
            imageViewPhoto.setRotation(0);
            imageViewPhoto.setX(container.getWidth()/2 - imageViewPhoto.getWidth()/2);
            imageViewPhoto.setY(container.getHeight()/2 - imageViewPhoto.getHeight()/2);
            configViewBounds(imageViewPhoto);
            currentScale = 1;
            imageViewPhoto.setScaleX(1);
            imageViewPhoto.setScaleY(1);
        }

        //ON DRAG ENTERED
        //---------------
        private void onDragEntered(View v){
            switch (v.getId()){
                case R.id.layoutCamera:
                    //startAnimation(imageViewPhoto, animRotateCenterToRight);
                    //startAnimation(imageViewCircleCamera, animResizeUpCircle);
                    isDragOnRightUp = true;
                    isDragOnCenterUp = false;
                    isDragOnLefttUp = false;
                    isDragOnCenterDown = false;
                    break;
                case R.id.layoutGallery:
                    //startAnimation(imageViewPhoto, animRotateCenterToLeft);
                    //startAnimation(imageViewCircleGallery, animResizeUpCircle);
                    isDragOnRightUp = false;
                    isDragOnCenterUp = false;
                    isDragOnLefttUp = true;
                    isDragOnCenterDown = false;
                    break;
                case R.id.layoutDelete:
                    //startAnimation(imageViewPhoto, animReduceToZero);
                    //startAnimation(imageViewCircleDelete, animResizeUpCircle);
                    isDragOnRightUp = false;
                    isDragOnCenterUp = false;
                    isDragOnLefttUp = false;
                    isDragOnCenterDown = true;
                    break;

                case R.id.layoutShare:
                    //startAnimation(imageViewPhoto, animReduceToZeroUp);
                    //startAnimation(imageViewCircleShare, animResizeUpCircle);
                    isDragOnRightUp = false;
                    isDragOnCenterUp = true;
                    isDragOnLefttUp = false;
                    isDragOnCenterDown = false;
                    break;
            }
        }

        //ON DRAG EXITED
        //---------------
        private void onDragExited(View v){
            switch (v.getId()){
                case R.id.layoutCamera:
                    //startAnimation(imageViewPhoto, animRotateRightToCenter);
                    //startAnimation(imageViewCircleCamera, animResizeDownCircle);
                    isDragOnRightUp = false;
                    isDragOnCenterUp = false;
                    isDragOnLefttUp = false;
                    isDragOnCenterDown = false;
                    imageViewCircleCamera.setScaleX(0);
                    imageViewCircleCamera.setScaleY(0);
                    imageViewCircleCamera.setVisibility(View.INVISIBLE);
                    //rotateToZero(currentRotation);
                    imageViewPhoto.setRotation(0);

                    break;
                case R.id.layoutGallery:
                    //startAnimation(imageViewPhoto, animRotateLeftToCenter);
                    //startAnimation(imageViewCircleGallery, animResizeDownCircle);
                    isDragOnRightUp = false;
                    isDragOnCenterUp = false;
                    isDragOnLefttUp = false;
                    isDragOnCenterDown = false;
                    imageViewCircleGallery.setScaleX(0);
                    imageViewCircleGallery.setScaleY(0);
                    imageViewCircleGallery.setVisibility(View.INVISIBLE);
                    //rotateToZero(currentRotation);
                    imageViewPhoto.setRotation(0);
                    break;
                case R.id.layoutDelete:
                    //startAnimation(imageViewPhoto, animSizeToOne);
                    //startAnimation(imageViewCircleDelete, animResizeDownCircle);
                    isDragOnRightUp = false;
                    isDragOnCenterUp = false;
                    isDragOnLefttUp = false;
                    isDragOnCenterDown = false;
                    imageViewCircleDelete.setScaleX(0);
                    imageViewCircleDelete.setScaleY(0);
                    imageViewCircleDelete.setVisibility(View.INVISIBLE);
                    imageViewPhoto.setRotation(0);
                    break;
                case R.id.layoutShare:
                    //startAnimation(imageViewPhoto, animSizeToOneUp);
                    //startAnimation(imageViewCircleShare, animResizeDownCircle);
                    isDragOnRightUp = false;
                    isDragOnCenterUp = false;
                    isDragOnLefttUp = false;
                    isDragOnCenterDown = false;
                    imageViewCircleShare.setScaleX(0);
                    imageViewCircleShare.setScaleY(0);
                    imageViewCircleShare.setVisibility(View.INVISIBLE);
                    break;
            }

            currentRotation = 0;
        }

        private void showInfoDrop(String text){
            Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
        }

    }

    //-----------------------------------------------------------------------------------------
    // START ANIMATION
    //-----------------------------------------------------------------------------------------
    private void startAnimation(View view, Animation animation){
        view.startAnimation(animation);

    }
    //-----------------------------------------------------------------------------------------
    // MY DRAG SHADOW BUILDER
    //-----------------------------------------------------------------------------------------
    private static class MyDragShadowBuilder extends View.DragShadowBuilder {

        private Point mScaleFactor;
        public MyDragShadowBuilder(View v) {
            super(v);
        }

        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            int width;
            int height;

            width = getView().getWidth() / 2;
            height = getView().getHeight() / 2;
            size.set(0, 0);
            mScaleFactor = size;
            touch.set(width / 2, height / 2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            canvas.scale(mScaleFactor.x/(float)getView().getWidth(), mScaleFactor.y/(float)getView().getHeight());
            getView().draw(canvas);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bitmap != null){
            bitmap.recycle();
        }
        animRotateRightToCenter.cancel();
    }

    //---------------------------------------------------------------------
    // ZOOM IMAGE FROM THUMB
    //---------------------------------------------------------------------
    private void zoomImageFromThumb(final View thumbView, Uri uri) {

        hideActions();

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView imageViewExpanded = (ImageView) findViewById(
                R.id.expanded_image);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            bitmap = Utils.getResizedBitmap(bitmap, 0.5f);
            imageViewExpanded.setImageBitmap(bitmap);
        }catch (Exception e){

        }


        configViewBounds(thumbView);

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        imageViewExpanded.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        imageViewExpanded.setPivotX(0f);
        imageViewExpanded.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(imageViewExpanded, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(imageViewExpanded, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(imageViewExpanded, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(imageViewExpanded,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        imageViewExpanded.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick (View view) {
                zoomOut(thumbView);
                return true;
            }
        });


    }

    private void configViewBounds(View thumbView){
        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.


        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).

        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }


    }


    // ZOOM OUT
    private void zoomOut(final View thumbView){

        showActions();

        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }
        //JARROYO HOY
        final float startScaleFinal = startScale;
        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(imageViewExpanded, View.X, startBounds.left))
                .with(ObjectAnimator
                        .ofFloat(imageViewExpanded,
                                View.Y,startBounds.top))
                .with(ObjectAnimator
                        .ofFloat(imageViewExpanded,
                                View.SCALE_X, startScaleFinal))
                .with(ObjectAnimator
                        .ofFloat(imageViewExpanded,
                                View.SCALE_Y, startScaleFinal));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                thumbView.setAlpha(1f);
                imageViewExpanded.setVisibility(View.GONE);
                mCurrentAnimator = null;

                startDragAndDrop();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

                thumbView.setAlpha(1f);
                imageViewExpanded.setVisibility(View.GONE);
                mCurrentAnimator = null;


            }
        });
        set.start();
        mCurrentAnimator = set;

    }
    //--------------------------------------------------------------------------
    // SHOW ACTIONS
    //--------------------------------------------------------------------------
    private void showActions(){
        layoutCamera.startAnimation(animAlphaZeroToOne);
        layoutGallery.startAnimation(animAlphaZeroToOne);
        layoutDelete.startAnimation(animAlphaZeroToOne);
        layoutShare.startAnimation(animAlphaZeroToOne);

        //layoutCamera.startAnimation(animMoveViewToBottom);
        //layoutGallery.startAnimation(animMoveViewToBottom);
        //layoutDelete.startAnimation(animMoveViewToBottom);
        //layoutShare.startAnimation(animMoveViewToBottom);

        //layoutCamera.setVisibility(View.VISIBLE);
        //layoutGallery.setVisibility(View.VISIBLE);
        //layoutShare.setVisibility(View.VISIBLE);
        //layoutDelete.setVisibility(View.VISIBLE);
    }

    //--------------------------------------------------------------------------
    // SHOW ACTIONS
    //--------------------------------------------------------------------------
    private void hideActions(){
        layoutCamera.startAnimation(animAlphaOneToZero);
        layoutGallery.startAnimation(animAlphaOneToZero);
        layoutDelete.startAnimation(animAlphaOneToZero);
        layoutShare.startAnimation(animAlphaOneToZero);

        //layoutCamera.startAnimation(animMoveViewToTop);
        //layoutGallery.startAnimation(animMoveViewToTop);
        //layoutDelete.startAnimation(animMoveViewToTop);
        //layoutShare.startAnimation(animMoveViewToTop);

        //layoutCamera.setVisibility(View.GONE);
        //layoutGallery.setVisibility(View.GONE);
        //layoutShare.setVisibility(View.GONE);
        //layoutDelete.setVisibility(View.GONE);
    }

    private void rotateToZero(float currentRotation){
        Log.d("In", "Rotate to Zero from "+currentRotation);
        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setFillAfter(true);
        animSet.setFillEnabled(true);

        final RotateAnimation animRotate = new RotateAnimation(currentRotation, 0.0f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        long duration = Math.abs((long)(currentRotation/MAX_ROTATION)*durationZoom);
        animRotate.setDuration(duration);
        animRotate.setFillAfter(true);
        animSet.addAnimation(animRotate);

        imageViewPhoto.startAnimation(animSet);
        imageViewPhoto.invalidate();
    }

    //--------------------------------------------------------------------------
    // SHOW ACTIONS //Jarroyo22/05
    //--------------------------------------------------------------------------
    private void hideCircles() {
        imageViewCircleCamera.setScaleX(0);
        imageViewCircleCamera.setScaleY(0);

        imageViewCircleGallery.setScaleX(0);
        imageViewCircleGallery.setScaleY(0);

        imageViewCircleShare.setScaleX(0);
        imageViewCircleShare.setScaleY(0);

        imageViewCircleDelete.setScaleX(0);
        imageViewCircleDelete.setScaleY(0);

    }
}
