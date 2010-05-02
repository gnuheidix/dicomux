package dicomux;

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
		
		@SuppressWarnings("unused")
		Controller ctrl = new Controller(model, view);
	}
}
