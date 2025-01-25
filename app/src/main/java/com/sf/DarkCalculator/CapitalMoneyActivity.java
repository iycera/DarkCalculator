package com.sf.DarkCalculator;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.sf.DarkCalculator.databinding.ActivityCapitalMoneyBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.logging.Logger;

public class CapitalMoneyActivity extends BaseActivity {

    private ActivityCapitalMoneyBinding binding;
    private static final Logger logger = Logger.getLogger(CapitalMoneyActivity.class.getName());
    private static final String[] DIGITS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] RADICES = {"", "十", "百", "千"};
    private static final String[] BIG_RADICES = {"", "万", "亿", "兆", "京", "垓", "秭", "穰", "沟", "涧", "正", "载", "极"};
    private static final String[] DECIMALS = {"角", "分", "厘", "毫", "丝"};

    // 定义千极的阈值，即 10^52。
    private static final BigDecimal THRESHOLD = new BigDecimal("1E52");//E12是千亿，E52是千极。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCapitalMoneyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initTextOut();
        initTextIn();
    }

    private void initTextOut() {
        TextView textOut = binding.textOut;
        AutofitHelper.create(textOut).setMaxLines(6);
        //textOut.setShowSoftInputOnFocus(false);//不加载键盘

        textOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cmb = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(textOut.getText());
                Snackbar.make(v, "已复制转换结果", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
    private void initTextIn() {
        EditText editIn = binding.textIn;
        AutofitHelper.create(editIn).setMinTextSize(TypedValue.COMPLEX_UNIT_SP, 40); // 设置最小字号为 30sp;
        editIn.requestFocus();// 输入框获得焦点
        editIn.requestFocusFromTouch();
        editIn.setShowSoftInputOnFocus(true);
        editIn.setFocusable(true);
        editIn.setFocusableInTouchMode(true);

        editIn.setOnFocusChangeListener((v, hasFocus) -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!hasFocus && imm != null) {//失焦时
                //imm.showSoftInput(binding.textIn, InputMethodManager.SHOW_IMPLICIT);//不关闭键盘
                imm.hideSoftInputFromWindow(editIn.getWindowToken(), 0); // 关闭键盘
            }
        });

        editIn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                if (s.length() == 0 || str.equals(".")) {
                    binding.textOut.setText("···");
                    return;
                }
                int i = str.indexOf(".");
                if (i != -1) if (str.substring(i, str.length()).length() > 7) {
                    binding.textOut.setText("小数点后不得超过6位");
                    return;
                }

                try {
                    BigDecimal value = new BigDecimal(s.toString());

                    // 检查是否超过千极
                    if (value.abs().compareTo(THRESHOLD) >= 0) {
                        String chinese = convertScientificNotation(value);
                        binding.textOut.setText(chinese);
                    } else {
                        String chinese = format(value.toPlainString());
                        binding.textOut.setText(chinese);
                    }
                } catch (NumberFormatException e) {
                    binding.textOut.setText("输入格式错误");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * 使用 NumberCN 的逻辑替换原有的 format 方法
     */
    public String format(String s) {
        String dValueStr = s.replace(",", "");

        // 保留0.n形式的数字
        // 不去掉前导的0，继续处理
        if (dValueStr.startsWith("0.") || dValueStr.startsWith("."))
            dValueStr = dValueStr.replaceFirst("^0+", "0"); // 处理前导0
        else dValueStr = dValueStr.replaceFirst("^0+", ""); // 其他情况去掉前导的0

        logger.info("Input value: " + dValueStr);

        // 检查是否为科学计数法
        if (dValueStr.contains("E") || dValueStr.contains("e")) {
            BigDecimal scientificValue = new BigDecimal(dValueStr);
            return convertScientificNotation(scientificValue);
        }

        // 处理合法数值的情况
        if (!isNumeric(dValueStr)) return "错误：不是合法数值！";

        String minus = "";
        String CN_SYMBOL = ""; // 币种名称（如“人民币”，默认空）

        if (dValueStr.length() > 1) {
            if (dValueStr.startsWith("-")) {
                dValueStr = dValueStr.replace("-", "");
                minus = "负";
            }
            if (dValueStr.startsWith("+")) dValueStr = dValueStr.replace("+", "");
        }

        String vInt = "";
        String vDec = "";
        StringBuilder resAIW = new StringBuilder();
        String[] parts = dValueStr.split("\\.");

        // 处理整数部分
        if (parts.length > 0) {
            vInt = parts[0];
            BigDecimal bigIntValue = new BigDecimal(vInt);
            if (bigIntValue.compareTo(BigDecimal.ZERO) > 0) {
                int zeroCount = 0;
                for (int i = 0; i < vInt.length(); i++) {
                    int p = vInt.length() - i - 1;
                    int d = Character.getNumericValue(vInt.charAt(i));
                    int quotient = p / 4;
                    int modulus = p % 4;

                    if (d == 0) zeroCount++;
                    else {
                        if (zeroCount > 0) resAIW.append(DIGITS[0]);
                        zeroCount = 0;
                        resAIW.append(DIGITS[d]).append(RADICES[modulus]);
                    }
                    if (modulus == 0 && zeroCount < 4 && quotient < BIG_RADICES.length)
                        resAIW.append(BIG_RADICES[quotient]);
                }

                // 只替换开头的“一十”为“十”
                String resultStr = resAIW.toString();
                if (resultStr.startsWith("一十"))
                    resultStr = "十" + resultStr.substring(2); // 替换开头的“一十”为“十”

                // 替换“零一十二”为“零十二”
                resultStr = resultStr.replace("零一十二", "零十二");

                logger.info("Integer part converted: " + resultStr);
                resAIW.setLength(0); // 清空 StringBuilder
                resAIW.append(resultStr); // 重新添加替换后的结果
            }
        }

        // 处理小数部分
        if (parts.length > 1) {
            vDec = parts[1];

            // 处理小数部分四舍五入
            int maxDec = Math.min(vDec.length(), 5);
            BigDecimal rDec = new BigDecimal("0." + vDec).setScale(maxDec, BigDecimal.ROUND_HALF_UP);
            vDec = rDec.toString().split("\\.")[1];
            logger.info("Rounded decimal part: " + vDec);

            // 小数部分存在时，才在整数和小数之间添加“点”
            if (!vDec.isEmpty()) {
                resAIW.append("点");
                if (vInt.isEmpty() || vInt.equals("0")) {
                    resAIW.insert(resAIW.length() - 1, "零"); // 如果整数部分为空，添加零在“点”前
                    logger.info("zero before dot:" + resAIW);
                }
                for (int i = 0; i < vDec.length(); i++) {
                    int d = Character.getNumericValue(vDec.charAt(i));
                    resAIW.append(DIGITS[d]); // 保留小数间的零
                }
            }
        }

        // 替换以.0结尾的情况
        String result = CN_SYMBOL + minus + resAIW.toString();
        if (result.endsWith("点零")) result = result.replace("点零", ""); // 替换掉.0

        logger.info("Final result: " + result); // 输出最终结果
        return result;
    }

    /**
     * 替换原有的 transform 方法，使用 NumberCN 的逻辑
     */
    private String transform(String original) {
        // 由于在 format 方法中已经完成转换，这里可以直接调用 format 方法
        return format(original);
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * 处理科学计数法，确保基数部分在1（含）到10（不含）之间
     */
    private String convertScientificNotation(BigDecimal value) {
        StringBuilder result = new StringBuilder();

        BigDecimal absValue = value.abs();

        // 计算指数：位数减1
        int exponent = absValue.precision() - absValue.scale() - 1;
        if (exponent < 0) exponent = 0;

        // 计算基数部分，确保基数在1（含）到10（不含）之间
        BigDecimal base = absValue.divide(BigDecimal.TEN.pow(exponent), 10, RoundingMode.HALF_UP);

        // 如果基数 >=10，则需要调整
        if (base.compareTo(BigDecimal.TEN) >= 0) {
            base = base.divide(BigDecimal.TEN, 10, RoundingMode.HALF_UP);
            exponent += 1;
        }

        // 将基数和指数转换为中文
        String baseStr = base.stripTrailingZeros().toPlainString();
        String baseChinese = format(baseStr);
        String exponentChinese = format(new BigDecimal(exponent).toPlainString());

        // 处理负数
        if (value.signum() < 0) result.append("负");

        result.append(baseChinese).append("乘十的").append(exponentChinese).append("次方");
        return result.toString();
    }

    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, CapitalMoneyActivity.class));
    }

    protected void onResume() {
        super.onResume();
        binding.textIn.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                binding.textIn.requestFocus();
                imm.showSoftInput(binding.textIn, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }
}
