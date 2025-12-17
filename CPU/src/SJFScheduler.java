import java.util.*;

class SJFScheduler {

    public SchedulerResult run(List<Process> processes, int contextSwitch) {
        List<Process> procs = copyProcesses(processes);
        List<String> execution = new ArrayList<>();

        int time = 0;
        int completed = 0;
        int n = procs.size();
        Process lastProcess = null;

        while (completed < n) {
            // Get all ready processes
            List<Process> ready = new ArrayList<>();
            for (Process p : procs) {
                if (p.getArrivalTime() <= time && p.getRemainingTime() > 0) {
                    ready.add(p);
                }
            }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            // Select shortest remaining time
            Process shortest = ready.get(0);
            for (Process p : ready) {
                if (p.getRemainingTime() < shortest.getRemainingTime()) {
                    shortest = p;
                }
            }

            // Apply context switch if changing process
            if (lastProcess != null && lastProcess != shortest) {
                time += contextSwitch;
            }

            // Execute one unit
            execution.add(shortest.getName());
            shortest.setRemainingTime(shortest.getRemainingTime() - 1);
            time++;

            if (shortest.getRemainingTime() == 0) {
                shortest.setCompletionTime(time);
                completed++;
            }

            lastProcess = shortest;
        }

        // Calculate metrics
        for (Process p : procs) {
            int tat = p.getCompletionTime() - p.getArrivalTime();
            int wt = tat - p.getBurstTime();
            p.setTurnaroundTime(tat);
            p.setWaitingTime(wt);
        }

        return new SchedulerResult(execution, procs);
    }

    private List<Process> copyProcesses(List<Process> original) {
        List<Process> res = new ArrayList<>();
        for (Process p : original) {
            res.add(new Process(p.getName(), p.getArrivalTime(),
                    p.getBurstTime(), p.getPriority(), p.getQuantum()));
        }
        return res;
    }
}
