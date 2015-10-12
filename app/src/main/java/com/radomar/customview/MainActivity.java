package com.radomar.customview;

import android.app.Activity;
import android.os.Bundle;

import com.radomar.customview.circleMenu.TestViewGroup;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TestViewGroup testViewGroup = (TestViewGroup)findViewById(R.id.testViewGroup);

        int[] menuItems = {R.drawable.abc_ic_clear_mtrl_alpha,
                R.drawable.abc_ic_voice_search_api_mtrl_alpha, R.drawable.abc_ic_menu_moreoverflow_mtrl_alpha,
                R.drawable.abc_ic_menu_copy_mtrl_am_alpha, R.drawable.abc_ic_menu_selectall_mtrl_alpha,
                R.drawable.abc_ic_menu_paste_mtrl_am_alpha, R.drawable.abc_ic_menu_copy_mtrl_am_alpha, };

//        int[] menuItems = {R.drawable.abc_ic_voice_search_api_mtrl_alpha, R.drawable.abc_ic_clear_mtrl_alpha,
//                R.drawable.abc_ic_menu_paste_mtrl_am_alpha, R.drawable.abc_ic_menu_copy_mtrl_am_alpha,};
        testViewGroup.initMenu(menuItems);


    }

}
