package dicomux;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

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
	
	private Dimension contend_dim = new Dimension();;
	private int pdf_page = 1;
	private JLabel page_lable;
	JTextField scaleTextField ;
	private Locale language;
	private static ResourceBundle m_languageBundle; 
	private final String m_langBaseName = "language";
	private final JScrollPane currentScroll;
	private final int prefered_scale = 76;
	private PdfDecoder pdfDecoder = new PdfDecoder(true);
	
	// for zooming with mouse
	private int mpsX,mpsY;
	private int mpeX,mpeY;
	
	private JButton zoomOut = new JButton();
	private JButton zoomIn = new JButton();
	private JButton zoomFit = new JButton();
	private JButton prevPage = new JButton();
	private JButton nextPage = new JButton();
	
	@Override
	public String getName() {
		return "Encapsulated PDF";
	}
	
	public PDFPlugin() throws Exception{
		super();
		m_keyTag.addKey(Tag.MIMETypeOfEncapsulatedDocument, "application/pdf");
		m_keyTag.addKey(Tag.EncapsulatedDocument, null);
		
		if(language == null)
		language = new Locale(System.getProperty("user.language"));
		
		m_languageBundle = ResourceBundle.getBundle(m_langBaseName,language );
		currentScroll = new JScrollPane();
		scaleTextField = new JTextField(6);
		// get a new content pane and add the PDF scrollpane to it
		m_content = new JPanel(new BorderLayout(5, 5));
		// Lable PageOf
		page_lable = new JLabel("Page " + pdf_page + " of " + pdfDecoder.getPageCount() );
	}
	
	// TODO: check for crap; add zoom and page select buttons;
	@Override
	public void setData(DicomObject dcm) throws Exception{	
		try {
			// open PDF file
			pdfDecoder.openPdfArray(dcm.get(Tag.EncapsulatedDocument).getBytes());
			// decode first page
			pdfDecoder.decodePage(pdf_page);
			// set scaling to 100%
			pdfDecoder.setPageParameters((prefered_scale/100),pdf_page);	
			scaleTextField.setText(new Integer(prefered_scale).toString() + "%");
			currentScroll.getVerticalScrollBar().setUnitIncrement(40);
			currentScroll.setViewportView(pdfDecoder);
			currentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			currentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);	
			JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
			JPanel tools_navigation = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
			currentScroll.add(pdfDecoder);
			currentScroll.setViewportView(pdfDecoder);
		
			tools.add(zoomOut);
			tools.add(zoomIn);
			tools.add(zoomFit);
			tools_navigation.add(prevPage);
			tools_navigation.add(page_lable);
			
			tools_navigation.add(nextPage);
			tools.add(scaleTextField);
			
			nextPage.setIcon(new ImageIcon(this.getClass().getResource("/go-next.png").getPath()));
			prevPage.setIcon(new ImageIcon(this.getClass().getResource("/go-previous.png").getPath()));
			zoomFit.setIcon(new ImageIcon(this.getClass().getResource("/fitToPage.png").getPath()));
			zoomIn.setIcon(new ImageIcon(this.getClass().getResource("/zoomIn.png").getPath()));
			zoomOut.setIcon(new ImageIcon(this.getClass().getResource("/zoomOut.png").getPath()));	
			
			if(pdfDecoder.getPageCount() > 1)
			tools.add(tools_navigation);
			
			m_content.add(tools, BorderLayout.NORTH);
			m_content.add(currentScroll, BorderLayout.CENTER);
			
			addToolbarListener();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	private void addToolbarListener(){
		m_content.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println("m_content size " + m_content.getSize().toString());
				System.out.println("PDF-Size "+pdfDecoder.getSize());
				super.componentResized(e);
				contend_dim = m_content.getSize();
		}});
		zoomOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				zoomOut((float)0.1,pdf_page);
		}});
		zoomIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				zoomIn((float)0.1,pdf_page);
		}});
		zoomFit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				fitToPage();
				fitToPage();
				m_content.repaint();
				currentScroll.updateUI();
		}});
		scaleTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					String scale = scaleTextField.getText();
					scale = scale.replace("%", "");
					int new_scale;
					try{
						new_scale = Integer.parseInt(scale);
					}
					catch(NumberFormatException exc){
						new_scale = prefered_scale;
					}
					setScale(new_scale);
				}
			}
			@Override
			public void keyPressed(KeyEvent e) {}
		});
		prevPage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {		
				if(pdf_page == 1){
					pdf_page = pdfDecoder.getPageCount();
				}
				else{
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
		nextPage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if(pdf_page == pdfDecoder.getPageCount()){
					pdf_page = 1;
				}
				else{
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
	}

	private void fitToPage()
	{
		PdfPageData pageData = pdfDecoder.getPdfPageData();

		System.out.println("getScaledMediaBoxHeight"+pageData.getScaledMediaBoxHeight(pdf_page));
		System.out.println("getScaledMediaBoxWidth"+pageData.getScaledMediaBoxWidth(pdf_page));
		int height = pageData.getScaledMediaBoxHeight(pdf_page);
		int width = pageData.getScaledMediaBoxWidth(pdf_page);
		
		float scale_y, scale_x;
		scale_x = ((float)pdfDecoder.getScaling() / (float)width) * (float)((float)contend_dim.width - (float)200*(float)pdfDecoder.getScaling());
		scale_y = ((float)pdfDecoder.getScaling() / (float)height) * (float)((float)contend_dim.height + (float)140*(float)pdfDecoder.getScaling());
		
		System.out.println("scaling before: " + pdfDecoder.getScaling());
		System.out.println("contend_dim.width: "+contend_dim.width);
		System.out.println("contend_dim.height: "+contend_dim.height);
		System.out.println("scale_x: " + scale_x  );
		System.out.println("scale_y: " + scale_y  );

		if(scale_x < scale_y)
		{
			pdfDecoder.setPageParameters(scale_x,pdf_page);
			scaleTextField.setText(new Integer((int) (scale_x*100)).toString() + "%");
			System.out.println("scale_x after"+ scale_x);
		}
		else
		{
			pdfDecoder.setPageParameters(scale_y,pdf_page);
			scaleTextField.setText(new Integer((int) (scale_y*100)).toString() + "%");
			System.out.println("scale_y after"+ scale_y);
		}	
	}
	
	private void zoomIn(float inc,int page)
	{
		//pdfDecoder.setSize(pdfDecoder.getWidth()+inc, pdfDecoder.getHeight()+inc);
		pdfDecoder.setPageParameters(pdfDecoder.getScaling()+inc,pdf_page);
		scaleTextField.setText(new Integer((int) (pdfDecoder.getScaling()*100)).toString() + "%");
		m_content.repaint();
		currentScroll.updateUI();
	}

	private void zoomOut(float dec,int page)
	{
		//pdfDecoder.setSize(pdfDecoder.getWidth()-dec, pdfDecoder.getHeight()-dec);
		pdfDecoder.setPageParameters(pdfDecoder.getScaling()- dec,pdf_page);
		scaleTextField.setText(new Integer((int) (pdfDecoder.getScaling()*100)).toString() + "%");
		m_content.repaint();
		currentScroll.updateUI();
	}
	
	private void setScale(int scaleInPercent)
	{
		float scale = (float)((float)scaleInPercent/(float)100);
		pdfDecoder.setPageParameters(scale,pdf_page);
		scaleTextField.setText(new Integer((int) (pdfDecoder.getScaling()*100)).toString() + "%");
		m_content.repaint();
		currentScroll.updateUI();
	}
	
	// TODO: implement
	@Override
	public void setLanguage(Locale locale) {
		language = locale;
		// set the global language for all GUI Elements (load the ResourceBundle)
	}
}
