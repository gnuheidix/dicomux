package dicomux;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PagePanel;

/**
 * This plug-in is for opening an encapsulated PDF in an DicomObject
 * @author heidi
 *
 */
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
	 * the scrollPane for the pdf page
	 */
	JScrollPane m_scroll;
	
	/**
	 * holds whether the zoom mode is on or not
	 */
	boolean m_zoomActive = false;
	
	/**
	 * number of the current page
	 */
	int m_currentPageNum = 0;
	
	/**
	 * the prefered pdf scale
	 */
	int m_preferedScale = 100;
	/**
	 * the current pdf scale
	 */
	int m_currentScale = 100;
	
	/**
	 * button which is used for enabeling / disabeling the zoom-part mode
	 */
	JToggleButton m_zoomPartModeToggleButton;
	/**
	 * button which is used for enabeling / disabeling the zoom-in mode
	 */
	JToggleButton m_zoomInModeToggleButton;
	/**
	 * button which is used for enabeling / disabeling the zoom-out mode
	 */
	JToggleButton m_zoomOutModeToggleButton;
	/**
	 * button which is used for enabeling / disabeling the next-page mode
	 */
	JToggleButton m_nextPageModeToggleButton;
	/**
	 * button which is used for enabeling / disabeling the prev-page mode
	 */
	JToggleButton m_prevPageModeToggleButton;
	/**
	 * TextFile which is used for scaling mode
	 */
	JTextField m_scaleModeTextField;
	/**
	 * Lable which is used for Page of
	 */
	JLabel m_pageOfLable;
	
	/**
	 * the Language
	 */
	Locale m_locale;

	/**
	 * page of language Strings
	 */
	String page = "Page " ,of = " of ";
	
	/**
	 * @throws Exception 
	 * 
	 */
	public PDFPlugin() throws Exception {
		super();
		m_keyTag.addKey(Tag.MIMETypeOfEncapsulatedDocument, "application/pdf");
		m_keyTag.addKey(Tag.EncapsulatedDocument, null);
		m_content = new JPanel(new BorderLayout(5, 5));
		m_content.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				if (m_zoomPartModeToggleButton != null &&		// reset zoom on resize
						m_zoomPartModeToggleButton.getModel().isSelected()) {
					m_zoomPartModeToggleButton.doClick();
				}
			}
		});
		
		m_pdfPanel = new PagePanel();
		m_pdfPanel.setSize(300, 300); // dummy call to make it work
		
//		m_scroll = new JScrollPane();
//		m_scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		m_scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//		m_scroll.setSize(100,100);
//		m_scroll.add(m_pdfPanel);
//		m_scroll.setViewportView(m_pdfPanel);
		
		m_content.add(m_pdfPanel, BorderLayout.CENTER);
		
		m_locale =  new Locale(System.getProperty("user.language"));
	}
	/**
	 * convenience method - creates m_zoomModeToggleButton and returns it
	 * @return the new button :-)
	 */
	public void addTools(){
		JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		//add tools
		tools.add(createZoomPartButton());
//		tools.add(createZoomInButton());
//		tools.add(createZoomOutButton());
//		tools.add(createScalePageTextField());	
		if(m_pdfFile != null && m_pdfFile.getNumPages() > 1){
			tools.add(createPrevPageButton());
			tools.add(createPageOfLable());
			tools.add(createNextPageButton());
		}
		tools.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
		m_content.add(tools, BorderLayout.NORTH);
	}
	
	/**
	 * convenience method - creates m_zoomModeToggleButton and returns it
	 * @return the new button :-)
	 */
	public JToggleButton createZoomPartButton() { 
		m_zoomPartModeToggleButton = new JToggleButton(new ImageIcon(this.getClass().getClassLoader().getResource("zoomPart.png")), false);
		m_zoomPartModeToggleButton.setSelected(false);
		m_zoomPartModeToggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_zoomActive = !m_zoomActive;			// invert the current state			
				m_pdfPanel.useZoomTool(m_zoomActive);	// write the new state to the GUI
				m_zoomPartModeToggleButton.setSelected(m_zoomActive);
				
				if (!m_zoomActive){
					m_pdfPanel.setClip(null);			// reset zoom
					m_pdfPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				else{
					m_pdfPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				}
			}
		});
		m_zoomPartModeToggleButton.addMouseListener(new MouseListener() {	
			@Override
			public void mouseReleased(MouseEvent arg0) {	
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
			}	
			@Override
			public void mouseExited(MouseEvent arg0) {
				if (!m_zoomActive)
					m_zoomPartModeToggleButton.setSelected(false);
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {	
				m_zoomPartModeToggleButton.setSelected(true);
			}
			@Override
			public void mouseClicked(MouseEvent arg0) {	
			}
		});
		return m_zoomPartModeToggleButton;
	}
	
	/**
	 * convenience method - creates m_zoomInModeToggleButton and returns it
	 * @return the new button :-)
	 */
	public JToggleButton createZoomInButton() { 
		m_zoomInModeToggleButton = new JToggleButton(new ImageIcon(this.getClass().getClassLoader().getResource("zoomIn.png")), false);
		m_zoomInModeToggleButton.setSelected(false);
		m_zoomInModeToggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//do zoom
				setScale(m_currentScale - 10);
			}
		});
		m_zoomInModeToggleButton.addMouseListener(new MouseListener() {	
			@Override
			public void mouseReleased(MouseEvent arg0) {	
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
			}	
			@Override
			public void mouseExited(MouseEvent arg0) {
				m_zoomInModeToggleButton.setSelected(false);
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {	
				m_zoomInModeToggleButton.setSelected(true);
			}
			@Override
			public void mouseClicked(MouseEvent arg0) {	
			}
		});
		return m_zoomInModeToggleButton;
	}
	
	/**
	 * convenience method - creates m_zoomOutModeToggleButton and returns it
	 * @return the new button :-)
	 */
	public JToggleButton createZoomOutButton() { 
		m_zoomOutModeToggleButton = new JToggleButton(new ImageIcon(this.getClass().getClassLoader().getResource("zoomOut.png")), false);
		m_zoomOutModeToggleButton.setSelected(false);
		m_zoomOutModeToggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//do zoom
				setScale(m_currentScale + 10);
			}
		});
		m_zoomOutModeToggleButton.addMouseListener(new MouseListener() {	
			@Override
			public void mouseReleased(MouseEvent arg0) {	
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
			}	
			@Override
			public void mouseExited(MouseEvent arg0) {
				m_zoomOutModeToggleButton.setSelected(false);
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {	
				m_zoomOutModeToggleButton.setSelected(true);
			}
			@Override
			public void mouseClicked(MouseEvent arg0) {	
			}
		});
		return m_zoomOutModeToggleButton;
	}
	
	/**
	 * convenience method - creates m_prevPageModeToggleButton and returns it
	 * @return the new button :-)
	 */
	public JToggleButton createPrevPageButton() { 
		m_prevPageModeToggleButton = new JToggleButton(new ImageIcon(this.getClass().getClassLoader().getResource("go-previous.png")), false);
		m_prevPageModeToggleButton.setSelected(false);
		m_prevPageModeToggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//do prev
				if(m_pdfPanel.getPage().getPageNumber() - 1 < 1)
					showPage(m_pdfFile.getNumPages());
				else
					showPage(m_pdfPanel.getPage().getPageNumber()-1);
				
			}
		});
		m_prevPageModeToggleButton.addMouseListener(new MouseListener() {	
			@Override
			public void mouseReleased(MouseEvent arg0) {	
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
			}	
			@Override
			public void mouseExited(MouseEvent arg0) {
				m_prevPageModeToggleButton.setSelected(false);
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {	
				m_prevPageModeToggleButton.setSelected(true);
			}
			@Override
			public void mouseClicked(MouseEvent arg0) {	
			}
		});
		return m_prevPageModeToggleButton;
	}
	
	/**
	 * convenience method - creates m_nextPageModeToggleButton and returns it
	 * @return the new button :-)
	 */
	public JToggleButton createNextPageButton() { 
		m_nextPageModeToggleButton = new JToggleButton(new ImageIcon(this.getClass().getClassLoader().getResource("go-next.png")), false);
		m_nextPageModeToggleButton.setSelected(false);
		m_nextPageModeToggleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//do next
				if(m_pdfPanel.getPage().getPageNumber()+1 > m_pdfFile.getNumPages() )
					showPage(1);
				else
					showPage(m_pdfPanel.getPage().getPageNumber()+1);
			}
		});
		m_nextPageModeToggleButton.addMouseListener(new MouseListener() {	
			@Override
			public void mouseReleased(MouseEvent arg0) {	
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
			}	
			@Override
			public void mouseExited(MouseEvent arg0) {
				m_nextPageModeToggleButton.setSelected(false);
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {	
				m_nextPageModeToggleButton.setSelected(true);
			}
			@Override
			public void mouseClicked(MouseEvent arg0) {	
			}
		});
		return m_nextPageModeToggleButton;
	}
	
	/**
	 * convenience method - creates m_scaleModeTextField and returns it
	 * @return the new button :-)
	 */
	public JTextField createScalePageTextField() { 
		m_scaleModeTextField = new JTextField(6);
		m_scaleModeTextField.setText(m_preferedScale + "%");
		m_scaleModeTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {	
			}
			@Override
			public void keyReleased(KeyEvent arg0) {	
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER)
				{
					String scale = m_scaleModeTextField.getText();
					scale = scale.replace("%", "");
					int new_scale;
					try{
						new_scale = Integer.parseInt(scale);
					}
					catch(NumberFormatException exc){
						new_scale = m_preferedScale;
					}
					setScale(new_scale);
				}
			}
			@Override
			public void keyTyped(KeyEvent arg0) {
			}
		});
		return m_scaleModeTextField;
	}
	
	/**
	 * convenience method - creates m_pageOfLable and returns it
	 * @return the new button :-)
	 */
	public JLabel createPageOfLable() { 
		m_pageOfLable = new JLabel(page + of);
		return m_pageOfLable;
	}
	public void updatePageOfLable(){
		if(m_pageOfLable != null){
			if(m_pdfPanel != null && m_pdfFile != null)
				m_pageOfLable.setText(page + (m_pdfPanel.getPage().getPageNumber() ) + of + m_pdfFile.getNumPages());
			else
				m_pageOfLable.setText(page + of);
		}
	}
	
	@Override
	public String getName() {
		return "Encapsulated PDF";
	}
	
	@Override
	public void setData(DicomObject dcm) throws Exception {
		if (dcm != null) {
			DicomElement element = dcm.get(Tag.EncapsulatedDocument);
			if (element != null) {
				ByteBuffer buf = ByteBuffer.wrap(element.getBytes());
				
				m_pdfFile = new PDFFile(buf);
				addTools();
				showPage(1);
			}
		}
		//test
//		File file = new File("C:\\Users\\tobi\\Desktop\\gpl-3.0.pdf");
//		FileInputStream fin = new FileInputStream(file);
//		byte fileContent[] = new byte[(int)file.length()];
//		fin.read(fileContent);
//		m_pdfFile = new PDFFile(ByteBuffer.wrap(fileContent));
//		addTools();
//		showPage(1);
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
			m_pdfPanel.setSize(300,300);
			updatePageOfLable();
		}
	}
	
	/**
	 * convenience method - scale the page with the scale scaleInPercent
	 * @param scaleInPercent the scale in percent
	 */
	private void setScale(int scaleInPercent)
	{

		int less_scale_Site;
		Rectangle2D rect = null;
		double height, width;//, x , y;
		if(m_pdfPanel.getCurClip() == null){
			height = m_pdfPanel.getPage().getHeight();
			width = m_pdfPanel.getPage().getWidth();
//			x = 0;
//			y = 0;
			if(scaleInPercent < m_currentScale){
				less_scale_Site = (m_currentScale - scaleInPercent)/2;
				double less_px_height =  ((height/100)*less_scale_Site);
				double less_px_width =  ((width/100)*less_scale_Site);
				double new_wi = width-(7*less_px_width);
				double new_hi = height-(7*less_px_width);
				System.out.println("height:"+height);
				System.out.println("width:"+width);
				System.out.println("new height:"+new_hi);
				System.out.println("new width::"+new_wi);
				rect = new Rectangle2D.Double(less_px_width,less_px_height,new_wi,new_hi);
			}
 		}
//		else{
//			height = m_pdfPanel.getCurClip().getHeight();
//			width = m_pdfPanel.getCurClip().getWidth();
//			x = m_pdfPanel.getCurClip().getX();
//			y = m_pdfPanel.getCurClip().getY();
//		}
//		//int y1 = scaleInPercent > m_currentScale ? 1 : -1;
//		if(scaleInPercent > m_currentScale){
//			less_scale_Site = (-(m_currentScale - scaleInPercent))/2;
//			double less_px_height =  ((height/100)*less_scale_Site);
//			double less_px_width =  ((width/100)*less_scale_Site);
//
//			rect = new Rectangle2D.Double(x - less_px_width,y - less_px_height,
//										width+(2*less_px_width),height+(2*less_px_width));
//		}

	
		m_pdfPanel.setClip(rect);
		m_currentScale = scaleInPercent;
		m_content.repaint();
		m_scroll.updateUI();
	}
	
	@Override
	public void setLanguage(Locale locale) {
		m_locale = locale;
		
		if(m_locale.getLanguage() == "de"){
			page = "Seite "; of = " von ";}
		else{
			page = "Page "; of = " of ";}
		
		updatePageOfLable();
	}
}
