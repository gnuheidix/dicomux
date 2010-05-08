package dicomux;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Locale;

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
//		JLabel msg = new JLabel("Dicomux is currently unable to render ECG waveforms.");
//		msg.setHorizontalAlignment(JLabel.CENTER);
//		m_content.add(msg, BorderLayout.CENTER);
		
		// get WaveformSequence
		DicomElement temp = dcm.get(Tag.WaveformSequence);
		if(temp == null) {
			System.out.println("Error: could not read WaveformSequence");
			return;
		}
		dcm = temp.getDicomObject();
		
		// read the number of allocated bits per sample 
		// used to differ between general ECG and 12 Lead ECG
		DicomElement bitsAllocated = dcm.get(Tag.WaveformBitsAllocated);
		if(bitsAllocated == null) {
			System.out.println("Error: could not read WaveformBitsAllocated");
			return;
		}
		
		// read waveform data which contains the samples
		DicomElement waveformData = dcm.get(Tag.WaveformData);
		if(waveformData == null) {
			System.out.println("Error: could not read WaveformData");
			return;
		}
		
		DicomElement samplingFrequency = dcm.get(Tag.SamplingFrequency);
		if(samplingFrequency == null) {
			System.out.println("Error: could not read SamplingFrequency");
			return;
		}
		double frequency = samplingFrequency.getDouble(true);
		
		//read number of samples per channel
		DicomElement samples = dcm.get(Tag.NumberOfWaveformSamples);
		if(samples == null) {
			System.out.println("Error: could not read NumberOfWaveformSamples");
			return;
		}
		int numberOfSamples = samples.getInt(true);
		
		int seconds = (int) (numberOfSamples / frequency);
		
		// read number of channels
		DicomElement channels = dcm.get(Tag.NumberOfWaveformChannels);
		if(channels == null) {
			System.out.println("Error: could not read NumberOfWaveformChannels");
			return;
		}
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
		{
			System.out.println("Error: bitsAllocated is an unexpected value, value: " + bitsAllocated.getInt(true));
			return;
		}
		
		JPanel channelpane = new JPanel();
		channelpane.setBackground(Color.BLACK);
		channelpane.setLayout(new GridLayout(12, 1, 0, 2));
		
		for(int i = 0; i < numberOfChannels; i++) {
			ChannelPanel chPannel = new ChannelPanel(data[i], 765, 92, seconds);
			channelpane.add(chPannel);
		}
		
		JScrollPane scroll = new JScrollPane(channelpane);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		m_content.add(scroll, BorderLayout.CENTER);
		
	}
	
	// TODO implement if necessary
	@Override
	public void setLanguage(Locale locale) {
		
	}


	private class ChannelPanel extends JPanel {
		
		private static final long serialVersionUID = 856943381513072262L;
		private int[] data;
		private float scalingWidth;
		private int secs;
		
		public ChannelPanel(int[] values, int width, int height, int secs) {
			this.data = values;
			this.setPreferredSize(new Dimension(width, height));
			this.secs = secs;
		}
		
		public void paintComponent( Graphics g ){
		
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
			for(int i  = 0; i < (this.data.length - 1); i++)
			{
				int a = i;
				int b = i + 1;
				Line2D line = new Line2D.Double(this.scalingWidth * a, (dim.height /2 - ( (float)(this.data[a] / (float) 100) * cellheight) ), 
						this.scalingWidth * b, ( dim.height /2 - ( (float)(this.data[b] / (float) 100) * cellheight ) ));
				g2.draw(line);
			 }
			
		}	
		
	}
	
}
