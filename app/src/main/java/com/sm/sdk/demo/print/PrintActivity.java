package com.sm.sdk.demo.print;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class PrintActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);
        initToolbarBringBack(R.string.print);
        initView();

    }

    private void initView() {
        View item = this.findViewById(R.id.print_text);
        TextView leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.print_text);
        item.setOnClickListener(this);

        item = this.findViewById(R.id.print_config);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.print_set_param);
        item.setOnClickListener(this);

        item = this.findViewById(R.id.print_bitmap);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.print_bitmap);
        item.setOnClickListener(this);

        item = this.findViewById(R.id.print_merchant);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.print_merchant);
        item.setOnClickListener(this);

        item = this.findViewById(R.id.print_input);
        leftText = item.findViewById(R.id.left_text);
        leftText.setText(R.string.print_input);
        item.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.print_text:
                openActivity(PrintTextActivity.class);
                break;
            case R.id.print_config:
                openActivity(PrintConfigActivity.class);
                break;
            case R.id.print_bitmap:
                openActivity(PrintBitmapActivity.class);
                break;
            case R.id.print_merchant:
                openActivity(PrintMerchantActivity.class);
                break;
            case R.id.print_input:
                openActivity(PrintInput.class);
                break;
        }
    }
}
