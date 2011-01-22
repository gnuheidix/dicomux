package dicomux;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Vector;
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
	
	private Vector<DrawingPanel> pannels = new Vector<DrawingPanel>(12);
	private double zoomLevel;
	private int mv_cells;
	private int seconds;
	private boolean fitToPage;
	private JScrollPane scroll;
	private JPanel channelpane;
	private int numberOfChannels;
	private String displayFormat;
	private ToolPanel tools;
	private InfoPanel infoPanel;
	private int samples_per_second;
	private int data[][];
	private ChannelDefinition[] channelDefinitions;
	private boolean displayFormatChanged;
	private int displayFactorWidth;
	private int displayFactorHeight;
	
	private DrawingPanel rhythm;
	
	private final double MAX_ZOOM_OUT = 1.0f;
	private final double MAX_ZOOM_IN = 10.0f;
	private final double ZOOM_UNIT = 0.5f;
	private final double NO_ZOOM = 1.0f;
	
	private final String DEFAULTFORMAT = "1x10s";
	private final String FOURPARTS = "4x2.5s";
	private final String FOURPARTSPLUS = "4x2.5s & RS";
	private final String TWOPARTS = "2x5s";
	
	// Strings used for localization
	private String labelMinimum;
	private String labelMaximum;
	private String labelPosition;
	private String labelSecond;
	private String labelDisplayFormat;
	private String displayFormatDefault;
	private String displayFormatFourParts;
	private String displayFormatFourPartsPlus;
	private String displayFormatTwoParts;	
	
	public WaveformPlugin() throws Exception {
		super();
		m_keyTag.addKey(Tag.Modality, "ECG");
		m_keyTag.addKey(Tag.WaveformSequence, null);
		m_keyTag.addKey(Tag.WaveformData, null);
		
		this.zoomLevel = NO_ZOOM;
		this.fitToPage = true;
		this.displayFormat = DEFAULTFORMAT;
		this.displayFormatChanged = false;
		this.rhythm = null;
		
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
		this.samples_per_second = numberOfSamples / seconds;
		
		// read number of channels
		DicomElement channels = dcm.get(Tag.NumberOfWaveformChannels);
		if(channels == null)
			throw new Exception("Could not read NumberOfWaveformChannels");
			
		this.numberOfChannels = channels.getInt(true);
		
		// write the sample data into a 2-dimensional array
		// first dimension: channel
		// second dimension: samples
		
		this.data = new int[numberOfChannels][numberOfSamples];
		if(bitsAllocated.getInt(true) == 16) {
			
			boolean order = dcm.bigEndian();
			byte[] tmp_bytes = waveformData.getBytes();
			short[] tmp = toShort(tmp_bytes, order);
			
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
		this.channelDefinitions = new ChannelDefinition[numberOfChannels];
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
			if(channelSensitivityCorrection == null)
				throw new Exception("Could not read ChannelSensitivityCorrectionFactor");
			// and again we are going the long way
			tmp_value = channelSensitivityCorrection.getValueAsString(new SpecificCharacterSet("UTF-8"), 50);
			double help = Double.parseDouble(tmp_value);
			int sensitivityCorrection = (int) help;
			
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
			DrawingPanel drawPannel = new DrawingPanel(data[i], 0, channelDefinitions[i]);
			channelpane.add(drawPannel);
			channelpane.add(Box.createRigidArea(new Dimension(0,2)));
			// add panel to vector, used to refresh all panels (see repaintPanels)
			this.pannels.add(drawPannel);
		}		
		
		// in most cases we have to many channels so we use a scrollpane
		this.scroll = new JScrollPane(channelpane);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		// Panel which includes the Buttons for zooming 
		this.tools = new ToolPanel();
		this.tools.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		this.tools.setPreferredSize(new Dimension(m_content.getWidth(), 30));

		
		// Panel with information about the channel the mouse cursor is over
		this.infoPanel = new InfoPanel();
		this.infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.LINE_AXIS));
		this.infoPanel.setPreferredSize(new Dimension(m_content.getWidth(), 70));
		
		BoxLayout mlayout = new BoxLayout(m_content, BoxLayout.PAGE_AXIS);
		m_content.setLayout(mlayout);
		
		this.tools.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		m_content.add(this.tools);
		m_content.add(this.infoPanel);
		m_content.add(scroll);
		
		// this gets called when the application is resized
		m_content.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				
				repaintPanels();
			}
		});
		
		this.displayFactorWidth = 1;
		this.displayFactorHeight = this.numberOfChannels;
	}
	
	// TODO implement if necessary
	@Override
	public void setLanguage(Locale locale) {
		if(locale.getLanguage() == "de") {
			// deutsch
			this.labelMinimum = "Mimimum";
			this.labelMaximum = "Maximum";
			this.labelPosition = "Position";
			this.labelSecond = "Sekunde";
			this.labelDisplayFormat = "Anzeigeformat";
			this.displayFormatDefault = "1x10 Sekunden";
			this.displayFormatFourParts = "4x2,5 Sekunden";
			this.displayFormatFourPartsPlus = "4x2,5 Sekunden mit Rhythmusstreifen";
			this.displayFormatTwoParts = "2X5 Sekunden";	
		}
		else {
		   // englisch (default/dropback)
			this.labelMinimum = "Minimum";
			this.labelMaximum = "Maximum";
			this.labelPosition = "Position";
			this.labelSecond = "Second";
			this.labelDisplayFormat = "Display format";
			this.displayFormatDefault = "1x10 Seconds";
			this.displayFormatFourParts = "4x2.5 Seconds";
			this.displayFormatFourPartsPlus = "4x2.5 Seconds with rhythm strip";
			this.displayFormatTwoParts = "2x5 Seconds";	
		}
		
		if(this.infoPanel != null) {
			this.infoPanel.updateLanguage();
		}
		if(this.tools != null) {
			this.tools.updateLanguage();
		}
	}
	
	/**
	 * Convert an byte array to a short array
	 * 
	 * @param data				the array to convert
	 * @param isBigEndian		tell the convertion the byte order of the data 
	 *                          true if the bytes are in big endian order
	 *                          false if the bytes are in little endian order
	 * @return
	 */
	private short[] toShort(byte[] data, boolean isBigEndian) {
		
		short[] retdata = new short[data.length / 2];
		int pos = 0;
		ByteBuffer bb = ByteBuffer.allocate(2);
		if(isBigEndian)
		{
			bb.order(ByteOrder.BIG_ENDIAN);
		}
		else
		{
			bb.order(ByteOrder.LITTLE_ENDIAN);
		}
		
		for (int i = 0; i < data.length; ++i) {
			byte firstByte = data[i];
			byte secondByte = data[++i];
			
			bb.put(firstByte);
			bb.put(secondByte);
			retdata[pos] = bb.getShort(0);
			pos++;
			bb.clear();
		}
		
		return retdata;
	}
	
	
	/**
	 * Iterate over all ChannelPanels, set their size to the given Dimension and repaints them
	 */
	private void repaintPanels() {
		if(!this.pannels.isEmpty()) {
			Dimension m_content_dim = m_content.getSize();
			// we take 20 pixels for the scrollbar
			// height is divided by zoomLevel so the channels will not be too high
			double width = 0;
			double height = 0;
			double rythm_with = m_content.getWidth() - 4;

			if( this.numberOfChannels == 12 && displayFormatChanged) {
				if(displayFormat.equals(DEFAULTFORMAT)) {
					displayDefault();
					this.rhythm = null;
					displayFactorHeight = this.numberOfChannels;
					displayFactorWidth = 1;
				}
				if(displayFormat.equals(FOURPARTS)) {
					this.channelpane = displayFourParts(this.channelpane);
					this.scroll.setViewportView(this.channelpane); 
					this.rhythm = null;
					displayFactorWidth = 4;
					displayFactorHeight = 3;
				}
				if(displayFormat.equals(FOURPARTSPLUS)) {
					displayFourPartsPlus();
					displayFactorWidth = 4;
					displayFactorHeight = 4;
				}
				if(displayFormat.equals(TWOPARTS)) {
					displayTwoParts(); 
					this.rhythm = null;
					displayFactorWidth = 2;
					displayFactorHeight = 6;
				}
			}
			
			if(fitToPage)
			{
				width = (m_content.getWidth() - (displayFactorWidth * 4)) / displayFactorWidth;
				height = ((m_content_dim.getHeight() - (displayFactorHeight * 4) - (this.tools.getHeight() + this.infoPanel.getHeight())) - 10) / displayFactorHeight;
			}
			else
			{
				width = ((m_content.getWidth() - (displayFactorWidth * 4)) / displayFactorWidth) * zoomLevel;
				height = (((m_content_dim.getHeight() - (displayFactorHeight * 4) - (this.tools.getHeight() + this.infoPanel.getHeight())) - 10) / displayFactorHeight) * zoomLevel;
			}
			
			Dimension dim = new Dimension((int) width, (int)height);
			for (DrawingPanel p : this.pannels) {
				p.setPreferredSize(dim);
				p.setSize(dim);
				p.repaint();
			}
			
			if(this.rhythm != null) {
				dim = new Dimension((int) rythm_with, (int) height);
				this.rhythm.setPreferredSize(dim);
				this.rhythm.setSize(dim);
				this.rhythm.repaint();
			}
			
			this.channelpane.repaint();
			this.scroll.revalidate();
			m_content.repaint();
		}
	}
	
	private void displayDefault() {
		 
		this.channelpane = new JPanel();
		channelpane.setBackground(Color.BLACK);
		
		// using a BoxLayout, top-to-bottom  
		BoxLayout layout = new BoxLayout(channelpane, BoxLayout.PAGE_AXIS);	
		channelpane.setLayout(layout);
		
		// remove all panels, as we are about to create them again
		this.pannels.removeAllElements();
		
		// sort Leads
		int[][] temp_data = new int[this.data.length][this.data[0].length];
		ChannelDefinition[] temp_definitions = new ChannelDefinition[this.channelDefinitions.length];
		for (int i = 0; i < this.data.length; i++) {
			if(this.channelDefinitions[i].getName().equalsIgnoreCase("Lead I")) {
				temp_data[0] = this.data[i];
				temp_definitions[0] = this.channelDefinitions[i]; 
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead II")) {
				temp_data[1] = this.data[i];
				temp_definitions[1] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead III")) {
				temp_data[2] = this.data[i];
				temp_definitions[2] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead aVR")) {
				temp_data[3] = this.data[i];
				temp_definitions[3] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead aVL")) {
				temp_data[4] = this.data[i];
				temp_definitions[4] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead aVF")) {
				temp_data[5] = this.data[i];
				temp_definitions[5] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V1")) {
				temp_data[6] = this.data[i];
				temp_definitions[6] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V2")) {
				temp_data[7] = this.data[i];
				temp_definitions[7] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V3")) {
				temp_data[8] = this.data[i];
				temp_definitions[8] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V4")) {
				temp_data[9] = this.data[i];
				temp_definitions[9] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V5")) {
				temp_data[10] = this.data[i];
				temp_definitions[10] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V6")) {
				temp_data[11] = this.data[i];
				temp_definitions[11] = this.channelDefinitions[i];
			}
		}
		this.data = temp_data;
		this.channelDefinitions = temp_definitions;
		
		// creating the Panels for each channel 
		for(int i = 0; i < numberOfChannels; i++) {
			DrawingPanel drawPannel = new DrawingPanel(data[i], 0, channelDefinitions[i]);
			channelpane.add(drawPannel);
			channelpane.add(Box.createRigidArea(new Dimension(0,2)));
			// add panels to vector
			this.pannels.add(drawPannel);
		}		
		
		this.scroll.setViewportView(this.channelpane); 
	}
	
	private void displayTwoParts() {
		this.channelpane = new JPanel();
		channelpane.setBackground(Color.BLACK);
		
		GridLayout layout = new GridLayout(6, 2, 2, 2);
		channelpane.setLayout(layout);
		
		this.pannels.removeAllElements();
		// sort Leads
		int[][] temp_data = new int[this.data.length][this.data[0].length];
		ChannelDefinition[] temp_definitions = new ChannelDefinition[this.channelDefinitions.length];
		for (int i = 0; i < this.data.length; i++) {
			if(this.channelDefinitions[i].getName().equalsIgnoreCase("Lead I")) {
				temp_data[0] = this.data[i];
				temp_definitions[0] = this.channelDefinitions[i]; 
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V1")) {
				temp_data[1] = this.data[i];
				temp_definitions[1] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead II")) {
				temp_data[2] = this.data[i];
				temp_definitions[2] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V2")) {
				temp_data[3] = this.data[i];
				temp_definitions[3] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead III")) {
				temp_data[4] = this.data[i];
				temp_definitions[4] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V3")) {
				temp_data[5] = this.data[i];
				temp_definitions[5] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead aVR")) {
				temp_data[6] = this.data[i];
				temp_definitions[6] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V4")) {
				temp_data[7] = this.data[i];
				temp_definitions[7] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead aVL")) {
				temp_data[8] = this.data[i];
				temp_definitions[8] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V5")) {
				temp_data[9] = this.data[i];
				temp_definitions[9] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead aVF")) {
				temp_data[10] = this.data[i];
				temp_definitions[10] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V6")) {
				temp_data[11] = this.data[i];
				temp_definitions[11] = this.channelDefinitions[i];
			}
		}
		this.data = temp_data;
		this.channelDefinitions = temp_definitions;
		
		double start = 0;
		for(int i = 0; i < numberOfChannels; i++) {
			if(i != 0 && (i % 2) != 0)
			{
				start = 5.0;
			}
			else
			{
				start = 0;
			}
			DrawingPanel drawPannel = new DrawingPanel(data[i], start, channelDefinitions[i]);
			channelpane.add(drawPannel);
			// add panel to vector
			this.pannels.add(drawPannel);
		}
		this.scroll.setViewportView(this.channelpane); 
		
	}
	
	private JPanel displayFourParts(JPanel pane) {
		pane = new JPanel();
		pane.setBackground(Color.BLACK);
		
		GridLayout layout = new GridLayout(3, 4, 2, 2);
		pane.setLayout(layout);
		
		this.pannels.removeAllElements();
		// sort Leads
		int[][] temp_data = new int[this.data.length][this.data[0].length];
		ChannelDefinition[] temp_definitions = new ChannelDefinition[this.channelDefinitions.length];
		for (int i = 0; i < this.data.length; i++) {
			if(this.channelDefinitions[i].getName().equalsIgnoreCase("Lead I")) {
				temp_data[0] = this.data[i];
				temp_definitions[0] = this.channelDefinitions[i]; 
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead aVR")) {
				temp_data[1] = this.data[i];
				temp_definitions[1] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V1")) {
				temp_data[2] = this.data[i];
				temp_definitions[2] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V4")) {
				temp_data[3] = this.data[i];
				temp_definitions[3] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead II")) {
				temp_data[4] = this.data[i];
				temp_definitions[4] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead aVL")) {
				temp_data[5] = this.data[i];
				temp_definitions[5] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V2")) {
				temp_data[6] = this.data[i];
				temp_definitions[6] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V5")) {
				temp_data[7] = this.data[i];
				temp_definitions[7] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead III")) {
				temp_data[8] = this.data[i];
				temp_definitions[8] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead aVF")) {
				temp_data[9] = this.data[i];
				temp_definitions[9] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V3")) {
				temp_data[10] = this.data[i];
				temp_definitions[10] = this.channelDefinitions[i];
			}
			else if (this.channelDefinitions[i].getName().equalsIgnoreCase("Lead V6")) {
				temp_data[11] = this.data[i];
				temp_definitions[11] = this.channelDefinitions[i];
			}
		}
		this.data = temp_data;
		this.channelDefinitions = temp_definitions;
		
		double start = 0;
		
		for(int i = 0; i < numberOfChannels; i++) {
			
			switch(i) {
				case 0:
				case 4:
				case 8:
					start = 0;
					break;
				case 1:
				case 5:
				case 9:
					start = 2.5;
					break;
				case 2:
				case 6:
				case 10:
					start = 5.0;
					break;
				case 3:
				case 7:
				case 11:
					start = 7.5;
					break;
			}
			
			DrawingPanel drawPannel = new DrawingPanel(data[i], start, channelDefinitions[i]);
			pane.add(drawPannel);
			// add panel to vector
			this.pannels.add(drawPannel);
		}
		return pane;
	}
	
	private void displayFourPartsPlus() {		
		JPanel pane = new JPanel();
		
		pane = displayFourParts(pane);
		
		this.channelpane = new JPanel();
		this.channelpane.setLayout(new BoxLayout(channelpane, BoxLayout.PAGE_AXIS));
		this.channelpane.setBackground(Color.BLACK);
		
		int rhythm_index = 0;
		for (int i = 0; i < this.channelDefinitions.length; i++) {
			if(channelDefinitions[i].getName().equalsIgnoreCase("Lead II")) {
				rhythm_index = i;
			}
		}
		
		this.rhythm = new DrawingPanel(data[rhythm_index], 0, channelDefinitions[rhythm_index]);
		this.rhythm.setRhythm(true);
		
		this.channelpane.add(pane);
		this.channelpane.add(Box.createRigidArea(new Dimension(0,2)));
		this.channelpane.add(this.rhythm);
		
		this.scroll.setViewportView(this.channelpane); 
		
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
	
	private class InfoPanel extends JPanel {
		

		private static final long serialVersionUID = -470038831713011257L;
		// labels for the values
		private JLabel maximum;
		private JLabel minimum;
		private JLabel miliVolt;
		private JLabel seconds;
		private JLabel lead;
		// labels for the identification of the values
		private JLabel maximumLabel;
		private JLabel minimumLabel;
		private JLabel positionLabel;
		private JLabel secondsLabel;
		
		private JPanel nameMinMaxPanel;
		private JPanel positionPanel;
		
		public InfoPanel() {
			this.maximum = new JLabel();
			this.minimum = new JLabel();
			this.miliVolt = new JLabel();
			this.seconds = new JLabel();
			this.lead = new JLabel(" ");
			this.maximumLabel = new JLabel();
			this.minimumLabel = new JLabel();
			this.positionLabel = new JLabel();
			this.secondsLabel = new JLabel();
			
			this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			
			nameMinMaxPanel = new JPanel();
			nameMinMaxPanel.setPreferredSize(new Dimension(150, 70));
			nameMinMaxPanel.setMinimumSize(new Dimension(150, 70));
			nameMinMaxPanel.setMaximumSize(new Dimension(150, 70));
			
			GridBagLayout nameMinMaxlayout = new GridBagLayout();
			nameMinMaxPanel.setLayout(nameMinMaxlayout);
			
			GridBagConstraints c1 = new GridBagConstraints();
			c1.weightx = 0.5;
			c1.gridwidth = 3;
			c1.gridx = 0;
			c1.gridy = 0;
			c1.ipady = 5;
			c1.anchor = GridBagConstraints.LINE_START;
			
			nameMinMaxPanel.add(this.lead, c1);
			
			GridBagConstraints c2 = new GridBagConstraints();
			c2.weightx = 0.5;
			c2.gridwidth = 2;
			c2.gridx = 0;
			c2.gridy = 1;
			c2.ipady = 5;
			c2.anchor = GridBagConstraints.LINE_START;
			
			this.minimumLabel = new JLabel(labelMinimum + ":");
			nameMinMaxPanel.add(this.minimumLabel, c2);
			
			GridBagConstraints c3 = new GridBagConstraints();
			c3.weightx = 0.5;
			c3.gridx = 1;
			c3.gridy = 1;
			c3.ipady = 5;
			c3.anchor = GridBagConstraints.LINE_END;
			
			nameMinMaxPanel.add(this.minimum, c3);
			
			GridBagConstraints c4 = new GridBagConstraints();
			c4.weightx = 0.5;
			c4.gridwidth = 2;
			c4.gridx = 0;
			c4.gridy = 2;
			c4.ipady = 5;
			c4.anchor = GridBagConstraints.LINE_START;
			
			this.maximumLabel = new JLabel( labelMaximum + ":");
			nameMinMaxPanel.add(this.maximumLabel, c4);
			
			GridBagConstraints c5 = new GridBagConstraints();
			c5.weightx = 0.5;
			c5.gridx = 1;
			c5.gridy = 2;
			c5.ipady = 5;
			c5.anchor = GridBagConstraints.LINE_END;
			
			nameMinMaxPanel.add(this.maximum, c5);
			
			positionPanel = new JPanel();
			positionPanel.setPreferredSize(new Dimension(200, 70));
			positionPanel.setMinimumSize(new Dimension(200, 70));
			positionPanel.setMaximumSize(new Dimension(200, 70));
			GridBagLayout positionLayout = new GridBagLayout();
			positionPanel.setLayout(positionLayout);
			
			GridBagConstraints c6 = new GridBagConstraints();
			c6.weightx = 0.5;
			c6.gridwidth = 3;
			c6.gridx = 0;
			c6.gridy = 3;
			c6.ipady = 5;
			c6.anchor = GridBagConstraints.LINE_START;
			
			this.positionLabel = new JLabel(labelPosition + "");
			positionPanel.add(this.positionLabel, c6);
			
			GridBagConstraints c7 = new GridBagConstraints();
			c7.weightx = 0.5;
			c7.gridx = 0;
			c7.gridy = 4;
			c7.ipady = 5;
			c7.anchor = GridBagConstraints.LINE_START;
			
			JLabel mv_pos = new JLabel("mV:");
			positionPanel.add(mv_pos, c7);
			
			GridBagConstraints c8 = new GridBagConstraints();
			c8.weightx = 0.5;
			c8.gridx = 1;
			c8.gridy = 4;
			c8.ipady = 5;
			c8.anchor = GridBagConstraints.LINE_END;
			
			positionPanel.add(this.miliVolt, c8);
			
			GridBagConstraints c9 = new GridBagConstraints();
			c9.weightx = 0.5;
			c9.gridx = 0;
			c9.gridy = 5;
			c9.ipady = 5;
			c9.anchor = GridBagConstraints.LINE_START;
			
			this.secondsLabel = new JLabel(labelSecond + ":");
			positionPanel.add(this.secondsLabel, c9);

			GridBagConstraints c10 = new GridBagConstraints();
			c10.weightx = 0.5;
			c10.gridx = 1;
			c10.gridy = 5;
			c10.ipady = 5;
			c10.anchor = GridBagConstraints.LINE_END;
			
			positionPanel.add(this.seconds, c10);
			
			this.add(nameMinMaxPanel);
			this.add(Box.createRigidArea(new Dimension(5,0)));
			this.add(positionPanel);
		}
		
		public void setLead(String lead) {
			this.lead.setText(lead);
		}
		
		public void setMaximum(double maximum) {
			
			DecimalFormat form = new DecimalFormat("####.##");
			this.maximum.setText(form.format(maximum));
		}
		
		public void setMinimum(double minimum) {
			DecimalFormat form = new DecimalFormat("####.##");
			this.minimum.setText(form.format(minimum));
		}
		
		public void setMiliVolt(double miliVolt) {
			DecimalFormat form = new DecimalFormat("####.##");
			this.miliVolt.setText(form.format(miliVolt));
		}
		
		public void setSeconds(double seconds) {
			DecimalFormat form = new DecimalFormat("####.##");
			this.seconds.setText(form.format(seconds));
		}
		
		public void paintComponent( Graphics g ) {
			super.paintComponent(g); 
			
		    nameMinMaxPanel.setPreferredSize(new Dimension(150, 70));
		    nameMinMaxPanel.setMinimumSize(new Dimension(150, 70));
			nameMinMaxPanel.setMaximumSize(new Dimension(150, 70));
			
			positionPanel.setPreferredSize(new Dimension(200, 70));
			positionPanel.setMinimumSize(new Dimension(200, 70));
			positionPanel.setMaximumSize(new Dimension(200, 70));

		}
		
		public void updateLanguage() {
			this.minimumLabel.setText(labelMinimum + ":");
			this.maximumLabel.setText( labelMaximum + ":");
			this.positionLabel.setText(labelPosition + "");
			this.secondsLabel.setText(labelSecond + ":");
		}
		
	}
	
	/**
	 * This class handles the drawing of the waveform
	 * 
	 * @author norbert
	 *
	 */
	private class DrawingPanel extends JPanel {
		
		private static final long serialVersionUID = 856943381513072262L;
		private int[] data;
		private float scalingWidth;
		private ChannelDefinition definition;
		private int mv_cell_count;
		private int secs_cell_count;
		private double cellheight;
		private double cellwidth;
		private Dimension dim;
		private int start;
		private int end;
		private double valueScaling;
		private double offset; 
		private boolean isRhythm;
		
		public DrawingPanel(int[] values, double start, ChannelDefinition definition) {
			super();
			this.data = values;
			this.definition = definition;			
			this.mv_cell_count = mv_cells;
			this.secs_cell_count = seconds * 10;
			this.dim = getPreferredSize();
			// calculate height and width of the cells
			this.cellheight = dim.getHeight() / mv_cell_count;
			this.cellwidth = dim.getWidth() / secs_cell_count;
			this.start = (int) (start * samples_per_second);
			this.end = data.length;
			this.offset = start;
			// calculate scaling of the sample values
			this.valueScaling = this.definition.getSensitity() *
								this.definition.getSensitivityCorrection();
			
			addListeners();
			this.isRhythm = false;
		}
		
		public void setRhythm(boolean mode) {
			this.isRhythm = mode;
		}
		
		private void addListeners() {
			// used to get the current position of the mouse pointer into the information panel
			this.addMouseMotionListener( new MouseMotionAdapter() {
						
					public void mouseMoved(MouseEvent e) {						
						double sec = offset + (e.getPoint().getX() / cellwidth * 0.1);
						double mv = ((dim.getHeight() / 2.0) - e.getPoint().getY()) / cellheight * 1000;
						
						infoPanel.setSeconds(sec);
						infoPanel.setMiliVolt(mv);
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
					
					infoPanel.setLead(definition.getName());
					infoPanel.setMaximum(definition.getMaximum());
					infoPanel.setMinimum(definition.getMinimum());
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
			
			if(displayFormat.equals(DEFAULTFORMAT) || isRhythm) {
				this.secs_cell_count = seconds * 10;
				this.end = this.start + this.data.length;
			}
			else if(displayFormat.equals(FOURPARTS)) {
				this.secs_cell_count = (int) (2.5 * 10);
				this.end = this.start + (int) (2.5 * samples_per_second);
			}
			else if(displayFormat.equals(FOURPARTSPLUS)) {
				this.secs_cell_count = (int) (2.5 * 10);
				this.end = this.start + (int) (2.5 * samples_per_second);
			}
			else if(displayFormat.equals(TWOPARTS)) {
				this.secs_cell_count = 5 * 10;
				this.end = this.start + 5 * samples_per_second;
			}

			//set background color to white
			this.setBackground(Color.WHITE);
						
			this.dim = getPreferredSize();
			// calculate height and width of the cells
			this.cellheight = dim.getHeight() / this.mv_cell_count;
			this.cellwidth = dim.getWidth() / this.secs_cell_count;
			
			// calculate the scaling which is dependent to the width	
			this.scalingWidth =  (float) (cellwidth / ((this.end - this.start) / secs_cell_count ));			
			
			drawGrid(g2);
			drawGraph(g2);
			drawName(g2);
			
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
			for(int i  = this.start; i < (this.end - 1); i++) {
				int a = i;
				int b = i + 1;
				// draw a line between two points
				// dim.height / 2 is our base line
				Line2D line = new Line2D.Double(
						this.scalingWidth * (a - this.start), 
						(this.dim.height /2 - this.valueScaling * ( (float)(this.data[a] / (float) 1000) * this.cellheight) ), 
						this.scalingWidth * (b - this.start), 
						( this.dim.height /2 - this.valueScaling * ( (float)(this.data[b] / (float) 1000) * this.cellheight ) ));
				g2.draw(line);
			 }	
		}
		
		private void drawName(Graphics2D g2) {
			g2.setColor(Color.black);
			
			g2.setFont(new Font("SanSerif", Font.BOLD, 12));
		
			g2.drawString(definition.getName(), 5, 15);
			
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
		private JButton zoomOut;
		private JButton zoomIn;
		private JButton zoomFit;
		private JLabel displayLabel;
		private JComboBox displayCombo;
		private Vector<String> displayFormatsStrings;
		
		public ToolPanel() {
						
			fillVector();
			
			addZoomButtons();
			if(numberOfChannels == 12)
			{
				addDisplayFormatComponent();
			}
		}
		
		private void fillVector() {
			this.displayFormatsStrings = new Vector<String>();
			this.displayFormatsStrings.add(displayFormatDefault);
			this.displayFormatsStrings.add(displayFormatTwoParts);
			this.displayFormatsStrings.add(displayFormatFourParts);
			this.displayFormatsStrings.add(displayFormatFourPartsPlus);
		}
		
		private void addZoomButtons() {
			
			this.zoomOut = new JButton();
			this.zoomOut.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("zoomOut.png")));
			this.zoomOut.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(zoomLevel > MAX_ZOOM_OUT)
					{
						zoomLevel -= ZOOM_UNIT;
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
					if(zoomLevel < MAX_ZOOM_IN) {
						zoomLevel += ZOOM_UNIT;
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
					zoomLevel = NO_ZOOM;
					fitToPage = true;
					
					repaintPanels();
					
				}});
			this.add(zoomFit);
		}
		
		private void addDisplayFormatComponent() {
			displayLabel = new JLabel(labelDisplayFormat + ":");
			this.add(displayLabel);
				
			displayCombo = new JComboBox(displayFormatsStrings);	
			displayCombo.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					JComboBox cb = (JComboBox) e.getSource();
					String choosen = (String) cb.getSelectedItem();
					if(choosen.equals(displayFormatDefault)) {
						displayFormat = DEFAULTFORMAT;
					}
					else if(choosen.equals(displayFormatTwoParts)) {
						displayFormat = TWOPARTS;
					}
					else if(choosen.equals(displayFormatFourParts)) {
						displayFormat = FOURPARTS;
					}
					else if(choosen.equals(displayFormatFourPartsPlus)) {
						displayFormat = FOURPARTSPLUS;
					}
					displayFormatChanged = true;
					repaintPanels();
					displayFormatChanged = false;
				}
			});
			this.add(displayCombo);
	
		}
		
		public void paintComponent( Graphics g ) {
			super.paintComponent(g); 
			
		    this.setPreferredSize(new Dimension(m_content.getWidth(), 35));
		    this.setSize(new Dimension(m_content.getWidth(), 35));
		    this.setMinimumSize(new Dimension(m_content.getWidth(), 35));
			this.setMaximumSize(new Dimension(m_content.getWidth(), 35));

		}
		
		public void updateLanguage() {
			if(numberOfChannels == 12)
			{
				this.remove(this.displayLabel);
				this.remove(this.displayCombo);
				fillVector();
				addDisplayFormatComponent();
			}
		}
		
	}
	
}
