package dicomux;

/**
 * Represents all possible states of a TabObject
 * @author heidi
 * @see TabObject
 */
public enum TabState {
	WELCOME,
	FILE_OPEN,
	DIR_OPEN,
	ABOUT,
	PLUGIN_CHOOSE,
	PLUGIN_ACTIVE,
	ERROR_OPEN
}
