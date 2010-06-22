package dicomux;

import java.util.Locale;

/**
 * all concrete controllers have to implement this interface
 * @author heidi
 *
 */
public interface IController {
	/**
	 * closes the currently active workspace
	 */
	public void closeWorkspace();
	
	/**
	 * closes all workspaces and opens a file open dialog
	 */
	public void closeAllWorkspaces();
	
	/**
	 * opens a file open dialog
	 */
	public void openDicomFileDialog();
	
	/**
	 * opens a directory open dialog
	 */
	public void openDicomDirectoryDialog();
	
	/**
	 * opens the about information
	 */
	public void openAbout();
	
	/**
	 * closes the application
	 */
	public void closeApplication();
	
	/**
	 * opens a dicom file
	 * @param path file path of the dicom file
	 */
	public void openDicomFile(String path);
	
	/**
	 * tells the model, which workspace is currently active<br/>
	 * this call will not trigger a refresh of the view
	 * @param n id of the workspace
	 */
	public void setActiveWorkspace(int n);
	
	/**
	 * opens a dicom directory
	 * @param path
	 */
	public void openDicomDirectory(String path);
	
	/**
	 * informs the model that the language of the application has been changed
	 * @param locale 
	 */
	void setLanguage(Locale locale);
	
	/**
	 * sets the active plug-in for the active workspace
	 * @param name the name of the plug-in
	 */
	public void setActivePlugin(String name);
	
//	public void selectPlugin(int n);
}
