package dicomux;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Line2D;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;

/**
 * This plug-in is for displaying waveform ecg data in a graphical way.
 * @author heidi
 * @author norbert
 */
public class WaveformPlugin extends APlugin {
	
	private Vector<ChannelPanel> pannels = new Vector<ChannelPanel>(12);
	
	
	@Override
	public int[] getKeyTags() {
		final int[] keyTags = {Tag.WaveformSequence};
		return keyTags;
	}
	
	@Override
	public String getName() {
		return "Waveform ECG";
	}
	
	// implement
	@Override
	public void setData(DicomObject dcm) throws Exception {
		m_content = new JPanel(new BorderLayout(5, 5));
		
		// get WaveformSequence
		DicomElement temp = dcm.get(Tag.WaveformSequence);
		if(temp == null)
			throw new Exception("Error: could not read WaveformSequence");
		
		dcm = temp.getDicomObject();
		
		// read the number of allocated bits per sample 
		// used to differ between general ECG and 12 Lead ECG
		DicomElement bitsAllocated = dcm.get(Tag.WaveformBitsAllocated);
		if(bitsAllocated == null)
			throw new Exception("Error: could not read WaveformBitsAllocated");
		
		// read waveform data which contains the samples
		DicomElement waveformData = dcm.get(Tag.WaveformData);
		if(waveformData == null)
			throw new Exception("Error: could not read WaveformData");
		
		DicomElement samplingFrequency = dcm.get(Tag.SamplingFrequency);
		if(samplingFrequency == null)
			throw new Exception("Error: could not read SamplingFrequency");
		
		double frequency = samplingFrequency.getDouble(true);
		
		//read number of samples per channel
		DicomElement samples = dcm.get(Tag.NumberOfWaveformSamples);
		if(samples == null)
			throw new Exception("Error: could not read NumberOfWaveformSamples");
			
		int numberOfSamples = samples.getInt(true);
		
		int seconds = (int) (numberOfSamples / frequency);
		
		// read number of channels
		DicomElement channels = dcm.get(Tag.NumberOfWaveformChannels);
		if(channels == null)
			throw new Exception("Error: could not read NumberOfWaveformChannels");
			
		int numberOfChannels = channels.getInt(true);
		
		int[][] data = new int[numberOfChannels][numberOfSamples];
		if(bitsAllocated.getInt(true) == 16) {
			short[] tmp = waveformData.getShorts(true);	
			for (int i = 0; i < tmp.length; i++ ) {
				data[i%12][i/12] = (int) tmp[i];
			}
		}
		else if(bitsAllocated.getInt(true) == 8)
		{
			byte[] tmp = waveformData.getBytes();
			for (int i = 0; i < tmp.length; i++ ) {
				data[i%12][i/12] = (int) tmp[i];
			}
		}
		else
			throw new Exception("Error: bitsAllocated is an unexpected value, value: " + bitsAllocated.getInt(true));
		
		DicomElement channelDef = dcm.get(Tag.ChannelDefinitionSequence);
		if(channelDef == null) 
			throw new Exception("Error: could not read ChannelDefinitionSequence");
		
		
		String[] leads = new String[numberOfChannels];  
		for(int i = 0; i < channelDef.countItems(); i++) {
			DicomObject object = channelDef.getDicomObject(i);
			DicomElement tmpElement =  object.get(Tag.ChannelSourceSequence);
			
			if(tmpElement == null)
				throw new Exception("Error: could not read ChannelSourceSequence");
			
			DicomObject channelSS =  tmpElement.getDicomObject();
			if(channelSS == null) 
				throw new Exception("Error: could not read ChannelSourceSequence DicomObject");
			
			DicomElement meaning = channelSS.get(Tag.CodeMeaning);
			if(meaning == null) 
				throw new Exception("Error: could not read Code Meaning");
			
			String lead = meaning.getValueAsString(new SpecificCharacterSet("UTF-8"), 50);
			leads[i] = lead;	
		}
		
		
		
		JPanel channelpane = new JPanel();
		channelpane.setBackground(Color.BLACK);
		BoxLayout layout = new BoxLayout(channelpane, BoxLayout.PAGE_AXIS);
		
		channelpane.setLayout(layout);
		
		for(int i = 0; i < numberOfChannels; i++) {
			ChannelPanel chPannel = new ChannelPanel(data[i], 765, 92, seconds, leads[i]);
			channelpane.add(chPannel);
			channelpane.add(Box.createRigidArea(new Dimension(0,2)));
			// add panel to vector
			this.pannels.add(chPannel);
		}
		
		JScrollPane scroll = new JScrollPane(channelpane);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		m_content.add(scroll, BorderLayout.CENTER);
		
		m_content.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				Dimension m_content_dim = m_content.getSize();
				Dimension dim = new Dimension(m_content.getWidth() - 20, (int) (m_content_dim.getHeight() / 4));
				repaintPanels(dim);
			}
		});
		
		
	}
	
	// TODO implement if necessary
	@Override
	public void setLanguage(Locale locale) {
		
	}
	
	private void repaintPanels(Dimension dim) {
		if(!this.pannels.isEmpty()) {
			for (ChannelPanel p : this.pannels) {
				p.setPreferredSize(dim);
				p.setSize(dim);
				p.repaint();
			}
		}
	}
	
	private class ChannelPanel extends JPanel {
		
		private static final long serialVersionUID = 2025755356632083060L;
		private int secs;
		private double height;
		private double width;
		private int[] data;
		private String lead;
		private JPanel info;
		private int max;
		private int min;
		private DrawingPanel graph;
		private final int infowidth = 120;

		public ChannelPanel(int[] values, int width, int height, int secs, String lead) {
			this.data = values;
			
			this.min = this.data[0];
			this.max = this.data[0];
			
			for(int i = 0; i < this.data.length; i++ ) {
				if(this.min > this.data[i])
					this.min = this.data[i];
				if(this.max < this.data[i])
					this.max = this.data[i];
			}
			
			this.setPreferredSize(new Dimension(width, height));
			this.setSize(new Dimension(width, height));
			this.secs = secs;
			this.lead = lead;
						
			Dimension dim = this.getPreferredSize();
			this.height = dim.getHeight();
			this.width = dim.getWidth();
			
			BoxLayout layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
			this.setLayout(layout);
			
			this.info = new JPanel();
			BoxLayout infolayout = new BoxLayout(info, BoxLayout.PAGE_AXIS);
			info.setLayout(infolayout);
			info.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			info.setPreferredSize(new Dimension(this.infowidth,(int)this.height));
			info.setSize(new Dimension(this.infowidth,(int)this.height));
			info.setMaximumSize(new Dimension(this.infowidth, Short.MAX_VALUE));
			
			JLabel leadname = new JLabel(this.lead);
			info.add(leadname);
			info.add(Box.createRigidArea(new Dimension(0, 3)));
			
			JLabel minimum = new JLabel("Minimum: " + this.min);
			info.add(minimum);
			info.add(Box.createRigidArea(new Dimension(0, 3)));
			
			JLabel maximum = new JLabel("Minimum: " + this.max);
			info.add(maximum);
			info.add(Box.createRigidArea(new Dimension(0, 3)));
			
			
			this.add(info, BorderLayout.WEST);
			
			this.graph = new DrawingPanel(this.data,(int) (this.width - this.infowidth), (int) this.height, this.secs);
			dim = new Dimension((int) (this.width - this.infowidth), (int) this.height);
			graph.setPreferredSize(dim);
			graph.setSize(dim);
			
			this.add(graph, BorderLayout.EAST);
		}
		
		public void paintComponent( Graphics g ) {
			super.paintComponent(g);
			
			Dimension dim = this.getPreferredSize();
			this.height = dim.getHeight();
			this.width = dim.getWidth();
			
			info.setPreferredSize(new Dimension(this.infowidth,(int)this.height));
			info.setSize(new Dimension(this.infowidth,(int)this.height));
			info.setMaximumSize(new Dimension(this.infowidth, Short.MAX_VALUE));
			info.repaint();
			
			dim = new Dimension((int) (this.width - this.infowidth), (int) this.height);
			graph.setPreferredSize(dim);
			graph.setSize(dim);
			graph.repaint();
			
		}
		
	}
	

	private class DrawingPanel extends JPanel {
		
		private static final long serialVersionUID = 856943381513072262L;
		private int[] data;
		private float scalingWidth;
		private int secs;
		
		public DrawingPanel(int[] values, int width, int height, int secs) {
			this.data = values;
			this.setPreferredSize(new Dimension(width, height));
			this.setSize(new Dimension(width, height));
			this.secs = secs;
		}
		
		public void paintComponent( Graphics g ) {
		
			int mv_cell_count = 6;
			int secs_cell_count = this.secs * 5;
			
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			
			//set background color to white
			this.setBackground(Color.WHITE);
			// set rendering options
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);    
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
			g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
						
			Dimension dim = getPreferredSize();
			// calculate height and width of the cells
			double cellheight = dim.getHeight() / mv_cell_count;
			double cellwidth = dim.getWidth() / secs_cell_count;
			
			// calculate the scaling which is dependent to the width
			this.scalingWidth =  (float) (cellwidth / (data.length / secs_cell_count ));
			
			// set line color
			g2.setColor(new Color(231, 84, 72));
			// draw horizontal lines
			for(int i = 0; i < mv_cell_count; i++) {
				if(i % 10 == 0)
				{
					g2.setStroke(new BasicStroke(2.0f));
				}
				else
				{
					g2.setStroke(new BasicStroke(1.0f));
				}
				g2.draw(new Line2D.Double(0, i * cellheight, 
						dim.getWidth(), i * cellheight));
				
			}
			
			// draw vertical lines
			for(int i = 0; i < secs_cell_count; i++ ) {
				if(i % 5 == 0)
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
			
			// draw waveform as line using the given values
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(1.2f));
			for(int i  = 0; i < (this.data.length - 1); i++) {
				int a = i;
				int b = i + 1;
				Line2D line = new Line2D.Double(this.scalingWidth * a, (dim.height /2 - ( (float)(this.data[a] / (float) 100) * cellheight) ), 
						this.scalingWidth * b, ( dim.height /2 - ( (float)(this.data[b] / (float) 100) * cellheight ) ));
				g2.draw(line);
			 }
			
		}	
		
	}
	
}
