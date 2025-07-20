import java.util.Calendar;
import java.util.Locale;

public class test_calendar {
    public static void main(String[] args) {
        // Test cho ngày 31/7/2025 (cuối tuần 5 của tháng 7)
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2025, 6, 31); // Month is 0-based, so 6 = July
        
        System.out.println("=== 31/7/2025 ===");
        System.out.println("Week of Month: " + cal1.get(Calendar.WEEK_OF_MONTH));
        System.out.println("Week of Year: " + cal1.get(Calendar.WEEK_OF_YEAR));
        System.out.println("Month: " + cal1.get(Calendar.MONTH)); // 6 = July
        System.out.println("Day of Week: " + cal1.get(Calendar.DAY_OF_WEEK));
        
        // Test cho ngày 1/8/2025 (đầu tháng 8)
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2025, 7, 1); // Month is 0-based, so 7 = August
        
        System.out.println("\n=== 1/8/2025 ===");
        System.out.println("Week of Month: " + cal2.get(Calendar.WEEK_OF_MONTH));
        System.out.println("Week of Year: " + cal2.get(Calendar.WEEK_OF_YEAR));
        System.out.println("Month: " + cal2.get(Calendar.MONTH)); // 7 = August
        System.out.println("Day of Week: " + cal2.get(Calendar.DAY_OF_WEEK));
        
        // Test cho ngày 2/8/2025
        Calendar cal3 = Calendar.getInstance();
        cal3.set(2025, 7, 2);
        
        System.out.println("\n=== 2/8/2025 ===");
        System.out.println("Week of Month: " + cal3.get(Calendar.WEEK_OF_MONTH));
        System.out.println("Week of Year: " + cal3.get(Calendar.WEEK_OF_YEAR));
        System.out.println("Month: " + cal3.get(Calendar.MONTH));
        System.out.println("Day of Week: " + cal3.get(Calendar.DAY_OF_WEEK));
        
        // Test cả tuần đầu tháng 8
        System.out.println("\n=== Tuần đầu tháng 8/2025 ===");
        for (int day = 1; day <= 7; day++) {
            Calendar cal = Calendar.getInstance();
            cal.set(2025, 7, day);
            System.out.println("Ngày " + day + "/8: Week of Month = " + cal.get(Calendar.WEEK_OF_MONTH) + 
                             ", Day of Week = " + cal.get(Calendar.DAY_OF_WEEK));
        }
    }
}