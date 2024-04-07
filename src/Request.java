import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
public class Request {
    private final int requestID;
    private final int startingFloor;
    private final int destinationFloor;
    private boolean finished;

    Request(int requestID, int startingFloor, int destinationFloor) throws IllegalArgumentException {
        this.requestID = requestID;
        finished = false;
        if (startingFloor == destinationFloor) {
            throw new IllegalArgumentException("starting floor can't be equal to destination floor");
        }
        this.startingFloor = startingFloor;
        this.destinationFloor = destinationFloor;
    }

    public int getRequestID() {
        return requestID;
    }

    public int getStartingFloor() {
        return startingFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public void complete() {
        this.finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * Converts this Request into a string that can be put into a UDP packet
     * @return String that can be put into a UDP packet
     */
    public String convertToPacketMessage() {
        int f;
        if(this.isFinished()){
            f = 1;
        } else {
            f = 0;
        }
        return String.valueOf(this.requestID) + "," + String.valueOf(this.startingFloor) + "," + String.valueOf(this.destinationFloor) + "," + String.valueOf(f);
    }

    /**
     * Returns a Request object from a UDP packet
     * @param packet the packet to be parsed into a Request
     * @return Request according to the data in the packet
     */
    public static Request parsePacket(DatagramPacket packet) throws IllegalArgumentException {
        byte[] d = packet.getData();
        String m = new String(d);
        return parseString(m);
    }

    /**
     * Returns a Request object from a string, following the proper formatting
     * @param message the string to be parsed into a Request
     * @return Request according to the data in the string
     */
    public static Request parseString(String message) throws IllegalArgumentException {
        String[] pm = message.split(",");// message format will be requestID,startingFloor,destinationFloor,f
        Request ret = new Request(Integer.valueOf(pm[0]),Integer.valueOf(pm[1]), Integer.valueOf(pm[2]));
        char temp = pm[3].charAt(0); // Separate "finished" indicator bit

        if(Character.getNumericValue(temp) == 1){
            ret.complete();
        }
        return ret;
    }
}
