package dicomux;

import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;

/**
 * This plug-in is for inspecting all elements of a DicomObject in a JTree.
 * @author heidi
 *
 */
public class RawPlugin extends APlugin {
	private JTree m_tree;
	
	@Override
	public String getName() {
		return "RawData";
	}
	
	@Override
	public void setData(DicomObject dcm) {
		// create a new JTree and extract all DicomElements into it
		m_tree = new JTree(extractAllDicomElements("/", dcm));
		
		// disable the visibility of the root node
		m_tree.setRootVisible(false);
		
		// add the JTree to a JScrollPane, get a fresh content pane and add the scroll pane to the content pane of the plug-in
		JScrollPane scrollpane = new JScrollPane(m_tree);
		m_content = new JPanel(new BorderLayout(5, 5));
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
