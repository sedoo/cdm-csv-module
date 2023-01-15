package fr.sedoo.cdm.csv;

import java.util.ArrayList;
import java.util.List;

import fr.sedoo.cdm.csv.config.Column;
import fr.sedoo.cdm.csv.config.Location;
import fr.sedoo.cdm.csv.config.Time;
import fr.sedoo.cdm.csv.header.Header;
import fr.sedoo.cdm.csv.header.SingleLineHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CsvConfig {
	
	private String delimiter;
	private Header header;
	private Time time;
	private Location location;
	private List<Column> columns = new ArrayList<Column>();

	public CsvConfig() {
		//Default header
		setHeader(new SingleLineHeader());
	}

	
}
