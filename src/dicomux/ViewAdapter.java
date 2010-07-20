package dicomux;

import java.util.Locale;

/**
 * This is an adapter for IView. This is used during initialization of Dicomux
 * @author heidi
 *
 */
public class ViewAdapter implements IView {

	@Override
	public int getActiveWorkspaceId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Locale getLanguage() {
		// TODO Auto-generated method stub
		return new Locale("en");
	}

	@Override
	public void notifyView() {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerController(IController controller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerModel(IModel model) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLanguage(Locale locale) {
		// TODO Auto-generated method stub

	}

}
