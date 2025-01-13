package com.sf.DarkCalculator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.sf.DarkCalculator.databinding.ActivityBigDecimalBinding;

import java.math.BigDecimal;

public class BigDecimalActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityBigDecimalBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBigDecimalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.edit1Big.setOnClickListener(this);
        binding.edit2Big.setOnClickListener(this);
        binding.buttonAdd.setOnClickListener(this);
        binding.buttonSub.setOnClickListener(this);
        binding.buttonMul.setOnClickListener(this);
        binding.buttonDiv.setOnClickListener(this);
    }

    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, BigDecimalActivity.class));
    }

    public void onClick(View v) {
        BigDecimal b1, b2;
        String s1 = binding.edit1Big.getText().toString().trim();
        String s2 = binding.edit2Big.getText().toString().trim();

        // 清理输入字符串，移除无效字符
        s1 = s1.replaceAll("[^\\d.]", ""); // 只保留数字和小数点
        s2 = s2.replaceAll("[^\\d.]", ""); // 只保留数字和小数点

        // 检查输入是否有效
        if (s1.isEmpty() || s2.isEmpty()) {
            Snackbar.make(v, "请输入有效的数字", Snackbar.LENGTH_SHORT).show();
            return;
        }

        try {
            b1 = new BigDecimal(s1);
            b2 = new BigDecimal(s2);
        } catch (NumberFormatException e) {
            Snackbar.make(v, "输入格式不正确", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // 继续进行计算
        switch (v.getId()) {
            case R.id.button_add:
                ResultsActivity.actionStart(this, b1.add(b2).toString());
                break;
            case R.id.button_sub:
                ResultsActivity.actionStart(this, b1.subtract(b2).toString());
                break;
            case R.id.button_mul:
                ResultsActivity.actionStart(this, b1.multiply(b2).toString());
                break;
            case R.id.button_div:
                if (b2.doubleValue() == 0) {
                    Snackbar.make(v, "除数不能为零", Snackbar.LENGTH_SHORT).show();
                    break;
                }
                ResultsActivity.actionStart(this, b1.divide(b2, 100000, BigDecimal.ROUND_HALF_UP).toString());
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // 确保调用父类方法
        finish(); // 结束当前活动
    }
}
