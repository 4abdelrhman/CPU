import java.util.*;

class Process {

    private String name;
    private int arrivalTime;
    private int burstTime;
    private int remainingTime;
    private int priority;
    private int quantum;
    private List<Integer> quantumHistory = new ArrayList<>();
    private int waitingTime;
    private int turnaroundTime;
    private int completionTime;

    // extra fields for AG
    private int remainingBurstTime;
    private int remainingQuantum;

    public Process(String name, int arrival, int burst, int priority, int quantum) {
        this.name = name;
        this.arrivalTime = arrival;
        this.burstTime = burst;
        this.remainingTime = burst;
        this.priority = priority;
        this.quantum = quantum;
        if (quantum > 0) {
            quantumHistory.add(quantum);
        }
        this.remainingBurstTime = burst;
        this.remainingQuantum = quantum;
    }

    public String getName() { return name; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }

    public int getRemainingTime() { return remainingTime; }
    public void setRemainingTime(int t) { this.remainingTime = t; }

    public int getPriority() { return priority; }
    public void setPriority(int p) { this.priority = p; }

    public int getQuantum() { return quantum; }
    public void setQuantum(int q) {
        this.quantum = q;
        this.quantumHistory.add(q);
        // keep AG state consistent
        this.remainingQuantum = q;
    }

    public List<Integer> getQuantumHistory() { return quantumHistory; }

    public int getWaitingTime() { return waitingTime; }
    public void setWaitingTime(int w) { this.waitingTime = w; }

    public int getTurnaroundTime() { return turnaroundTime; }
    public void setTurnaroundTime(int t) { this.turnaroundTime = t; }

    public int getCompletionTime() { return completionTime; }
    public void setCompletionTime(int c) { this.completionTime = c; }

    // ---- extra getters/setters used only by AG ----
    public int getRemainingBurstTime() { return remainingBurstTime; }
    public void setRemainingBurstTime(int v) { this.remainingBurstTime = v; }

    public int getRemainingQuantum() { return remainingQuantum; }
    public void setRemainingQuantum(int v) { this.remainingQuantum = v; }
}
