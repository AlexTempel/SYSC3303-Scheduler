import java.net.InetAddress;

public class ElevatorSchedulerData { //Meant only for Scheduler
    //This class is used by the scheduler to hold the data it knows about an Elevator
    private int currentFloor;
    private final int socketNumber;
    private final InetAddress ipAddress;
    private int numberOfPassengers;
    private boolean upwards;
    private final int capacity;
    private boolean broken;

    ElevatorSchedulerData(int socketNumber, InetAddress ipAddress) {
        currentFloor = 1;
        numberOfPassengers = 0;
        upwards = true;
        this.socketNumber = socketNumber;
        this.ipAddress = ipAddress;
        capacity = 5;
        broken = false;
    }

    ElevatorSchedulerData(ElevatorInfo info, int socketNumber, InetAddress ipAddress) {
        this.update(info);
        this.socketNumber = socketNumber;
        this.ipAddress = ipAddress;
        this.capacity = 5;
    }

    public void update(ElevatorInfo info) {
        this.broken = info.isBroken();
        this.currentFloor = info.getFloor();
        this.upwards = info.goingUpwards();
        this.numberOfPassengers = info.getNumberOfPassengers();
    }

    public boolean isBroken() {
        return broken;
    }
    public void setBroken(boolean broken) {
        this.broken = broken;
    }
    public int getCapacity() {
        return capacity;
    }

    public boolean isFull() {
        return numberOfPassengers >= capacity;
    }

    public boolean isEmpty() {
        return numberOfPassengers == 0;
    }

    public void setUpwards(boolean upwards) {
        this.upwards = upwards;
    }

    public boolean isUpwards() {
        return upwards;
    }

    public boolean isDownwards() {
        return !upwards;
    }

    public int getNumberOfPassengers() {
        return numberOfPassengers;
    }

    public void incrementNumberOfPassengers() {
        setNumberOfPassengers(getNumberOfPassengers() + 1);
        if (numberOfPassengers > capacity) {
            numberOfPassengers = capacity;
        }
    }

    public void decrementNumberOfPassengers() {
        setNumberOfPassengers(getNumberOfPassengers() - 1);
        if (numberOfPassengers < 0) {
            numberOfPassengers = 0;
        }
    }

    public void setNumberOfPassengers(int number) throws IllegalArgumentException {
        if (number < 0 || number > capacity) {
            throw new IllegalArgumentException("Number of passengers must be 0 or more and less than capacity");
        }
        numberOfPassengers = number;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }
    public void setCurrentFloor(int newFloor) {
        currentFloor = newFloor;
    }
    public int getSocketNumber() {
        return socketNumber;
    }
    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public boolean compare(ElevatorSchedulerData otherElevator) {
        return (this.socketNumber == otherElevator.socketNumber);
    }
}
