package dicomux;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
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

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SpecificCharacterSet;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.iod.composite.Image;
import org.dcm4che2.tool.jpg2dcm.Jpg2Dcm;
import org.jpedal.examples.simpleviewer.utils.IconiseImage;


public class DirectoryPlugin extends APlugin{
	
	public class el_map{
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
	HashMap<Integer,String> ref_img_serie = new HashMap<Integer, String>();
	HashMap<Integer,String> ref_img_study = new HashMap<Integer, String>();
	HashMap<Integer,String> ref_img_patient = new HashMap<Integer, String>();
	
	
	enum dir_type{patient,studie,serie,recource,invalid,initial};
	
	final JComboBox patient_combo = new JComboBox(new Object[]{"patients"});
	final JComboBox studie_combo = new JComboBox(new Object[]{"studies"});
	final JComboBox serie_combo = new JComboBox(new Object[]{"series"});
	final JComboBox recource_combo = new JComboBox(new Object[]{"recources"});
	
	private String actual_patientID = "";
	private String actual_studyID = "";
	private String actual_serieID = "";
	private String actual_recourceID = "";
	private DicomObject dcm;
	private String DirFilePath = "";
	
	private HashMap<Integer,BufferedImage> images = new HashMap<Integer,BufferedImage>();
	JPanel recourcePanel = new JPanel();
	
	@Override
	public String getName() {
		return "Directory";
	}

	@Override
	public void setData(DicomObject _dcm) throws Exception {
		dcm = _dcm;
		
		if(DirFilePath == ""){
			//throw new Exception();
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
	
		drawImage();
	}
	
	private void drawImage()
	{
		
		BufferedImage img = images.get(recource_combo.getSelectedItem());
		if(img != null){

		recourcePanel.removeAll();
		recourcePanel.add(new JLabel(new ImageIcon(img)));
		m_content.repaint();
		recourcePanel.updateUI();
//		recourcePanel.add(new JLabel("Test"));
		}

	}
	
	private void getRecources()
	{
		Vector<String> recources = new Vector<String>();
		  for (Entry<String, HashMap<String, String>> entry : recource_map.entrySet()) {
			  File base =  new File(DirFilePath);
			  String path = base.toString() + File.separator +entry.getValue().get(dcm.nameOf(Tag.ReferencedFileID));
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
				
				ref_img_patient.put(dicomObject.hashCode(),dicomObject.get(Tag.PatientID).getValueAsString(new SpecificCharacterSet("UTF-8"), 200));
				ref_img_serie.put(dicomObject.hashCode(), dicomObject.get(Tag.SeriesInstanceUID).getValueAsString(new SpecificCharacterSet("UTF-8"), 200));
				ref_img_study.put(dicomObject.hashCode(), dicomObject.get(Tag.StudyInstanceUID).getValueAsString(new SpecificCharacterSet("UTF-8"), 200));
			  }
			  catch (Exception e) {
				  System.out.println(e.getMessage());
				continue;
			}
		  }	
	}

	
	private void addComboListener()
	{
		/*
		patient_combo.addItemListener(new ItemListener(){
		      public void itemStateChanged(ItemEvent ie){
		    	  	//TODO: actual_patientID = ???
		  			setCombos(dir_type.patient);
			      }
			    });
		studie_combo.addItemListener(new ItemListener(){
		      public void itemStateChanged(ItemEvent ie){
		    	  	//TODO: actual_studyID = ???
		  			setCombos(dir_type.studie);
			      }
			    });
		serie_combo.addItemListener(new ItemListener(){
		      public void itemStateChanged(ItemEvent ie){
		    	  //dcm.nameOf(Tag.SeriesInstanceUID)
		    	  	actual_serieID = ref_key_ser.get((String)serie_combo.getSelectedItem());
		  			setCombos(dir_type.serie);
			      }
			    });
			    */
		recource_combo.addItemListener(new ItemListener(){
		      public void itemStateChanged(ItemEvent ie){
		    	  	//TODO: actual_patientID = ???
		  			//setCombos(dir_type.recource);
		  			drawImage();
			      }
			    });
	}
	
	private void setCombos(dir_type level)
	{

		drawImage();
		
		  if(level.equals(dir_type.initial))
		  {
			  patient_combo.removeAllItems();
			  patient_combo.addItem("all");
			  	//TODO: find out reference key between patient and study
				  for (Entry<String, HashMap<String, String>> entry : patient_map.entrySet()) {
					  patient_combo.addItem(entry.getValue().get(dcm.nameOf(Tag.PatientName)));
				   }
		  }
		  if(level.equals(dir_type.initial)||level.equals(dir_type.patient))
		  {
			  studie_combo.removeAllItems();
				//TODO: find out reference key between study and serie
			  	studie_combo.addItem("all");
				 for (Entry<String, HashMap<String, String>> entry : studie_map.entrySet()) {
					  studie_combo.addItem(entry.getValue().get(dcm.nameOf(Tag.StudyDescription)));
				   }
		  }
		  if(level.equals(dir_type.initial)||level.equals(dir_type.patient)||level.equals(dir_type.studie))
		  {
			  serie_combo.removeAllItems();
			  serie_combo.addItem("all");
				  for (Entry<String, HashMap<String, String>> entry : serie_map.entrySet()) {
					  serie_combo.addItem(entry.getValue().get(dcm.nameOf(Tag.SeriesDescription)));
					  ref_key_ser.put(entry.getValue().get(dcm.nameOf(Tag.SeriesDescription)), entry.getValue().get(dcm.nameOf(Tag.SeriesInstanceUID)));
				   }
		  }

		  if( level.equals(dir_type.initial)||level.equals(dir_type.patient)||level.equals(dir_type.studie)||level.equals(dir_type.serie))
		  {
			  recource_combo.removeAllItems();
				 int imageCount = 1;
				 Iterator it = images.keySet().iterator(); 
				 
				 while(it.hasNext()) { 
					 Object key = it.next();
					 Object val = images.get(key); 
					 recource_combo.addItem(key);
					 imageCount++;
					 } 
		  }
	}
	
	/**
	 * recursive function for extracting all DicomElements from an DicomObject; This function calls itself if a DicomObject is encapsulated in object
	 * @param rootElement the rootElement of the tree which will be returned
	 * @param object DicomObject which has to be extracted
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
			else { // the DicomElement doesn't contain more DicomObjects
				
				int tagId = element.tag();
				int elementLength = element.length();

				if (elementLength > 0){
					actualSeq.put(dcm.nameOf(element.tag()), element.getValueAsString(new SpecificCharacterSet("UTF-8"), 50));
				
					if(	dcm.nameOf(tagId) == dcm.nameOf(Tag.PatientID) ||
						dcm.nameOf(tagId) == dcm.nameOf(Tag.StudyInstanceUID) ||
						dcm.nameOf(tagId) == dcm.nameOf(Tag.SeriesInstanceUID) ||
						dcm.nameOf(tagId) == dcm.nameOf(Tag.ReferencedSOPInstanceUIDInFile)){
						
						id = element.getValueAsString(new SpecificCharacterSet("UTF-8"), 70);
					}
	
					else if(dcm.nameOf(tagId) == dcm.nameOf(Tag.DirectoryRecordType))
					{			
						String tmp = element.getValueAsString(new SpecificCharacterSet("UTF-8"), 50);
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
