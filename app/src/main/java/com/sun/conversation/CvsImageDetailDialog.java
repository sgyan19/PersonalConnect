package com.sun.conversation;

import android.app.Dialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.MenuPopupWindow;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sun.dialogs.SlideUpMenuDialog;
import com.sun.personalconnect.R;
import com.sun.utils.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by guoyao on 2017/2/13.
 */
public class CvsImageDetailDialog extends AppCompatDialogFragment implements ListView.OnItemClickListener{
    private static final String TAG = "CvsImageDetailDialog";
    private File mImageFile;
    private ImageView mImageView;
    private View mContentView;
    private SlideUpMenuDialog mMenuDialog;
    private String[] mMenu = new String[]{"保存图片"};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogTranslucent);
        setCancelable(true);
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog= super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        mContentView = inflater.inflate(R.layout.dialog_image_detail, container, true);
        mImageView = (ImageView) (mContentView.findViewById(R.id.img_detail));
        if(mImageFile != null && mImageFile.exists()){
            ImageLoader.getInstance().displayImage(Uri.fromFile(mImageFile).toString(), mImageView);
        }

        mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                Log.d(TAG, "long click");
                if (mMenuDialog == null) {
                    mMenuDialog = new SlideUpMenuDialog(getActivity());
                    mMenuDialog.setOnItemClickListener(CvsImageDetailDialog.this);
                    if (Build.VERSION.SDK_INT >= 21) {
                        mMenuDialog.create();
                    }
                }
                mMenuDialog.show(mMenu);
                return true;
            }
        });
        return mContentView;
    }

    public void setImagePath(File file) {
        this.mImageFile = file;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(i == 0){
            Log.d(TAG, "save_image click");
            try {
                MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), mImageFile.getAbsolutePath(), mImageFile.getName(), null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ToastUtils.show("保存成功", Toast.LENGTH_SHORT);
        }
    }
}
