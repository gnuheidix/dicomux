package test;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

public class languageTest {

	   static public void main(String[] args) {

	      Locale[] supportedLocales = {

	         Locale.GERMAN,
	         Locale.ENGLISH
	      };


	      System.out.println();
	      
//	      iterateKeys(supportedLocales[0]);
	      ResourceBundle labels_global = 
	          ResourceBundle.getBundle("LabelsBundle",Locale.JAPAN);
	      
	      System.out.println(labels_global.getString("s1"));
	      System.out.println(labels_global.getString("s2"));
	      System.out.println(labels_global.getString("s3"));
	      

	   } // main

}
