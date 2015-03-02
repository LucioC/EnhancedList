package de.timroes.android.listview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class EnhancedRecyclerListTouchListener implements RecyclerView.OnItemTouchListener {
    private final EnhancedRecyclerListView recyclerView;

    public static interface OnItemClickListener {
        public void onItemClick(View view, int position);

        public void onItemLongClick(View view, int position);
    }

    private OnItemClickListener listener;
    private GestureDetector gestureDetector;

    public EnhancedRecyclerListTouchListener(Context context, final EnhancedRecyclerListView recyclerView) {
        this.recyclerView = recyclerView;
        listener = new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        };

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

                if (childView != null && listener != null) {
                    listener.onItemLongClick(childView, recyclerView.getChildPosition(childView));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());

//        if (childView != null && listener != null && gestureDetector.onTouchEvent(e)) {
//            listener.onItemClick(childView, view.getChildPosition(childView));
//            return false;
//        }

        return recyclerView.onTouchEventCustom(e);
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
    }
}