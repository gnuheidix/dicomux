package dicomux;

import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class RawPlugin implements IPlugin {
	private final Tag[] m_supportedFormats = {};
	private final Tag[] m_keyFormats = {};
	private JComponent m_content;
	private JTree m_tree;
	
	public RawPlugin(Locale locale) {
		// locale is not needed in this plug-in 
		m_content = new JPanel(new BorderLayout(5, 5));
	}
	
	@Override
	public JComponent getContent() {
		return m_content;
	}
	
	@Override
	public Tag[] getKeyFormats() {
		return m_keyFormats;
	}
	
	@Override
	public String getName() {
		return "RawData";
	}
	
	@Override
	public Tag[] getSupportedFormats() {
		return m_supportedFormats;
	}
	
	@Override
	public void setData(DicomObject dcm) {
		// create a new JTree, add it to a JScrollPane and add it to the m_content pane
		m_tree = new JTree(extractAllDicomElements("/", dcm));
		
		// disable the visibility of the root node
		m_tree.setRootVisible(false);
		
		// add the JTree to a JScrollPane and add it to the content pane of the plug-in
		JScrollPane scrollpane = new JScrollPane(m_tree);
		m_content.add(scrollpane, BorderLayout.CENTER);
	}
	
	/**
	 * recursive function for extracting all DicomElements from an DicomObject; This function calls itself if a DicomObject is encapsulated in object
	 * @param rootElement the rootElement of the tree which will be returned
	 * @param object DicomObject which has to be extracted
	 * @return a MutableTreeNode which has the rootElement as root node and all DicomElements of object
	 */
	private MutableTreeNode extractAllDicomElements(Object rootElement, DicomObject object) {
		// create root node
		DefaultMutableTreeNode retVal = new DefaultMutableTreeNode(rootElement, true);
		
		// iterate through the DicomObject
		Iterator<DicomElement> iter = object.datasetIterator();
		while (iter.hasNext()) {
			DicomElement element = iter.next();
			
			if (element.hasDicomObjects()) { // the DicomElement contains more DicomObjects
				// create a root node which holds the name of this big DicomElement
				DefaultMutableTreeNode multiNode = new DefaultMutableTreeNode(element.toString(), true);
				
				// extract all DicomObjects from the DicomElement recursively
				for (int i = 0; i < element.countItems(); ++i)
					multiNode.add(extractAllDicomElements("Item " + i, element.getDicomObject(i)));
				
				// add the extracted tree to our main tree
				retVal.add(multiNode);
			}
			else { // the DicomElement doesn't contain more DicomObjects
				// simply add the DicomElement to the main tree
				retVal.add(new DefaultMutableTreeNode(element.toString(), false));
			}
		}
		
		return retVal;
	}
	
	@Override
	public void setLanguage(Locale locale) {
		// not needed in this plug-in
	}
}
