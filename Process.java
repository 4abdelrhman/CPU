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
    }
    public List<Integer> getQuantumHistory() { return quantumHistory; }

    public int getWaitingTime() { return waitingTime; }
    public void setWaitingTime(int w) { this.waitingTime = w; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public void setTurnaroundTime(int t) { this.turnaroundTime = t; }
    public int getCompletionTime() { return completionTime; }
    public void setCompletionTime(int c) { this.completionTime = c; }
}
