import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SchedulerSubsystem implements Runnable {

    /*
    Check for elevator updates
    Update Elevator's info
    Check for requests
    If it is a complete request, update elevator, add it to complete request list
    If it is a new request put it in pending list and outstanding list
    Try to send pending requests to elevators
     */

    private final DatagramSocket requestSocket;
    private final DatagramSocket infoSocket;
    private final ArrayList<ElevatorSchedulerData> elevatorList;
    private final ArrayList<RequestWrapper> completeRequestList;
    private final ArrayList<RequestWrapper> outstandingRequestList;
    private final ArrayList<RequestWrapper> pendingRequestList;
    private JTable table;
    private JLabel averageTimeLabel;
    private JLabel numberOfCompletedRequestsLabel;

    private enum SchedulerState {
        WAITINGFORREQUEST,
        WAITINGFORELEVATORUPDATE,
        UPDATINGINFO,
        CHECKINGPENDINGREQUESTS,
        SENDING
    }

    private SchedulerState currentState;

    SchedulerSubsystem(int requestSocketPort, int infoSocketPort, ArrayList<ElevatorSchedulerData> elevators) throws SocketException {
        currentState = SchedulerState.WAITINGFORREQUEST;

        requestSocket = new DatagramSocket(requestSocketPort);
        requestSocket.setSoTimeout(10);
        infoSocket = new DatagramSocket(infoSocketPort);
        infoSocket.setSoTimeout(10);

        completeRequestList = new ArrayList<>();
        outstandingRequestList = new ArrayList<>();
        pendingRequestList = new ArrayList<>();

        elevatorList = elevators;

        createConsole();
    }

    /**
     * This method will receive elevator info updates from the elevators of their positions and then update the elevator list
     * It will keep calling itself if it does not time out, this ensures that if there are a bunch of pending updates they are all cleared
     * @throws IOException if socket fails
     */
    public void checkForElevatorUpdates() throws IOException {
        if (currentState != SchedulerState.WAITINGFORELEVATORUPDATE) {
            if (currentState == SchedulerState.WAITINGFORREQUEST) {
                currentState = SchedulerState.WAITINGFORELEVATORUPDATE;
            } else {
                currentState = SchedulerState.WAITINGFORELEVATORUPDATE;
                System.out.println("Scheduler state: " + currentState);
            }
        }
        DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
        try {
            infoSocket.receive(receivePacket);
        } catch (SocketTimeoutException e) {
            return;
        }

        //Update ElevatorList
        updateElevators(new ElevatorSchedulerData(ElevatorInfo.parsePacket(receivePacket), receivePacket.getPort(), receivePacket.getAddress()));
        //Call this again
        //Recursively calling it again makes sure any pending updates get dealt with before sending any new requests out
        checkForElevatorUpdates();
    }

    /**
     * Updates the elevator list with the status of the elevator
     * @param elevator the Elevator object to update the list with
     */
    public void updateElevators(ElevatorSchedulerData elevator) {
        if (currentState != SchedulerState.UPDATINGINFO) {
            currentState = SchedulerState.UPDATINGINFO;
            System.out.println("Scheduler state: " + currentState);
        }
        for (ElevatorSchedulerData e : elevatorList) {
            if (e.compare(elevator)) {
                elevatorList.remove(e);
                elevatorList.add(elevator);

                outputConsole();
                return;
            }
        }
    }

    /**
     * Check the socket for any requests
     * If they are complete remove that request from the list of outstanding requests and update the elevator
     * If they are incomplete add it to the list of outstanding requests and pending requests
     */
    public void checkForRequests() throws IOException {
        if (currentState != SchedulerState.WAITINGFORREQUEST) {
            if (currentState == SchedulerState.WAITINGFORELEVATORUPDATE) {
                currentState = SchedulerState.WAITINGFORREQUEST;
            } else {
                currentState = SchedulerState.WAITINGFORREQUEST;
                System.out.println("Scheduler state: " + currentState);
            }
        }
        DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
        try {
            requestSocket.receive(receivePacket);
        } catch (SocketTimeoutException e) {
            return;
        }

        RequestWrapper request = new RequestWrapper(Request.parsePacket(receivePacket), findElevator(receivePacket.getPort()));
        if (request.getRequest().isFinished()) {
            //Remove request from list of outstanding requests
            for (RequestWrapper r : outstandingRequestList) {
                if (r.getRequest().getRequestID() == request.getRequest().getRequestID()) {
                    if (currentState != SchedulerState.UPDATINGINFO) {
                        currentState = SchedulerState.UPDATINGINFO;
                        System.out.println("Scheduler state: " + currentState);
                    }
                    //Set finished time
                    r.complete();
                    //Add it to the list of complete requests
                    completeRequestList.add(r);
                    //Remove it from the list of outstanding requests
                    outstandingRequestList.remove(r);
                    //Decrease the number of passengers (Assuming one passenger per request
                    //request.getElevator().decrementNumberOfPassengers();
                    //Set the floor to the destination floor
                    request.getElevator().setCurrentFloor(request.getRequest().getDestinationFloor());

                    outputConsole();
                    return;
                }
            }
            /*
            Remove from outstanding request list
            Add to complete request list
            Update Elevator
             */
        } else { //If it is not complete
            /*
            Add it to the list of outstanding requests
            Add it to the pending list of requests
             */
            if (currentState != SchedulerState.UPDATINGINFO) {
                currentState = SchedulerState.UPDATINGINFO;
                System.out.println("Scheduler state: " + currentState);
            }
            request.setElevator(null);
            outstandingRequestList.add(request);
            pendingRequestList.add(request);
            return;
        }
    }

    /**
     * Find the elevator that has this port number
     * @param portNumber port of the elevator you are trying to find
     * @return the elevator object from elevator list that matches that port number
     */
    public ElevatorSchedulerData findElevator(int portNumber) {
        for (ElevatorSchedulerData e : elevatorList) {
            if (e.getSocketNumber() == portNumber) {
                return e;
            }
        }
        return null;
    }

    /**
     * Try to send all the pending messages to elevators
     * If you can send one try with the next until you can't send any or the list is empty
     */
    public void clearPending() {
        if (pendingRequestList.isEmpty()) {
            return;
        }
        if (currentState != SchedulerState.CHECKINGPENDINGREQUESTS) {
            currentState = SchedulerState.CHECKINGPENDINGREQUESTS;
            System.out.println("Scheduler state: " + currentState);
        }
        if (selectElevator(pendingRequestList.getFirst())) {
            pendingRequestList.removeFirst();
            clearPending();
        }
    }

    /**
     * Select and send to the best elevator for this request
     * @param r The request to be sent
     * @return If request was successfully sent
     */
    public boolean selectElevator(RequestWrapper r) {
        /*
        Criteria for best elevator
        Not at capacity
        Going in right direction (if it is empty direction doesn't matter since it should not be moving)
        Closest
         */

        //If the elevator isn't full and not broken
        ArrayList<ElevatorSchedulerData> notFullElevators = new ArrayList<>();
        for (ElevatorSchedulerData e : elevatorList) {
            if (!e.isFull() && !e.isBroken()) {
                notFullElevators.add(e);
            }
        }

        //If the elevator is going in the direction of the starting floor of the request
        ArrayList<ElevatorSchedulerData> correctDirectionElevators = new ArrayList<>();
        for (ElevatorSchedulerData e : notFullElevators) {
            if (e.getCurrentFloor() > r.getRequest().getStartingFloor()) {
                if (e.isDownwards()) {
                    correctDirectionElevators.add(e);
                }
            } else if (e.getCurrentFloor() < r.getRequest().getStartingFloor()) {
                if (e.isUpwards()) {
                    correctDirectionElevators.add(e);
                }
            } else if (e.isEmpty() && e.getCurrentFloor() == r.getRequest().getStartingFloor()){ //If an elevator is moving (not empty) then it can pick up passengers on that floor
                correctDirectionElevators.add(e);
            }
        }

        //If the elevator is going in the same direction as the request and is heading towards the starting floor
        boolean requestDirectionUp;
        if (r.getRequest().getStartingFloor() > r.getRequest().getDestinationFloor()) {
            requestDirectionUp = false;
        } else {
            requestDirectionUp = true;
        }
        ArrayList<ElevatorSchedulerData> sameDirectionElevators = new ArrayList<>();
        for (ElevatorSchedulerData e : notFullElevators) {
            if (requestDirectionUp && e.isUpwards() && e.getCurrentFloor() < r.getRequest().getStartingFloor()) {
                sameDirectionElevators.add(e);
            } else if (!requestDirectionUp && e.isDownwards() && e.getCurrentFloor() > r.getRequest().getStartingFloor()) {
                sameDirectionElevators.add(e);
            }
        }

        ElevatorSchedulerData bestElevator = null;

        if (!sameDirectionElevators.isEmpty()) {
            bestElevator = findClosestElevator(sameDirectionElevators, r);
        } else if (!correctDirectionElevators.isEmpty()) {
            bestElevator  = findClosestElevator(correctDirectionElevators, r);
        } else if (!notFullElevators.isEmpty()){
            bestElevator = findClosestElevator(notFullElevators, r);
        }

        if (bestElevator == null) {
            return false;
        }

        //Send request to best elevator

        try {
            if (currentState != SchedulerState.SENDING) {
                currentState = SchedulerState.SENDING;
                System.out.println("Scheduler state: " + currentState);
            }

            requestSocket.connect(bestElevator.getIpAddress(), bestElevator.getSocketNumber());
            String message = r.getRequest().convertToPacketMessage();
            DatagramPacket sendPacket = new DatagramPacket(message.getBytes(StandardCharsets.UTF_8), message.getBytes().length);
            requestSocket.send(sendPacket);
            requestSocket.disconnect();
            //bestElevator.incrementNumberOfPassengers();
            r.setElevator(bestElevator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Finds the elevator that is the closest to the starting floor of the request
     * @param elevators List of elevators to check
     * @param r Request to check
     * @return The elevator that is the closest, null if none of them are
     */
    public ElevatorSchedulerData findClosestElevator(ArrayList<ElevatorSchedulerData> elevators,  RequestWrapper r) {
        if (elevators.isEmpty()) {
            return null;
        }
        ElevatorSchedulerData returnElevator = null;
        int smallestDifference = Math.abs(elevators.getFirst().getCurrentFloor() - r.getRequest().getStartingFloor());
        for (ElevatorSchedulerData e : elevators) {
            if (smallestDifference >= Math.abs(e.getCurrentFloor() - r.getRequest().getStartingFloor())) {
                smallestDifference = Math.abs(e.getCurrentFloor() - r.getRequest().getStartingFloor());
                returnElevator = e;
            }
        }
        return returnElevator;
    }

    /**
     * Set up the GUI frame with starting values
     */
    public void createConsole() {
        //Create Frame
        JFrame frame = new JFrame("Console");
        frame.setLayout(new BorderLayout());

        //Create Table
        String[] columnNames = {"Elevator", "Current Floor", "Number of Passengers", "Status"};
        Object[][] defaultData = new Object[elevatorList.size()][4];
        //Put values into table
        sortElevator();
        for (int i = 0; i < elevatorList.size(); i++) {
            defaultData[i][0] = elevatorList.get(i).getSocketNumber();
            defaultData[i][1] = elevatorList.get(i).getCurrentFloor();
            defaultData[i][2] = elevatorList.get(i).getNumberOfPassengers();
            defaultData[i][3] = elevatorList.get(i).isBroken() ? "Broken" : "Normal";
        }
        //Put the column names
        JPanel columnNamesPanel = new JPanel(new GridLayout(1, columnNames.length));
        for (String columnName : columnNames) {
            JLabel columnLabel = new JLabel(columnName);
            columnLabel.setHorizontalAlignment(JLabel.CENTER);
            columnNamesPanel.add(columnLabel);
        }
        frame.add(columnNamesPanel, BorderLayout.NORTH);
        table = new JTable(defaultData, columnNames);
        frame.add(table, BorderLayout.CENTER);

        //Text that says the average completion time and number of requests serviced
        JPanel summaryPanel = new JPanel(new GridLayout(2,1));
        averageTimeLabel = new JLabel(" Average time to complete requests: 0 seconds");
        summaryPanel.add(averageTimeLabel);
        numberOfCompletedRequestsLabel = new JLabel(" Completed requests: 0");
        summaryPanel.add(numberOfCompletedRequestsLabel);
        frame.add(summaryPanel, BorderLayout.SOUTH);

        //Center the frame and make it the right size
        int x = 600;
        int y = 80 + elevatorList.size() * 17;
        frame.setPreferredSize(new Dimension(x, y));
        frame.setResizable(false);
        frame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - x)/2, (Toolkit.getDefaultToolkit().getScreenSize().height-y)/2);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Updates the values displayed in the GUI
     */
    public void outputConsole() {
        //Update the console values
        sortElevator();
        for (int i = 0; i <elevatorList.size(); i++) {
            table.setValueAt(elevatorList.get(i).getSocketNumber(), i, 0);
            table.setValueAt(elevatorList.get(i).getCurrentFloor(), i, 1);
            table.setValueAt(elevatorList.get(i).getNumberOfPassengers(), i, 2);
            table.setValueAt(elevatorList.get(i).isBroken() ? "Broken" : "Normal", i, 3);
        }
        //Calculate average completion time
        long avgCompleteTime = 0;
        for(int j = 0; j < completeRequestList.size(); j++){
            long timeToComplete = ChronoUnit.SECONDS.between(completeRequestList.get(j).getReceiveTime(),completeRequestList.get(j).getCompletetionTime());
            avgCompleteTime += timeToComplete;
        }
        if (completeRequestList.size() != 0) {
            avgCompleteTime = avgCompleteTime / completeRequestList.size();
        } else {
            avgCompleteTime = 0;
        }
        //Update text
        averageTimeLabel.setText(" Average time to complete requests: " + avgCompleteTime + " seconds");
        numberOfCompletedRequestsLabel.setText(" Completed requests: " + completeRequestList.size());
    }

    /**
     * Sort the elements of ElevatorList
     * Important so that console output stays in the same order
     */
    private void sortElevator() {
        elevatorList.sort(new Comparator<ElevatorSchedulerData>() {
            @Override
            public int compare(ElevatorSchedulerData e1, ElevatorSchedulerData e2) {
                return Integer.compare(e1.getSocketNumber(), e2.getSocketNumber());
            }
        });
    }

    @Override
    public void run() {
        while (true) {
            try {
                checkForElevatorUpdates();
                checkForRequests();
            } catch (IOException e) {
                continue;
            }
            clearPending();
        }
    }

    public ArrayList<ElevatorSchedulerData> getElevatorList() {
        return elevatorList;
    }

    public ArrayList<RequestWrapper> getCompleteRequestList() {
        return completeRequestList;
    }

    public ArrayList<RequestWrapper> getOutstandingRequestList() {
        return outstandingRequestList;
    }

    public ArrayList<RequestWrapper> getPendingRequestList() {
        return pendingRequestList;
    }
}
