package com.luciocossio.android.enhancedlist;

public interface EnhancedList {

    void discardUndo();

    EnhancedList enableSwipeToDismiss();

    EnhancedList disableSwipeToDismiss();

    EnhancedList setDismissCallback(OnDismissCallback dismissCallback);

    EnhancedList setShouldSwipeCallback(OnShouldSwipeCallback shouldSwipeCallback);

    EnhancedList setUndoStyle(UndoStyle undoStyle);

    EnhancedList setUndoHideDelay(int hideDelay);

    EnhancedList setRequireTouchBeforeDismiss(boolean touchBeforeDismiss);

    EnhancedList setSwipeDirection(SwipeDirection direction);

    EnhancedList setSwipingLayout(int swipingLayoutId);

    void delete(int position);
}
