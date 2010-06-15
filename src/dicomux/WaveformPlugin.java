package dicomux;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;

/**
 * This plug-in is for displaying waveform ecg data in a graphical way.
 * @author norbert
 */
public class WaveformPlugin extends APlugin {
	
	private Vector<ChannelPanel> pannels = new Vector<ChannelPanel>(12);
	private double zoomLevel;
	private int mv_cells;
	private int seconds;
	private boolean fitToPage;
	private JScrollPane scroll;
	private JPanel channelpane;
	private int numberOfChannels;
	private String displayFormat;
	private int showPart;
	private JPanel tools;
	
	private final String DEFAULTFORMAT = "1x10s";
	private final String FOURPARTS = "4x2.5s";
	private final String TWOPARTS = "2x5s";
	
	public WaveformPlugin() throws Exception {
		super();
		m_keyTag.addKey(Tag.Modality, "ECG");
		m_keyTag.addKey(Tag.WaveformSequence, null);
		m_keyTag.addKey(Tag.WaveformData, null);
		
		this.zoomLevel = 3.0f;
		this.fitToPage = true;
		this.showPart = 1;
		this.displayFormat = DEFAULTFORMAT;
	}
	
	@Override
	public String getName() {
		return "Waveform ECG";
	}
	
	@Override
	public void setData(DicomObject dcm) throws Exception {
		m_content = new JPanel(new BorderLayout(5, 5));
		
		// get WaveformSequence
		DicomElement temp = dcm.get(Tag.WaveformSequence);
		if(temp == null)
			throw new Exception("Could not read WaveformSequence");
		
		dcm = temp.getDicomObject();
		
		// read the number of allocated bits per sample 
		// used to differ between general ECG and 12 Lead ECG
		DicomElement bitsAllocated = dcm.get(Tag.WaveformBitsAllocated);
		if(bitsAllocated == null)
			throw new Exception("Could not read WaveformBitsAllocated");
		
		// read waveform data which contains the samples
		DicomElement waveformData = dcm.get(Tag.WaveformData);
		if(waveformData == null)
			throw new Exception("Could not read WaveformData");
		
		// read the sampling frequency, used to calculate the seconds 
		DicomElement samplingFrequency = dcm.get(Tag.SamplingFrequency);
		if(samplingFrequency == null)
			throw new Exception("Could not read SamplingFrequency");
		
		double frequency = samplingFrequency.getDouble(true);
		
		//read number of samples per channel
		DicomElement samples = dcm.get(Tag.NumberOfWaveformSamples);
		if(samples == null)
			throw new Exception("Could not read NumberOfWaveformSamples");
			
		int numberOfSamples = samples.getInt(true);
		
		// calculate the seconds		
		this.seconds = (int) (numberOfSamples / frequency);
		
		// read number of channels
		DicomElement channels = dcm.get(Tag.NumberOfWaveformChannels);
		if(channels == null)
			throw new Exception("Could not read NumberOfWaveformChannels");
			
		this.numberOfChannels = channels.getInt(true);
		
		// write the sample data into a 2-dimensional array
		// first dimension: channel
		// second dimension: samples
		int[][] data = new int[numberOfChannels][numberOfSamples];
		if(bitsAllocated.getInt(true) == 16) {
			short[] tmp = waveformData.getShorts(true);	
			for (int i = 0; i < tmp.length; i++ ) {
				data[i%numberOfChannels][i/numberOfChannels] = (int) tmp[i];
			}
		}
		else if(bitsAllocated.getInt(true) == 8)
		{
			byte[] tmp = waveformData.getBytes();
			for (int i = 0; i < tmp.length; i++ ) {
				data[i%numberOfChannels][i/numberOfChannels] = (int) tmp[i];
			}
		}
		else
			throw new Exception("bitsAllocated is an unexpected value, value: " + bitsAllocated.getInt(true));
		
		// read the ChannelDefinitionSequence for additional info about the channels
		DicomElement channelDef = dcm.get(Tag.ChannelDefinitionSequence);
		if(channelDef == null) 
			throw new Exception("Could not read ChannelDefinitionSequence");
		
		// iterate over the definitions of the channels
		ChannelDefinition[] channelDefinitions = new ChannelDefinition[numberOfChannels];
		for(int i = 0; i < channelDef.countItems(); i++) {
			DicomObject object = channelDef.getDicomObject(i);
			
			// read ChannelSensitivity used to calculate the real sample value
			// ChannelSensitivity is the unit of each waveform sample
			DicomElement channelSensitivity = object.get(Tag.ChannelSensitivity);
			if(channelSensitivity == null)
				throw new Exception("Could not read ChannelSensitivity");
			// unfortunately had to go the complicated way and read the value as string
			String tmp_value = channelSensitivity.getValueAsString(new SpecificCharacterSet("UTF-8"), 50);
			double sensitivity = Double.parseDouble(tmp_value);
			
			// read ChannelSensitivityCorrectionFactor used to calculate the real sample value
			// ChannelSensitivityCorrectionFactor is a form of calibration of the values
			DicomElement channelSensitivityCorrection = object.get(Tag.ChannelSensitivityCorrectionFactor);
			if(channelSensitivity == null)
				throw new Exception("Could not read ChannelSensitivityCorrectionFactor");
			// and again we are going the long way
			tmp_value = channelSensitivityCorrection.getValueAsString(new SpecificCharacterSet("UTF-8"), 50);
			int sensitivityCorrection = Integer.parseInt(tmp_value);
			
			// read channel source sequence which contains the name of the channel (lead)
			DicomElement tmpElement =  object.get(Tag.ChannelSourceSequence);
			if(tmpElement == null)
				throw new Exception("Could not read ChannelSourceSequence");
			// read the DicomObject which contains to get the needed DicomEelements
			DicomObject channelSS =  tmpElement.getDicomObject();
			if(channelSS == null) 
				throw new Exception("Could not read ChannelSourceSequence DicomObject");
			// read The name of the channel
			DicomElement meaning = channelSS.get(Tag.CodeMeaning);
			if(meaning == null) 
				throw new Exception("Could not read Code Meaning");
			
			String name = meaning.getValueAsString(new SpecificCharacterSet("UTF-8"), 50);
			// safe name, sensitivity and sensitivityCorrection in a new ChannelDefinition-Object
			channelDefinitions[i] = new ChannelDefinition(name, sensitivity, sensitivityCorrection); 
		}
		
		// this panel will hold all channels and their drawings of the waveform
		this.channelpane = new JPanel();
		channelpane.setBackground(Color.BLACK);
		
		// using a BoxLayout, top-to-bottom  
		BoxLayout layout = new BoxLayout(channelpane, BoxLayout.PAGE_AXIS);	
		channelpane.setLayout(layout);
		
		//get minmax
		getMinMax(data, channelDefinitions);
		
		// creating the Panels for each channel 
		for(int i = 0; i < numberOfChannels; i++) {
			ChannelPanel chPannel = new ChannelPanel(data[i], 765, 92, seconds, channelDefinitions[i]);
			channelpane.add(chPannel);
			channelpane.add(Box.createRigidArea(new Dimension(0,2)));
			// add panel to vector, used to refresh all panels (see repaintPanels)
			this.pannels.add(chPannel);
		}		
		
		// in most cases we have to many channels so we use a scrollpane
		this.scroll = new JScrollPane(channelpane);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		scroll.getHorizontalScrollBar().addAdjustmentListener(new ScrollListener());
		scroll.getVerticalScrollBar().addAdjustmentListener(new ScrollListener());
		
		// Panel which includes the Buttons for zooming 
		this.tools = new ToolPanel();
		this.tools.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		
		m_content.add(this.tools, BorderLayout.NORTH);
		m_content.add(scroll, BorderLayout.CENTER);
		
		// this gets called when the application is resized
		m_content.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				repaintPanels();
			}
		});
		
		
	}
	
	// TODO implement if necessary
	@Override
	public void setLanguage(Locale locale) {
		
	}
	
	/**
	 * Iterate over all ChannelPanels, set their size to the given Dimension and repaints them
	 * 
	 * @param dim The new Dimension the ChannelPannels will use
	 */
	private void repaintPanels() {
		if(!this.pannels.isEmpty()) {
			Dimension m_content_dim = m_content.getSize();
			// we take 20 px for the scrollbar
			// height is divided by zoomLevel so the channels will not be too high
			double width = 0;
			int height = 0;
			if(fitToPage)
			{
				width = m_content.getWidth() - 20;
				height = (int) (m_content_dim.getHeight() / zoomLevel);
			}
			else
			{
				height = (int) (m_content_dim.getHeight() / zoomLevel);
				int cellheight = height / mv_cells;
				
				width = cellheight * seconds * 10 / zoomLevel + 140 - 20;
			}
			Dimension dim = new Dimension((int) width, height);
			for (ChannelPanel p : this.pannels) {
				p.setPreferredSize(dim);
				p.setSize(dim);
				p.repaint();
				p.revalidate();
			}
			dim = new Dimension((int) width, height * this.numberOfChannels + 2 * this.numberOfChannels);
			tools.repaint();
			tools.revalidate();
			channelpane.setPreferredSize(dim);
			channelpane.setSize(dim);
			channelpane.repaint();
			channelpane.revalidate();
			scroll.getViewport().setView(channelpane);
			m_content.repaint();
			m_content.revalidate();
		}
	}
	
	private void getMinMax(int data[][], ChannelDefinition definitions[]) {
						
			for(int i = 0; i < data.length; i++) {
				double min = 0;
				double max = 0;
				double scalingValue = definitions[i].getSensitity() * definitions[i].getSensitivityCorrection();
				for(int j = 0; j < data[i].length; j++) {
					if(min > data[i][j] * scalingValue)
					{
						min = data[i][j] * scalingValue;
					}
					if(max < data[i][j] * scalingValue)
					{
						max = data[i][j] * scalingValue;
					}
				}
				definitions[i].setMaximum(max);
				definitions[i].setMinimum(min);
			}
			
			double max = Double.MIN_VALUE;
			double min = Double.MAX_VALUE;
			for(int i = 0; i < definitions.length; i++) {
				if(min > definitions[i].getMinimum()){
					min = definitions[i].getMinimum();
				}
				if(max < definitions[i].getMaximum()) {
					max = definitions[i].getMaximum();
				}
			}
			
			double minmax = Math.max(Math.abs(max), Math.abs(min));
			int mv_cells = (int) (minmax / 1000);
			if((int) minmax % 1000 != 0) {
				++mv_cells;
			}
			mv_cells *= 2;			
			this.mv_cells = mv_cells;
	}
	
	/**
	 * This class represents the waveform channel.
	 * This panel contains the drawn graph and an info panel which shows the minimum and maximum value
	 * as well as the current position of the cursor on the graph.
	 * 
	 * @author norbert
	 *
	 */
	private class ChannelPanel extends JPanel {
		
		private static final long serialVersionUID = 2025755356632083060L;
		/**
		 *  The number of seconds of the recorded waveform
		 */
		private int secs;
		/**
		 * The height of the panel
		 */
		private double height;
		/**
		 * The width of the panel
		 */
		private double width;
		/**
		 * The waveform sample data for this channel
		 */
		private int[] data;
		/**
		 * The waveform channel definition for this channel
		 */
		private ChannelDefinition definition;
		/**
		 * the info panel
		 */
		private JPanel info;
		/**
		 * the highest sample value
		 */
		private double max;
		/**
		 * the lowest sample value
		 */
		private double min;
		/**
		 * Label for showing the y-position in the graph
		 */
		private JLabel mv_pos_label;
		/**
		 * Label for showing the x-position in the graph
		 */
		private JLabel secs_pos_label;
		/**
		 * This panel will hold the drawn graph
		 */
		private DrawingPanel graph;
		/**
		 * the width of the info panel
		 */
		private final int infowidth = 140;

		public ChannelPanel(int[] values, int width, int height, int secs, ChannelDefinition definition) {
			
			super();
			this.data = values;
			
			this.mv_pos_label = new JLabel();
			this.secs_pos_label = new JLabel();
			
			// get min and max value
			this.min = definition.getMinimum();
			this.max = definition.getMaximum();
			
			this.setPreferredSize(new Dimension(width, height));
			this.setSize(new Dimension(width, height));
			this.secs = secs;
			this.definition = definition;
						
			Dimension dim = this.getPreferredSize();
			this.height = dim.getHeight();
			this.width = dim.getWidth();
			
			BoxLayout layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
			this.setLayout(layout);
			
			// create info panel
			addInfoPane();
			// create graph
			addGraph();
		}
		
		private void addInfoPane() {
			this.info = new JPanel();
			GridBagLayout infolayout = new GridBagLayout();
			info.setLayout(infolayout);
			info.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			// set the size of the info panel
			info.setPreferredSize(new Dimension(this.infowidth,(int)this.height));
			
			GridBagConstraints c1 = new GridBagConstraints();
			c1.weightx = 0.5;
			c1.gridwidth = 3;
			c1.gridx = 0;
			c1.gridy = 0;
			c1.ipady = 5;
			c1.anchor = GridBagConstraints.LINE_START;
			
			JLabel leadname = new JLabel(this.definition.getName());
			info.add(leadname, c1);
			
			GridBagConstraints c2 = new GridBagConstraints();
			c2.weightx = 0.5;
			c2.gridwidth = 2;
			c2.gridx = 0;
			c2.gridy = 1;
			c2.ipady = 5;
			c2.anchor = GridBagConstraints.LINE_START;
			
			JLabel minimum = new JLabel("Minimum:");
			info.add(minimum, c2);
			
			GridBagConstraints c3 = new GridBagConstraints();
			c3.weightx = 0.5;
			c3.gridx = 1;
			c3.gridy = 1;
			c3.ipady = 5;
			c3.anchor = GridBagConstraints.LINE_END;
			
			DecimalFormat form = new DecimalFormat("####.##");
			JLabel minimum_value = new JLabel("" + form.format(this.min));
			info.add(minimum_value, c3);
			
			GridBagConstraints c4 = new GridBagConstraints();
			c4.weightx = 0.5;
			c4.gridwidth = 2;
			c4.gridx = 0;
			c4.gridy = 2;
			c4.ipady = 5;
			c4.anchor = GridBagConstraints.LINE_START;
			
			JLabel maximum = new JLabel("Maximum:");
			info.add(maximum, c4);
			
			GridBagConstraints c5 = new GridBagConstraints();
			c5.weightx = 0.5;
			c5.gridx = 1;
			c5.gridy = 2;
			c5.ipady = 5;
			c5.anchor = GridBagConstraints.LINE_END;
			
			JLabel maximum_value = new JLabel("" + form.format(this.max));
			info.add(maximum_value, c5);
			
			GridBagConstraints c6 = new GridBagConstraints();
			c6.weightx = 0.5;
			c6.gridwidth = 3;
			c6.gridx = 0;
			c6.gridy = 3;
			c6.ipady = 5;
			c6.anchor = GridBagConstraints.LINE_START;
			
			JLabel position = new JLabel("Position");
			info.add(position, c6);
			
			GridBagConstraints c7 = new GridBagConstraints();
			c7.weightx = 0.5;
			c7.gridx = 0;
			c7.gridy = 4;
			c7.ipady = 5;
			c7.anchor = GridBagConstraints.LINE_START;
			
			JLabel mv_pos = new JLabel("mV:");
			info.add(mv_pos, c7);
			
			GridBagConstraints c8 = new GridBagConstraints();
			c8.weightx = 0.5;
			c8.gridx = 1;
			c8.gridy = 4;
			c8.ipady = 5;
			c8.anchor = GridBagConstraints.LINE_END;
			
			info.add(this.mv_pos_label, c8);
			
			GridBagConstraints c9 = new GridBagConstraints();
			c9.weightx = 0.5;
			c9.gridx = 0;
			c9.gridy = 5;
			c9.ipady = 5;
			c9.anchor = GridBagConstraints.LINE_START;
			
			JLabel secs_pos = new JLabel("Seconds:");
			info.add(secs_pos, c9);

			GridBagConstraints c10 = new GridBagConstraints();
			c10.weightx = 0.5;
			c10.gridx = 1;
			c10.gridy = 5;
			c10.ipady = 5;
			c10.anchor = GridBagConstraints.LINE_END;
			
			info.add(this.secs_pos_label, c10);
				
			this.add(info, BorderLayout.WEST);
		}
		
		private void addGraph() {
			this.graph = new DrawingPanel(this.data,(int) (this.width - this.infowidth), (int) this.height, this.secs, this);
			Dimension dim = new Dimension((int) (this.width - this.infowidth), (int) this.height);
			graph.setPreferredSize(dim);
			
			this.add(graph, BorderLayout.EAST);
		}
		
		/**
		 * Used to repaint the panel, using the possibly changed size
		 */
		public void paintComponent( Graphics g ) {
			super.paintComponent(g);
			// get current size
			Dimension dim = this.getPreferredSize();
			this.height = dim.getHeight();
			this.width = dim.getWidth();
			// set info to new size
			dim = new Dimension(this.infowidth,(int)this.height);
			info.setPreferredSize(dim);
			info.setSize(dim);
			info.setMaximumSize(dim);
			info.repaint();
			// set graph to new size
			dim = new Dimension((int) (this.width - this.infowidth), (int) this.height);
			graph.setPreferredSize(dim);
			graph.setSize(dim);
			graph.repaint();
			this.validate();
		}
		
		/**
		 * Set the position values in the info panel
		 * 
		 * @param mv  The y-position in mV
		 * @param sec The y-position in seconds
		 */
		public void setPosition(double mv, double sec) {
		
			// format the given seconds to use only two decimal place
			DecimalFormat form = new DecimalFormat("#.##");
			this.secs_pos_label.setText(form.format(sec));
			// do the same for the mV value
			form = new DecimalFormat("####.##");
			this.mv_pos_label.setText(form.format(mv));
		}
		
		public ChannelDefinition getDefinition() {
			return this.definition;
		}
		
	}
	
	/**
	 * This class contains the drawing of the waveform
	 * 
	 * @author norbert
	 *
	 */
	private class DrawingPanel extends JPanel {
		
		private static final long serialVersionUID = 856943381513072262L;
		private int[] data;
		private float scalingWidth;
		private int secs;
		private ChannelPanel upper;
		private int mv_cell_count;
		private int secs_cell_count;
		private double cellheight;
		private double cellwidth;
		private Dimension dim;
		private int start;
		private int lenght;
		private double valueScaling;
		
		public DrawingPanel(int[] values, int width, int height, int secs, final ChannelPanel upper) {
			super();
			this.data = values;
			this.setPreferredSize(new Dimension(width, height));
			this.setSize(new Dimension(width, height));
			this.secs = secs;
			this.upper = upper;			
			this.mv_cell_count = mv_cells;
			this.secs_cell_count = this.secs * 10;
			this.dim = getPreferredSize();
			// calculate height and width of the cells
			this.cellheight = dim.getHeight() / mv_cell_count;
			this.cellwidth = dim.getWidth() / secs_cell_count;
			this.start = 0;
			this.lenght = 0; 
			
			// calculate scaling of the sample values
			this.valueScaling = this.upper.getDefinition().getSensitity() *
								this.upper.getDefinition().getSensitivityCorrection();
			
			addListeners();
			
		}
		
		private void addListeners() {
			// used to get the current position of the mouse pointer into the information panel
			this.addMouseMotionListener( new MouseMotionAdapter() {
						
					public void mouseMoved(MouseEvent e) {
						double offset = 0;
						
						if(displayFormat.equals(DEFAULTFORMAT)) {
							offset = 0.0;
						}
						if(displayFormat.equals(FOURPARTS)) {
							offset = 2.5 * (showPart - 1);
						}
						if(displayFormat.equals(TWOPARTS)) {
							offset = 5.0 * (showPart - 1);
						}
						
						double sec = offset + (e.getPoint().getX() / cellwidth * 0.1);
						double mv = ((dim.getHeight() / 2.0) - e.getPoint().getY()) / cellheight * 1000;
						
						upper.setPosition(mv, sec);
					}
				}
			);
			
			this.addMouseListener( new MouseAdapter() {
				
				public void mouseEntered(MouseEvent e) {
					Toolkit toolkit = Toolkit.getDefaultToolkit();  
					Image image = new ImageIcon(this.getClass().getClassLoader().getResource("cursorHand.png")).getImage();					
					Point hotspot = new Point(7,0);
					Cursor cursor = toolkit.createCustomCursor(image, hotspot, "dicomux"); 
					setCursor(cursor);
				}
				
				public void mouseExited(MouseEvent e) {
					Cursor normal = new Cursor(Cursor.DEFAULT_CURSOR);
					setCursor(normal);
				}
			});
		}
		
		
		public void paintComponent( Graphics g ) {
			
			super.paintComponent(g);
			final Graphics2D g2 = (Graphics2D) g;
			
			// set rendering options
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);    
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
			g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
			
			if(displayFormat.equals(DEFAULTFORMAT)) {
				this.secs_cell_count = this.secs * 10;
				this.lenght = this.data.length;
			}
			if(displayFormat.equals(FOURPARTS)) {
				this.secs_cell_count = (int) (2.5 * 10);
				this.lenght = this.data.length / 4;
			}
			if(displayFormat.equals(TWOPARTS)) {
				this.secs_cell_count = 5 * 10;
				this.lenght = this.data.length / 2;
			}
			this.start = this.lenght * (showPart - 1);

			//set background color to white
			this.setBackground(Color.WHITE);
						
			this.dim = getPreferredSize();
			// calculate height and width of the cells
			this.cellheight = dim.getHeight() / this.mv_cell_count;
			this.cellwidth = dim.getWidth() / this.secs_cell_count;
			
			// calculate the scaling which is dependent to the width	
			this.scalingWidth =  (float) (cellwidth / (lenght / secs_cell_count ));			
			
			drawGrid(g2);
			drawGraph(g2);
			
		}
		
		private void drawGrid(Graphics2D g2) {
			// set line color
			g2.setColor(new Color(231, 84, 72));
			// draw horizontal lines
			g2.setStroke(new BasicStroke(2.0f));
			for(int i = 0; i < mv_cell_count; i++) {
				g2.draw(new Line2D.Double(0, i * cellheight, 
						dim.getWidth(), i * cellheight));			
			}
			
			// draw vertical lines
			for(int i = 0; i < secs_cell_count; i++ ) {
				// draw every 10th line which represents a full second bigger 
				if(i % 10 == 0)
				{
					g2.setStroke(new BasicStroke(2.0f));
				}
				else
				{
					g2.setStroke(new BasicStroke(1.0f));
				}
				g2.draw(new Line2D.Double(i * cellwidth , 0, 
						i * cellwidth, dim.getHeight()));
			}
		}
		
		private void drawGraph(Graphics2D g2) {
			// draw waveform as line using the given values
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(1.2f));
			for(int i  = start; i < (start + lenght - 1); i++) {
				int a = i;
				int b = i + 1;
				// draw a line between two points
				// dim.height / 2 is our base line
				Line2D line = new Line2D.Double(
						this.scalingWidth * (a - start), 
						(this.dim.height /2 - this.valueScaling * ( (float)(this.data[a] / (float) 1000) * this.cellheight) ), 
						this.scalingWidth * (b - start), 
						( this.dim.height /2 - this.valueScaling * ( (float)(this.data[b] / (float) 1000) * this.cellheight ) ));
				g2.draw(line);
			 }	
		}
	}
	
	// used to save information about a channel
	private class ChannelDefinition {
		
		private String name;
		private double sensitivity;
		private int sensitivityCorrection;
		private double minimum;
		private double maximum;
					
		public ChannelDefinition(String name, double sensitity,
				int sensitivityCorrection) {
			this.name = name;
			this.sensitivity = sensitity;
			this.sensitivityCorrection = sensitivityCorrection;
			this.maximum = 0.0;
			this.minimum = 0.0;
		}
		
		public String getName() {
			return name;
		}

		public double getSensitity() {
			return sensitivity;
		}

		public int getSensitivityCorrection() {
			return sensitivityCorrection;
		}

		public double getMinimum() {
			return minimum;
		}

		public void setMinimum(double minimum) {
			this.minimum = minimum;
		}

		public double getMaximum() {
			return maximum;
		}

		public void setMaximum(double maximum) {
			this.maximum = maximum;
		}
	}
	
	private class ToolPanel extends JPanel {
		private static final long serialVersionUID = 2827148456926205919L;
		private int numberOfParts;
		private JLabel partsLabel;
		private JButton prevButton;
		private JButton nextButton;
		private JButton zoomOut;
		private JButton zoomIn;
		private JButton zoomFit;
		private JLabel displayLabel;
		private JComboBox displayCombo;
		private Vector<String> displayFormats;
		
		public ToolPanel() {
			this.numberOfParts = 1;
			
			this.displayFormats = new Vector<String>();
			this.displayFormats.add(DEFAULTFORMAT);
			this.displayFormats.add(FOURPARTS);
			this.displayFormats.add(TWOPARTS);
			
			addZoomButtons();
			addDisplayFormatComponent();
			
		}
		
		private void addZoomButtons() {
			
			this.zoomOut = new JButton();
			this.zoomOut.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("zoomOut.png")));
			this.zoomOut.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(zoomLevel < 6.0)
					{
						zoomLevel += 0.5;
					}
					fitToPage = false;
					repaintPanels();
				}});
			this.add(this.zoomOut);		
			
			this.zoomIn = new JButton();
			this.zoomIn.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("zoomIn.png")));
			this.zoomIn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(zoomLevel > 1.0) {
						zoomLevel -= 0.5;
					}
					fitToPage = false;
					repaintPanels();
				}});
			this.add(zoomIn);
			
			zoomFit = new JButton();
			zoomFit.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("fitToPage.png")));
			zoomFit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					zoomLevel = 3.0f;
					fitToPage = true;
					repaintPanels();
					
				}});
			this.add(zoomFit);
		}
		
		private void addDisplayFormatComponent() {
			displayLabel = new JLabel("display format: ");
			this.add(displayLabel);
				
			displayCombo = new JComboBox(displayFormats);	
			displayCombo.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					JComboBox cb = (JComboBox) e.getSource();
					displayFormat = (String) cb.getSelectedItem();
					showPart = 1;
					
					repaintPanels();
				}
			});
			this.add(displayCombo);
			
			this.partsLabel = new JLabel("part " + showPart + " of " + numberOfParts);
			this.prevButton = new JButton();
			prevButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("go-previous.png")));
			prevButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					--showPart;
					if(showPart < 1)
						showPart = 1;
					
					repaintPanels();
				}
			});
			
			this.nextButton = new JButton();
			nextButton.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("go-next.png")));
			nextButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					++showPart;
					if(showPart > numberOfParts)
						showPart = numberOfParts;
					
					repaintPanels();
				}
			});
			
			this.add(prevButton);
			this.add(partsLabel);
			this.add(nextButton);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
				
			if(displayFormat.equals(FOURPARTS))
			{
				this.numberOfParts = 4;
			}
			if(displayFormat.equals(TWOPARTS))
			{
				this.numberOfParts = 2;
			}
			if(displayFormat.equals(DEFAULTFORMAT)) {
				this.numberOfParts = 1;
			}
			
			this.partsLabel.setText("part " + showPart + " of " + numberOfParts);
			
		}
		
	}
	
	private class ScrollListener implements AdjustmentListener {

		@Override
		public void adjustmentValueChanged(AdjustmentEvent e) {
			repaintPanels();
		}
		
	}
	
}
