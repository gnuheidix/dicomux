package dicomux;

import javax.swing.JComponent;

/**
 * Each concrete plugin has to implement this interface
 * @author heidi
 *
 */
public interface Plugin {

	public JComponent getContent();
}
