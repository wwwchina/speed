package com.baidu.aip.asrwakeup3.recog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.baidu.aip.asrwakeup3.R;
import com.baidu.aip.asrwakeup3.core.recog.listener.ChainRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
import com.baidu.aip.asrwakeup3.core.util.MyLogger;
import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;
import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialogFragment;
import com.baidu.voicerecognition.android.ui.DigitalDialogInput;

import java.util.ArrayList;
import java.util.Map;

/**
 * UI 界面调用
 * <p>
 * 本类仅仅初始化及释放MyRecognizer，具体识别逻辑在BaiduASRDialog。对话框UI在BaiduASRDigitalDialog
 * 依赖SimpleTransApplication 在两个activity中传递输入参数
 * <p>
 * Created by fujiayi on 2017/10/17.
 */

public class ActivityUiDialog extends ActivityAbstractRecog {

    /**
     * 对话框界面的输入参数
     */
    private DigitalDialogInput input;
    private ChainRecogListener chainRecogListener;
    private static String TAG = "ActivityUiDialog";

    public ActivityUiDialog() {
        super(R.raw.uidialog_recog, false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 有2个listner，一个是用户自己的业务逻辑，如MessageStatusRecogListener。另一个是UI对话框的。
         * 使用这个ChainRecogListener把两个listener和并在一起
         */
        chainRecogListener = new ChainRecogListener();
        // DigitalDialogInput 输入 ，MessageStatusRecogListener可替换为用户自己业务逻辑的listener
        chainRecogListener.addListener(new MessageStatusRecogListener(handler));
        myRecognizer.setEventListener(chainRecogListener); // 替换掉原来的listener

        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    switch (status) {
                        case STATUS_NONE: // 初始状态
                            start();
                            status = STATUS_WAITING_READY;
                            updateBtnTextByStatus();
                            txtLog.setText("");
//                        txtResult.setText("");
                            break;
                        case STATUS_WAITING_READY: // 调用本类的start方法后，即输入START事件后，等待引擎准备完毕。
                        case STATUS_READY: // 引擎准备完毕。
                        case STATUS_SPEAKING: // 用户开始讲话
                        case STATUS_FINISHED: // 一句话识别语音结束
                        case STATUS_RECOGNITION: // 识别中
                            stop();
                            status = STATUS_STOPPED; // 引擎识别中
                            updateBtnTextByStatus();
                            break;
                        case STATUS_LONG_SPEECH_FINISHED: // 长语音识别结束
                        case STATUS_STOPPED: // 引擎识别中
                            cancel();
                            status = STATUS_NONE; // 识别结束，回到初始状态
                            updateBtnTextByStatus();
                            break;
                        default:
                            break;

                    }

                    return true;
                }else if(event.getAction()==MotionEvent.ACTION_UP){
                                  autoStop();
                }



                return true;
            }
        });
    }

    @Override
    public void autoStop() {
        super.autoStop();
        if(baiduASRDigitalDialogFragment.isVisible()){
            baiduASRDigitalDialogFragment.beginRecognitionn();
        }
    }

    BaiduASRDigitalDialogFragment baiduASRDigitalDialogFragment;
    /**
     * 开始录音，点击“开始”按钮后调用。
     */
    @Override
    protected void start() {
        // 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        final Map<String, Object> params = fetchParams();

        // BaiduASRDigitalDialog的输入参数
        input = new DigitalDialogInput(myRecognizer, chainRecogListener, params);
//        BaiduASRDigitalDialogFragment.setInput(input); // 传递input信息，在BaiduASRDialog中读取,

//        Intent intent = new Intent(this, BaiduASRDigitalDialog.class);

        // 修改对话框样式
        // intent.putExtra(BaiduASRDigitalDialog.PARAM_DIALOG_THEME, BaiduASRDigitalDialog.THEME_ORANGE_DEEPBG);

        running = true;
//        startActivityForResult(intent, 2);
        baiduASRDigitalDialogFragment=new BaiduASRDigitalDialogFragment();
        baiduASRDigitalDialogFragment.setInput(input);
        baiduASRDigitalDialogFragment.show(getSupportFragmentManager(),"a");
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.e("停止了","停止了");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        running = false;
        Log.i(TAG, "requestCode" + requestCode);
        if (requestCode == 2) {
            String message = "对话框的识别结果：";
            if (resultCode == RESULT_OK) {
                ArrayList results = data.getStringArrayListExtra("results");
                if (results != null && results.size() > 0) {
                    message += results.get(0);
                }
            } else {
                message += "没有结果";
            }
            MyLogger.info(message);
        }

    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        Log.e("暂停了","暂停了");
        super.onPause();
        if (!running) {
            myRecognizer.release();
            finish();
        }
    }


}