import java.util.*;

class AGScheduler {

    public SchedulerResult run(List<Process> processes) {
        List<Process> procs = copyProcesses(processes);
        List<String> executionOrder = new ArrayList<>();

        LinkedList<Process> ready = new LinkedList<>();
        int time = 0;

        while (procs.stream().anyMatch(p -> p.getRemainingBurstTime() > 0)) {

            // enqueue arrived, unfinished processes
            for (Process p : procs) {
                if (p.getArrivalTime() <= time &&
                        p.getRemainingBurstTime() > 0 &&
                        !ready.contains(p)) {
                    ready.add(p);
                }
            }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            Process current = ready.poll();
            executionOrder.add(current.getName());

            // scenario (i) at start of cycle
            if (current.getRemainingQuantum() <= 0 &&
                    current.getRemainingBurstTime() > 0) {
                int newQ = current.getQuantum() + 2;
                current.setQuantum(newQ);
                current.setRemainingQuantum(newQ);
                ready.add(current);
                continue;
            }

            int q = current.getQuantum();
            int rq = current.getRemainingQuantum();
            int fcfsPart = (int) Math.ceil(q * 0.25);
            int prioPart = (int) Math.ceil(q * 0.25);

            // ===== FCFS phase =====
            int fcfsTime = Math.min(fcfsPart, rq);
            time = runFor(current, fcfsTime, time, procs, ready);
            rq = current.getRemainingQuantum();

            if (current.getRemainingBurstTime() == 0) {
                current.setCompletionTime(time);
                current.setQuantum(0);          // scenario (iv)
                current.setRemainingQuantum(0);
                continue;
            }

            // ===== Priority phase =====
            int prioTime = Math.min(prioPart, rq);
            Process highest = highestPriorityProcess(procs, time);

            if (highest != null && highest.getName().equals(current.getName())) {
                // still highest priority → execute priority chunk
                time = runFor(current, prioTime, time, procs, ready);
                rq = current.getRemainingQuantum();

                if (current.getRemainingBurstTime() == 0) {
                    current.setCompletionTime(time);
                    current.setQuantum(0);      // scenario (iv)
                    current.setRemainingQuantum(0);
                    continue;
                }

                if (rq <= 0 && current.getRemainingBurstTime() > 0) {
                    // scenario (i): used all quantum
                    int newQ = current.getQuantum() + 2;
                    current.setQuantum(newQ);
                    current.setRemainingQuantum(newQ);
                    ready.add(current);
                    continue;
                }

                // ===== SJF phase =====
                Process shortest = shortestProcess(procs, time);

                if (shortest != null &&
                        (shortest.getName().equals(current.getName()) ||
                                shortest.getRemainingBurstTime() == current.getRemainingBurstTime())) {

                    // still shortest: run whole remaining quantum
                    int runTime = current.getRemainingQuantum();
                    time = runFor(current, runTime, time, procs, ready);

                    if (current.getRemainingBurstTime() == 0) {
                        current.setCompletionTime(time);
                        current.setQuantum(0);  // scenario (iv)
                        current.setRemainingQuantum(0);
                        continue;
                    }

                    if (current.getRemainingQuantum() <= 0 &&
                            current.getRemainingBurstTime() > 0) {
                        // scenario (i)
                        int newQ = current.getQuantum() + 2;
                        current.setQuantum(newQ);
                        current.setRemainingQuantum(newQ);
                        ready.add(current);
                    }

                } else {
                    // different shortest job exists → scenario (iii)
                    int add = current.getRemainingQuantum();
                    int newQ = current.getQuantum() + add;
                    current.setQuantum(newQ);
                    current.setRemainingQuantum(newQ);
                    ready.add(current);

                    if (shortest != null && ready.contains(shortest)) {
                        ready.remove(shortest);
                        ready.addFirst(shortest);
                    }
                }

            } else {
                // lost highest priority → scenario (ii)
                int add = (int) Math.ceil(current.getRemainingQuantum() / 2.0);
                int newQ = current.getQuantum() + add;
                current.setQuantum(newQ);
                current.setRemainingQuantum(newQ);
                ready.add(current);

                if (highest != null && ready.contains(highest)) {
                    ready.remove(highest);
                    ready.addFirst(highest);
                }
            }
        }

        // metrics
        for (Process p : procs) {
            int tat = p.getCompletionTime() - p.getArrivalTime();
            int wt = tat - p.getBurstTime();
            p.setTurnaroundTime(tat);
            p.setWaitingTime(wt);
        }

        return new SchedulerResult(executionOrder, procs);
    }

    // run current for up to 't' time units (respecting remainingQuantum and remainingBurstTime)
    private int runFor(Process cur, int t, int time,
                       List<Process> procs, LinkedList<Process> ready) {
        int run = Math.min(t, cur.getRemainingQuantum());
        for (int i = 0; i < run && cur.getRemainingBurstTime() > 0; i++) {
            cur.setRemainingBurstTime(cur.getRemainingBurstTime() - 1);
            cur.setRemainingQuantum(cur.getRemainingQuantum() - 1);
            time++;
            for (Process p : procs) {
                if (p.getArrivalTime() == time &&
                        p.getRemainingBurstTime() > 0 &&
                        !ready.contains(p) &&
                        p != cur) {
                    ready.add(p);
                }
            }
        }
        return time;
    }

    private Process highestPriorityProcess(List<Process> procs, int time) {
        Process best = null;
        for (Process p : procs) {
            if (p.getArrivalTime() <= time && p.getRemainingBurstTime() > 0) {
                if (best == null ||
                        p.getPriority() < best.getPriority() ||
                        (p.getPriority() == best.getPriority() &&
                                p.getArrivalTime() < best.getArrivalTime())) {
                    best = p;
                }
            }
        }
        return best;
    }

    private Process shortestProcess(List<Process> procs, int time) {
        Process best = null;
        for (Process p : procs) {
            if (p.getArrivalTime() <= time && p.getRemainingBurstTime() > 0) {
                if (best == null ||
                        p.getRemainingBurstTime() < best.getRemainingBurstTime()) {
                    best = p;
                }
            }
        }
        return best;
    }

    private List<Process> copyProcesses(List<Process> original) {
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
