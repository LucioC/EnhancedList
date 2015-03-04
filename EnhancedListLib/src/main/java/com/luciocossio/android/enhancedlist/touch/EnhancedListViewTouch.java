package com.luciocossio.android.enhancedlist.touch;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewParent;
import android.widget.AbsListView;

import com.luciocossio.android.enhancedlist.EnhancedListView;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class EnhancedListViewTouch {

    private float downX;
    private int downPosition;
    private boolean swiping;
    private VelocityTracker velocityTracker;
    private View swipeDownView;
    private View swipeDownChild;
    private float slop;
    private int minFlingVelocity;
    private int maxFlingVelocity;
    private int animationTime;
    private int viewWidth = 1; // 1 and not 0 to prevent dividing by zero

    EnhancedListView enhancedListView;
    private boolean swipePaused;

    public EnhancedListViewTouch(EnhancedListView enhancedListView, TouchSetup touchSetup) {
        this.enhancedListView = enhancedListView;
        this.slop = touchSetup.getSlop();
        this.minFlingVelocity = touchSetup.getMinFlingVelocity();
        this.maxFlingVelocity = touchSetup.getMaxFlingVelocity();
        this.animationTime = touchSetup.getAnimationTime();

        enhancedListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                EnhancedListViewTouch.this.onScrollStateChanged(view, scrollState);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void onScrollStateChanged(View view, int scrollState) {
        swipePaused = (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!enhancedListView.isSwipeEnabled()) {
            return enhancedListView.superOnTouchEvent(ev);
        }
        // Send a delayed message to hide popup
        if (enhancedListView.getTouchBeforeAutoHide() && enhancedListView.isUndoPopupShowing()) {
            enhancedListView.hidePopupMessageDelayed();
        }

        // Store width of this list for usage of swipe distance detection
        if (viewWidth < 2) {
            viewWidth = enhancedListView.getWidth();
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (swipePaused) {
                    return enhancedListView.superOnTouchEvent(ev);
                }
                // TODO: ensure this is a finger, and set a flag
                // Find the child view that was touched (perform a hit test)
                Rect rect = new Rect();
                int childCount = enhancedListView.getChildCount();
                int[] listViewCoords = new int[2];
                enhancedListView.getLocationOnScreen(listViewCoords);
                int x = (int) ev.getRawX() - listViewCoords[0];
                int y = (int) ev.getRawY() - listViewCoords[1];
                View child;
                for (int i = enhancedListView.getHeaderViewsCount(); i < childCount; i++) {
                    child = enhancedListView.getChildAt(i);
                    if (child != null) {
                        child.getHitRect(rect);
                        if (rect.contains(x, y)) {
                            // if a specific swiping layout has been giving, use this to swipe.
                            if (enhancedListView.hasSwipingLayout()) {
                                View swipingView = child.findViewById(enhancedListView.getSwipingLayout());
                                if (swipingView != null) {
                                    swipeDownView = swipingView;
                                    swipeDownChild = child;
                                    break;
                                }
                            }
                            // If no swiping layout has been found, swipe the whole child
                            swipeDownView = swipeDownChild = child;
                            break;
                        }
                    }
                }
                if (swipeDownView != null) {
                    // test if the item should be swiped
                    int position = enhancedListView.getPositionForView(swipeDownView) - enhancedListView.getHeaderViewsCount();
                    if ((!enhancedListView.hasSwipeCallback()) ||
                            enhancedListView.onShouldSwipe(position)) {
                        downX = ev.getRawX();
                        downPosition = position;
                        velocityTracker = VelocityTracker.obtain();
                        velocityTracker.addMovement(ev);
                    } else {
                        // set back to null to revert swiping
                        swipeDownView = swipeDownChild = null;
                    }
                }
                enhancedListView.superOnTouchEvent(ev);
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if (velocityTracker == null) {
                    break;
                }
                float deltaX = ev.getRawX() - downX;
                velocityTracker.addMovement(ev);
                velocityTracker.computeCurrentVelocity(1000);
                float velocityX = Math.abs(velocityTracker.getXVelocity());
                float velocityY = Math.abs(velocityTracker.getYVelocity());
                boolean dismiss = false;
                boolean dismissRight = false;
                if (Math.abs(deltaX) > viewWidth / 2 && swiping) {
                    dismiss = true;
                    dismissRight = deltaX > 0;
                } else if (minFlingVelocity <= velocityX && velocityX <= maxFlingVelocity
                        && velocityY < velocityX && swiping && enhancedListView.isSwipeDirectionValid(velocityTracker.getXVelocity())
                        && deltaX >= viewWidth * 0.2f) {
                    dismiss = true;
                    dismissRight = velocityTracker.getXVelocity() > 0;
                }
                if (dismiss) {
                    // dismiss
                    enhancedListView.slideOutView(swipeDownView, swipeDownChild, downPosition, dismissRight);
                } else if (swiping) {
                    // Swipe back to regular position
                    ViewPropertyAnimator.animate(swipeDownView)
                            .translationX(0)
                            .alpha(1)
                            .setDuration(animationTime)
                            .setListener(null);
                }
                velocityTracker = null;
                downX = 0;
                swipeDownView = null;
                swipeDownChild = null;
                downPosition = AbsListView.INVALID_POSITION;
                swiping = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (velocityTracker == null || swipePaused) {
                    break;
                }
                velocityTracker.addMovement(ev);
                float deltaX = ev.getRawX() - downX;
                // Only start swipe in correct direction
                if (enhancedListView.isSwipeDirectionValid(deltaX)) {
                    ViewParent parent = enhancedListView.getParent();
                    if (parent != null) {
                        // If we swipe don't allow parent to intercept touch (e.g. like NavigationDrawer does)
                        // otherwise swipe would not be working.
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    if (Math.abs(deltaX) > slop) {
                        swiping = true;
                        enhancedListView.requestDisallowInterceptTouchEvent(true);
                        // Cancel ListView's touch (un-highlighting the item)
                        MotionEvent cancelEvent = MotionEvent.obtain(ev);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL
                                | (ev.getActionIndex()
                                << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        enhancedListView.superOnTouchEvent(cancelEvent);
                    }
                } else {
                    // If we swiped into wrong direction, act like this was the new
                    // touch down point
                    downX = ev.getRawX();
                    deltaX = 0;
                }
                if (swiping) {
                    ViewHelper.setTranslationX(swipeDownView, deltaX);
                    ViewHelper.setAlpha(swipeDownView, Math.max(0f, Math.min(1f,
                            1f - 2f * Math.abs(deltaX) / viewWidth)));
                    return true;
                }
                break;
            }
        }
        return enhancedListView.superOnTouchEvent(ev);
    }
}
