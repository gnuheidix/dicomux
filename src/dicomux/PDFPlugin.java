package dicomux;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Locale;

import javax.swing.JButton;
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
	Dimension contend_dim;
	Dimension pdf_dim;
	
	@Override
	public String getName() {
		return "Encapsulated PDF";
	}
	private PdfDecoder pdfDecoder;
	
	// TODO: check for crap; add zoom and page select buttons;
	@Override
	public void setData(DicomObject dcm) throws Exception{
		pdfDecoder = new PdfDecoder(true);
		
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
		pdfDecoder.setPageParameters((float)0.77,1);
		
		// get a new content pane and add the PDF scrollpane to it
		m_content = new JPanel(new BorderLayout(5, 5));
		
		m_content.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println("m_content size " + m_content.getSize().toString());
				System.out.println("PDF-Size "+pdfDecoder.getSize());
				super.componentResized(e);
				
				contend_dim = m_content.getSize();
				pdf_dim = pdfDecoder.getSize();
				
			}
		});
		
		
		// add to JScrollPane
		final JScrollPane currentScroll = new JScrollPane();
		currentScroll.setViewportView(pdfDecoder);
//		
		currentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		currentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//		//currentScroll.setSize(200, 200);
//		
		JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
//		
//		pdfDecoder.setSize(m_content.getSize());
		currentScroll.add(pdfDecoder);
		currentScroll.setViewportView(pdfDecoder);
		
		// get a new content pane and add the PDF scrollpane to it
		//JPanel content = new JPanel(new BorderLayout(5, 5));

		//add zoom buttons
		JButton zoomOut = new JButton("-");
		zoomOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				zoomOut((float)0.1,1);
				m_content.repaint();
				currentScroll.updateUI();
			}});
		tools.add(zoomOut);

		JButton zoomIn = new JButton("+");
		zoomIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				zoomIn((float)0.1,1);
				m_content.repaint();
				currentScroll.updateUI();
			}});
		tools.add(zoomIn);
		
		JButton zoomFit = new JButton("fit");
		zoomFit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				float scale_y, scale_x;
				scale_x = ((pdfDecoder.getScaling() / ((float)pdf_dim.width + 20))) * (float)contend_dim.width;
				scale_y = ((pdfDecoder.getScaling() / (float)pdf_dim.height)) * (float)contend_dim.height;
				System.out.println("scale_x: " + scale_x  );
				pdfDecoder.setPageParameters(scale_x,1);
				if(scale_x <= scale_y)
				{
					pdfDecoder.setPageParameters(scale_x,1);
					System.out.println("scale_x"+ scale_x);
					
				}
				else
				{
					pdfDecoder.setPageParameters(scale_y,1);
					System.out.println("scale_y"+ scale_y);
				}
				
				System.out.println("scaleX"+scale_x+"scaleY"+scale_y);
				
				m_content.repaint();
				currentScroll.updateUI();
			}});
		tools.add(zoomFit);

		m_content.add(tools, BorderLayout.NORTH);
		m_content.add(currentScroll, BorderLayout.CENTER);
	}

	private void zoomIn(float inc,int page)
	{
		//pdfDecoder.setSize(pdfDecoder.getWidth()+inc, pdfDecoder.getHeight()+inc);
		pdfDecoder.setPageParameters(pdfDecoder.getScaling()+inc,1);
	}

	private void zoomOut(float dec,int page)
	{
		//pdfDecoder.setSize(pdfDecoder.getWidth()-dec, pdfDecoder.getHeight()-dec);
		pdfDecoder.setPageParameters(pdfDecoder.getScaling()- dec,1);
	}
	
	// TODO: implement
	@Override
	public void setLanguage(Locale locale) {
		
	}
	
	@Override
	public int[] getKeyTags() {
		final int[] keyFormats = {Tag.MIMETypeOfEncapsulatedDocument, Tag.EncapsulatedDocument};
		return keyFormats;
	}
}
