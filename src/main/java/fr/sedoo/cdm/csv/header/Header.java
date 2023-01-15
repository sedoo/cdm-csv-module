package fr.sedoo.cdm.csv.header;

import java.io.IOException;

import fr.sedoo.cdm.csv.CsvConfig;
import ucar.nc2.NetcdfFile;
import ucar.unidata.io.RandomAccessFile;

public interface Header {

	void read(RandomAccessFile raf, NetcdfFile ncfile, CsvConfig config) throws IOException;

}
