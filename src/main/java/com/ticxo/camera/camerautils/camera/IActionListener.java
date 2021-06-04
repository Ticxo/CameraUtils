package com.ticxo.camera.camerautils.camera;

public interface IActionListener {

	void move(float side, float forward, boolean jump, boolean sneak);

	void leftClick();
	void rightClick();

	void drop();
	void swap();

	void slot(int slot);

}
