package com.ubtechinc.goldenpig.voiceChat.model;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.TIMFileElem;
import com.tencent.TIMMessage;
import com.tencent.TIMValueCallBack;
import com.ubtechinc.goldenpig.R;
import com.ubtechinc.goldenpig.app.UBTPGApplication;
import com.ubtechinc.goldenpig.common.adapter.ViewHolder;
import com.ubtechinc.goldenpig.voiceChat.util.FileUtil;

/**
 * 文件消息
 */
public class FileMessage extends Message {


    public FileMessage(TIMMessage message){
        this.message = message;
    }

    public FileMessage(String filePath){
        message = new TIMMessage();
        TIMFileElem elem = new TIMFileElem();
        elem.setPath(filePath);
        elem.setFileName(filePath.substring(filePath.lastIndexOf("/")+1));
        message.addElement(elem);
    }



    /**
     * 显示消息
     *
     * @param viewHolder 界面样式
     * @param context    显示消息的上下文
     */
    @Override
    public void showMessage(ViewHolder viewHolder, Context context) {
        clearView(viewHolder);
        TIMFileElem e = (TIMFileElem) message.getElement(0);
        TextView tv = new TextView(UBTPGApplication.getContext());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv.setTextColor(UBTPGApplication.getContext().getResources().getColor(isSelf() ? R.color.white : R.color.black));
        tv.setText(e.getFileName());
        getBubbleView(viewHolder).addView(tv);
        showStatus(viewHolder);
    }


    /**
     * 获取消息摘要
     */
    @Override
    public String getSummary() {
        return UBTPGApplication.getContext().getString(R.string.summary_file);
    }

    /**
     * 保存消息或消息文件
     */
    @Override
    public void save() {
        if (message == null) return;
        final TIMFileElem e = (TIMFileElem) message.getElement(0);
        e.getFile(new TIMValueCallBack<byte[]>() {
            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "getFile failed. code: " + i + " errmsg: " + s);
            }

            @Override
            public void onSuccess(byte[] bytes) {
                String[] str = e.getFileName().split("/");
                String filename = str[str.length-1];
                if (FileUtil.isFileExist(filename, Environment.DIRECTORY_DOWNLOADS)) {
                    Toast.makeText(UBTPGApplication.getContext(), UBTPGApplication.getContext().getString(R.string.save_exist), Toast.LENGTH_SHORT).show();
                    return;
                }
                java.io.File mFile = FileUtil.createFile(bytes, filename, Environment.DIRECTORY_DOWNLOADS);
                if (mFile != null){
                    Toast.makeText(UBTPGApplication.getContext(), UBTPGApplication.getContext().getString(R.string.save_succ) +
                            "path : " + mFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(UBTPGApplication.getContext(), UBTPGApplication.getContext().getString(R.string.save_fail), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
