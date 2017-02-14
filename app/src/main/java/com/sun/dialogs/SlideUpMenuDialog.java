package com.sun.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sun.personalconnect.R;

public class SlideUpMenuDialog extends Dialog implements ListView.OnItemClickListener {
    private Context mContext;
    private ListView mMenuList;
    private ListView.OnItemClickListener mOnItemClickListener;

    public SlideUpMenuDialog(Context context) {
        super(context, R.style.DialogTranslucent);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_slide_menu);
        getWindow().setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
        getWindow().setWindowAnimations(R.style.dialogWindowAnim);

        mMenuList = (ListView) findViewById(R.id.device_list);
        mMenuList.setOnItemClickListener(this);
        ((ViewGroup) mMenuList.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    public void show(String[] menu) {
        super.show();
        mMenuList.setAdapter(new MenuAdapter(menu));
    }

    private class MenuAdapter extends BaseAdapter {
        private String[] menu;
        private LayoutInflater inflater;
        public MenuAdapter(String[] menu) {
            super();
            this.menu = menu;
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return menu.length;
        }

        @Override
        public Object getItem(int position) {
            return menu[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_dialog_menu, parent, false);
            }
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(menu[position]);
            return convertView;
        }
    }

    public void setOnItemClickListener(ListView.OnItemClickListener clickListener) {
        mOnItemClickListener = clickListener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(parent, view, position, id);
        }
        dismiss();
    }
}
