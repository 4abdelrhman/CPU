import java.util.*;

class PriorityScheduler {

    public SchedulerResult run(List<Process> processes, int contextSwitch, int agingInterval) {
        // Work on a copy so other schedulers are unaffected
        List<Process> procs = copy(processes);
        List<String> execution = new ArrayList<>();

        int time = 0;
        int completed = 0;
        int n = procs.size();
        Process running = null;
        String lastRecorded = null;

        while (completed < n) {

            // 1) Apply aging at the BEGINNING of the tick (on multiples of agingInterval)
            if (time > 0 && time % agingInterval == 0) {
                for (Process p : procs) {
                    if (p.getArrivalTime() <= time &&
                            p.getRemainingTime() > 0 &&
                            p != running) {
                        p.setPriority(p.getPriority() - 1);
                    }
                }
            }

            // 2) Build ready queue (all arrived and not finished)
            List<Process> ready = new ArrayList<>();
            for (Process p : procs) {
                if (p.getArrivalTime() <= time && p.getRemainingTime() > 0) {
                    ready.add(p);
                }
            }

            // If nothing is ready, advance time
            if (ready.isEmpty()) {
                time++;
                running = null;
                lastRecorded = null;
                continue;
            }

            // 3) Choose best process:
            //    lowest priority value, then earlier arrival, then name
            ready.sort((a, b) -> {
                if (a.getPriority() != b.getPriority()) {
                    return Integer.compare(a.getPriority(), b.getPriority());
                }
                if (a.getArrivalTime() != b.getArrivalTime()) {
                    return Integer.compare(a.getArrivalTime(), b.getArrivalTime());
                }
                return a.getName().compareTo(b.getName());
            });
            Process best = ready.get(0);

            // 4) Handle preemption + context switch
            if (running != null && running != best) {
                // pay context switch cost (CPU idle)
                time += contextSwitch;
                running = null;
                lastRecorded = null;
                // after context switch, go back and re-evaluate (new arrivals / aging may apply)
                continue;
            }

            // 5) Run the chosen process for 1 time unit
            running = best;

            if (!running.getName().equals(lastRecorded)) {
                execution.add(running.getName());
                lastRecorded = running.getName();
            }

            running.setRemainingTime(running.getRemainingTime() - 1);
            time++;

            // 6) If finished, record completion and clear running
            if (running.getRemainingTime() == 0) {
                running.setCompletionTime(time);
                completed++;
                running = null;
                lastRecorded = null;
            }
        }

        // 7) Compute waiting and turnaround times
        for (Process p : procs) {
            int tat = p.getCompletionTime() - p.getArrivalTime();
            int wt  = tat - p.getBurstTime();
            p.setTurnaroundTime(tat);
            p.setWaitingTime(wt);
        }

        return new SchedulerResult(execution, procs);
    }

    private List<Process> copy(List<Process> original) {
        List<Process> res = new ArrayList<>();
        for (Process p : original) {
            res.add(new Process(
                    p.getName(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getPriority(),
                    p.getQuantum()
            ));
        }
        return res;
    }
}
