import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Problem2 {

    private static final int NUM_THREADS = 8;
    private static final List<Integer> highTemps = Collections.synchronizedList(new ArrayList<>());
    private static final List<Integer> lowTemps = Collections.synchronizedList(new ArrayList<>());
    private static final List<Integer[]> highLowMinutePairs = Collections.synchronizedList(new ArrayList<>());
    private static PrintWriter pw;
    private static int currentHour = 0;
    private static int currentMinute = 0;

    public static void main(String[] args) {

        // Create the TemperatureReaders
        System.out.println("Creating TemperatureReaders...");
        TemperatureProbe[] probes = new TemperatureProbe[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            probes[i] = new TemperatureProbe();
        }
        System.out.println("done.");

        // Create the ProbeScanner
        System.out.println("Creating ProbeScanner...");
        ProbeScanner ps = new ProbeScanner(probes);
        System.out.println("done.");

        // For a full 24 hours, run the ProbeScanner every minute
        for (int h = 0; h < 24; h++) {
            currentHour = h;
            for (int m = 0; m < 60; m++) {
                currentMinute = m;
                ps.run();
                // Calculate the high-low pair for the minute, and store it to the highLowMinutePairs list
                // Highest temp will always be last in the highs, and lowest temp will always be first in the lows, this is because of the sorting
                Integer[] highLowPair = new Integer[2];
                highLowPair[0] = highTemps.get(4);
                highLowPair[1] = lowTemps.get(0);
                highLowMinutePairs.add(highLowPair);
            }
            // Each hour, write the hourly report. Use the PrintWriter, the naming format will be "hourly_report_H.txt"
            writeHourlyReport();

            // Clear the high, low, and pair lists
            highTemps.clear();
            lowTemps.clear();
            highLowMinutePairs.clear();
        }


    }

    public static synchronized void addTemperature(int temp) {
        // check if the temperature is higher than any of the current temperatures in the highs
        // if the list is not full, then you can just add it in
        if (highTemps.size() < 5) {
            highTemps.add(temp);
            Collections.sort(highTemps);

        // if the list is full, check against the current temps, and replace if necessary, once replaced, break out of the loop and sort
        } else {
            for(int i = 0; i < highTemps.size(); i++) {
                if (temp > highTemps.get(i)) {
                    highTemps.set(i, temp);
                    break;
                }
            }
            Collections.sort(highTemps);
        }

        // check if the temperature is lower than any of the current temperatures in the lows
        // if the list is not full, then you can just add it in
        if (lowTemps.size() < 5) {
            lowTemps.add(temp);
            Collections.sort(lowTemps);

        // if the list is full, check against the current temps, and replace if necessary, once replaced, break out of the loop and sort
        } else {
            for(int i = 0; i < lowTemps.size(); i++) {
                if (temp < lowTemps.get(i)) {
                    lowTemps.set(i, temp);
                    break;
                }
            }
            Collections.sort(lowTemps);
        }
    }

    private static void writeHourlyReport() {
        // Create the new PrintWriter for the hourly report
        try{
            // If "hourly_reports" folder does not exist, create it
            if(new File("hourly_reports").mkdirs())
                System.out.println("Created hourly_reports folder");

            pw = new PrintWriter("hourly_reports/hourly_report_" + (currentHour < 10 ? "0":"") + currentHour + ".txt");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Create the hourly report string
        StringBuilder reportString = new StringBuilder("Hourly Report:\n");

        // Write the 5 highest and 5 lowest temperatures for the hour
        reportString.append("Highest Temperatures:\n");
        for (Integer highTemp : highTemps) {
            reportString.append("\t")
                    .append((highTemp < 0 ? "":" "))
                    .append(highTemp)
                    .append("F\n");
        }

        reportString.append("Lowest Temperatures:\n");
        for (Integer lowTemp : lowTemps) {
            reportString.append("\t")
                    .append((lowTemp < 0 ? "":" "))
                    .append(lowTemp)
                    .append("F\n");
        }

        // Using the high-low pairs, calculate the largest temperature change over a 10-minute period within the hour
        // Store the starting minute of that interval with the largest temperature change
        int largestChange = 0;
        int startMinute = 0;
        for (int i = 0; i < highLowMinutePairs.size() - 10; i++) {
            int high = highLowMinutePairs.get(i)[0];
            int low = highLowMinutePairs.get(i + 10)[1];
            int change = high - low;
            if (change > largestChange) {
                largestChange = change;
                startMinute = i;
            }
        }
        reportString.append("Largest Temperature Change Over 10 Minutes:\n\t")
                .append(largestChange)
                .append("F\n");

        reportString.append("Interval for largest change:\n\t")
                .append(currentHour < 10 ? "0":"")
                .append(currentHour).append(":")
                .append(startMinute).append(" - ")
                .append(currentHour < 10 ? "0":"")
                .append(currentHour).append(":")
                .append(startMinute + 10)
                .append("\n");

        // Write the hourly report to the file
        pw.write(reportString.toString());
        pw.close();
    }

    private static class TemperatureProbe implements Runnable {

        public TemperatureProbe() {
        }

        @Override
        public void run() {
            // read the temperature (this is just a random integer value between -100F and 70F, inclusive)
            int temp = (int) (Math.random() * 171) - 100;
            addTemperature(temp);
        }
    }

    private static class ProbeScanner implements Runnable {
        private final TemperatureProbe[] probes;

        public ProbeScanner(TemperatureProbe[] probes) {
            this.probes = probes;
        }

        @Override
        public void run() {
            // Create array to store threads
            Thread[] threads = new Thread[NUM_THREADS];
            for(int i = 0; i < NUM_THREADS; i++) {
                threads[i] = new Thread(probes[i]);
                threads[i].start();
            }


            for (int i = 0; i < NUM_THREADS; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
