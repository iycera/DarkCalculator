package com.sf.DarkCalculator;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.sf.DarkCalculator.databinding.ActivityCapitalMoneyBinding;

public class CapitalMoneyActivity extends BaseActivity {

    private ActivityCapitalMoneyBinding binding;

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
        AutofitHelper.create(editIn);
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
                if (i != -1) {
                    if (str.substring(i, str.length()).length() > 7) {
                        binding.textOut.setText("小数点后不得超过6位");
                        return;
                    }
                }
                binding.textOut.setText(format(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private static final char[] cnNumbers = {'零', '壹', '贰', '叁', '肆', '伍',
            '陆', '柒', '捌', '玖'};

    private static final char[] units = {'纳', '微', '毫', '厘', '分', '角', '元', '拾', '佰',
            '仟', '萬', '拾', '佰', '仟', '億', '拾', '佰', '仟', '兆', '拾',
            '佰', '仟', '京', '拾', '佰', '仟', '垓', '拾', '佰', '仟', '杼',
            '拾', '佰', '仟', '穰', '拾', '佰', '仟', '溝', '拾', '佰', '仟',
            '澗', '拾', '佰', '仟', '正', '拾', '佰', '仟', '載', '拾', '佰',
            '仟', '極', '拾', '佰', '仟'};

    public String format(String s) {
        if (Double.parseDouble(s) > 10E51)
            return "数值太大，无法转换";
        return this.transform(s);
    }

    private String transform(String original) {
        String integerPart = "";
        String floatPart = "";

        if (original.indexOf(".") > -1) {
            int dotIndex = original.indexOf(".");
            integerPart = original.substring(0, dotIndex);
            floatPart = original.substring(dotIndex + 1);
        } else {
            integerPart = original;
        }
        StringBuffer sb = new StringBuffer();


        for (int i = 0; i < integerPart.length(); i++) {
            int number = Integer
                    .parseInt(String.valueOf(integerPart.charAt(i)));
            if(number!=0||i==0&&number==0&&integerPart.length()==1){//不为零，或单独零
                sb.append(cnNumbers[number]);
            }else if(i>1){//十位起全加
                sb.append(cnNumbers[number]);
            }

            if (number != 0) {//为零时不添加单位
                sb.append(units[integerPart.length() + 5 - i]);
            }
        }
        

        if (floatPart.length() >= 1) {
            for (int i = 0; i < floatPart.length(); i++) {
                int number = Integer.parseInt(String.valueOf(floatPart
                        .charAt(i)));
                sb.append(cnNumbers[number]);
                if (i < 6) {
                    sb.append("-");
                    //sb.append(units[5 - i]);
                }
            }
        } else {
            //sb.append('整');
        }

        return sb.toString().replaceAll("零+","零").replaceAll("-","");//去掉多余的零;
    }


    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, CapitalMoneyActivity.class));
    }
}
