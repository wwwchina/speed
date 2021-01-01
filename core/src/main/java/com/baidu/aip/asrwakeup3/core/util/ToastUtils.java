package com.baidu.aip.asrwakeup3.core.util;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.asrwakeup3.core.R;

public class ToastUtils {
    public static void showMessage(Context context, String msg) {
        if (context==null|| TextUtils.isEmpty(msg))
            return;
        View toastRoot = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(toastRoot);
        TextView tv = (TextView) toastRoot.findViewById(R.id.toast_notice);
        tv.setText(msg);
        toast.show();
    }
}
