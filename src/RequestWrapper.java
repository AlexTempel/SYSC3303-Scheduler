import java.sql.Time;
import java.time.LocalDateTime;

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
