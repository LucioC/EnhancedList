package com.luciocossio.android.enhancedlist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;

public class EnhancedRecyclerListTouchListener implements RecyclerView.OnItemTouchListener {
    private final EnhancedRecyclerListView recyclerView;

    public EnhancedRecyclerListTouchListener(Context context, final EnhancedRecyclerListView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        //When it returns true, subsequent events will go to onTouchEvent
        return recyclerView.onTouchEventCustom(e);
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        //If returned true from intercept method, continue to handle it here
        recyclerView.onTouchEventCustom(motionEvent);
    }
}