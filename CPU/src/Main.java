import java.io.File;
import java.util.*;


public class Main {

    public static void main(String[] args) throws Exception {
        String folderPath = "tests";
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("ERROR: 'tests' folder not found!");
            return;
        }

        TestCaseLoader loader = new TestCaseLoader();
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No test files found in testcases folder!");
            return;
        }

        for (File f : files) {
            if (!f.getName().endsWith(".json")) continue;

            System.out.println("\n============================================");
            System.out.println("TEST FILE: " + f.getName());
            System.out.println("============================================");

            try {
                // Check if it's AG test (starts with AG_test)
                if (f.getName().startsWith("AG_test")) {
                    AGInput in = loader.loadAG(f.getPath());
                    AGScheduler ag = new AGScheduler();
                    SchedulerResult r = ag.run(in.processes);
                    printResult("AG Scheduler", r, true);
                }
                // Otherwise it's a multi-scheduler test (starts with test_)
                else if (f.getName().startsWith("test_")) {
                    MultiInput in = loader.loadMulti(f.getPath());

                    SJFScheduler sjf = new SJFScheduler();
                    RRScheduler rr = new RRScheduler();
                    PriorityScheduler pr = new PriorityScheduler();

                    SchedulerResult rsjf = sjf.run(in.processes, in.contextSwitch);
                    SchedulerResult rrr = rr.run(in.processes, in.contextSwitch, in.rrQuantum);
                    SchedulerResult rpr = pr.run(in.processes, in.contextSwitch, in.agingInterval);

                    printResult("SJF Scheduler", rsjf, false);
                    printResult("Round Robin Scheduler", rrr, false);
                    printResult("Priority Scheduler", rpr, false);
                }
            } catch (Exception e) {
                System.out.println("ERROR processing " + f.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void printResult(String name, SchedulerResult r, boolean printQuantumHistory) {
        System.out.println("\n---------- " + name + " ----------");

        // Compress execution order - only show when process changes
        List<String> compressed = compressExecutionOrder(r.getExecutionOrder());
        System.out.println("Execution Order: " + compressed);

        System.out.println("\nProcess Results:");
        for (Process p : r.getProcessResults()) {
            System.out.print("  " + p.getName());
            System.out.print(" - Waiting Time: " + p.getWaitingTime());
            System.out.print(", Turnaround Time: " + p.getTurnaroundTime());
            if (printQuantumHistory) {
                System.out.print(", Quantum History: " + p.getQuantumHistory());
            }
            System.out.println();
        }
        System.out.printf("\nAverage Waiting Time: %.1f\n", r.getAverageWaitingTime());
        System.out.printf("Average Turnaround Time: %.1f\n", r.getAverageTurnaroundTime());
    }

    // Compress consecutive identical process names into single entry
    private static List<String> compressExecutionOrder(List<String> fullOrder) {
        List<String> compressed = new ArrayList<>();
        if (fullOrder.isEmpty()) return compressed;

        String last = fullOrder.get(0);
        compressed.add(last);

        for (int i = 1; i < fullOrder.size(); i++) {
            if (!fullOrder.get(i).equals(last)) {
                compressed.add(fullOrder.get(i));
                last = fullOrder.get(i);
            }
        }

        return compressed;
    }
}
