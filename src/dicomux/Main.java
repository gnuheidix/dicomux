package dicomux;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;

public class Main {

	public static void main(String[] args) {
		DicomObject dcmObj;
		DicomInputStream din = null;
		try {
			din = new DicomInputStream(new File("test/pdf.dcm"));
			dcmObj = din.readDicomObject();
			System.out.println(dcmObj.toString());
			FileOutputStream fis = new FileOutputStream(new File("test/test.pdf"));
			byte[] pdfBytes = dcmObj.get(Tag.EncapsulatedDocument).getBytes();
			fis.write(pdfBytes, 0, pdfBytes.length);
			fis.close();
			View mainFrame = new View();
			mainFrame.pack();
			mainFrame.setVisible(true);
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		finally {
			try {
				din.close();
			}
			catch (IOException ignore) {
			}
		}
	}
}
