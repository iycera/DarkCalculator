package com.sf.DarkCalculator;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;

import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.sf.DarkCalculator.databinding.ActivityMainBinding;
import com.sf.ExpressionHandler.Constants;
import com.sf.ExpressionHandler.ExpressionHandler;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends BaseActivity {

    public static MainActivity activity;
    private Context context;
    private Toolbar toolbar;
    private EditText inText;
    private TextView stateText;
    private TextView outText;
    private ViewPager drawerPager;
    private DrawerLayout drawer;
    private ArrayList<View> drawerPageList;
    private FrameLayout delete;
    private SharedPreferences preferences;

    private int[] XX = {1, 3, 1, 3};//列数：
    private int[] YY = {6, 5, 6, 5};//行数：[0]切换大数、进制、大写汉字的侧边栏,[1]数字键,[2]运算符，[3]函数、常数

    private static final String[] OPERATOR = {"÷", "×", "－", "＋", "%", ",", "i"};
    private static final String[] OPERATOR_VICE = {"√", "^", "()", "!", "°", "∞", "x"};

    private static final String[][] BUTTON = {
            {"sqrt", "cbrt", "root", "perm", "comb", "remn", "prec", "rand", "randInt", "lg", "ln", "log",
                    "min", "max", "fact", "sin", "cos", "tan", "asin", "acos",
                    "atan", "re", "im", "arg", "norm", "reg", "conj", "diff",
                    "sum", "lim", "eval", "fzero", "integ", "exp", "gcd", "lcm",
                    "gamma", "round", "floor", "ceil", "sign",
                    "abs", "prime", "isPrime", "base"},
            {"ans", "reg", "π", "e", "F", "h", "ћ", "γ", "φ", "c",
                    "N", "R", "K", "k", "G", "Φ", "true", "false", "me", "mn", "mp"}};

    private static final String[][] BUTTON_VICE = {
            {"平方根", "立方根", "开方", "排列", "组合", "取余", "输出精度", "随机复数", "随机整数", "常用对数", "自然对数", "对数",
                    "最小", "最大", "阶乘", "正弦", "余弦", "正切", "反正弦", "反余弦", "反正切", "实部",
                    "虚部", "辐角", "模长", "寄存", "共轭复数", "导函数", "累加求和", "极限", "求值",
                    "函数零点", "定积分", "e底指数", "最大公约", "最小公倍", "伽玛函数",
                    "四舍五入", "向下取整", "向上取整", "取正负号", "绝对值", "质数", "判断质数", "输出进制"},
            {"上次运算", "寄存器", "圆周率", "自然底数", "法拉第", "普朗克", "约化普朗克",
                    "欧拉", "黄金分割", "光速", "阿伏伽德罗", "理想气体", "卡钦", "玻尔兹曼",
                    "万有引力", "磁通量子", "真", "假", "电子质量", "质子质量", "中子质量"}};

    private static final Pattern FUNCTIONS_KEYWORDS = Pattern.compile(
            "\\b(" + "sqrt|cbrt|root|rand|randInt|lg|ln|log|abs|min|max|fact|" +
                    "sin|cos|tan|asin|acos|atan|re|im|arg|norm|reg|conj|diff|" +
                    "sum|lim|eval|fzero|integ|exp|gcd|lcm|perm|comb|round|floor|" +
                    "ceil|sign|gamma|remn|prime|isPrime|prec|base|Γ" + ")\\b");

    private static final Pattern CONSTANS_KEYWORDS2 = Pattern.compile(
            "\\b(" + "ans|reg|true|false|me|mn|mp" + ")\\b");

    private static final Pattern CONSTANS_KEYWORDS1 = Pattern.compile("[∞i°%πeFhћγφcNRkGΦ]");

    private static final String[] FUNCTION_LIST = {"科学计算", "大数计算", "进制转换", "大写数字"};

    private static final String[] NUMERIC = {
            "7", "8", "9",
            "4", "5", "6",
            "1", "2", "3",
            "·", "0", "=",
            "A", "B", "C",
            "D", "E", "F",
            "⑵", "⑶", "⑷",
            "⑸", "⑹", "⑺",
            "⑻", "⑼", "⑽",
            "⑾", "⑿", "⒀",
            "⒁", "⒂", "⒃"};

    private ActivityMainBinding binding;

    private int decimalPlaces; // 声明为成员变量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        context = this;
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化 preferences
        preferences = getSharedPreferences("your_preferences_name", MODE_PRIVATE);

        // 从 SharedPreferences 中读取小数位数
        decimalPlaces = preferences.getInt("decimalPlaces", 10); // 默认10位
        Log.d("MainActivity","onCreate取得小数保留 "+decimalPlaces+" 位");

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            XX = new int[]{1, 3, 1, 3};
            YY = new int[]{6, 5, 7, 5};
            Log.d("MainActivity","横向");
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            XX = new int[]{1, 3, 1, 3};
            YY = new int[]{6, 4, 5, 5};
            Log.d("MainActivity","竖向");
        }

        initToolBar();
        initEditText();
        initTextView();
        initDrawer();
        initPages();
        initTabs();
        initDelete();
        initSideBar();
        initNumeric();
        initOperator();
        initFunction();

    }

    private void initDelete() {
        delete = binding.delete;
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = binding.editText.getText();
                int index = binding.editText.getSelectionStart();
                int index2 = binding.editText.getSelectionEnd();
                if (index == index2) {
                    if (index == 0) return;
                    editable.delete(index - 1, index);
                } else {
                    editable.delete(index, index2);
                }
            }
        });
        delete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ExpressionHandler.stop();
                binding.editText.setText(null);
                return true;
            }
        });
    }

    private void initTextView() {
        stateText = binding.textState;
        stateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpressionHandler.stop();
                stateText.setText(null);
            }
        });
        outText = binding.textOut;
        outText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(rootValue);
                Snackbar.make(v, "已复制运算结果", Snackbar.LENGTH_SHORT).show();
            }
        });
        outText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String resultMessage = "initTextView小数位数: " + decimalPlaces + "\n结果: " + rootValue;
                ResultsActivity.actionStart(v.getContext(), resultMessage);
                return true;
            }
        });
    }

    private void initDrawer() {
        drawer = binding.drawerMain;
        drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.END);
            }
        });
    }

    private void initTabs() {
        TabLayout tabs = binding.tabsMain;
        tabs.setupWithViewPager(drawerPager);
        tabs.getTabAt(0).setText("函数");
        tabs.getTabAt(1).setText("常数");
    }

    private void initPages() {
        drawerPageList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            GridView gridView = new GridView(this);
            drawerPageList.add(gridView);
        }

        drawerPager = binding.viewPagerDrawer;
        MainPagerAdapter drawerPagerAdapter = new MainPagerAdapter(drawerPageList);
        drawerPager.setAdapter(drawerPagerAdapter);

        drawerPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);
                } else {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);//.LOCK_MODE_LOCKED_OPEN
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);//.LOCK_MODE_LOCKED_CLOSED,
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void initSideBar() {
        final GridView sideBar = binding.sideBar;
        sideBar.setNumColumns(XX[0]);
        sideBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        BigDecimalActivity.actionStart(context);
                        break;
                    case 2:
                        BaseConversionActivity.actionStart(context);
                        break;
                    case 3:
                        CapitalMoneyActivity.actionStart(context);
                        break;
                    default:
                        Snackbar.make(sideBar, "功能还未完善", Snackbar.LENGTH_SHORT).show();
                }
                drawer.closeDrawer(GravityCompat.START);
            }
        });
        GridViewAdapter sideBarAdapter = new GridViewAdapter(sideBar, Arrays.asList(FUNCTION_LIST),
                null, R.layout.button_sidebar, YY[0]);
        sideBar.setAdapter(sideBarAdapter);
    }

    private void initNumeric() {
        GridView numericBar = binding.barNumeric;
        numericBar.setNumColumns(XX[1]);
        numericBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = position == 9 ? "." : NUMERIC[position];
                if (str.equals("=")) {
                    if (calcThread != null) {
                        Snackbar.make(view, "请等待当前运算完成", Snackbar.LENGTH_SHORT)
                                .setAction("停止运算", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ExpressionHandler.stop();
                                    }
                                }).show();
                        return;
                    }
                    outText.setTextColor(0xffbdbdbd);
                    stateText.setText("运算中...");
                    calcThread = new Calc(inText.getText().toString());
                    calcThread.start();
                    return;
                }
                modifyInText(str);
            }
        });
        GridViewAdapter numericAdapter = new GridViewAdapter(numericBar, Arrays.asList(NUMERIC),
                null, R.layout.button_numeric, YY[1]);
        numericBar.setAdapter(numericAdapter);
    }

    private void initOperator() {
        GridView operatorBar = binding.barOperator;
        operatorBar.setNumColumns(XX[2]);
        operatorBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String str = position == 0 ? "/" : OPERATOR[position];
                //modifyInText(position == 1 ? "•" : str);
                String str = OPERATOR[position];
                modifyInText(str);
            }
        });
        operatorBar.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                modifyInText(OPERATOR_VICE[position]);
                return true;
            }
        });
        GridViewAdapter operatorAdapter = new GridViewAdapter(operatorBar, Arrays.asList(OPERATOR),
                Arrays.asList(OPERATOR_VICE), R.layout.button_operator, YY[2]);
        operatorBar.setAdapter(operatorAdapter);
    }

    private void initFunction() {
        int i = 0;
        for (View view : drawerPageList) {
            GridView operatorProBar = (GridView) view;
            operatorProBar.setNumColumns(XX[3]);

            if (i == 0) {
                operatorProBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        modifyInText((BUTTON[0][position].equals("gamma") ? "Γ" : BUTTON[0][position]) + "()");
                    }
                });

                operatorProBar.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        String text = BUTTON[0][position];
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setTitle(text);
                        dialog.setMessage(HelpUtil.getFunctionHelp(text));
                        dialog.setPositiveButton("确定", null);
                        dialog.show();
                        return true;
                    }
                });
            } else {
                operatorProBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        modifyInText(BUTTON[1][position]);
                    }
                });
            }
            int id = i == 0 ? R.layout.button_function : R.layout.button_constant;
            GridViewAdapter operatorProAdapter = new GridViewAdapter(operatorProBar,
                    Arrays.asList(BUTTON[i++]), Arrays.asList(BUTTON_VICE[i - 1]), id, YY[3]);

            operatorProBar.setAdapter(operatorProAdapter);
        }
    }

    private void modifyInText(String str) {
        int index = inText.getSelectionStart();
        int index2 = inText.getSelectionEnd();
        if (index == index2) {
            inText.getText().insert(index, str);
        } else {
            inText.getText().replace(index, index2, str);
        }
    }

    class FastCalc extends Thread implements Runnable {
        private String exp;

        public FastCalc(String exp) {
            this.exp = exp;
        }

        @Override
        public void run() {
            final long t = System.currentTimeMillis();
            final String[] value = ExpressionHandler.calculation(exp);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    outText.setTextColor(0xffbdbdbd);
                    stateText.setText("运算结束，耗时 " + (System.currentTimeMillis() - t) + " 毫秒");
                    if (value[0].getBytes().length > 1000) {
                        outText.setText("数值太大，长按此处查看");
                    } else
                        outText.setText(value[0]);
                    rootValue = value[0];
                    calcThread = null;
                }
            });
        }
    }

    class Calc extends Thread implements Runnable {
        private String exp;

        public Calc(String exp) {
            this.exp = exp;
        }

        @Override
        public void run() {
            final long t = System.currentTimeMillis();
            final String[] value = ExpressionHandler.calculation(exp);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stateText.setText("运算结束，耗时 " + (System.currentTimeMillis() - t) + " 毫秒");
                    if (value[1].equals("true")) {
                        outText.setTextColor(0xffff4081);                        
                        outText.setText(value[0]);
                    } else {
                        Constants.setAns(value[0]);
                        rootValue = value[0];
                        if (rootValue.getBytes().length > 1000) {
                            outText.setText("数值太大，长按此处查看");
                            String resultMessage = "Class Calc run小数位数: " + decimalPlaces + "\n结果: " + rootValue;
                            ResultsActivity.actionStart(context, resultMessage);
                        } else {
                            try {                                
                                outText.setText(rootValue);
                            } catch (NumberFormatException e) {
                                Snackbar.make(outText, "计算出错，请检查输入", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                    calcThread = null;
                }
            });
        }

    }

    private boolean modified = true;
    private int selection = 0;
    private Thread calcThread;
    private String rootValue;

    private void initEditText() {
        inText = binding.editText;
        AutofitHelper.create(inText).setMinTextSize(28).setMaxLines(1);
        inText.requestFocus();
        inText.requestFocusFromTouch();
        inText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    if (calcThread == null)
                        stateText.setText(null);
                    outText.setTextColor(0xffbdbdbd);
                    outText.setText(null);
                    rootValue = null;
                    return;
                }

                if (calcThread == null) {
                    stateText.setText("运算中...");
                    calcThread = new FastCalc(s.toString());
                    calcThread.start();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!modified) return;

                selection = inText.getSelectionStart();
                s.clearSpans();

                for (Matcher m = Pattern.compile("x").matcher(s); m.find(); )//粉色
                    s.setSpan(new ForegroundColorSpan(0xfff48fb1), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = CONSTANS_KEYWORDS1.matcher(s); m.find(); )//黄色
                    s.setSpan(new ForegroundColorSpan(0xfffff59d), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = CONSTANS_KEYWORDS2.matcher(s); m.find(); )//黄色
                    s.setSpan(new ForegroundColorSpan(0xfffff59d), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = FUNCTIONS_KEYWORDS.matcher(s); m.find(); )//函数：绿色
                    s.setSpan(new ForegroundColorSpan(0xffa5d6a7), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                for (Matcher m = Pattern.compile("[()\\-*+.,/!^=√•＋－×÷＝=]").matcher(s); m.find(); )//蓝色#3f51b5
                    s.setSpan(new ForegroundColorSpan(0xff81d4fa), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                modified = false;
                inText.setText(s);
                modified = true;

                if (selection >= 2 && s.toString().substring(selection - 2, selection).equals("()"))
                    selection--;
                inText.setSelection(selection);
            }
        });
    }

    private void initToolBar() {
        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        setTitle(null);
        toolbar.setSubtitle("科学计算");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    private void setGodMode(boolean isGodMode) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        ActionBar actionBar = getSupportActionBar();
        godMenuItem.setChecked(isGodMode);
        if (isGodMode) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            drawer.setVisibility(View.GONE);
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(inText, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            imm.showSoftInput(inText, InputMethodManager.SHOW_FORCED);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);
            drawer.setVisibility(View.VISIBLE);
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(inText, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            imm.hideSoftInputFromWindow(inText.getWindowToken(), 0);
        }
    }
    private void setPortraitLocked(boolean isPortraitLocked) {
        if (isPortraitLocked) {
            Log.d("MainActivity","锁定竖屏");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else{
            //isPortraitLocked=false;
            Log.d("MainActivity","未锁定竖屏");
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//跟随系统
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);//四种屏幕方向
        }
    }
    private MenuItem godMenuItem;
    private MenuItem PortraitLockedMenuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean isGodMode = preferences.getBoolean("godMode", false);
        godMenuItem = menu.add("上帝输入").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                boolean isGodMode = !item.isChecked();
                preferences.edit().putBoolean("godMode", isGodMode).apply();
                setGodMode(isGodMode);
                return true;
            }
        }).setCheckable(true).setChecked(isGodMode);
        setGodMode(isGodMode);

        boolean isPortraitLocked = preferences.getBoolean("PortraitLocked", true);
        PortraitLockedMenuItem = menu.add("锁定竖屏").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                boolean isPortraitLocked = !item.isChecked();
                item.setChecked(isPortraitLocked); // 更新菜单项的选中状态
                preferences.edit().putBoolean("PortraitLocked", isPortraitLocked).apply();
                setPortraitLocked(isPortraitLocked);
                return true;
            }
        }).setCheckable(true).setChecked(isPortraitLocked);
        setPortraitLocked(isPortraitLocked);

        menu.add("帮助").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("帮助")
                        .setMessage(R.string.app_help)
                        .setPositiveButton("确定", null)
                        .show();
                return true;
            }
        });
        menu.add("关于").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AboutActivity.actionStart(context);
                return true;
            }
        });
        menu.add("退出").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                finish();
                return true;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.d("MainActivity", "物理键返回");
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            Log.d("MainActivity", "1是什么");
            drawerPager.setCurrentItem(0);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            drawer.closeDrawer(GravityCompat.END);
            return;
        } else if (drawer.isDrawerOpen(GravityCompat.START)) {
            Log.d("MainActivity", "物理键返回的什么");
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d("MainActivity", "点击三杠0");
                if (drawer.isDrawerOpen(GravityCompat.END)) {
                    Log.d("MainActivity", "关闭抽屉1");
                    drawerPager.setCurrentItem(0);
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
                    drawer.closeDrawer(GravityCompat.END);//关闭抽屉
                } else if (drawer.isDrawerOpen(GravityCompat.START)) {
                    Log.d("MainActivity", "关闭抽屉2");
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    Log.d("MainActivity", "打开抽屉3");
                    drawer.openDrawer(GravityCompat.START);//打开抽屉
                    hideKeyboard();
                }
                break;
        }
        return true;
    }

    public FrameLayout getDelete() {
        return delete;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "kb_view2: " + isKeyboardVisible2(this));
        if (isKeyboardVisible2(this)) {
            // 确保焦点不为 null，如果没有焦点，可以手动设置一个视图获取焦点
            View view = getCurrentFocus();
            if (view == null) {
                // 设置焦点到根视图，防止 getCurrentFocus() 返回 null
                view = new View(this);
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                view.requestFocus();
            }

            // 使用 Handler 延迟执行隐藏键盘
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideKeyboard(); // 隐藏输入法
                    Log.d("MainActivity", "KB_View after hide:" + isKeyboardVisible2(MainActivity.this));
                }
            }, 200); // 延迟200ms，确保焦点正确
        }
        // 从 SharedPreferences 中读取最新的小数位数
        decimalPlaces = preferences.getInt("decimalPlaces", 10); // 默认10位
        Log.d("MainActivity", "onResume更新小数保留 " + decimalPlaces + " 位");
    }

    public boolean isKeyboardVisible2(Activity activity) {
        // 获取根视图
        View rootView = activity.getWindow().getDecorView().getRootView();
        // 获取可视区域的高度
        Rect rect = new Rect();
        rootView.getWindowVisibleDisplayFrame(rect);
        // 屏幕高度
        int screenHeight = rootView.getHeight();
        // 可视区域高度与屏幕高度的差值
        int heightDifference = screenHeight - rect.height();
        // 如果高度差超过一定值（如200），说明软键盘显示了
        Log.d("MainActivit","键盘高度"+heightDifference);
        return heightDifference > 100;
    }

    private void calculateResult(String expression) {
        final long t = System.currentTimeMillis();
        final String[] value = ExpressionHandler.calculation(expression);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                outText.setTextColor(0xffbdbdbd);
                stateText.setText("运算结束，耗时 " + (System.currentTimeMillis() - t) + " 毫秒");
                if (value[1].equals("true")) {
                    outText.setText(value[0]);
                } else {
                    Constants.setAns(value[0]);
                    rootValue = value[0];
                    try {
                        BigDecimal result = new BigDecimal(rootValue);
                        // 使用 decimalPlaces 设置精度
                        result = result.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
                        
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
                        String resultMessage = "CalculateResult小数位数: " + decimalPlaces + "\n结果: " + output;
                        ResultsActivity.actionStart(context, resultMessage);
                    } catch (NumberFormatException e) {
                        Snackbar.make(outText, "计算出错，请检查输入", Snackbar.LENGTH_SHORT).show();
                    }
                }
                calcThread = null;
            }
        });
    }
}
