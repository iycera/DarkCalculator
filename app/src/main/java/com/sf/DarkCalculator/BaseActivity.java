package com.sf.DarkCalculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager; // 确保导入这个类
public class BaseActivity extends AppCompatActivity {

    public static SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        hideKeyboard();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                hideKeyboard();
                Log.d("BaseActivity", "物理键返回");
                finish();
                break;
        }
        return true;
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0); // 隐藏输入法
        }
    }

    public void onBackPressed() {
        hideKeyboard(); // 隐藏输入法
        super.onBackPressed(); // 调用父类的 onBackPressed 方法
    }
}
