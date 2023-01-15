package fr.sedoo;

import java.io.IOException;

import org.junit.Test;

import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;

public class Test1 {

	@Test
	public void basicTest() throws IOException {
		String filename = "/home/fandre/lev.csv";
		 NetcdfFile ncfile = null; 
		 try { 
		 ncfile = NetcdfFiles.open( filename ) ; 
		 ncfile.getVariables();
		 } catch ( IOException ioe ) {
			 System.out.println("trying to open " + filename );
			 }
			 finally
			 {
			 if ( null != ncfile ) {
			 try {
			 ncfile.close() ;
			 } catch ( IOException ioe ) {
				 System.out.println("trying to close " + filename );
			 }
			 }
			 } 
	}
	
	
}
