package com.luciocossio.android.enhancedlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.luciocossio.android.enhancedlist.touch.TouchSetup;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

public class EnhancedListFlow {

    private Context context;
    private EnhancedListControl enhancedList;

    private float slop;
    private int minFlingVelocity;
    private int maxFlingVelocity;
    private int animationTime;

    public void init(Context ctx, final EnhancedListControl enhancedList) {
        this.context = ctx;
        this.enhancedList = enhancedList;

        if (enhancedList.isInEditMode()) {
            // Skip initializing when in edit mode (IDE preview).
            return;
        }

        ViewConfiguration vc = ViewConfiguration.get(ctx);
        slop = ctx.getResources().getDimension(R.dimen.elv_touch_slop);
        minFlingVelocity = vc.getScaledMinimumFlingVelocity();
        maxFlingVelocity = vc.getScaledMaximumFlingVelocity();

        animationTime = ctx.getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        // Initialize undo popup
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View undoView = inflater.inflate(R.layout.elv_undo_popup, null);

        Button mUndoButton = (Button) undoView.findViewById(R.id.undo);
        mUndoButton.setOnClickListener(new UndoClickListener(enhancedList, this));
        mUndoButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // If the user touches the screen invalidate the current running delay by incrementing
                // the valid message id. So this delay won't hide the undo popup anymore
                enhancedList.incrementValidDelayedMsgId();
                return false;
            }
        });
        enhancedList.setUndoButton(mUndoButton);

        enhancedList.setUndoPopupTextView((TextView) undoView.findViewById(R.id.text));

        PopupWindow mUndoPopup = new PopupWindow(undoView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        mUndoPopup.setAnimationStyle(R.style.elv_fade_animation);
        enhancedList.setUndoPopup(mUndoPopup);

        enhancedList.setScreenDensity(ctx.getResources().getDisplayMetrics().density);

    }


    /**
     * Changes the text of the undo popup. If more then one item can be undone, the number of deleted
     * items will be shown. If only one deletion can be undone, the title of this deletion (or a default
     * string in case the title is {@code null}) will be shown.
     */
    public void changePopupText() {
        String msg = null;
        if (enhancedList.undoActionsSize() > 1) {
            msg = context.getResources().getString(R.string.elv_n_items_deleted, enhancedList.undoActionsSize());
        } else if (enhancedList.undoActionsSize() >= 1) {
            // Set title from single undoable or when no multiple deletion string
            // is given
            msg = enhancedList.getTitleFromUndoAction(enhancedList.undoActionsSize() - 1);

            if (msg == null) {
                msg = context.getResources().getString(R.string.elv_item_deleted);
            }
        }
        enhancedList.setUndoPopupText(msg);
    }

    /**
     * Changes the label of the undo button.
     */
    public void changeButtonLabel() {
        String msg;
        if (enhancedList.undoActionsSize() > 1 && enhancedList.getUndoStyle() == UndoStyle.COLLAPSED_POPUP) {
            msg = context.getResources().getString(R.string.elv_undo_all);
        } else {
            msg = context.getResources().getString(R.string.elv_undo);
        }
        enhancedList.setUndoButtonText(msg);
    }

    /**
     * Discard all stored undos and hide the undo popup dialog.
     * This method must be called in {@link android.app.Activity#onStop()}. Otherwise
     * {@link com.luciocossio.android.enhancedlist.Undoable#discard()} might not be called for several items, what might
     * break your data consistency.
     */
    public void discardUndo() {
        enhancedList.discardAllUndoables();
        enhancedList.dismissUndoPopup();
    }

    /**
     * Delete the list item at the specified position. This will animate the item sliding out of the
     * list and then collapsing until it vanished (same as if the user slides out an item).
     * <p/>
     * NOTE: If you are using list headers, be aware, that the position argument must take care of
     * them. Meaning 0 references the first list header. So if you want to delete the first list
     * item, you have to pass the number of list headers as {@code position}. Most of the times
     * that shouldn't be a problem, since you most probably will evaluate the position which should
     * be deleted in a way, that respects the list headers.
     *
     * @param position The position of the item in the list.
     * @throws java.lang.IndexOutOfBoundsException when trying to delete an item outside of the list range.
     * @throws java.lang.IllegalStateException     when this method is called before an {@link com.luciocossio.android.enhancedlist.OnDismissCallback}
     *                                             is set via {@link com.luciocossio.android.enhancedlist.EnhancedListView#setDismissCallback(com.luciocossio.android.enhancedlist.OnDismissCallback)}.
     */
    public void delete(int position) {
        if (!enhancedList.hasDismissCallback()) {
            throw new IllegalStateException("You must set an OnDismissCallback, before deleting items.");
        }
        if (position < 0 || position >= enhancedList.getItemsCount()) {
            throw new IndexOutOfBoundsException(String.format("Tried to delete item %d. #items in list: %d", position, enhancedList.getItemsCount()));
        }
        View childView = enhancedList.getChild(position);
        View view = null;
        if (enhancedList.hasSwipingLayout()) {
            view = childView.findViewById(enhancedList.getSwipingLayout());
        }
        if (view == null) {
            view = childView;
        }
        enhancedList.slideOutView(view, childView, position, true);
    }

    /**
     * Slide out a view to the right or left of the list. After the animation has finished, the
     * view will be dismissed by calling {@link #performDismiss(android.view.View, android.view.View, int)}.
     *
     * @param view        The view, that should be slided out.
     * @param childView   The whole view of the list item.
     * @param position    The item position of the item.
     * @param toRightSide Whether it should slide out to the right side.
     */
    public void slideOutView(final View view, final View childView, final int position, boolean toRightSide) {
        if (enhancedList.shouldPrepareAnimation(view)) {
            enhancedList.animateSlideOut(view, enhancedList.getWidth(), animationTime, toRightSide, childView, position);
        }
    }

    /**
     * Animate the dismissed list item to zero-height and fire the dismiss callback when
     * all dismissed list item animations have completed.
     *
     * @param dismissView     The view that has been slided out.
     * @param listItemView    The list item view. This is the whole view of the list item, and not just
     *                        the part, that the user swiped.
     * @param dismissPosition The position of the view inside the list.
     */
    public void performDismiss(final View dismissView, final View listItemView, final int dismissPosition) {

        final ViewGroup.LayoutParams lp = listItemView.getLayoutParams();
        final int originalLayoutHeight = lp.height;

        int originalHeight = listItemView.getHeight();
        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(animationTime);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                // Make sure no other animation is running. Remove animation from running list, that just finished
                boolean noAnimationLeft = enhancedList.removeAnimation(dismissView);

                if (noAnimationLeft) {
                    // No active animations, process all pending dismisses.

                    for (PendingDismissData dismiss : enhancedList.getPendingDismisses()) {
                        if (enhancedList.getUndoStyle() == UndoStyle.SINGLE_POPUP) {
                            enhancedList.discardAllUndoables();
                        }
                        Undoable undoable = enhancedList.onDismiss(dismiss.position);
                        if (undoable != null) {
                            enhancedList.addUndoAction(undoable);
                        }
                        enhancedList.incrementValidDelayedMsgId();
                    }

                    if (enhancedList.hasUndoActions()) {
                        changePopupText();
                        changeButtonLabel();

                        // Show undo popup
                        float yLocationOffset = context.getResources().getDimension(R.dimen.elv_undo_bottom_offset);
                        enhancedList.showUndoPopup(yLocationOffset);

                        // Queue the dismiss only if required
                        if (!enhancedList.getTouchBeforeAutoHide()) {
                            // Send a delayed message to hide popup
                            enhancedList.hidePopupMessageDelayed();
                        }
                    }

                    ViewGroup.LayoutParams lp;
                    for (PendingDismissData pendingDismiss : enhancedList.getPendingDismisses()) {
                        ViewHelper.setAlpha(pendingDismiss.view, 1f);
                        ViewHelper.setTranslationX(pendingDismiss.view, 0);
                        lp = pendingDismiss.childView.getLayoutParams();
                        lp.height = originalLayoutHeight;
                        pendingDismiss.childView.setLayoutParams(lp);
                    }

                    enhancedList.clearPendingDismissed();
                }
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                listItemView.setLayoutParams(lp);
            }
        });

        enhancedList.addPendingDismiss(new PendingDismissData(dismissPosition, dismissView, listItemView));
        animator.start();
    }


    public TouchSetup getTouchSetup() {
        return new TouchSetup(slop, minFlingVelocity, maxFlingVelocity, animationTime);
    }
}
