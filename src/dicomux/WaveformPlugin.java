package dicomux;

import java.awt.BorderLayout;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dcm4che2.data.DicomObject;
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
		JLabel msg = new JLabel("Dicomux is currently unable to render ECG waveforms.");
		msg.setHorizontalAlignment(JLabel.CENTER);
		m_content.add(msg, BorderLayout.CENTER);
	}
	
	// TODO implement if necessary
	@Override
	public void setLanguage(Locale locale) {
		
	}

}
