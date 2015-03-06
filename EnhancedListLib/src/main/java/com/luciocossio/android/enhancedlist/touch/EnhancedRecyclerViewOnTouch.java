package com.luciocossio.android.enhancedlist.touch;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewParent;
import android.widget.AbsListView;

import com.luciocossio.android.enhancedlist.EnhancedRecyclerListView;
import com.nineoldandroids.view.ViewHelper;

public class EnhancedRecyclerViewOnTouch {

    private float downX;
    private int downPosition;
    private boolean swiping;
    private VelocityTracker velocityTracker;
    private View swipeDownView;
    private View swipeDownChild;
    private int viewWidth = 1; // 1 and not 0 to prevent dividing by zero

    private float slop;
    private int minFlingVelocity;
    private int maxFlingVelocity;
    private int animationTime;
    private EnhancedRecyclerListView enhancedRecyclerListView;
    private boolean swipePaused;

    public EnhancedRecyclerViewOnTouch(EnhancedRecyclerListView enhancedRecyclerListView, TouchSetup touchSetup) {
        this.enhancedRecyclerListView = enhancedRecyclerListView;
        this.slop = touchSetup.getSlop();
        this.minFlingVelocity = touchSetup.getMinFlingVelocity();
        this.maxFlingVelocity = touchSetup.getMaxFlingVelocity();
        this.animationTime = touchSetup.getAnimationTime();

        enhancedRecyclerListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {
                EnhancedRecyclerViewOnTouch.this.onScrollStateChanged(view, scrollState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        });
    }

    private void onScrollStateChanged(View view, int scrollState) {
        swipePaused = (scrollState == RecyclerView.SCROLL_STATE_DRAGGING);
    }

    public boolean onTouchEventNew(MotionEvent ev) {

        if (!enhancedRecyclerListView.isSwipeEnabled()) {
            return false;
        }

        // Send a delayed message to hide popup
        if (enhancedRecyclerListView.getTouchBeforeAutoHide() && enhancedRecyclerListView.isUndoPopupShowing()) {
            enhancedRecyclerListView.hidePopupMessageDelayed();
        }

        // Store width of this list for usage of swipe distance detection
        if (viewWidth < 2) {
            viewWidth = enhancedRecyclerListView.getWidth();
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (swipePaused) {
                    return false;
                }

                View child = enhancedRecyclerListView.findChildViewUnder(ev.getX(), ev.getY());
                if (child != null) {
                    // if a specific swiping layout has been giving, use this to swipe.
                    if (enhancedRecyclerListView.hasSwipingLayout()) {
                        View swipingView = child.findViewById(enhancedRecyclerListView.getSwipingLayout());
                        if (swipingView != null) {
                            swipeDownView = swipingView;
                            swipeDownChild = child;
                        }
                    } else {
                        // If no swiping layout has been found, swipe the whole child
                        swipeDownView = child;
                        swipeDownChild = child;
                    }
                }

                if (swipeDownView != null) {
                    // test if the item should be swiped
                    int position = enhancedRecyclerListView.getPositionSwipeDownView(swipeDownChild);
                    if ((!enhancedRecyclerListView.hasSwipeCallback()) ||
                            enhancedRecyclerListView.onShouldSwipe(position)) {
                        downX = ev.getRawX();
                        downPosition = position;
                        velocityTracker = VelocityTracker.obtain();
                        velocityTracker.addMovement(ev);
                    } else {
                        // set back to null to revert swiping
                        swipeDownView = null;
                        swipeDownChild = null;
                    }
                }
                break;
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
                        && velocityY < velocityX && swiping && enhancedRecyclerListView.isSwipeDirectionValid(velocityTracker.getXVelocity())
                        && deltaX >= viewWidth * 0.2f) {
                    dismiss = true;
                    dismissRight = velocityTracker.getXVelocity() > 0;
                }
                if (dismiss) {
                    // dismiss
                    enhancedRecyclerListView.slideOutView(swipeDownView, swipeDownChild, downPosition, dismissRight);
                } else if (swiping) {
                    enhancedRecyclerListView.animateSwipeBack(swipeDownView, animationTime);
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
                if (enhancedRecyclerListView.isSwipeDirectionValid(deltaX)) {
                    ViewParent parent = enhancedRecyclerListView.getParent();
                    if (parent != null) {
                        // If we swipe don't allow parent to intercept touch (e.g. like NavigationDrawer does)
                        // otherwise swipe would not be working.
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    if (Math.abs(deltaX) > slop) {
                        swiping = true;

                        // Cancel child view touch
                        MotionEvent cancelEvent = MotionEvent.obtain(ev);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL
                                | (ev.getActionIndex()
                                << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        swipeDownChild.dispatchTouchEvent(cancelEvent);
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
                }
            }
        }

        return false;
    }
}
