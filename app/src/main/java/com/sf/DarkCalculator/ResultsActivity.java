package com.sf.DarkCalculator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {

    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results); // 确保布局文件正确

        resultTextView = findViewById(R.id.text_results); // 获取结果显示的 TextView

        // 获取传递过来的数据
        Intent intent = getIntent();
        String resultMessage = intent.getStringExtra("resultMessage");

        // 显示结果
        if (resultMessage != null) {
            resultTextView.setText(resultMessage);
        }
    }

    public static void actionStart(Context context, String resultMessage) {
        Intent intent = new Intent(context, ResultsActivity.class);
        intent.putExtra("resultMessage", resultMessage); // 传递结果信息
        context.startActivity(intent);
    }
}
