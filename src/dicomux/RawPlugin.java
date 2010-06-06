package dicomux;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.util.TagUtils;

/**
 * This plug-in is for inspecting all elements of a DicomObject in a JTree.
 * @author heidi
 *
 */
public class RawPlugin extends APlugin {
	/**
	 * the tree
	 */
	private JTree m_tree;
	
	/**
	 * the details button
	 */
	private JButton m_detailsButton;
	
	/**
	 * the panel with the CardLayout for the main content
	 */
	private JPanel m_cards;
	
	/**
	 * the action listener for m_details
	 */
	private ActionListener m_action = null;
	
	/**
	 * the text box for the detail information
	 */
	private JTextArea m_detailsText = new JTextArea();
	
	@Override
	public String getName() {
		return "Raw Data";
	}
	
	@Override
	public void setLanguage(Locale locale) {
		// TODO implement
	}
	
	@Override
	public void setData(DicomObject dcm) throws Exception{
		// create a new JTree and extract all DicomElements into it
		m_tree = new JTree(extractAllDicomElements("/", dcm));
		m_tree.setCellRenderer(new CellRendererWithButton());
		
		// disable the visibility of the root node
		m_tree.setRootVisible(false);
		
		// get a panel with CardLayout for the main content
		m_cards = new JPanel(new CardLayout(5, 5));
		
		// the content card with the JTree
		JPanel treeCard = new JPanel(new BorderLayout(5, 5));
		treeCard.add(new JScrollPane(m_tree), BorderLayout.CENTER);
		
		m_detailsButton = new JButton("Details");
		m_detailsButton.setEnabled(false);
		m_detailsButton.setVisible(true);
		treeCard.add(m_detailsButton, BorderLayout.SOUTH);
		
		m_cards.add(treeCard, "TREE");
		
		// the content card with the detail information
		JPanel detailCard = new JPanel(new BorderLayout(5, 5));
		m_detailsText.setLineWrap(true);
		detailCard.add(new JScrollPane(m_detailsText), BorderLayout.CENTER);
		
		JButton closeDetails = new JButton("Zurück");
		closeDetails.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CardLayout cl = (CardLayout)(m_cards.getLayout());
				cl.show(m_cards, "TREE");
			}
		});
		detailCard.add(closeDetails, BorderLayout.SOUTH);
		m_cards.add(detailCard, "DETAILS");
		
		m_content = new JPanel(new BorderLayout(5, 5));
		m_content.add(m_cards, BorderLayout.CENTER);
	}
	
	/**
	 * sets the ActionListener of the m_details button
	 * @param action
	 */
	private void setDetailsAction(final String data) {
		if (m_action != null) {
			m_detailsButton.removeActionListener(m_action);
		}
		
		if (data != null && !data.equals("")) {
			m_action = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					m_detailsText.setText(data);
					CardLayout cl = (CardLayout)(m_cards.getLayout());
					cl.show(m_cards, "DETAILS");
				}
			};
			m_detailsButton.addActionListener(m_action);
			m_detailsButton.setEnabled(true);
		}
		else
			m_detailsButton.setEnabled(false);
	}
	
	/**
	 * recursive function for extracting all DicomElements from an DicomObject; This function calls itself if a DicomObject is encapsulated in object
	 * @param rootElement the rootElement of the tree which will be returned
	 * @param object DicomObject which has to be extracted
	 * @return a MutableTreeNode which has the rootElement as root node and all DicomElements of object
	 */
	private MutableTreeNode extractAllDicomElements(Object rootElement, DicomObject object) throws Exception {
		// create root node
		DefaultMutableTreeNode retVal = new DefaultMutableTreeNode(new TDO(rootElement.toString(), null), true);
		
		// iterate through the DicomObject
		Iterator<DicomElement> iter = object.datasetIterator();
		while (iter.hasNext()) {
			DicomElement element = iter.next();
			
			// extract TagAddress, VR and TagName from the element
			int tagId = element.tag();
			String nodeValue = TagUtils.toString(tagId) + " [" + element.vr().toString() + "] " + object.nameOf(tagId);
			
			if (element.hasDicomObjects()) { // the DicomElement contains more DicomObjects
				// extract ItemCount from the element and create a root node which holds the name of this big DicomElement
				nodeValue += " [Items: " + element.countItems() + "]";
				DefaultMutableTreeNode multiNode = new DefaultMutableTreeNode(new TDO(nodeValue,null), true);
				
				// extract all DicomObjects of the DicomElement recursively
				for (int i = 0; i < element.countItems(); ++i)
					multiNode.add(extractAllDicomElements("Item " + i, element.getDicomObject(i)));
				
				// add the extracted tree to our main tree
				retVal.add(multiNode);
			}
			else { // the DicomElement doesn't contain more DicomObjects and maybe data
				retVal.add(new DefaultMutableTreeNode(
							new TDO(nodeValue, element.getValueAsString(
													new SpecificCharacterSet("UTF-8"), element.length())),
							false));
			}
		}
		
		return retVal;
	}
	
	/**
	 * TreeDataObject - a convenience class for the data of a tree node
	 * @author heidi
	 *
	 */
	private class TDO {
		String m_description = "";
		String m_data = null;
		
		/**
		 * @return the description
		 */
		public String getDescription() {
			return m_description;
		}
		
		/**
		 * @return the data
		 */
		public String getData() {
			return m_data;
		}
		
		/**
		 * @param description
		 * @param data
		 */
		public TDO(String description, String data) {
			super();
			this.m_description = description;
			this.m_data = data;
		}
	}
	
	private class CellRendererWithButton implements TreeCellRenderer {
		private DefaultTreeCellRenderer m_defaultRenderer = new DefaultTreeCellRenderer();
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			m_defaultRenderer.setBorderSelectionColor(Color.WHITE); // deactivate selection border
			Component stdRenderer = m_defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			JPanel myRenderer = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0)); // that's our new renderer
			
			if (value != null && value instanceof DefaultMutableTreeNode) { // type check
				Object userObject = ((DefaultMutableTreeNode)value).getUserObject(); // extraction
				
				if (userObject != null && userObject instanceof TDO) { // type check again
					TDO tdo = (TDO) userObject; // extraction again - now we build our renderer
					
					myRenderer.add(leaf ? new JLabel(m_defaultRenderer.getDefaultLeafIcon()) // add correct icon
										: new JLabel(m_defaultRenderer.getDefaultClosedIcon()));
					
					myRenderer.setBackground(selected ? m_defaultRenderer.getBackgroundSelectionColor() // set the correct background
														: m_defaultRenderer.getBackgroundNonSelectionColor());
					
					String description = tdo.getDescription(); // extract TDO
					String data = tdo.getData();
					
					if (selected)
						setDetailsAction(data);
					
					JLabel descLabel = new JLabel(); // add the description to our renderer
					descLabel.setFont(stdRenderer.getFont());
					myRenderer.add(descLabel);
					
					if (data != null) { // build the complete description depending on the data length
						int dataLength = data.length();
						if (dataLength > 0) {
							description += " [Length: " + dataLength + "] [Data: " + data + "]";
						}
					}
					descLabel.setText(description); 
					return myRenderer;
				}
			}
			
			return stdRenderer;
		}
	}
}
