package dicomux;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PagePanel;

public class PDFPlugin extends APlugin {
	/**
	 * the panel which holds the PDF
	 */
	PagePanel m_pdfPanel;
	
	/**
	 * the pdf file, we want to show to the user
	 */
	PDFFile m_pdfFile;
	
	/**
	 * holds whether the zoom mode is on or not
	 */
	boolean m_zoomActive = false;
	
	/**
	 * number of the current page
	 */
	int m_currentPageNum = 0;
	
	/**
	 * button which is used for enabeling / disabeling the zoom mode
	 */
	JToggleButton m_zoomModeToggleButton;

	/**
	 * @throws Exception 
	 * 
	 */
	public PDFPlugin() throws Exception {
		super();
		m_content = new JPanel(new BorderLayout(5, 5));
		m_keyTag.addKey(Tag.MIMETypeOfEncapsulatedDocument, "application/pdf");
		m_keyTag.addKey(Tag.EncapsulatedDocument, null);
		
		JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		tools.add(createZoomButton());
		m_content.add(tools, BorderLayout.NORTH);
		
		m_pdfPanel = new PagePanel();
		m_pdfPanel.setSize(100, 100); // dummy call to make it work
		m_content.add(m_pdfPanel, BorderLayout.CENTER);
	}
	
	/**
	 * convenience method - creates m_zoomModeToggleButton and returns it
	 * @return the new button :-)
	 */
	public JToggleButton createZoomButton() { 
		m_zoomModeToggleButton = new JToggleButton(new ImageIcon(this.getClass().getResource("/fitToPage.png").getPath()), false);
		m_zoomModeToggleButton.setSelected(false);
		m_zoomModeToggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_zoomActive = !m_zoomActive;			// invert the current state
				
				m_pdfPanel.useZoomTool(m_zoomActive);	// write the new state to the GUI
				m_zoomModeToggleButton.setSelected(m_zoomActive);
				if (!m_zoomActive)
					m_pdfPanel.setClip(null);			// reset zoom
			}
		});
		return m_zoomModeToggleButton;
	}
	
	@Override
	public String getName() {
		return "Encapsulated PDF";
	}
	
	@Override
	public void setData(DicomObject dcm) throws Exception {
		ByteBuffer buf = ByteBuffer.wrap(dcm.get(Tag.EncapsulatedDocument).getBytes());
		
		m_pdfFile = new PDFFile(buf);
		showPage(0);
	}
	
	/**
	 * convenience method - show a specific page to the user
	 * @param pageNum page number
	 */
	private void showPage(int pageNum) {
		if (m_pdfFile != null && m_pdfFile.getNumPages() > 0) {
			m_currentPageNum = pageNum;
			PDFPage page = m_pdfFile.getPage(pageNum);
			m_pdfPanel.showPage(page);
		}
	}
	
	@Override
	public void setLanguage(Locale locale) {
		// TODO Auto-generated method stub
	}
}
