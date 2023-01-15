package fr.sedoo.cdm.csv.header;

import java.io.IOException;
import java.util.List;

import fr.sedoo.cdm.csv.CsvConfig;
import fr.sedoo.cdm.csv.StringUtils;
import fr.sedoo.cdm.csv.config.Column;
import fr.sedoo.cdm.csv.config.Time;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Dimension.Builder;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.constants._Coordinate;
import ucar.unidata.io.RandomAccessFile;

public class SingleLineHeader implements Header {

	int numberOfLines = 0;

	public void read(RandomAccessFile raf, NetcdfFile ncfile, CsvConfig config) throws IOException {
//		parseFile(raf);
		Builder dimensionBuilder = Dimension.builder();
		//Dimension timeDimension = dimensionBuilder.setIsUnlimited(true).setName("time").build();
		Dimension timeDimension = dimensionBuilder.setLength(40).setName("time").build();
		dimensionBuilder = Dimension.builder();
		Dimension latDimension = dimensionBuilder.setLength(1).setName("latitude").build();
		dimensionBuilder = Dimension.builder();
		Dimension lonDimension = dimensionBuilder.setLength(1).setName("longitude").build();
		ncfile.addDimension(null, timeDimension);
		ncfile.addDimension(null, latDimension);
		ncfile.addDimension(null, lonDimension);

		Time time = config.getTime();
		Variable timeVariable = new Variable(ncfile, null, null, "time");
		timeVariable.setDimensions("time");
		timeVariable.setDataType(DataType.LONG);
		String timeUnit = "seconds since 1970-01-01 00:00:00";
		timeVariable.addAttribute(new Attribute("long_name", "time"));
		timeVariable.addAttribute(new Attribute("units", timeUnit));
		timeVariable.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Time.toString()));
		ncfile.addVariable(null, timeVariable);

		Variable lat = new Variable(ncfile, null, null, "lat");
		lat.setDimensions("latitude");
		lat.setDataType(DataType.DOUBLE);
		lat.addAttribute(new Attribute("long_name", "latitude"));
		lat.addAttribute(new Attribute("units", "degrees_north"));
		lat.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Lat.toString()));
		ncfile.addVariable(null, lat);

		Variable lon = new Variable(ncfile, null, null, "lon");
		lon.setDimensions("longitude");
		lon.setDataType(DataType.DOUBLE);
		lon.addAttribute(new Attribute("long_name", "longitude"));
		lon.addAttribute(new Attribute("units", "degrees_east"));
		lon.addAttribute(new Attribute(_Coordinate.AxisType, AxisType.Lon.toString()));
		ncfile.addVariable(null, lon);

		List<Column> columns = config.getColumns();

		for (Column column : columns) {

			Variable newVariable = new Variable(ncfile, null, null, column.getName());
			newVariable.setDimensions("time latitude longitude");
			newVariable.addAttribute(new Attribute(_Coordinate.Axes, "time latitude longitude"));
			newVariable.setDataType(DataType.DOUBLE);
			newVariable.addAttribute(new Attribute("long_name", column.getName()));
			ncfile.addVariable(null, newVariable);
		}

		ncfile.finish();
	}

	/**
	 * We parse the file to find - The variables names - The number of data lines
	 * 
	 * @param raf
	 */
	private void parseFile(RandomAccessFile raf) throws IOException {

		boolean headerFound = false;
		String headerLine = null;
		raf.seek(0);
		String line = raf.readLine();
		while (line != null) {
			line = line.trim();
			if (!StringUtils.isEmpty(line)) {
				if (!headerFound) {
					headerLine = line;
					headerFound = true;
				} else {
					numberOfLines++;
				}
			}
			line = raf.readLine();
		}
	}

}
