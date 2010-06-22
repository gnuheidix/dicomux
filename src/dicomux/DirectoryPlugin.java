package dicomux;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.io.DicomInputStream;


public class DirectoryPlugin extends APlugin{
	
	private class el_map{
		public el_map(String _id,dir_type _type,HashMap<String, String> _actualSeq) {
			id = _id;
			type = _type;
			actualSeq = _actualSeq;
		}
		public String id;
		public dir_type type;
		public HashMap<String, String> actualSeq;
	}
	
	HashMap<String, HashMap<String, String>> patient_map = new HashMap<String, HashMap<String, String> >();
	HashMap<String, HashMap<String, String>> studie_map = new HashMap<String, HashMap<String, String> >();
	HashMap<String, HashMap<String, String>> serie_map = new HashMap<String, HashMap<String, String> >();
	HashMap<String, HashMap<String, String>> recource_map = new HashMap<String, HashMap<String, String> >();
	
	// Reference Container
	HashMap<String,String> ref_key_ser = new HashMap<String, String>();
	HashMap<String,String> ref_key_pat = new HashMap<String, String>();
	HashMap<String,String> ref_key_stu = new HashMap<String, String>();
	
	HashMap<Integer,String> ref_img_serie = new HashMap<Integer, String>();
	HashMap<Integer,String> ref_img_study = new HashMap<Integer, String>();
	HashMap<Integer,String> ref_img_patient = new HashMap<Integer, String>();
	
	enum dir_type{patient,studie,serie,recource,invalid,initial};
	
	final JComboBox patient_combo = new JComboBox(new Object[]{"patients"});
	final JComboBox studie_combo = new JComboBox(new Object[]{"studies"});
	final JComboBox serie_combo = new JComboBox(new Object[]{"series"});
	final JComboBox recource_combo = new JComboBox(new Object[]{"recources"});

	private DicomObject dcm;
	private String DirFilePath = "";
	
	private HashMap<Integer,BufferedImage> images = new HashMap<Integer,BufferedImage>();
	JPanel recourcePanel = new JPanel(new BorderLayout(5, 5));
	
	@Override
	public String getName() {
		return "Directory";
	}

	@Override
	public void setData(DicomObject _dcm) throws Exception {
		dcm = _dcm;
		if(DirFilePath == ""){
			throw new Exception();
		}	
	
		extractAllDicomElements(dcm);	//GET ALL INFORMATION ABOUT PATIENT, STUDY,SERIE AND RECOURCES FROM THE DIRECTORY-FILE
		getRecources();
		setCombos(dir_type.initial);
		addComboListener();	
		JPanel navigation = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		navigation.add(patient_combo);
		navigation.add(studie_combo);
		navigation.add(serie_combo);
		navigation.add(recource_combo);
		m_content = new JPanel(new BorderLayout(5, 5));
		m_content.add(navigation,BorderLayout.NORTH);
		m_content.add(recourcePanel,BorderLayout.CENTER);
		//drawImage();
		writeInformation(serie_combo.getSelectedItem().toString(), dir_type.serie);

	}
	
	/**
	 * This method draws the selected image. 
	 * If no image is selected the method clears the content panel and draws nothing.
	 */
	private void drawImage()
	{	
		BufferedImage img = images.get(recource_combo.getSelectedItem());
		recourcePanel.removeAll();
		if(img != null){
		recourcePanel.add(new JLabel(new ImageIcon(img)));
		m_content.repaint();
		recourcePanel.updateUI();
		}

	}
	
	/**
	 * 
	 * @param key
	 * @param type
	 */
	private void writeInformation(String key,dir_type type)
	{
		recourcePanel.removeAll();
		HashMap<String, String> information = null;
		JLabel lable = new JLabel();
		lable.setFont(new Font("Information",0, 30));
		if(ref_key_pat.containsKey(key) || ref_key_stu.containsKey(key) || ref_key_ser.containsKey(key)){
			switch(type){
			case patient:	information = new HashMap<String, String>(patient_map.get(ref_key_pat.get(key)));
							lable.setText("Patient information");
				break;
			case studie:	information = new HashMap<String, String>(studie_map.get(ref_key_stu.get(key)));
							lable.setText("Study information");
				break;
			case serie:		information = new HashMap<String, String>(serie_map.get(ref_key_ser.get(key)));
							lable.setText("Serie information");
				break;
			}
		}
		if(information != null){
			String col[] = {"Tag","Value"};
			String[][] values = new String[information.size()][2];
			int counter = 0;
			for (Entry<String, String> entry : information.entrySet()) {
				values[counter][0] = entry.getKey();
				values[counter][1] = entry.getValue();
				counter++;
			}
			JTable table = new JTable(values,col);
			JScrollPane scroll = new JScrollPane(table);
			table.setSize(800,600);
		    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		    recourcePanel.add(lable,BorderLayout.NORTH);
		    recourcePanel.add(scroll,BorderLayout.CENTER);
		}
		m_content.repaint();
		recourcePanel.updateUI();
	}
	
	private void getRecources()
	{
		Vector<String> recources = new Vector<String>();
		  for (Entry<String, HashMap<String, String>> entry : recource_map.entrySet()) {
			  File base =  new File(DirFilePath);
			  String path = base.toString() + File.separator + entry.getValue().get(dcm.nameOf(Tag.ReferencedFileID)).replace("\\", File.separator);
			  recources.add(path);
		  }
		  Iterator<String> itr = recources.iterator();
		  while(itr.hasNext()){
			  try{
				String file = (String) itr.next();
			  	File fileObject = new File(file);
			  	
				DicomInputStream din = new DicomInputStream(fileObject);
				DicomObject dicomObject = din.readDicomObject();
//				byte[] input = dicomObject.get(Tag.PixelData).getBytes();
				
				BufferedImage jpg = null;
				Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("DICOM");
				ImageReader reader = (ImageReader) iter.next();
				DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();

				ImageInputStream iis = ImageIO.createImageInputStream(fileObject);
				reader.setInput(iis,false);
				jpg = reader.read(0,param);
				iis.close();
				
				images.put(dicomObject.hashCode(),jpg );
				
				ref_img_patient.put(dicomObject.hashCode(),dicomObject.get(Tag.PatientID).getValueAsString(new SpecificCharacterSet("UTF-8"), 100));
				ref_img_serie.put(dicomObject.hashCode(), dicomObject.get(Tag.SeriesInstanceUID).getValueAsString(new SpecificCharacterSet("UTF-8"), 100));
				ref_img_study.put(dicomObject.hashCode(), dicomObject.get(Tag.StudyInstanceUID).getValueAsString(new SpecificCharacterSet("UTF-8"), 100));
			  }
			  catch (Exception e) {
				  System.out.println(e.getMessage());
				continue;
			}
		  }	
	}
	
	private void addComboListener()
	{
		
		patient_combo.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				setCombos(dir_type.patient);
	  			serie_combo.setVisible(false);
	  			recource_combo.setVisible(false);
	  			writeInformation(patient_combo.getSelectedItem().toString(),dir_type.patient);
			}
		});
		studie_combo.addActionListener(new ActionListener() {  
			@Override
			public void actionPerformed(ActionEvent e) {
				setCombos(dir_type.studie);
	  			serie_combo.setVisible(true);
	  			recource_combo.setVisible(false);				
	  			writeInformation(studie_combo.getSelectedItem().toString(),dir_type.studie);
			}});
		serie_combo.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
	  			setCombos(dir_type.serie);
	  			recource_combo.setVisible(true);
	  			writeInformation(serie_combo.getSelectedItem().toString(),dir_type.serie);
			}});
			    
		recource_combo.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
					if(recource_combo.getSelectedItem() == "select"){
						writeInformation(serie_combo.getSelectedItem().toString(),dir_type.serie);
					}
					else{
						drawImage();
					}
				}
			});
	}
	
	private void setCombos(dir_type level)
	{

		drawImage();
		
		  if(level.equals(dir_type.initial))
		  {
			  patient_combo.removeAllItems();
			  //patient_combo.addItem("all");
			  	//TODO: find out reference key between patient and study
				  for (Entry<String, HashMap<String, String>> entry : patient_map.entrySet()) {
					  patient_combo.addItem(entry.getValue().get(dcm.nameOf(Tag.PatientName)));
					  ref_key_pat.put(entry.getValue().get(dcm.nameOf(Tag.PatientName)), entry.getValue().get(dcm.nameOf(Tag.PatientID)));
						
				   }
			  studie_combo.removeAllItems();
				//TODO: find out reference key between study and serie
			  int counter = 0;
			  	//studie_combo.addItem("all");
				 for (Entry<String, HashMap<String, String>> entry : studie_map.entrySet()) {
					  studie_combo.addItem(counter +" " + entry.getValue().get(dcm.nameOf(Tag.StudyDescription)));
					  ref_key_stu.put(counter +" " + entry.getValue().get(dcm.nameOf(Tag.StudyDescription)), entry.getValue().get(dcm.nameOf(Tag.StudyInstanceUID)));
					  counter ++;
				   }
			 serie_combo.removeAllItems();
			  //serie_combo.addItem("all");
			  int counter2 = 0;
			  //serie_combo.addItem("all");
				  for (Entry<String, HashMap<String, String>> entry : serie_map.entrySet()) {
					  serie_combo.addItem(counter2 +" " + entry.getValue().get(dcm.nameOf(Tag.SeriesDescription)));
					  ref_key_ser.put(counter2 +" " + entry.getValue().get(dcm.nameOf(Tag.SeriesDescription)), entry.getValue().get(dcm.nameOf(Tag.SeriesInstanceUID)));
					  counter2 ++;
				  }
		  }
		  //if(level.equals(dir_type.initial)||level.equals(dir_type.patient)){  }
		  //if(level.equals(dir_type.initial)||level.equals(dir_type.patient)||level.equals(dir_type.studie)){}

		  if(!level.equals(dir_type.recource))
		  {
			  recource_combo.removeAllItems();
				 int imageCount = 1;
				 Iterator<Integer> it = images.keySet().iterator(); 	 
				 String selected_serieUID = ref_key_ser.get(serie_combo.getSelectedItem());
				 recource_combo.addItem("select");
					 while(it.hasNext()) { 
						 Object key = it.next();
						 //Object val = images.get(key);  
						 if(ref_img_serie.get(key) != null && ref_img_serie.get(key).equals(selected_serieUID)){
							 recource_combo.addItem(key);
							 imageCount++;
						 }
				}
		  }
	}
	
	/**
	 * recursive function for extracting all DicomElements from an DicomObject; This function calls itself if a DicomObject is encapsulated in object
	 * @param dcm DicomObject which has to be extracted
	 * @return a MutableTreeNode which has the rootElement as root node and all DicomElements of object
	 */
	private el_map extractAllDicomElements(DicomObject dcm) throws Exception {

		HashMap<String, String> actualSeq = new HashMap<String, String >();
		dir_type _type = dir_type.invalid;
		String id = "";
		
		Iterator<DicomElement> iter = dcm.datasetIterator();
		while (iter.hasNext()) {
			DicomElement element = iter.next();
			
			if (element.hasDicomObjects()) { // the DicomElement contains more DicomObjects
								
				// extract all DicomObjects of the DicomElement recursively
				for (int i = 0; i < element.countItems(); ++i){
					el_map lastSeq = extractAllDicomElements(element.getDicomObject(i));
					
					switch(lastSeq.type){
					case patient:	patient_map.put(lastSeq.id, lastSeq.actualSeq);
						break;
					case studie:	studie_map.put(lastSeq.id, lastSeq.actualSeq);
						break;
					case serie:		serie_map.put(lastSeq.id, lastSeq.actualSeq);
						break;
					case recource:	recource_map.put(lastSeq.id, lastSeq.actualSeq);
						break;
					}
				}
			}
			else { 
				// the DicomElement doesn't contain more DicomObjects
				int tagId = element.tag();
				int elementLength = element.length();

				if (elementLength > 0){
					actualSeq.put(dcm.nameOf(element.tag()), element.getValueAsString(new SpecificCharacterSet("UTF-8"), 100));
				
					if(	dcm.nameOf(tagId) == dcm.nameOf(Tag.PatientID) ||
						dcm.nameOf(tagId) == dcm.nameOf(Tag.StudyInstanceUID) ||
						dcm.nameOf(tagId) == dcm.nameOf(Tag.SeriesInstanceUID) ||
						dcm.nameOf(tagId) == dcm.nameOf(Tag.ReferencedSOPInstanceUIDInFile)){
						
						id = element.getValueAsString(new SpecificCharacterSet("UTF-8"), 100);
					}
					else if(dcm.nameOf(tagId) == dcm.nameOf(Tag.DirectoryRecordType))
					{			
						String tmp = element.getValueAsString(new SpecificCharacterSet("UTF-8"), 100);
						if(tmp.equals("PATIENT")){
							_type = dir_type.patient;
						}
						if(tmp.equals("STUDY")){
							_type = dir_type.studie;
						}
						if(tmp.equals("SERIES")){
							_type = dir_type.serie;
						}
						if(tmp.equals("IMAGE")){
							_type = dir_type.recource;
						}
					}
				}
			}
		}
		return  new el_map(id,_type,actualSeq);
	}
	
	public void setDirFilePath(String path){
		DirFilePath = path;
	}

	@Override
	public void setLanguage(Locale locale) {
		// TODO Auto-generated method stub
		
	}
}
