package com.luciocossio.android.enhancedlist.touch;

public class TouchSetup {
    private float slop;
    private int minFlingVelocity;
    private int maxFlingVelocity;
    private int animationTime;

    public TouchSetup(float slop, int minFlingVelocity, int maxFlingVelocity, int animationTime) {
        this.slop = slop;
        this.minFlingVelocity = minFlingVelocity;
        this.maxFlingVelocity = maxFlingVelocity;
        this.animationTime = animationTime;
    }

    public float getSlop() {
        return slop;
    }

    public int getMinFlingVelocity() {
        return minFlingVelocity;
    }

    public int getMaxFlingVelocity() {
        return maxFlingVelocity;
    }

    public int getAnimationTime() {
        return animationTime;
    }
}
