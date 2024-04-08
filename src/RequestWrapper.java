import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RequestWrapper { //Meant only for scheduler
    //Combines the request data and Elevator data
    private Request request;
    private ElevatorSchedulerData elevator;
    private final LocalDateTime receiveTime;
    private LocalDateTime completetionTime;


    RequestWrapper(Request request, ElevatorSchedulerData elevator) {
        this.request = request;
        this.elevator = elevator;
        this.receiveTime = LocalDateTime.now();
    }

    public void complete() {
        completetionTime = LocalDateTime.now();
        logToFile("log.txt");
    }

    private void logToFile(String fileName) {
        try {
            FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            String completeRequest = elevator.getSocketNumber() + ", " + receiveTime.toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString() + ", " + completetionTime.toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString() + ", " + request.getStartingFloor() + ", " + request.getDestinationFloor();
            bw.write(completeRequest);
            bw.newLine();
            bw.close();
        } catch (IOException e) {

        }

    }

    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }

    public LocalDateTime getCompletetionTime() { return completetionTime; }
    public Request getRequest() {
        return request;
    }
    public ElevatorSchedulerData getElevator() {
        return elevator;
    }

    public void setRequest(Request r) {
        this.request = r;
    }

    public void setElevator(ElevatorSchedulerData e) {
        this.elevator = e;
    }
}
