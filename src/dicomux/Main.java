package dicomux;

import java.io.File;

/**
 * Launches Dicomux by determining, which model, view and controller shall be used.
 * @author heidi
 *
 */
public class Main {
	public static void main(String[] args) {
		// create model and view
		IView view = new View();
		IModel model = new Model(view);
		
		// register the model on the view
		view.registerModel(model);
		
		// create a controller and register him on the view
		Controller ctrl;
		try {
			ctrl = new Controller(model, view);
			view.registerController(ctrl);
			
			// check if we have some files as argument
			if(args.length >0){
				for (String arg : args) {
					File f = new File(arg); 
					// check if it is a file
					if(f.exists() && f.isFile()){
						//check if it is a directory file
						if(arg.contains("DICOMDIR")||
								arg.contains("dicomdir") ||
									arg.contains("dir") ||
										arg.contains("DIR")){
							ctrl.openDicomDirectory(arg);
						}
						else{
							ctrl.openDicomFile(arg);
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Controller instantiation failed!");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
