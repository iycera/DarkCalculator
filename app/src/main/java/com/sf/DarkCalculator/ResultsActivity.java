package com.sf.DarkCalculator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.sf.DarkCalculator.databinding.ActivityResultsBinding;

public class ResultsActivity extends BaseActivity {
    private ActivityResultsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        // 使用 ViewBinding 代替 findViewById
        binding = ActivityResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 确保调用的是 AppCompatActivity 的 getSupportActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 从 Intent 获取数据并显示
        String results = getIntent().getStringExtra("results");
        binding.textResults.setText(results);
    }

    public static void actionStart(Context context, String results) {
        Intent intent = new Intent(context, ResultsActivity.class);
        intent.putExtra("results", results);
        context.startActivity(intent);

    }
}
