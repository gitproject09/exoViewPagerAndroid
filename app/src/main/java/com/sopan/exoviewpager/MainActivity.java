package com.sopan.exoviewpager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.sopan.exoviewpager.gallery.ItemViewerFragment;
import com.sopan.exoviewpager.gallery.ViewPagerAdapter;
import com.sopan.exoviewpager.loader.MediaLoader;
import com.sopan.exoviewpager.model.GalleryModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private static final int REQUEST_CODE_EXTERNAL = 100;

    public ViewPager mViewPager;

    protected int mPreviousPos = 0;

    private List<GalleryModel> mData = new ArrayList<>();

    private Toolbar toolbar;

//    protected PreviewPagerAdapter mAdapter;

    protected ViewPagerAdapter mAdapter;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 3;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkStoragePermissions();

        mViewPager = findViewById(R.id.imageViewerPager);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayUseLogoEnabled(true);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }
        if (hasStoragePermission()) {
            buildDemoDataSet();
        }
    }

    /**
     * Check Storage permission
     * before back up data on Local Storage
     */
    private void checkStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (requestCode == REQUEST_EXTERNAL_STORAGE) {
                        Toast.makeText(MainActivity.this, "Permission Granted!!!", Toast.LENGTH_LONG).show();
                        buildDemoDataSet();
                    }
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setMessage("You need to allow storage permission");
                    alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    alert.setCancelable(false);
                    alert.show();
                }
                break;
        }
    }


    private void buildDemoDataSet() {

        MediaLoader mediaLoader = new MediaLoader(this);
        mData = mediaLoader.getAllMediaFiles();

//        mAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), null);
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        for (GalleryModel model : mData) {
            mAdapter.addFragment(ItemViewerFragment.newInstance(model), model.getName());
        }

        mViewPager.addOnPageChangeListener(this);
//      mAdapter.addAll(mData);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mPreviousPos, false);
        updateTheToolbar(mPreviousPos);

    }


    private void updateTheToolbar(int position) {

        if (toolbar != null && position <= mData.size()) {
            GalleryModel model = mData.get(position);
            toolbar.setTitle(model.getName());
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        try {
            ((ItemViewerFragment) mAdapter.getItem(mPreviousPos)).imHiddenNow();
            ((ItemViewerFragment) mAdapter.getItem(position)).imVisibleNow();

        } catch (Exception e) {
            e.printStackTrace();
        }

        mPreviousPos = position;
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_EXTERNAL);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

}
