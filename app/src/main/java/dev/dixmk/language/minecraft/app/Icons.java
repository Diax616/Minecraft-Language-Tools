package dev.dixmk.language.minecraft.app;

import javafx.scene.image.Image;

public final class Icons {
	private Icons() {
		throw new AssertionError("Utility class should not be instantiated");
	}
    public static final Image WARDEN_WOMAN = new Image(Icons.class.getResourceAsStream("icons/ww.png"));
}
