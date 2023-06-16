package com.example.dbaproject.utils;

import android.view.View;

import net.cachapa.expandablelayout.ExpandableLayout;

public class ExpandableUtils {
    public static void toggleWithIndicator(ExpandableLayout layout, View indicator){
        if (layout.isExpanded()){
            layout.collapse();
            indicator.animate().rotation(0).setDuration(1000).start();
        } else {
            layout.expand();
            indicator.animate().rotation(90).setDuration(1000).start();
        }
    }
}
