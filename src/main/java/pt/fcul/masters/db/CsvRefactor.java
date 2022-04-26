package pt.fcul.masters.db;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;


public class CsvRefactor {

	private static final String NOT_NUMBER = "[^0-9\\.]+";


	public static void main(String[] args) throws IOException {
		if(args.length < 3)
			throw new IllegalArgumentException("It must receive path to file, market, timeframe");
		
		List<String> lines = Files.readAllLines(new File(args[0]).toPath());
		lines.remove(0); //remove columns [ Date , close, volume, open, high, low ]
		
		File outputFile = new File(args[0]);
		outputFile = new File(outputFile.getParentFile().getAbsolutePath()+File.separator+ args[1]+".sql");
		PrintWriter pw = new PrintWriter(outputFile);
		lines.stream().map(l->mapper(l,args[1],args[2])).forEach(pw::println);
		pw.flush();
		pw.close();
		System.out.println("Created file: "+ outputFile.getAbsolutePath());
	}
	
	
	
	public static String mapper(String line,String market, String timeframe) {
		String[] split = line.split(",");
		
		String[] date = split[0].replace("/", " ").trim().split(" ");
		LocalDateTime datetime = LocalDateTime.of(Integer.parseInt(date[2]), Integer.parseInt(date[0]), Integer.parseInt(date[1]), 0, 0);
		
		return String.format(
				"""
				INSERT INTO `forexdb`.`candlestick`	(`close`,`complete`,`datetime`,`high`,`low`,`market`,`open`,`timeframe`,`volume`)
				VALUES (%s,1,"%s",%s,%s,"%s",%s,"%s",%s);
				""", split[1].replaceAll(NOT_NUMBER, ""),datetime.toString(),split[4].replaceAll(NOT_NUMBER, ""),split[5].replaceAll(NOT_NUMBER, ""),market
				,split[3].replaceAll(NOT_NUMBER, ""),timeframe,split[2].replaceAll(NOT_NUMBER, ""));
				
	}
}
