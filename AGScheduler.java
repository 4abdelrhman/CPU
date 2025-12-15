import java.util.*;

class AGScheduler {

    public SchedulerResult run(List<Process> processes) {
        List<Process> procs = copyProcesses(processes);
        List<String> execution = new ArrayList<>();

        LinkedList<Process> ready = new LinkedList<>();
        Set<Process> inQueue = new HashSet<>();

        int time = 0;
        int completed = 0;
        int n = procs.size();

        while (completed < n) {
            // Add newly arrived processes
            for (Process p : procs) {
                if (p.getArrivalTime() == time && p.getRemainingTime() > 0 && !inQueue.contains(p)) {
                    ready.add(p);
                    inQueue.add(p);
                }
            }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            // Get next process from ready queue
            Process current = ready.poll();
            inQueue.remove(current);

            int quantum = current.getQuantum();
            int fcfsPhase = (int) Math.ceil(quantum * 0.25);
            int priorityPhase = (int) Math.ceil(quantum * 0.25);
            int sjfPhase = quantum - fcfsPhase - priorityPhase;

            int executed = 0;
            boolean preempted = false;
            boolean completed_job = false;

            // Phase 1: FCFS (non-preemptive)
            while (executed < fcfsPhase && current.getRemainingTime() > 0) {
                execution.add(current.getName());
                current.setRemainingTime(current.getRemainingTime() - 1);
                executed++;
                time++;

                // Add arrivals
                for (Process p : procs) {
                    if (p.getArrivalTime() == time && p.getRemainingTime() > 0 && !inQueue.contains(p)) {
                        ready.add(p);
                        inQueue.add(p);
                    }
                }
            }

            if (current.getRemainingTime() == 0) {
                current.setCompletionTime(time);
                current.setQuantum(0);
                completed++;
                continue;
            }

            // Phase 2: Non-preemptive Priority (but can be preempted by higher priority NEW arrivals)
            while (executed < fcfsPhase + priorityPhase && current.getRemainingTime() > 0 && !preempted) {
                // Check if higher priority process just arrived
                Process highestPriority = null;
                for (Process p : ready) {
                    if (p.getPriority() < current.getPriority()) {
                        if (highestPriority == null || p.getPriority() < highestPriority.getPriority()) {
                            highestPriority = p;
                        }
                    }
                }

                if (highestPriority != null) {
                    // Scenario ii: Preempted in priority phase
                    int remaining = quantum - executed;
                    current.setQuantum(current.getQuantum() + (int) Math.ceil(remaining / 2.0));
                    ready.addFirst(current);
                    inQueue.add(current);
                    preempted = true;
                    break;
                }

                execution.add(current.getName());
                current.setRemainingTime(current.getRemainingTime() - 1);
                executed++;
                time++;

                // Add arrivals
                for (Process p : procs) {
                    if (p.getArrivalTime() == time && p.getRemainingTime() > 0 && !inQueue.contains(p)) {
                        ready.add(p);
                        inQueue.add(p);
                    }
                }
            }

            if (preempted) continue;

            if (current.getRemainingTime() == 0) {
                current.setCompletionTime(time);
                current.setQuantum(0);
                completed++;
                continue;
            }

            // Phase 3: Preemptive SJF
            while (executed < quantum && current.getRemainingTime() > 0 && !preempted) {
                // Check for shorter job
                Process shortest = null;
                for (Process p : ready) {
                    if (p.getRemainingTime() < current.getRemainingTime()) {
                        if (shortest == null || p.getRemainingTime() < shortest.getRemainingTime()) {
                            shortest = p;
                        }
                    }
                }

                if (shortest != null) {
                    // Scenario iii: Preempted by shorter job
                    int remaining = quantum - executed;
                    current.setQuantum(current.getQuantum() + remaining);
                    ready.add(current);
                    inQueue.add(current);
                    preempted = true;
                    break;
                }

                execution.add(current.getName());
                current.setRemainingTime(current.getRemainingTime() - 1);
                executed++;
                time++;

                // Add arrivals
                for (Process p : procs) {
                    if (p.getArrivalTime() == time && p.getRemainingTime() > 0 && !inQueue.contains(p)) {
                        ready.add(p);
                        inQueue.add(p);
                    }
                }
            }

            if (preempted) continue;

            if (current.getRemainingTime() == 0) {
                // Scenario iv: Job completed
                current.setCompletionTime(time);
                current.setQuantum(0);
                completed++;
            } else if (executed >= quantum) {
                // Scenario i: Used all quantum
                current.setQuantum(current.getQuantum() + 2);
                ready.add(current);
                inQueue.add(current);
            }
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
