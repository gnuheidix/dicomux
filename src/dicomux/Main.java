package dicomux;

//TODO cleanup
/**
 * Launches Dicomux<br/>
 * We determine, which model and which view shall be used.
 * @author heidi
 *
 */
public class Main {

	public static void main(String[] args) {
		IModel model = new Model();
		IView view = new View();
		
		@SuppressWarnings("unused")
		Controller ctrl = new Controller(model, view);

//		DicomObject dcmObj = null;
//		DicomInputStream din = null;
//		try {
//			din = new DicomInputStream(new File("test/pdf.dcm"));
//			dcmObj = din.readDicomObject();
//			System.out.println(dcmObj.toString());
//			FileOutputStream fis = new FileOutputStream(new File("test/test.pdf"));
//			byte[] pdfBytes = dcmObj.get(Tag.EncapsulatedDocument).getBytes();
//			fis.write(pdfBytes, 0, pdfBytes.length);
//			fis.close();
//			
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//			return;
//		}
//		finally {
//			try {
//				din.close();
//			}
//			catch (IOException ignore) {
//			}
//		}
	}
}
