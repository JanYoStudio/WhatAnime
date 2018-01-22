package pw.janyo.whatanime.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import pw.janyo.whatanime.R;
import pw.janyo.whatanime.classes.Error;
import pw.janyo.whatanime.classes.Response;
import pw.janyo.whatanime.handler.UploadHandler;
import vip.mystery0.tools.HTTPok.HTTPok;
import vip.mystery0.tools.HTTPok.HTTPokResponse;
import vip.mystery0.tools.HTTPok.HTTPokResponseListener;
import vip.mystery0.tools.Logs.Logs;

/**
 * Created by myste.
 */

public class ErrorActivity extends AppCompatActivity {
    private static final String TAG = "ErrorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        TextView text_date = findViewById(R.id.text_date);
        TextView text_version = findViewById(R.id.text_version);
        TextView text_SDK = findViewById(R.id.text_SDK);
        TextView text_vendor = findViewById(R.id.text_vendor);
        TextView text_model = findViewById(R.id.text_model);
        TextView text_exception = findViewById(R.id.text_exception);
        Button button = findViewById(R.id.button_upload);

        final SpotsDialog spotsDialog = new SpotsDialog(this, getString(R.string.hint_upload_log), R.style.SpotsDialog);
        final UploadHandler uploadHandler = new UploadHandler();
        uploadHandler.spotsDialog = spotsDialog;
        uploadHandler.activity = this;
        uploadHandler.coordinatorLayout = findViewById(R.id.coordinatorLayout);

        if (getIntent().getBundleExtra("error") == null)
            finish();
        final Error error = (Error) getIntent().getBundleExtra("error").getSerializable("error");
        if (error != null) {
            text_date.setText(getString(R.string.exception_time, error.time));
            text_version.setText(getString(R.string.exception_version, error.appVersionName, error.appVersionCode));
            text_SDK.setText(getString(R.string.exception_sdk, error.AndroidVersion, error.sdk));
            text_vendor.setText(getString(R.string.exception_vendor, error.vendor));
            text_model.setText(getString(R.string.exception_model, error.model));
            StringWriter stringWriter = new StringWriter();
            error.throwable.printStackTrace(new PrintWriter(stringWriter));
            text_exception.setText(getString(R.string.exception_message, stringWriter.toString()));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    spotsDialog.show();
                    Map<String, Object> map = new HashMap<>();
                    map.put("logFile", getIntent().getBundleExtra("error").getSerializable("file"));
                    map.put("date", error.time);
                    map.put("appName", getString(R.string.app_name));
                    map.put("appVersionName", error.appVersionName);
                    map.put("appVersionCode", error.appVersionCode);
                    map.put("androidVersion", error.AndroidVersion);
                    map.put("sdk", error.sdk);
                    map.put("vendor", error.vendor);
                    map.put("model", error.model);
                    new HTTPok()
                            .setURL("http://202.5.19.14/interface/uploadLog.php")
                            .setRequestMethod(HTTPok.Companion.getPOST())
                            .setParams(map)
                            .isFileRequest()
                            .setListener(new HTTPokResponseListener() {
                                @Override
                                public void onError(String s) {
                                    Logs.e(TAG, "onError: " + s);
                                    uploadHandler.sendEmptyMessage(-1);
                                }

                                @Override
                                public void onResponse(HTTPokResponse httPokResponse) {
                                    try {
                                        Response response = httPokResponse.getJSON(Response.class);
                                        if (response.code == 0) {
                                            uploadHandler.response = response;
                                            uploadHandler.sendEmptyMessage(0);
                                        } else
                                            uploadHandler.sendEmptyMessage(-1);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        uploadHandler.sendEmptyMessage(-1);
                                    }
                                }
                            })
                            .open();
                }
            });
        }
    }
}
