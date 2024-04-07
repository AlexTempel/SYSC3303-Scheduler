import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        int schedulerPort = 20000;
        int infoPort = 20001;

        ArrayList<ElevatorSchedulerData> elevatorList = new ArrayList<>();

        int elevator1Port = 15001;
        InetAddress elevator1IP = InetAddress.getLoopbackAddress();
        ElevatorSchedulerData elevator1 = new ElevatorSchedulerData(elevator1Port, elevator1IP);
        elevatorList.add(elevator1);

        int elevator2Port = 15002;
        InetAddress elevator2IP = InetAddress.getLoopbackAddress();
        ElevatorSchedulerData elevator2 = new ElevatorSchedulerData(elevator2Port, elevator2IP);
        elevatorList.add(elevator2);

        int elevator3Port = 15003;
        InetAddress elevator3IP = InetAddress.getLoopbackAddress();
        ElevatorSchedulerData elevator3 = new ElevatorSchedulerData(elevator3Port, elevator3IP);
        elevatorList.add(elevator3);

        int elevator4Port = 15004;
        InetAddress elevator4IP = InetAddress.getLoopbackAddress();
        ElevatorSchedulerData elevator4 = new ElevatorSchedulerData(elevator4Port, elevator4IP);
        elevatorList.add(elevator4);

        try {
            SchedulerSubsystem schedulerSubsystem = new SchedulerSubsystem(schedulerPort, infoPort, elevatorList);

            Thread schedulerThread = new Thread(schedulerSubsystem);
            schedulerThread.start();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}