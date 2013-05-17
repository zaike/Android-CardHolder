package com.example.cardholder.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class FrontBackSwitchAnimator extends Animation {

	private Camera camera;

	// •\–ÊView
	private View frontView;
	
	// — –ÊView
	private View backView;

	private float centerX;
	
	private float centerY;
	
	private boolean forward = true;
	
	private boolean visibilitySwapped;

	public FrontBackSwitchAnimator(View frontView, View backView, int centerX, int centerY) {
		this.frontView = frontView;
		this.backView = backView;
		this.centerX = centerX;
		this.centerY = centerY;

		setDuration(500);
		setFillAfter(true);
		setInterpolator(new AccelerateDecelerateInterpolator());
	}

	public void reverse() {
		forward = false;
	}

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        camera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final double radians = Math.PI * interpolatedTime;
        float degrees = (float) (180.0 * radians / Math.PI);

        if (interpolatedTime >= 0.5f) {
            degrees -= 180.f;

            if (!visibilitySwapped) {
                frontView.setVisibility(View.GONE);
                backView.setVisibility(View.VISIBLE);
                visibilitySwapped = true;
            }
        }

        if (!forward)
            degrees = -degrees;

        final Matrix matrix = t.getMatrix();

        camera.save();
        camera.translate(0.0f, 0.0f, (float) (150.0 * Math.sin(radians)));
        camera.rotateY(degrees); // ‚±‚±‚ğrotateX()‚É‚·‚é‚ÆAc•ûŒü‚É‰ñ“]‚µ‚Ü‚·
        camera.getMatrix(matrix);
        camera.restore();

        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }
	
}
