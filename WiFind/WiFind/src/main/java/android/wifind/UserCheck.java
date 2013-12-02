package android.wifind;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


public class UserCheck extends Activity {

    private Button btn1;
    private TextView tv1;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user_check);

        btn1 = (Button) findViewById(R.id.btn1);
        tv1=(TextView) findViewById(R.id.txt1);
        tv1.setPaintFlags(tv1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv1.setText("I am an existing user");
//        btn1.getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
        btn1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                intent = new Intent(UserCheck.this, AskEmailNew.class);
                UserCheck.this.startActivity(intent);
            }
        });
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(UserCheck.this, AskEmailExist.class);
                UserCheck.this.startActivity(intent);
            }
        });
    }

}
