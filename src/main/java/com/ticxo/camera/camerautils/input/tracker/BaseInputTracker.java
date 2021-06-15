package com.ticxo.camera.camerautils.input.tracker;

import com.ticxo.camera.camerautils.input.IInputTracker;
import com.ticxo.camera.camerautils.input.WrapperInput;

public class BaseInputTracker implements IInputTracker {

	protected WrapperInput input;

	@Override
	public void asyncInputEvent(WrapperInput input) {
		this.input = input;
	}

	@Override
	public void syncInputEvent(WrapperInput input) {
		this.input = input;
	}

	@Override
	public void asyncLeftClickEvent() {

	}

	@Override
	public void syncLeftClickEvent() {

	}

	@Override
	public void asyncRightClickEvent() {

	}

	@Override
	public void syncRightClickEvent() {

	}

	@Override
	public void asyncDropItem() {

	}

	@Override
	public void syncDropItem() {

	}

	@Override
	public void asyncSwapItem() {

	}

	@Override
	public void syncSwapItem() {

	}

	@Override
	public void asyncSwitchHeldSlot(int slot) {

	}

	@Override
	public void syncSwitchHeldSlot(int slot) {

	}

}
