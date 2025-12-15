import java.util.*;

class PriorityScheduler {

    public SchedulerResult run(List<Process> processes, int contextSwitch, int agingInterval) {
        List<Process> procs = copyProcesses(processes);
        List<String> execution = new ArrayList<>();

        int time = 0;
        int completed = 0;
        int n = procs.size();
        Process lastProcess = null;
        int lastAgingTime = 0;

        while (completed < n) {
            // Apply aging every agingInterval
            if (time - lastAgingTime >= agingInterval && time > 0) {
                for (Process p : procs) {
                    if (p.getArrivalTime() <= time && p.getRemainingTime() > 0) {
                        p.setPriority(p.getPriority() - 1);
                    }
                }
                lastAgingTime = time;
            }

            // Get ready processes
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

            // Select highest priority (lowest number)
            Process best = ready.get(0);
            for (Process p : ready) {
                if (p.getPriority() < best.getPriority()) {
                    best = p;
                }
            }

            // Apply context switch if changing process
            if (lastProcess != null && lastProcess != best) {
                time += contextSwitch;
            }

            // Execute one unit
            execution.add(best.getName());
            best.setRemainingTime(best.getRemainingTime() - 1);
            time++;

            if (best.getRemainingTime() == 0) {
                best.setCompletionTime(time);
                completed++;
            }

            lastProcess = best;
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
