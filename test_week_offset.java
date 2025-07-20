import java.util.Calendar;

public class test_week_offset {
    public static void main(String[] args) {
        // Goal created at timestamp 1752932126000 (Sat Jul 19 2025, Week 29)
        long goalCreatedAt = 1752932126000L;
        Calendar goalCalendar = Calendar.getInstance();
        goalCalendar.setTimeInMillis(goalCreatedAt);
        
        int goalWeek = goalCalendar.get(Calendar.WEEK_OF_YEAR);
        int goalYear = goalCalendar.get(Calendar.YEAR);
        
        // Current time (Week 30)
        Calendar currentCalendar = Calendar.getInstance();
        int currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int currentYear = currentCalendar.get(Calendar.YEAR);
        
        System.out.println("=== Goal Info ===");
        System.out.println("Goal week: " + goalWeek + ", year: " + goalYear);
        System.out.println("Current week: " + currentWeek + ", year: " + currentYear);
        
        // Method 1: Simple week difference (current implementation)
        int weekDiff1 = goalWeek - currentWeek;
        System.out.println("Week diff (simple): " + weekDiff1);
        
        // Method 2: Using Calendar set (like in GetGoalsUseCase)
        Calendar goalCalendarSet = Calendar.getInstance();
        goalCalendarSet.set(Calendar.WEEK_OF_YEAR, goalWeek);
        goalCalendarSet.set(Calendar.YEAR, goalYear);
        
        Calendar currentCalendarSet = Calendar.getInstance();
        currentCalendarSet.set(Calendar.WEEK_OF_YEAR, currentWeek);
        currentCalendarSet.set(Calendar.YEAR, currentYear);
        
        long weeksDiff = ((goalCalendarSet.getTimeInMillis() - currentCalendarSet.getTimeInMillis()) / (1000 * 60 * 60 * 24 * 7));
        System.out.println("Week diff (calendar set): " + weeksDiff);
        
        // Test getGoalsForWeek with weekOffset = -1
        Calendar testCalendar = Calendar.getInstance();
        testCalendar.add(Calendar.WEEK_OF_YEAR, -1);
        int targetWeek = testCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = testCalendar.get(Calendar.YEAR);
        
        System.out.println("\n=== Test weekOffset = -1 ===");
        System.out.println("Target week: " + targetWeek + ", year: " + targetYear);
        System.out.println("Goal matches? " + (goalWeek == targetWeek && goalYear == targetYear));
        
        // Test getGoalsForWeek with weekOffset = 0
        testCalendar = Calendar.getInstance();
        testCalendar.add(Calendar.WEEK_OF_YEAR, 0);
        targetWeek = testCalendar.get(Calendar.WEEK_OF_YEAR);
        targetYear = testCalendar.get(Calendar.YEAR);
        
        System.out.println("\n=== Test weekOffset = 0 ===");
        System.out.println("Target week: " + targetWeek + ", year: " + targetYear);
        System.out.println("Goal matches? " + (goalWeek == targetWeek && goalYear == targetYear));
    }
}