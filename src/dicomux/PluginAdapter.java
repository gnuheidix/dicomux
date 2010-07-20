package dicomux;

import java.util.Locale;

import org.dcm4che2.data.DicomObject;

/**
 * This is an adapter for APlugin. It may be used for initialization purpose.
 * @author heidi
 *
 */
public class PluginAdapter extends APlugin {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public void setData(DicomObject dcm) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLanguage(Locale locale) {
		// TODO Auto-generated method stub

	}

}
