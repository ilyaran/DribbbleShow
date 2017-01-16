package mysite.com.dribbbleshow.other;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;
import mysite.com.dribbbleshow.R;

public class AlertDialog extends Dialog {
    private int LayoutResId;
    String msg,title;
    public Button dialogButtonCancel,dialogButtonOk,dialogButton1,dialogButton2;


    public AlertDialog(Context context, String title, String msg) {
        super(context);
        LayoutResId = R.layout.custom_alert_dialog;
        setContentView(LayoutResId);
        this.title = title;
        this.msg = msg;

        setDialogView();

    }
    public AlertDialog(Context context, String msg) {
        super(context);
        LayoutResId = R.layout.custom_alert_dialog;
        setContentView(LayoutResId);
        this.title = "ALERT";
        this.msg = msg;

        setDialogView();
    }

    private void setDialogView(){
        setTitle(title);

        TextView messageTextView = (TextView) findViewById(R.id.messageTextView);
        messageTextView.setText(msg);

        dialogButtonCancel = (Button) findViewById(R.id.customDialogCancel);
        dialogButton1 = (Button) findViewById(R.id.customDialog1);
        dialogButton2 = (Button) findViewById(R.id.customDialog2);
        dialogButtonOk = (Button) findViewById(R.id.customDialogOk);

        // Click cancel to dismiss android custom dialog box
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClick();
            }
        });

        // Your android custom dialog ok action
        // Action for custom dialog ok button click
        dialogButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkClick();
            }
        });
        dialogButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                on1Click();
            }
        });
        dialogButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                on2Click();
            }
        });
        show();
    }

    public void onCancelClick(){}
    public void onOkClick(){
        dismiss();
    }
    public void on1Click(){}
    public void on2Click(){}

    public AlertDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }
    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {

    }
}
