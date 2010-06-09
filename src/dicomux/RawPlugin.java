package dicomux;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
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
	 * the tree which is for displaying all Dicom elements
	 */
	private JTree m_tree;
	
	/**
	 * the button for showing the DETAILS card
	 */
	private JButton m_detailsButton;
	
	/**
	 * the button for showing the SAVE card
	 */
	private JButton m_saveContentButton;
	
	/**
	 * the button for showing the TREE-Card 
	 */
	private JButton m_closeDetailsButton;
	
	/**
	 * the panel with the CardLayout for the main content
	 */
	private JPanel m_cards;
	
	/**
	 * the content of the currently selected Dicom element
	 */
	private byte[] m_dicomContent = null;
	
	/**
	 * the text box for the detail information
	 */
	private JTextArea m_detailsText;
	
	/**
	 * the JFileChooser for saving Dicom content
	 */
	private JFileChooser m_saveDialog;
	
	@Override
	public String getName() {
		return "Raw Data";
	}
	
	public RawPlugin() {
		// get a panel with CardLayout for the main content
		m_cards = new JPanel(new CardLayout(5, 5));
		
		// add the content card with the JTree
		m_cards.add(getInitializedTreeCard(), "TREE");
		
		// add the content card with the detail information
		m_cards.add(getInitializedDetailsCard(), "DETAILS");
		
		// add the content card with the detail information
		m_cards.add(getInitializedSaveCard(), "SAVE");
		
		// add the card pane to the content pane
		m_content = new JPanel(new BorderLayout(5, 5));
		m_content.add(m_cards, BorderLayout.CENTER);
	}
	
	/**
	 * convenience method for initialization purpose
	 * @return a JPanel with m_tree and m_detailsButton
	 */
	private JPanel getInitializedTreeCard() {
		JPanel treeCard = new JPanel(new BorderLayout(5, 5));
		m_tree = new JTree();
		m_tree.setCellRenderer(new CellRendererWithButton());
		m_tree.setRootVisible(false);
		treeCard.add(new JScrollPane(m_tree), BorderLayout.CENTER);
		
		m_detailsButton = new JButton("m_detailsButton");
		m_detailsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				m_detailsText.setText(new String(m_dicomContent)); // write the content to the JTextArea
				switchToCard("DETAILS");
			}
		});
		m_detailsButton.setEnabled(false);
		treeCard.add(m_detailsButton, BorderLayout.SOUTH);
		
		return treeCard;
	}
	
	/**
	 * convenience method for initialization purpose
	 * @return a JPanel with m_detailsText and m_closeDetailsButton
	 */
	private JPanel getInitializedDetailsCard() {
		JPanel detailsCard = new JPanel(new BorderLayout(5, 5));
		
		m_saveContentButton = new JButton("m_saveContentButton");
		m_saveContentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				switchToCard("SAVE");
			}
		});
		detailsCard.add(m_saveContentButton, BorderLayout.NORTH);
		
		m_detailsText = new JTextArea();
		m_detailsText.setLineWrap(true);
		detailsCard.add(new JScrollPane(m_detailsText), BorderLayout.CENTER);
		
		m_closeDetailsButton = new JButton("m_closeDetailsButton");
		m_closeDetailsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				switchToCard("TREE");
			}
		});
		detailsCard.add(m_closeDetailsButton, BorderLayout.SOUTH);
		
		return detailsCard;
	}
	
	/**
	 * convenience method for initialization purpose
	 * @return a JPanel with m_saveDialog
	 */
	private JPanel getInitializedSaveCard() {
		JPanel saveCard = new JPanel(new BorderLayout(5, 5));
		
		// create a new JFileChooser and configure it
		m_saveDialog = new JFileChooser();
		m_saveDialog.setDialogType(JFileChooser.SAVE_DIALOG);
		m_saveDialog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = (JFileChooser) e.getSource();
				if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) { // the user wants us to save the content
					saveDicomContent(chooser.getSelectedFile().getPath());
				}
				switchToCard("DETAILS");
			}
		});
		saveCard.add(m_saveDialog, BorderLayout.CENTER);
		
		return saveCard;
	}
	
	/**
	 * convenience funtion which saves the content of m_dicomContent into a file
	 * @param path file path where you want to save the content
	 */
	private void saveDicomContent(String path) {
		try {
			FileOutputStream out = new FileOutputStream(path);
			out.write(m_dicomContent);
			out.close();
		} catch (Exception e) {
			// TODO inform the user if something bad happened
			e.printStackTrace();
		}
	}
	
	// TODO implement with ResourceBundles
	@Override
	public void setLanguage(Locale locale) {
		if (locale != null) {
			String tmp = locale.getLanguage();
			if(tmp == "de") {
				m_detailsButton.setText("Details");
				m_closeDetailsButton.setText("Zurück");
				m_saveContentButton.setText("Inhalt in externe Datei speichern");
			}
			else if (tmp == "en") {
				m_detailsButton.setText("view details");
				m_closeDetailsButton.setText("back");
				m_saveContentButton.setText("save content in external file");
			}
			if (m_saveDialog != null)
				m_saveDialog.setLocale(locale); // this call is buggy due to JRE :-(
		}
	}
	
	@Override
	public void setData(DicomObject dcm) throws Exception{
		// create a new DefaultTableModel and extract all DicomElements of the DicomObject into it
		m_tree.setModel(new DefaultTreeModel(extractAllDicomElements("/", dcm))); 
	}
	
	/**
	 * sets the status of the m_details button<br/>
	 * looks for data in m_dicomContent
	 */
	private void setDetailsAction() {
		if (m_dicomContent != null && m_dicomContent.length > 0)
			m_detailsButton.setEnabled(true);
		else
			m_detailsButton.setEnabled(false);
	}
	
	/**
	 * convenience method - switches to a specific card in the JPanel m_cards
	 * @param cardId
	 */
	private void switchToCard(String cardId) {
		CardLayout cl = (CardLayout)(m_cards.getLayout());
		cl.show(m_cards, cardId);
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
												new SpecificCharacterSet("UTF-8"), 20), element.getBytes()),false));
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
		byte[] m_bytes = null;
		
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
		 * @return the data s byte array
		 */
		public byte[] getBytes() {
			return m_bytes;
		}
		
		/**
		 * @param description
		 * @param data
		 * @param bytes
		 */
		public TDO(String description, String data, byte[] bytes) {
			super();
			this.m_description = description;
			this.m_data = data;
			this.m_bytes = bytes;
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
	
	/**
	 * This is a convenience class for having an own TreeCellRenderer which has the feature to
	 * write the content of a DicomElement to m_detailsText. Without it we wouldn't be able to
	 * do that.
	 * @author heidi
	 *
	 */
	private class CellRendererWithButton implements TreeCellRenderer {
		private DefaultTreeCellRenderer m_defaultRenderer = new DefaultTreeCellRenderer();
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			m_defaultRenderer.setBorderSelectionColor(Color.WHITE); // deactivate selection border
			Component stdRenderer = m_defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			
			if (value != null && value instanceof DefaultMutableTreeNode) { // type check
				Object userObject = ((DefaultMutableTreeNode)value).getUserObject(); // extraction
				
				if (userObject != null && userObject instanceof TDO) { // type check again
					TDO tdo = (TDO) userObject; // extraction again - now we build our renderer
					
					if (selected) {
						m_dicomContent = tdo.getBytes();
						setDetailsAction();
					}
					
					JLabel descLabel = new JLabel();
					descLabel.setFont(stdRenderer.getFont());
					
					JPanel myRenderer = createOwnCellRenderer(leaf, selected);
					myRenderer.add(descLabel);
					
					String description = tdo.getDescription(); // extract TDO
					String data = tdo.getData();
					
					if (data != null) { // build the complete description depending on the data length
						int dataLength = tdo.getBytes().length;
						if (dataLength > 0) {
							description += " [Length: " + dataLength + "] [Data: " + data + "]";
						}
					}
					descLabel.setText(description); 
					return myRenderer;
				}
			}
			
			return stdRenderer; // use the stdRenderer if something unexpected happened
		}
		
		/**
		 * convenience method for creating an own TreeCellRenderer
		 * @param leaf is the cell a leaf?
		 * @param selected is the cell selected?
		 * @return our own TreeCellRenderer
		 */
		private JPanel createOwnCellRenderer(boolean leaf, boolean selected) {
			JPanel myRenderer = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
			
			if (leaf) // set the correct icon
				myRenderer.add(new JLabel(m_defaultRenderer.getDefaultLeafIcon()));
			else
				myRenderer.add(new JLabel(m_defaultRenderer.getDefaultClosedIcon()));
			
			if (selected) // set the correct background
				myRenderer.setBackground(m_defaultRenderer.getBackgroundSelectionColor());
			else
				myRenderer.setBackground(m_defaultRenderer.getBackgroundNonSelectionColor());
			
			return myRenderer;
		}
	}
}
