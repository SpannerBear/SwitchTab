package cn.spannerbear.simple;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import cn.spannerbear.switch_tab.SwitchTab;

public class MainActivity extends AppCompatActivity {
    
    private SwitchTab mSwitchTab;
    int index = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwitchTab = findViewById(R.id.switchTab);
        Button button = findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index == 0) {
                    index = 1;
                } else {
                    index = 0;
                }
                mSwitchTab.setTabIndex(index);
            }
        });
        mSwitchTab.setTabArray(new String[]{"tab1","tab2"});
    }
}
