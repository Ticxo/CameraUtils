package com.ticxo.camera.camerautils.input;

public interface IInputTracker {

	void setPlayerEscaped(boolean escaped);

	void asyncInputEvent(WrapperInput input);
	void syncInputEvent(WrapperInput input);

	void asyncLeftClickEvent();
	void syncLeftClickEvent();
	void asyncRightClickEvent();
	void syncRightClickEvent();

	void asyncDropItem();
	void syncDropItem();
	void asyncSwapItem();
	void syncSwapItem();

	void asyncSwitchHeldSlot(int slot);
	void syncSwitchHeldSlot(int slot);

}
