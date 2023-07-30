import static java.util.Comparator.comparing;

import au.com.bytecode.opencsv.CSVReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.csv.*;

public class Main {

  public static void main(String[] args) {
    System.out.println("Hello world!");
    var foo = new Main();
    try {
      /**
       * TECHNIQUE ONE
       */
      //var doo  = foo.readAllExample();
      //print(doo);

      /**
       * TECHNIQUE TWO
       */
      String pathname = "/Users/peter/src/personal/metropolis/metropolis/src/parking_history.csv";
      //var path = Paths.get(pathname);
      //var woo = foo.readAllLines(path);
      //print(woo);


      /**
       * TECHNIQUE THREE
       */
      //File csvData = new File(pathname);
      //CSVParser parser = CSVParser.parse(csvData, Charset.defaultCharset(), CSVFormat.RFC4180);
      //for (CSVRecord csvRecord : parser) {
      //  System.out.println(csvRecord);
      //}

      var entryDateOrderedRecords = new ArrayList<Map.Entry<Date, Map<String, String>>>();
      var exitDateOrderedRecords = new ArrayList<Map.Entry<Date, Map<String, String>>>();

      try (CSVParser csvParser = new CSVParser(Files.newBufferedReader(Paths.get(pathname)),
          CSVFormat.RFC4180.builder().setHeader("ID","time_entry","time_exit","total_price","tax_price","user_id","site").setSkipHeaderRecord(true).build())) {
        for (CSVRecord c : csvParser) {
          var record = new HashMap<String, String>();
          record.put("ID", c.get("ID"));
          record.put("time_entry", c.get("time_entry"));
          record.put("time_exit", c.get("time_exit"));
          record.put("total_price", c.get("total_price"));
          record.put("user_id", c.get("user_id"));
          record.put("site", c.get("site"));

          Date date = getDate("time_entry",c);
          entryDateOrderedRecords.add(Map.entry( date, record));
          Date date2 = getDate("time_exit",c);
          exitDateOrderedRecords.add(Map.entry( date2, record));

        }

        Collections.sort(entryDateOrderedRecords, comparing(
            Entry::getKey));
        Collections.sort(exitDateOrderedRecords, comparing(
            Entry::getKey));


        System.out.println("entryDateOrderedRecords");
        print(entryDateOrderedRecords);
        System.out.println("exitDateOrderedRecords");
        print(exitDateOrderedRecords);

        var zeep = getRecordsSpanning(entryDateOrderedRecords,"2000-10-31T01:31:05.000-05:00", "2000-10-31T01:35:30.000-05:00");
        System.out.println("From 2000-10-31T01:31:05.000-05:00 to 2000-10-31T01:35:30.000-05:00:");
        print(zeep);
        zeep = getRecordsSpanning(entryDateOrderedRecords,"2000-10-31T01:31:06.000-05:00", "2000-10-31T01:35:32.000-05:00");
        System.out.println("From 2000-10-31T01:31:06.000-05:00 to 2000-10-31T01:35:32.000-05:00:");
        print(zeep);

      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }



  }

  private static List<Map.Entry<Date, Map<String, String>>> getRecordsSpanning(List<Map.Entry<Date, Map<String, String>>> records, String b, String e)
      throws ParseException {
    var beginning = getDate(b);
    var end = getDate(e);
    var doop = records.stream().map(x -> x.getKey()).collect(Collectors.toList());
    Date[] d = doop.toArray(new Date[0]);
    int index_left = binarySearchForLeftRange(d, records.size(), beginning);
    int index_right = binarySearchForRightRange(d, records.size(), end);

    System.out.println("index_left: " + index_left);
    System.out.println("index_right: " + index_right);

    var barp = records.stream().skip(index_left).limit(index_right-index_left+1).collect(Collectors.toList());
    return barp;
  }

  private static <T extends Comparable<T>> int binarySearchForLeftRange(T a[], int length, T left_range)
  {
    if (a[length-1].compareTo(left_range) < 0)
      return -1;

    int low = 0;
    int high = length-1;

    while (low<=high)
    {
      int mid = low+((high-low)/2);

      if(a[mid].compareTo(left_range) >= 0)
        high = mid-1;
      else //if(a[mid]<i)
        low = mid+1;
    }

    return high+1;
  }

  private static <T extends Comparable<T>> int binarySearchForRightRange(T a[], int length, T right_range)
  {
    if (a[0].compareTo(right_range) > 0)
      return -1;

    int low = 0;
    int high = length-1;

    while (low<=high)
    {
      int mid = low+((high-low)/2);

      if(a[mid].compareTo(right_range) > 0)
        high = mid-1;
      else //if(a[mid]<i)
        low = mid+1;
    }

    return low-1;
  }

  private static Date getDate(String fieldName, CSVRecord c) throws ParseException {
    var noop = c.get(fieldName);
    // "2000-10-31T01:30:00.000-05:00";
    return getDate(noop);
  }

  private static Date getDate(String noop) throws ParseException {
    String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    var simpleDateFormat = new SimpleDateFormat(pattern);
    Date date = simpleDateFormat.parse(noop);
    return date;
  }

  private static void print( List<Entry<Date, Map<String, String>>> doo) {
    int count = 0;
    for (var noo : doo) {
      count++;
      System.out.print("#" + count + " Date: " + noo.getKey() + " => {");
      for (var noo2 : noo.getValue().entrySet()) {
        System.out.print(noo2.getKey() + " : " + noo2.getValue() + ", ");
      }
      System.out.print(" }\n");
    }
  }

  public List<String[]> readAllLines(Path filePath) throws Exception {
    try (Reader reader = Files.newBufferedReader(filePath)) {
      try (CSVReader csvReader = new CSVReader(reader)) {
        return csvReader.readAll();
      }
    }
  }
  public List<String[]> readAllExample() throws Exception {
    Path path = Paths.get(
        ClassLoader.getSystemResource("parking_history.csv").toURI()
    );
    return readAllLines(path);
  }
}
