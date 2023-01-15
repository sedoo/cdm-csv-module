package fr.sedoo.cdm.csv;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import fr.sedoo.cdm.csv.config.Column;
import fr.sedoo.cdm.csv.config.Location;
import fr.sedoo.cdm.csv.config.Time;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayLong;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.AbstractIOServiceProvider;
import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;

public class Csviosp extends AbstractIOServiceProvider {

	private static final String INDEX_ATTRIBUTE_NAME = "index";
	private static final String NAME_ATTRIBUTE_NAME = "name";
	private static final String FORMAT_ATTRIBUTE_NAME = "format";
	private static final String TIME_ELEMENT_NAME = "time";
	private static final String COLUMNS_ELEMENT_NAME = "columns";
	private static final String COLUMN_ELEMENT_NAME = "column";
	private static final String LOCATION_ELEMENT_NAME ="location";
	private static final String SEPARATOR_ATTRIBUTE_NAME = "separator";
	private static final String LATITUDE_ATTRIBUTE_NAME = "lat";
	private static final String LONGITUDE_ATTRIBUTE_NAME = "lon";
	private static final String CSV_EXTENSION = ".csv";

	public boolean isValidFile(RandomAccessFile raf) throws IOException {
		String location = raf.getLocation();
		if (location.toLowerCase().endsWith(CSV_EXTENSION)) {
			return true;
		}
		return false;
	}

	 @Override
	  public void open(RandomAccessFile raf, NetcdfFile ncfile, CancelTask cancelTask) throws IOException {
		this.raf = raf;
	    this.location = (raf != null) ? raf.getLocation() : null;
	    this.ncfile = ncfile;
	    this.config.getHeader().read(raf, ncfile, config); // read header here
	  }
	
	CsvConfig config;
	
	public Array readData(Variable variable, Section section) throws IOException, InvalidRangeException {
		// TODO Auto-generated method stub
		if (variable.getShortName().equalsIgnoreCase("lat")) {
			int[] shape = new int[] {1};
			ArrayDouble.D1 array = (ArrayDouble.D1) Array.factory(DataType.DOUBLE, shape);
			array.set(0, config.getLocation().getLat());
			return array;
		}
		if (variable.getShortName().equalsIgnoreCase("lon")) {
			int[] shape = new int[] {1};
			ArrayDouble.D1 array = (ArrayDouble.D1) Array.factory(DataType.DOUBLE, shape);
			array.set(0, config.getLocation().getLon());
			return array;
		}
		if (variable.getShortName().equalsIgnoreCase("time")) {
			try {
				return getTimeArray();
			}
			catch (Exception e) {
				return null;
			}
		} else {
			try {
			return getDataColumn(variable.getShortName(), section.getRange(0));
			} catch (Exception e) {
				return null;
			}
		}
			
	}

	private Array getDataColumn(String shortName, Range range) throws Exception {
		boolean headerFound = false;
		int index = getColumnIndex(shortName);
		raf.seek(0);
		List<Double> data = new ArrayList<Double>();
		String line = raf.readLine();
		while (line != null) {
			line = line.trim();
			if (!StringUtils.isEmpty(line)) {
				if (!headerFound) {
					headerFound = true;
				} else {
					String[] split = splitLine(line);
					String value = split[index].trim();
//					value = value.replace('.', ',');
					data.add(new Double(value));
				}
			}
			line = raf.readLine();
		}
		data = data.subList(range.first(), range.last());
		int[] shape = new int[] {data.size(),1,1};
		ArrayDouble.D3 result = (ArrayDouble.D3) Array.factory(DataType.DOUBLE, shape);
		for (int i=0; i<data.size();i++) {
			result.set(i,0,0, data.get(i));
		}
		return result;
	}

	private int getColumnIndex(String shortName) {
		List<Column> columns = config.getColumns();
		for (Column column : columns) {
			if (column.getName().equalsIgnoreCase(shortName)) {
				return column.getIndex();
			}
		}
		throw new IllegalArgumentException(shortName+" is not a correct column name");
	}

	private Array getTimeArray() throws Exception {
		SimpleDateFormat format = new SimpleDateFormat(config.getTime().getFormat());
		boolean headerFound = false;
		raf.seek(0);
		List<Long> times = new ArrayList<Long>();
		String line = raf.readLine();
		while (line != null) {
			line = line.trim();
			if (!StringUtils.isEmpty(line)) {
				if (!headerFound) {
					headerFound = true;
				} else {
					String[] split = splitLine(line);
					String timeString = split[config.getTime().getIndex()];
					Date date = format.parse(timeString);
					times.add(date.getTime()/1000);
				}
			}
			line = raf.readLine();
		}
		int[] shape = new int[] {times.size()};
		ArrayLong.D1 result = (ArrayLong.D1) Array.factory(DataType.LONG, shape);
		for (int i=0; i<times.size();i++) {
			result.set(i, times.get(i));
		}
		return result;
	}

	private String[] splitLine(String line) {
		return line.split(config.getDelimiter());
	}

	public String getFileTypeId() {
		return "CSV";
	}

	public String getFileTypeDescription() {
		return "Basic CSV File";
	}
	
	 @Override
	  public Object sendIospMessage(Object message) {
		 if (message instanceof Element) {
			this.config = toConfig((Element) message); 
			return null;
		 }
		 else {
			 return super.sendIospMessage(message);
		 }
	  }

	private CsvConfig toConfig(Element element) {
		CsvConfig config = new CsvConfig();
		config.setDelimiter(element.getAttributeValue(SEPARATOR_ATTRIBUTE_NAME));
		Element timeElement = element.getChild(TIME_ELEMENT_NAME, element.getNamespace());
		if (timeElement != null) {
			Time time = new Time();
			if (timeElement.getAttribute(INDEX_ATTRIBUTE_NAME) != null) {
				try {
					time.setIndex(timeElement.getAttribute(INDEX_ATTRIBUTE_NAME).getIntValue());
				} catch (DataConversionException e) {
					// We ignore
				}
			}
			if (timeElement.getAttribute(FORMAT_ATTRIBUTE_NAME) != null) {
				time.setFormat(timeElement.getAttribute(FORMAT_ATTRIBUTE_NAME).getValue());
			}
			config.setTime(time);
		}
		
		Element locationeElement = element.getChild(LOCATION_ELEMENT_NAME, element.getNamespace());
		if (locationeElement != null) {
			Location location = new Location();
			if (locationeElement.getAttribute(LATITUDE_ATTRIBUTE_NAME) != null) {
				try {
					location.setLat(locationeElement.getAttribute(LATITUDE_ATTRIBUTE_NAME).getDoubleValue());
				} catch (DataConversionException e) {
					// We ignore
				}
			}
			if (locationeElement.getAttribute(LONGITUDE_ATTRIBUTE_NAME) != null) {
				try {
					location.setLon(locationeElement.getAttribute(LONGITUDE_ATTRIBUTE_NAME).getDoubleValue());
				} catch (DataConversionException e) {
					// We ignore
				}
			}
			config.setLocation(location);
			
		}
		
		Element columnsElement = element.getChild(COLUMNS_ELEMENT_NAME, element.getNamespace());
		if (columnsElement != null) {
			List<Element> children = columnsElement.getChildren(COLUMN_ELEMENT_NAME, element.getNamespace());
			for (Element columnElement : children) {
				Column column = new Column();
				if (columnElement.getAttribute(INDEX_ATTRIBUTE_NAME) != null) {
					try {
						column.setIndex(columnElement.getAttribute(INDEX_ATTRIBUTE_NAME).getIntValue());
					} catch (DataConversionException e) {
						// We ignore
					}
				}
				if (columnElement.getAttribute(NAME_ATTRIBUTE_NAME) != null) {
					column.setName(columnElement.getAttribute(NAME_ATTRIBUTE_NAME).getValue());
				}
				config.getColumns().add(column);
			}
		}
		return config;
	}

}
