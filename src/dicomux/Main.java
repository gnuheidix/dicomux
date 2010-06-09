package dicomux;

import javax.swing.JOptionPane;

/**
 * Launches Dicomux<br/>
 * We determine, which model and which view shall be used.
 * @author heidi
 *
 */
public class Main {
	
	public static void main(String[] args) {
		IModel model = new Model();
		IView view = new View();
		
		try {
			@SuppressWarnings("unused")
			Controller ctrl = new Controller(model, view);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Internal error during launch.\nPlease check plug-ins.");
		}
	}
}
