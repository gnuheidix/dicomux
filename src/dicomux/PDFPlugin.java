package dicomux;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.jpedal.PdfDecoder;
import org.jpedal.objects.PdfPageData;

/**
 * This plug-in is for opening an encapsulated PDF in an DicomObject
 * @author heidi
 * @author tobi
 *
 */
public class PDFPlugin extends APlugin {
	private Dimension contend_dim;
	private int pdf_page;
	private JLabel page_lable;
	
	@Override
	public String getName() {
		return "Encapsulated PDF";
	}
	private PdfDecoder pdfDecoder;
	
	// TODO: check for crap; add zoom and page select buttons;
	@Override
	public void setData(DicomObject dcm) throws Exception{
		pdfDecoder = new PdfDecoder(true);
		contend_dim = new Dimension();
		pdf_page = 1;
		
		try {
			// open PDF file
			pdfDecoder.openPdfArray(dcm.get(Tag.EncapsulatedDocument).getBytes());
			
			//for testing
			//pdfDecoder.openPdfFile("test/multi.pdf");
			// decode first page
			pdfDecoder.decodePage(pdf_page);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// set scaling to 100%
		pdfDecoder.setPageParameters((float)0.77,pdf_page);
		
		// get a new content pane and add the PDF scrollpane to it
		m_content = new JPanel(new BorderLayout(5, 5));
		
		m_content.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println("m_content size " + m_content.getSize().toString());
				System.out.println("PDF-Size "+pdfDecoder.getSize());
				super.componentResized(e);
				contend_dim = m_content.getSize();
				fitToPage();
				
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
		
		// Zoom out
		JButton zoomOut = new JButton("-");
		zoomOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				zoomOut((float)0.1,pdf_page);
				m_content.repaint();
				currentScroll.updateUI();
			}});
		tools.add(zoomOut);

		// Zoom In
		JButton zoomIn = new JButton("+");
		zoomIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				zoomIn((float)0.1,pdf_page);
				m_content.repaint();
				currentScroll.updateUI();
			}});
		tools.add(zoomIn);
		
		// Page fit
		JButton zoomFit = new JButton("fit");
		zoomFit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				fitToPage();
				m_content.repaint();
				currentScroll.updateUI();
			}});
		tools.add(zoomFit);
		
		//prev Page
		JButton prevPage = new JButton("prev");
		prevPage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if(pdf_page == 1)
				{
					pdf_page = pdfDecoder.getPageCount();
				}
				else
				{
					pdf_page = pdf_page - 1;
				}
				
				try {
					pdfDecoder.decodePage(pdf_page);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				page_lable.setText("Page " + pdf_page + " of " + pdfDecoder.getPageCount());
				m_content.repaint();
			}});
		tools.add(prevPage);
		
		// Lable PageOf
		page_lable = new JLabel("Page " + pdf_page + " of " + pdfDecoder.getPageCount() );
		tools.add(page_lable);
		
		//next Page
		JButton nextPage = new JButton("next");
		nextPage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if(pdf_page == pdfDecoder.getPageCount())
				{
					pdf_page = 1;
				}
				else
				{
					pdf_page = pdf_page + 1;
				}
				
				try {
					pdfDecoder.decodePage(pdf_page);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				page_lable.setText("Page " + pdf_page + " of " + pdfDecoder.getPageCount());
				m_content.repaint();
			}});
		tools.add(nextPage);
		

		m_content.add(tools, BorderLayout.NORTH);
		m_content.add(currentScroll, BorderLayout.CENTER);
	}

	private void fitToPage()
	{
		PdfPageData pageData = pdfDecoder.getPdfPageData();
		
		System.out.println("getMediaBoxHeight"+pageData.getMediaBoxHeight(pdf_page));
		System.out.println("getMediaBoxWidth"+pageData.getMediaBoxWidth(pdf_page));
		int height = pageData.getScaledMediaBoxHeight(pdf_page);
		int width = pageData.getScaledMediaBoxWidth(pdf_page);
		
		float scale_y, scale_x;
		scale_x = ((pdfDecoder.getScaling() / ((float)width))) * (float)contend_dim.width;
		scale_y = ((pdfDecoder.getScaling() / (float)height)) * (float)contend_dim.height;
		System.out.println("scale_x: " + scale_x  );
		System.out.println("scale_y: " + scale_y  );

		if(scale_x <= scale_y)
		{
			pdfDecoder.setPageParameters(scale_x,pdf_page);
			System.out.println("scale_x"+ scale_x);
		}
		else
		{
			pdfDecoder.setPageParameters(scale_y,pdf_page);
			System.out.println("scale_y"+ scale_y);
		}
		
	}
	
	private void zoomIn(float inc,int page)
	{
		//pdfDecoder.setSize(pdfDecoder.getWidth()+inc, pdfDecoder.getHeight()+inc);
		pdfDecoder.setPageParameters(pdfDecoder.getScaling()+inc,pdf_page);
	}

	private void zoomOut(float dec,int page)
	{
		//pdfDecoder.setSize(pdfDecoder.getWidth()-dec, pdfDecoder.getHeight()-dec);
		pdfDecoder.setPageParameters(pdfDecoder.getScaling()- dec,pdf_page);
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
