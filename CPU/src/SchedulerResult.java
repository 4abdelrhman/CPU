import java.util.*;

class SchedulerResult {
    private List<String> executionOrder;
    private List<Process> processResults;
    private double averageWaitingTime;
    private double averageTurnaroundTime;

    public SchedulerResult(List<String> executionOrder, List<Process> processResults) {
        this.executionOrder = executionOrder;
        this.processResults = processResults;
        computeAverages();
    }

    private void computeAverages() {
        int n = processResults.size();
        int sumW = 0, sumT = 0;
        for (Process p : processResults) {
            sumW += p.getWaitingTime();
            sumT += p.getTurnaroundTime();
        }
        this.averageWaitingTime = (double) sumW / n;
        this.averageTurnaroundTime = (double) sumT / n;
    }

    public List<String> getExecutionOrder() { return executionOrder; }
    public List<Process> getProcessResults() { return processResults; }
    public double getAverageWaitingTime() { return averageWaitingTime; }
    public double getAverageTurnaroundTime() { return averageTurnaroundTime; }
}
