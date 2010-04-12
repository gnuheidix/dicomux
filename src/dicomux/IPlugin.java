package dicomux;

import java.util.*;
import javax.swing.JComponent;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.Tag;

/**
 * Each concrete plugin has to implement this interface
 * @author heidi
 *
 */
public interface IPlugin {
	/**
	 * this method returns the view of the plug-in. This view is controlled by the plug-in only. It's independent from Dicomux.
	 * @return the view of the plug-in
	 */
	public JComponent getContent();
	
	/**
	 * Returns a vector with supported Tags. These tags are used by dicomux to deliver the correct data
	 * @return a vector with Tags
	 */
	public Vector<Tag> getSupportedFormats();
	
	/**
	 * Sets the dicom data which is used by the plugin in order to do its work.
	 * @param map a map of Tags and DicomElements
	 */
	public void setData(Map<Tag, DicomElement> map);
	
	/**
	 * Returns the name of the plug-in. This value is used by the controller in order to create the menu of available plug-ins.<br/>
	 * It's important to use void setLanguage(Locale) before you call this method.
	 * @return the name of the plugin
	 */
	public String getName();
	
	/**
	 * Sets the used language of the plug-in. This method shall be used before calling String getName().
	 * @param locale
	 */
	public void setLanguage(Locale locale);
}
