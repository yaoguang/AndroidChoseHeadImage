package com.library.admin.mosimage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.library.admin.mosimage.view.ClipImageLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * http://blog.csdn.net/lmj623565791/article/details/39761281
 *
 * @author zhy
 */
public class MainActivity extends Activity {
    private ClipImageLayout mClipImageLayout;

    private PopupWindow mImageMenuWnd = null;
    private File mCaptureFile = null;
    private static final int REQUEST_CAPTURE_IMAGE = 0;
    private View contview;
    private AlertDialog dialog;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mClipImageLayout = (ClipImageLayout) findViewById(R.id.id_clipImageLayout);
        contview = findViewById(R.id.contive);
        initImageDialog();
    }

    private void initImageDialog() {
        dialog = new AlertDialog.Builder(this).create();
        imageView = new ImageView(this);
        dialog.setTitle("裁剪图片结果");
        dialog.setView(imageView);
    }

    public void showHeadPopWindow(View view) {
        if (mImageMenuWnd == null) {
            initImagePopWin();
        }
        mImageMenuWnd.showAtLocation(contview, Gravity.BOTTOM
                | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void initImagePopWin() {
        View imageMenu = LayoutInflater.from(this).inflate(
                R.layout.item_2item_pop_menu, null);
        imageMenu.findViewById(R.id.item_00).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mImageMenuWnd.dismiss();
                        mCaptureFile = new File(
                                getExternalFilesDir(Environment.DIRECTORY_DCIM),
                                "" + System.currentTimeMillis() + ".jpg");
                        Intent intent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(mCaptureFile));
                        try {
                            startActivityForResult(intent,
                                    REQUEST_CAPTURE_IMAGE);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "无相机服务", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        imageMenu.findViewById(R.id.item_01).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mImageMenuWnd.dismiss();
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                                .addCategory(Intent.CATEGORY_OPENABLE).setType(
                                        "image/*");
                        startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
                    }
                });
        imageMenu.findViewById(R.id.cancel).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mImageMenuWnd.dismiss();
                    }
                });

        // initialize the image select menu window.
        mImageMenuWnd = new PopupWindow(imageMenu,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mImageMenuWnd.setFocusable(true);
        mImageMenuWnd.setOutsideTouchable(true);
        mImageMenuWnd.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mImageMenuWnd.update();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK
                && requestCode == REQUEST_CAPTURE_IMAGE) {
            try {
                final Cursor cr = getContentResolver().query(data.getData(),
                        new String[]{MediaStore.Images.Media.DATA}, null,
                        null, null);
                if (cr.moveToFirst()) {
                    String localPath = cr.getString(cr
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                    mClipImageLayout.getZoomImageView()
                            .setImageBitmap(BitmapFactory.decodeFile(localPath));
                }
                cr.close();
            } catch (Exception e) {
                if (mCaptureFile != null && mCaptureFile.exists()) {
                    mClipImageLayout.getZoomImageView()
                            .setImageBitmap(BitmapFactory.decodeFile(mCaptureFile.getAbsolutePath()));
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void chip(View view) {
        Bitmap bitmap = mClipImageLayout.clip();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();

        imageView.setImageBitmap(BitmapFactory.decodeByteArray(datas, 0, datas.length));
        dialog.show();
    }
}
