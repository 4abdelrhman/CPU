import java.util.*;

class RRScheduler {

    public SchedulerResult run(List<Process> processes, int contextSwitch, int rrQuantum) {
        List<Process> procs = copyProcesses(processes);
        List<String> execution = new ArrayList<>();

        Queue<Process> ready = new LinkedList<>();
        Set<Process> inQueue = new HashSet<>();
        int time = 0;
        int completed = 0;
        int n = procs.size();
        Process lastProcess = null;

        while (completed < n) {
            // Add newly arrived processes
            for (Process p : procs) {
                if (p.getArrivalTime() <= time && p.getRemainingTime() > 0 && !inQueue.contains(p)) {
                    ready.add(p);
                    inQueue.add(p);
                }
            }

            if (ready.isEmpty()) {
                time++;
                lastProcess = null;
                continue;
            }

            Process current = ready.poll();
            inQueue.remove(current);

            // Apply context switch if changing process
            if (lastProcess != null && lastProcess != current) {
                time += contextSwitch;
                // Check for new arrivals during context switch
                for (Process p : procs) {
                    if (p.getArrivalTime() <= time && p.getRemainingTime() > 0 && !inQueue.contains(p) && p != current) {
                        ready.add(p);
                        inQueue.add(p);
                    }
                }
            }

            // Execute for quantum or until done
            int executeTime = Math.min(rrQuantum, current.getRemainingTime());
            for (int i = 0; i < executeTime; i++) {
                execution.add(current.getName());
                current.setRemainingTime(current.getRemainingTime() - 1);
                time++;

                // Check for new arrivals during execution
                for (Process p : procs) {
                    if (p.getArrivalTime() == time && p.getRemainingTime() > 0 && !inQueue.contains(p)) {
                        ready.add(p);
                        inQueue.add(p);
                    }
                }
            }

            if (current.getRemainingTime() == 0) {
                current.setCompletionTime(time);
                completed++;
            } else {
                ready.add(current);
                inQueue.add(current);
            }

            lastProcess = current;
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
