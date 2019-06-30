package com.swissas.menu;

import com.intellij.ide.ui.customization.CustomizableActionGroupProvider;

/**
 * The CustomizationActionGroupProvider (right click menu)
 *
 * @author Tavan Alain
 */
public class TranslateActionGroupProvider extends CustomizableActionGroupProvider {

	@Override
	public void registerGroups(CustomizableActionGroupRegistrar registrar) {
		registrar.addCustomizableActionGroup("translation.menu", "test");
	}
}
