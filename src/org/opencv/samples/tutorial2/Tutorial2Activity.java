
package org.opencv.samples.tutorial2;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Tutorial2Activity extends Activity
        implements CvCameraViewListener2, OnSeekBarChangeListener {
    private static final String TAG = "OCVSample::Activity";

    private static final int VIEW_MODE_RGBA = 0;
    private static final int VIEW_MODE_GRAY = 1;
    private static final int VIEW_MODE_CANNY = 2;
    private static final int VIEW_MODE_FEATURES = 5;

    private Mat mRgba;
    private Mat mIntermediateMat;
    private Mat mGray;

    private MenuItem mItemPreviewRGBA;
    private MenuItem mItemPreviewGray;
    private MenuItem mItemPreviewCanny;
    private MenuItem mItemPreviewFeatures;

    private CameraBridgeViewBase mOpenCvCameraView;

    private SeekBar mLowH;
    private SeekBar mHightH;
    private SeekBar mLowS;
    private SeekBar mHightS;
    private SeekBar mLowV;
    private SeekBar mHightV;

    private TextView mTextLowH;
    private TextView mTextHightH;
    private TextView mTextLowS;
    private TextView mTextHightS;
    private TextView mTextLowV;
    private TextView mTextHightV;

    private View mHsvSettingsContainer;
    private Button mModeBtn;

    enum Mode {
        MODE_HSV_SETTING, MODE_OBJECT_TRACKING
    }

    private Mode mCurrentMode = Mode.MODE_HSV_SETTING;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("mixed_sample");
                    mOpenCvCameraView.setMaxFrameSize(810, 540);
                    mOpenCvCameraView.enableView();
                }
                    break;
                default: {
                    super.onManagerConnected(status);
                }
                    break;
            }
        }
    };

    private void setMode(Mode mode) {
        if (mode == Mode.MODE_HSV_SETTING) {
            mModeBtn.setText(R.string.object_traking);
            mHsvSettingsContainer.setVisibility(View.VISIBLE);
        } else {
            mModeBtn.setText(R.string.hsv_detecting);
            mHsvSettingsContainer.setVisibility(View.GONE);
        }
        mCurrentMode = mode;
    }

    private OnClickListener mModeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mCurrentMode == Mode.MODE_HSV_SETTING) {
                setMode(Mode.MODE_OBJECT_TRACKING);
            } else {
                setMode(Mode.MODE_HSV_SETTING);
            }
        }
    };

    public Tutorial2Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial2_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(
                R.id.tutorial2_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mLowH = (SeekBar) findViewById(R.id.lowH);
        mHightH = (SeekBar) findViewById(R.id.hightH);
        mLowS = (SeekBar) findViewById(R.id.iLowS);
        mHightS = (SeekBar) findViewById(R.id.iHighS);
        mLowV = (SeekBar) findViewById(R.id.iLowV);
        mHightV = (SeekBar) findViewById(R.id.iHighV);

        mTextLowH = (TextView) findViewById(R.id.textLowH);
        mTextHightH = (TextView) findViewById(R.id.textHightH);
        mTextLowS = (TextView) findViewById(R.id.textLowS);
        mTextHightS = (TextView) findViewById(R.id.textHighS);
        mTextLowV = (TextView) findViewById(R.id.textLowV);
        mTextHightV = (TextView) findViewById(R.id.textHighV);

        mLowH.setOnSeekBarChangeListener(this);
        mHightH.setOnSeekBarChangeListener(this);
        mLowS.setOnSeekBarChangeListener(this);
        mHightS.setOnSeekBarChangeListener(this);
        mLowV.setOnSeekBarChangeListener(this);
        mHightV.setOnSeekBarChangeListener(this);
        mHsvSettingsContainer = findViewById(R.id.hsv_controller);
        mModeBtn = (Button)findViewById(R.id.showing_mode);
        mModeBtn.setOnClickListener(mModeClickListener);
        setMode(mCurrentMode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("Preview RGBA");
        mItemPreviewGray = menu.add("Preview GRAY");
        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewFeatures = menu.add("Find features");
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG,
                    "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC3);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        if (lineImage == null) {
            lineImage = Mat.zeros(height, width, CvType.CV_8UC4);
        }
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // input frame has RGBA format
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        int parameters[] = new int[] {
                 mLowH.getProgress(), mHightH.getProgress(), mLowS.getProgress(),
                 mHightS.getProgress(), mLowV.getProgress(), mHightV.getProgress()
        };

        Mat out = new Mat();
        FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),
                out.getNativeObjAddr(), parameters);
        if (mCurrentMode == Mode.MODE_HSV_SETTING) {
            mRgba = out;
        } else {
            Moments oMoment = Imgproc.moments(out, true);
            double dM01 = oMoment.m01;
            double dM10 = oMoment.m10;
            double dArea = oMoment.m00;

            if (dArea > 1000) {
                int posX = (int) (dM10 / dArea);
                int posY = (int) (dM01 / dArea);
                if (iLastX >= 0 && iLastY >= 0 && posX >= 0 && posY >= 0) {
                    // Draw a red line from the previous point to the current point
                    Imgproc.line(lineImage, new Point(posX, posY), new Point(iLastX, iLastY),
                            new Scalar(0, 0, 255), 2);
                }

                iLastX = posX;
                iLastY = posY;
            }
            Core.add(mRgba, lineImage, mRgba);
            out.release();
        }

        return mRgba;
    }

    private Mat lineImage = null;
    private int iLastX = -1;
    private int iLastY = -1;

    public native void FindFeatures(long matAddrGr, long matAddrRgba, long out, int[] parameter);

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean changed) {
        TextView textView = null;
        if (seekBar.equals(mLowH)) {
            textView = mTextLowH;
        } else if (seekBar.equals(mHightH)) {
            textView = mTextHightH;
        } else if (seekBar.equals(mLowS)) {
            textView = mTextLowS;
        } else if (seekBar.equals(mHightS)) {
            textView = mTextHightS;
        } else if (seekBar.equals(mLowV)) {
            textView = mTextLowV;
        } else if (seekBar.equals(mHightV)) {
            textView = mTextHightV;
        }
        if (textView == null) {
            return;
        }
        textView.setText(value + "");
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
    }
}
