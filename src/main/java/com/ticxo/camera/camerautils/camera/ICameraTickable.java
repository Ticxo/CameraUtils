package com.ticxo.camera.camerautils.camera;

public interface ICameraTickable {

	/**
	 *
	 * @return true to continue
	 */
	boolean tick();
	void onEnd();

}
