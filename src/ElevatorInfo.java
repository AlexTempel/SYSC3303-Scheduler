import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

public class ElevatorInfo {
    private final int floor;
    private final int numberOfPassengers;
    private final boolean upwards;
    private final boolean broken; //This could be more advanced with 3 values - working, temporarily broken (doors jammed), permanently broken (stuck between floors)

    /*
    This is intended to be a read only object, you only create it, if you want different values make a new one
     */

    ElevatorInfo(int floor, int numberOfPassengers, boolean upwards, boolean broken) {
        this.floor = floor;
        this.numberOfPassengers = numberOfPassengers;
        this.upwards = upwards;
        this.broken = broken;
    }

    public int getFloor() {
        return floor;
    }

    public int getNumberOfPassengers() {
        return numberOfPassengers;
    }
    public boolean goingUpwards() {
        return upwards;
    }
    public boolean goingDownwards() {
        return !upwards;
    }
    public boolean isBroken() {
        return broken;
    }

    public DatagramPacket convertToPacket() {
        //Jake code this to convert this object to a packet
        String message = String.valueOf(this.floor) + "," + String.valueOf(this.numberOfPassengers) + "," + String.valueOf(this.upwards) + "," + String.valueOf(this.broken);
        return new DatagramPacket(message.getBytes(StandardCharsets.UTF_8), message.getBytes().length);
    }

    public static ElevatorInfo parsePacket(DatagramPacket packet) {
        //Jake code this to convert a packet (created by convertToPacket) to this object.
        byte[] b = packet.getData();
        String m = new String(b);
        String[] pm = m.split(",");// message format will be requestID,startingFloor,destinationFloor,f
        ElevatorInfo info = new ElevatorInfo(Integer.valueOf(pm[0]),Integer.valueOf(pm[1]), Integer.valueOf(pm[2]) != 0, Integer.valueOf(pm[2]) != 0);
        //char temp = pm[3].charAt(0); // Separate "finished" indicator bit

        return info;
    }

}
