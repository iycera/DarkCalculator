package com.sf.DarkCalculator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.sf.DarkCalculator.databinding.ActivityBigDecimalBinding;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class BigDecimalActivity extends BaseActivity implements View.OnClickListener {

    private ActivityBigDecimalBinding binding;
    private int decimalPlaces; // 小数位数
    private SharedPreferences preferences; // SharedPreferences 对象

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("decimalPlaces")) {
                decimalPlaces = sharedPreferences.getInt(key, 10); // 更新小数位数
                Log.d("BigDecimalActivity", "小数位数已更新: " + decimalPlaces);
            }
        }
    };

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

        // 初始化 SharedPreferences
        preferences = getSharedPreferences("your_preferences_name", MODE_PRIVATE);
        
        // 从 SharedPreferences 中读取小数位数
        decimalPlaces = preferences.getInt("decimalPlaces", 10); // 默认10位

        // 注册监听器
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        // 获取输入框
        EditText editTextDecimalPlaces = findViewById(R.id.editTextDecimalPlaces);
        
        // 设置默认值
        editTextDecimalPlaces.setText(String.valueOf(decimalPlaces));

        // 监听输入框变化
        editTextDecimalPlaces.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    decimalPlaces = Integer.parseInt(s.toString());
                    // 保存到 SharedPreferences
                    preferences.edit().putInt("decimalPlaces", decimalPlaces).apply();
                } else {
                    // 如果输入框为空，重置为默认值10
                    decimalPlaces = 10;
                    preferences.edit().putInt("decimalPlaces", decimalPlaces).apply();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销监听器
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
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
    
        // 声明 result 变量
        BigDecimal result;
    
        // 继续进行计算
        switch (v.getId()) {
            case R.id.button_add:
                result = b1.add(b2);
                break;
            case R.id.button_sub:
                result = b1.subtract(b2);
                break;
            case R.id.button_mul:
                result = b1.multiply(b2);
                break;
            case R.id.button_div:
                if (b2.compareTo(BigDecimal.ZERO) == 0) {
                    Snackbar.make(v, "除数不能为零", Snackbar.LENGTH_SHORT).show();
                    return;
                }
    
                // 进行除法运算
                result = b1.divide(b2, decimalPlaces, RoundingMode.HALF_UP); // 使用指定的小数位数和舍入模式
    
                // 检查是否为有限小数的商
                boolean isFiniteDecimal = isFiniteDecimal(b1, b2);

                Log.d("BDA","商是否有限："+isFiniteDecimal);
                // 去掉多余的尾随零
                result = result.stripTrailingZeros();
    
                // 动态决定使用普通格式还是科学计数法
                String output;
                if (result.compareTo(new BigDecimal("1E+9")) >= 0 || result.compareTo(new BigDecimal("1E-9")) <= 0) {
                    output = result.toString(); // 使用科学计数法
                } else {
                    output = result.toPlainString(); // 普通格式
                }
    
                // 生成输出信息
                String resultMessage;
                // 计算小数位数
                int calculatedDecimalPlaces = result.scale() > decimalPlaces ? decimalPlaces : result.scale();
    
                if (isFiniteDecimal) {
                    resultMessage = "能除尽，商的小数位数：" + calculatedDecimalPlaces + "\n保留 " + decimalPlaces + " 位小数\n计算结果：\n" + output;
                } else {
                    resultMessage = "除不尽，保留 " + decimalPlaces + " 位小数\n计算结果：\n" + result.setScale(decimalPlaces, RoundingMode.HALF_UP).toPlainString();
                }
                ResultsActivity.actionStart(this, resultMessage);
                return;
            default:
                return;
        }
    
        // 设置小数位数
        if (result.scale() > decimalPlaces) {
            result = result.setScale(decimalPlaces, RoundingMode.HALF_UP);
        }
        ResultsActivity.actionStart(this, "保留 " + decimalPlaces + " 位小数\n计算结果: \n" + result.toPlainString());
    }

    // 判断是否为有限小数的方法
    private boolean isFiniteDecimal(BigDecimal numerator, BigDecimal denominator) {
        BigInteger num = numerator.unscaledValue();
        BigInteger denom = denominator.unscaledValue();

        // 约分分母
        BigInteger gcd = num.gcd(denom);
        denom = denom.divide(gcd);

        // 反复除以 2 和 5
        while (denom.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
            denom = denom.divide(BigInteger.valueOf(2));
        }
        while (denom.mod(BigInteger.valueOf(5)).equals(BigInteger.ZERO)) {
            denom = denom.divide(BigInteger.valueOf(5));
        }

        // 如果最后分母变成 1，则是有限小数
        return denom.equals(BigInteger.ONE);
    }

    // 计算有限小数的小数位数
    private int countDecimalPlaces(BigDecimal result) {
        result = result.stripTrailingZeros(); // 去掉尾随的零
        return Math.max(0, result.scale());   // 返回小数位数
    }

    @Override
    public void onBackPressed() {
        // 调用 BaseActivity 中的 hideKeyboard 方法
        hideKeyboard(); // 隐藏输入法
        Log.d("BDA","BDA_KB_Closed");
        super.onBackPressed(); // 确保调用父类方法
        finish(); // 结束当前活动
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.edit1Big.post(() -> {
            binding.edit1Big.requestFocus(); // 确保输入框获得焦点
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.edit1Big, InputMethodManager.SHOW_IMPLICIT); // 显示输入法
            }
        });
    }
}

