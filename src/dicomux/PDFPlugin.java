package dicomux;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Locale;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.jpedal.PdfDecoder;

/**
 * This plug-in is for opening an encapsulated PDF in an DicomObject
 * @author heidi
 *
 */
public class PDFPlugin extends APlugin {
	protected final int[] m_keyFormats = {Tag.MIMETypeOfEncapsulatedDocument, Tag.EncapsulatedDocument};

	@Override
	public String getName() {
		return "Encapsulated PDF";
	}
	
	// TODO: check for crap; add zoom and page select buttons;
	@Override
	public void setData(DicomObject dcm) {
		PdfDecoder pdfDecoder = new PdfDecoder(true);
		
		try {
			// open PDF file
			pdfDecoder.openPdfArray(dcm.get(Tag.EncapsulatedDocument).getBytes());
			
			// decode first page
			pdfDecoder.decodePage(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// set scaling to 100%
		pdfDecoder.setPageParameters(1,1);
		
		// add to JScrollPane
		JScrollPane currentScroll = new JScrollPane();
		currentScroll.setViewportView(pdfDecoder);
		
		// get a new content pane and add the PDF scrollpane to it
		m_content = new JPanel(new BorderLayout(5, 5));
		
		m_content.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println(m_content.getSize().toString());
				super.componentResized(e);
			}
		});
		
		m_content.add(currentScroll);
	}
	
	// TODO: implement
	@Override
	public void setLanguage(Locale locale) {
		
	}
}
