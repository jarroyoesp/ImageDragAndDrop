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
import android.view.DragEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
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
    private Animator mCurrentAnimator;

    private int mShortAnimationDuration = 400;

    //Drag and Drop
    //-------------
    private MyDragShadowBuilder shadowBuilder;

    //ZOOM
    final Rect startBounds = new Rect();
    final Rect finalBounds = new Rect();
    final Point globalOffset = new Point();
    private float startScale;
    private final float startScaleFinal =0f;


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
    }

    //------------------------------------------------------------------------------------
    // CONFIG DRAG AND DROP
    //------------------------------------------------------------------------------------
    private void configDragAndDrop(){
        DragAndDropListener dragAndDropListener = new DragAndDropListener();
        imageViewCamera.setOnDragListener(dragAndDropListener);
        imageViewDelete.setOnDragListener(dragAndDropListener);
        imageViewGallery.setOnDragListener(dragAndDropListener);
        imageViewShare.setOnDragListener(dragAndDropListener);
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
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            bitmap = Utils.getResizedBitmap(bitmap, 0.5f);
            imageViewPhoto.setVisibility(View.VISIBLE);
            imageViewPhoto.setImageBitmap(bitmap);
            //imageViewExpanded.setImageBitmap(bitmap);
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
        imageViewPhoto.startDrag(data,shadowBuilder , imageViewPhoto, 0);
        //imageViewPhoto.setVisibility(View.GONE);
    }


    //---------------------------------------------------------------------
    // DRAG AND DROP LISTENER
    //---------------------------------------------------------------------
    private class DragAndDropListener implements View.OnDragListener {
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
                    if(v.getId() == R.id.imageViewBackground) {
                        float posXCurrentDrag = event.getX() - 200;
                        float posYCurrentDrag = event.getY() - 300;
                        imageViewPhoto.setX(posXCurrentDrag);
                        imageViewPhoto.setY(posYCurrentDrag);
                    }
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
        //ON DROP VIEW
        //-----------
        private void onDropView(View v){
            switch (v.getId()){
                case R.id.imageViewCamera:
                    showInfoDrop("Camera");
                    imageViewPhoto.setLayoutParams(layoutParamsImage);
                    startAnimation(imageViewPhoto, animRotateRightToCenter);
                    startAnimation(imageViewCircleCamera, animResizeDownCircle);
                    break;
                case R.id.imageViewGallery:
                    showInfoDrop("Gallery");
                    imageViewPhoto.setLayoutParams(layoutParamsImage);
                    startAnimation(imageViewPhoto, animRotateLeftToCenter);
                    startAnimation(imageViewCircleGallery, animResizeDownCircle);
                    break;
                case R.id.imageViewDelete:
                    showInfoDrop("Delete");
                    imageViewPhoto.setLayoutParams(layoutParamsImage);
                    startAnimation(imageViewPhoto, animSizeToOne);
                    startAnimation(imageViewCircleDelete, animResizeDownCircle);
                    break;

                case R.id.imageViewShare:
                    showInfoDrop("Share");
                    imageViewPhoto.setLayoutParams(layoutParamsImage);
                    startAnimation(imageViewPhoto, animSizeToOneUp);
                    startAnimation(imageViewCircleShare, animResizeDownCircle);
                    break;

                default:
                    zoomImageFromThumb(imageViewPhoto, selectedImageUri);
            }
        }

        //ON DRAG ENTERED
        //---------------
        private void onDragEntered(View v){
            switch (v.getId()){
                case R.id.layoutCamera:
                    startAnimation(imageViewPhoto, animRotateCenterToRight);
                    startAnimation(imageViewCircleCamera, animResizeUpCircle);
                    break;
                case R.id.layoutGallery:
                    startAnimation(imageViewPhoto, animRotateCenterToLeft);
                    startAnimation(imageViewCircleGallery, animResizeUpCircle);
                    break;
                case R.id.layoutDelete:
                    startAnimation(imageViewPhoto, animReduceToZero);
                    startAnimation(imageViewCircleDelete, animResizeUpCircle);
                    break;

                case R.id.layoutShare:
                    startAnimation(imageViewPhoto, animReduceToZeroUp);
                    startAnimation(imageViewCircleShare, animResizeUpCircle);
                    break;
            }
        }

        //ON DRAG EXITED
        //---------------
        private void onDragExited(View v){
            switch (v.getId()){
                case R.id.layoutCamera:
                    startAnimation(imageViewPhoto, animRotateRightToCenter);
                    startAnimation(imageViewCircleCamera, animResizeDownCircle);
                    break;
                case R.id.layoutGallery:
                    startAnimation(imageViewPhoto, animRotateLeftToCenter);
                    startAnimation(imageViewCircleGallery, animResizeDownCircle);
                    break;
                case R.id.layoutDelete:
                    startAnimation(imageViewPhoto, animSizeToOne);
                    startAnimation(imageViewCircleDelete, animResizeDownCircle);
                    break;
                case R.id.layoutShare:
                    startAnimation(imageViewPhoto, animSizeToOneUp);
                    startAnimation(imageViewCircleShare, animResizeDownCircle);
                    break;
            }
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
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

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
}
