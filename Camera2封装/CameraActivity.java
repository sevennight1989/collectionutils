public class CameraActivity extends Activity implements View.OnClickListener {

    private ImageView mCaptureImage;
    private CameraTextureView mCameraTextureView;

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_basic);
        File mFile = new File(getExternalFilesDir(null), "pic.jpg");

        findViewById(R.id.picture).setOnClickListener(this);
        findViewById(R.id.info).setOnClickListener(this);

        mCaptureImage = (ImageView) findViewById(R.id.iv_capture_pic);
        mCameraTextureView = (CameraTextureView) findViewById(R.id.texture);

        //配置CameraTexture
        mCameraTextureView.setActivity(this); <span style="white-space:pre;">		</span>//必须写入，传递必要参数
        mCameraTextureView.setPicSaveFile(mFile); <span style="white-space:pre;">	</span>//设置拍照图片的保存地址
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.picture: { //拍照方法
                mCameraTextureView.takePicture(new CameraTextureView.TackPhotoCallback() {
                    @Override
                    public void tackPhotoSuccess(String photoPath) {
                        showToast(photoPath);
                        mCaptureImage.setImageBitmap(BitmapFactory.decodeFile(photoPath));
                    }

                    @Override
                    public void tackPhotoError(Exception e) {
                        showToast(e.getMessage());
                    }
                });
                break;
            }
            case R.id.info: {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.intro_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        mCameraTextureView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraTextureView.onPause();
    }


    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ConfirmationDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), R.string.request_permission, Toast.LENGTH_SHORT).show();
            } else {
                //执行相机初始化操作
                mCameraTextureView.onResume();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void ConfirmationDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.request_permission)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(CameraActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                REQUEST_CAMERA_PERMISSION);
                    }
                })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(), R.string.camera_permission, Toast.LENGTH_SHORT).show();
                            }
                        })
                .create();
        alertDialog.show();
    }


    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }