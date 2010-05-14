package dicomux;

import java.awt.BorderLayout;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.dcm4che2.data.DicomObject;

/**
 * Each concrete plugin has to implement this interface
 * @author heidi
 *
 */
public abstract class APlugin {
	protected JComponent m_content;
	protected KeyTag m_keyTag;
	
	/**
	 * default constructor which initializes m_content with a JPanel including a BorderLayout layout manager
	 */
	public APlugin() {
		m_content = new JPanel(new BorderLayout(5, 5));
		m_keyTag = new KeyTag();
	}
	
	/**
	 * this method returns the view of the plug-in. This view is controlled by the plug-in only. It's independent from Dicomux.
	 * @return the view of the plug-in
	 */
	public JComponent getContent() {
		return m_content;
	}
	
	/**
	 * Returns an KeyTag object which holds all necessary information about the supported Tags of that plug-in.
	 * The main application can use this data in order to decide which plug-in can be used for which dicom file.
	 * @return a KeyTag object
	 * @see KeyTag
	 */
	public KeyTag getKeyTag() {
		return m_keyTag;
	}
	
	/**
	 * Sets the dicom data which is used by the plugin in order to do its work.
	 * @param dcm a DicomObject containing all data of the file
	 * @throws Exception 
	 */
	public abstract void setData(DicomObject dcm) throws Exception;
	
	/**
	 * Returns the name of the plug-in. This value is used by the controller in order to create the menu of available plug-ins.<br/>
	 * It's important to use void setLanguage(Locale) before you call this method.
	 * @return the name of the plugin
	 */
	public abstract String getName();
	
	/**
	 * Sets the used language of the plug-in. This method shall be used before calling String getName().
	 * @param locale
	 */
	public abstract void setLanguage(Locale locale);
}
