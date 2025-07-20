import java.util.Calendar;

public class test_goal_week {
    public static void main(String[] args) {
        // Goal duoc tao vao 1752932126000 (Thu 7, 19/7/2025)
        long goalCreatedAt = 1752932126000L;
        Calendar goalCalendar = Calendar.getInstance();
        goalCalendar.setTimeInMillis(goalCreatedAt);
        
        System.out.println("=== Goal duoc tao ===");
        System.out.println("Timestamp: " + goalCreatedAt);
        System.out.println("Ngay: " + goalCalendar.getTime());
        System.out.println("Week of Year: " + goalCalendar.get(Calendar.WEEK_OF_YEAR));
        System.out.println("Year: " + goalCalendar.get(Calendar.YEAR));
        System.out.println("Day of Week: " + goalCalendar.get(Calendar.DAY_OF_WEEK));
        
        // Thoi diem hien tai (Thu 2)
        Calendar currentCalendar = Calendar.getInstance();
        System.out.println("\n=== Thoi diem hien tai ===");
        System.out.println("Ngay: " + currentCalendar.getTime());
        System.out.println("Week of Year: " + currentCalendar.get(Calendar.WEEK_OF_YEAR));
        System.out.println("Year: " + currentCalendar.get(Calendar.YEAR));
        System.out.println("Day of Week: " + currentCalendar.get(Calendar.DAY_OF_WEEK));
        
        // Kiem tra xem co cung tuan khong
        boolean sameWeek = (goalCalendar.get(Calendar.WEEK_OF_YEAR) == currentCalendar.get(Calendar.WEEK_OF_YEAR)) &&
                          (goalCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR));
        
        System.out.println("\n=== So sanh ===");
        System.out.println("Goal va hien tai cung tuan? " + sameWeek);
        
        // Tinh weekOffset
        int goalWeek = goalCalendar.get(Calendar.WEEK_OF_YEAR);
        int currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int weekOffset = goalWeek - currentWeek;
        
        System.out.println("Week offset: " + weekOffset);
        
        // Test voi weekOffset = 0 (current week)
        Calendar testCalendar = Calendar.getInstance();
        testCalendar.add(Calendar.WEEK_OF_YEAR, 0);
        System.out.println("\n=== Test weekOffset = 0 ===");
        System.out.println("Target week: " + testCalendar.get(Calendar.WEEK_OF_YEAR));
        System.out.println("Target year: " + testCalendar.get(Calendar.YEAR));
    }
}