package dicomux;

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
	public void openAboutInformation();
	
	/**
	 * closes the application
	 */
	public void closeApplication();
	
	/**
	 * opens a dicom file
	 * @param name file path of the dicom file
	 */
	public void openDicomFile(String path);
	
//	public void selectPlugin(int n);
}
