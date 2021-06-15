package com.ticxo.camera.camerautils.input;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WrapperInput {

	private float forward;
	private float side;
	private boolean jump;
	private boolean sneak;

	public WrapperInput() {
		this(0, 0, false, false);
	}

	public WrapperInput(float forward, float side, boolean jump, boolean sneak) {
		setForward(forward);
		setSide(side);
		setJump(jump);
		setSneak(sneak);
	}

	public WrapperInput clone() {
		try {
			return (WrapperInput) super.clone();
		} catch (CloneNotSupportedException e) {
			return new WrapperInput(getForward(), getSide(), isJump(), isSneak());
		}
	}

}
