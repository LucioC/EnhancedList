package de.timroes.android.listview;

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
        return recyclerView.onTouchEventCustom(e);
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
    }
}