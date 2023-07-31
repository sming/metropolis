
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.csv.*;
import org.jetbrains.annotations.NotNull;

public class Main {

  private final ArrayList<Entry<Date, Map<String, String>>> entryDateOrderedRecords = new ArrayList<>();
  private final ArrayList<Entry<Date, Map<String, String>>> exitDateOrderedRecords = new ArrayList<>();

  public static void main(String[] args) {
    if (args == null || args.length == 0) {
      System.out.println("Usage: java -jar Main.jar <path_to_csv_file>");
    } else {
      new Main().parseCsvFile(args[0]);
    }
  }

  public void parseCsvFile(String csvPath) {
    try {
      try (var csvParser = buildCsvParser(csvPath)) {
        readCsvFile(csvParser);
        entryDateOrderedRecords.sort(Entry.comparingByKey());
        exitDateOrderedRecords.sort(Entry.comparingByKey());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void readCsvFile(CSVParser csvParser) throws ParseException {
    for (var csvRec : csvParser) {
      var rec = new HashMap<String, String>();
      extractFields(csvRec, rec);

      Date date = getDate("time_entry",csvRec);
      entryDateOrderedRecords.add(Map.entry( date, rec));
      Date date2 = getDate("time_exit",csvRec);
      exitDateOrderedRecords.add(Map.entry( date2, rec));
    }
  }

  @NotNull
  private static CSVParser buildCsvParser(String csvPath) throws IOException {
    return new CSVParser(
        Files.newBufferedReader(Paths.get(csvPath)),
        CSVFormat.RFC4180.builder()
            .setHeader("ID", "time_entry", "time_exit", "total_price", "tax_price", "user_id",
                "site")
            .setSkipHeaderRecord(true)
            .build());
  }

  private static void extractFields(CSVRecord csvRec, HashMap<String, String> rec) {
    rec.put("ID", csvRec.get("ID"));
    rec.put("time_entry", csvRec.get("time_entry"));
    rec.put("time_exit", csvRec.get("time_exit"));
    rec.put("total_price", csvRec.get("total_price"));
    rec.put("user_id", csvRec.get("user_id"));
    rec.put("site", csvRec.get("site"));
  }

  public static List<Map.Entry<Date, Map<String, String>>> getRecordsSpanning(List<Map.Entry<Date, Map<String, String>>> records, Date beg, Date end)
      throws ParseException {
    Date[] dts = records.stream().map(Entry::getKey).toArray(Date[]::new);

    int index_left = binarySearchForLeftRange(dts, records.size(), beg);
    int index_right = binarySearchForRightRange(dts, records.size(), end);

    return records.stream().skip(index_left).limit(index_right-index_left+1).collect(Collectors.toList());
  }

  /**
   * <a href="https://stackoverflow.com/a/20703626/1170932">Source</a>
   */
  private static <T extends Comparable<T>> int binarySearchForLeftRange(T[] a, int length, T left_range)
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

  /**
   * <a href="https://stackoverflow.com/a/20703626/1170932">Source</a>
   */
  private static <T extends Comparable<T>> int binarySearchForRightRange(T[] a, int length, T right_range)
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
    return getDateFromDateString(c.get(fieldName));
  }

  private static Date getDateFromDateString(String noop) throws ParseException {
    String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    var simpleDateFormat = new SimpleDateFormat(pattern);
    return simpleDateFormat.parse(noop);
  }

  public List<Map.Entry<Date, Map<String, String>>> queryTimeSpan(String isoStart, String isoEnd) {
    try {
      Date start = getDateFromDateString(isoStart);
      Date end = getDateFromDateString(isoEnd);
      return getRecordsSpanning(entryDateOrderedRecords, start, end);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static void printParkingStays( List<Entry<Date, Map<String, String>>> datesToProps) {
    int count = 0;
    for (var dateToProps : datesToProps) {
      count++;
      System.out.print("#" + count + " Date: " + dateToProps.getKey() + " => {");
      for (var noo2 : dateToProps.getValue().entrySet()) {
        System.out.print(noo2.getKey() + " : " + noo2.getValue() + ", ");
      }
      System.out.print(" }" + System.lineSeparator());
    }
  }

}
