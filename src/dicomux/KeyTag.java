package dicomux;

import java.util.Iterator;
import java.util.Vector;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.util.TagUtils;

/**
 * This class is for communication between a plug-in and the controller. It holds all 
 * necessary data in order to determine whether a plug-in is suitable for a certain dicom file or not.
 * @author heidi
 *
 */
public class KeyTag {
	
	/**
	 * initial capacity of the vectors
	 */
	private final int m_initialVectorCapacity = 3;
	
	/**
	 * the keytags which are supported by the plug-in
	 */
	private Vector<Integer> m_keyTag;
	
	/**
	 * this object holds the desired content of the tag; it will be used for comparison with values from a dicom object
	 */
	private Vector<String> m_desiredContent;
	
	/**
	 * Object for locking purposes; it helps to avoid write access to this class during a checkDicomObject() call
	 */
	private Object m_accessLock;
	
	/**
	 * Will be used during the check
	 */
	private Vector<Integer> m_tmpKeys;
	
	/**
	 * Will be used during the check
	 */
	private Vector<String> m_tmpValues;
	
	public KeyTag() {
		m_keyTag = new Vector<Integer>(m_initialVectorCapacity);
		m_desiredContent = new Vector<String>(m_initialVectorCapacity);
		m_accessLock = new Object();
	}
	
	/**
	 * Adds a pair of a key tag and its desired content to this object. 
	 * @param keyTag a dcm4che Tag
	 * @param desiredContent a string with the desired content of the specified tag or null if the value of the tag is not relevant
	 * @throws Exception
	 */
	public void addKey(int keyTag, String desiredContent) throws Exception {
		synchronized (m_accessLock) {
			if (TagUtils.toString(keyTag) != null) {
				m_keyTag.add(new Integer(keyTag));
				m_desiredContent.add(desiredContent);
			}
			else
				throw new Exception("Invalid parameters in KeyTag.addKey()");
		}
	}
	
	/**
	 * This method initiates the check of a DicomObject.
	 * @param dcm the DicomObject which should be checked
	 * @return true if the DicomObject matches to the current set of Keys of this KeyTag object.
	 * @throws Exception 
	 */
	public boolean checkDicomObject(DicomObject dcm) throws Exception {
		synchronized (m_accessLock) {
			if (m_keyTag.size() == m_desiredContent.size())
				if (m_keyTag.isEmpty())
					return true;
				else {
					m_tmpKeys = new Vector<Integer>(m_keyTag);
					m_tmpValues = new Vector<String>(m_desiredContent);
					traverseAndCheckDicomObject(dcm);
				}
			else
				throw new Exception("Vector sizes are not equal");
			
			return m_tmpKeys.size() == 0 && m_tmpValues.size() == 0;
		}
	}
	
	/**
	 * This method iterates over all DicomElements of a DicomObject and calls checkDicomElement() 
	 * @param dcm the DicomObject which should be iterated through
	 * @throws Exception for bad days
	 */
	private void traverseAndCheckDicomObject(DicomObject dcm) throws Exception {
		// iterate through the DicomObject
		Iterator<DicomElement> iter = dcm.datasetIterator();
		while (iter.hasNext()) {
			DicomElement element = iter.next();
			
			checkDicomElement(element);
			
			if (element.hasDicomObjects()) {
				// check all DicomObjects of the DicomElement recursively
				for (int i = 0; i < element.countItems(); ++i)
					traverseAndCheckDicomObject(element.getDicomObject(i));
			}
		}
	}
	
	/**
	 * Checks whether one of the pairs of tags and values stored by addKey are matching for this DicomElement.<br/>
	 * Only call it with properly initialized vectors m_tmpKeys and m_tmpValues!
	 * @param element the DicomElement which should be used for the check
	 * @throws Exception things go wrong sometimes
	 */
	private void checkDicomElement(DicomElement element) throws Exception {
		// iterate over all locally stored pairs 
		for (int i = 0; i < m_tmpKeys.size(); ++i) {
			// does the tag match?
			if (m_tmpKeys.get(i) == element.tag()) {
				String desiredContent = m_tmpValues.get(i);
				
				// is the content relevant?
				if (desiredContent == null) {
					removeElementFromTmpData(i);
				}
				else {
					String elementAsString = element.getValueAsString(new SpecificCharacterSet("UTF-8"), desiredContent.length());
					if (desiredContent.equals(elementAsString)) {
						removeElementFromTmpData(i);
					}
				}
			}
		}
	}
	
	/**
	 * Convenience method for removing a key-value-pair from the temporary data set
	 * @param index
	 */
	private void removeElementFromTmpData(int index) {
		m_tmpKeys.remove(index);
		m_tmpValues.remove(index);
	}
}
