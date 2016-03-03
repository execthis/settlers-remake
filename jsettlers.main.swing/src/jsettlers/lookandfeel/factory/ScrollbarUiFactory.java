package jsettlers.lookandfeel.factory;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import jsettlers.lookandfeel.ui.ScrollbarUi;

/**
 * Scrollbar UI factory
 * 
 * @author Andreas Butti
 */
public class ScrollbarUiFactory {

	/**
	 * Create PLAF
	 * 
	 * @param c
	 *            Component which need the UI
	 * @return UI
	 */
	public static ComponentUI createUI(JComponent c) {
		return new ScrollbarUi();
	}
}