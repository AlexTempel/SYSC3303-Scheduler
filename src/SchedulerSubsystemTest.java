import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class SchedulerSubsystemTest {

    @Test
    void checkForElevatorUpdates() {
        int schedulerRequestPort = 20002;
        int schedulerInfoPort = 20001;

        int elevator1Port = 10001;
        int elevator2Port = 10002;
        int elevator3Port = 10003;
        int elevator4Port = 10004;
        ElevatorSchedulerData elevator1 = new ElevatorSchedulerData(elevator1Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator2 = new ElevatorSchedulerData(elevator2Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator3 = new ElevatorSchedulerData(elevator3Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator4 = new ElevatorSchedulerData(elevator4Port, InetAddress.getLoopbackAddress());

        ArrayList<ElevatorSchedulerData> initialElevatorList = new ArrayList<>();
        initialElevatorList.add(elevator1);
        initialElevatorList.add(elevator2);
        initialElevatorList.add(elevator3);
        initialElevatorList.add(elevator4);


        int testFloor = 10;
        int testNumberOfPassengers = 3;
        boolean testDirection = true;
        boolean testBroken = true;
        DatagramPacket testPacket = ((new ElevatorInfo(testFloor, testNumberOfPassengers, testDirection, testBroken)).convertToPacket());
        assertNotNull(testPacket);
        System.out.println("1");

        try {
            SchedulerSubsystem testScheduler = new SchedulerSubsystem(schedulerRequestPort, schedulerInfoPort, initialElevatorList);
            DatagramSocket testSocket = new DatagramSocket(elevator2Port);
            System.out.println("2");

            testSocket.connect(InetAddress.getLoopbackAddress(), schedulerInfoPort);
            testSocket.send(testPacket);
            testSocket.disconnect();

            System.out.println("3");

            testScheduler.checkForElevatorUpdates();
            System.out.println("4");

            ElevatorSchedulerData elevatorTested = null;
            for (ElevatorSchedulerData e : initialElevatorList) {
                if (e.getSocketNumber() == elevator2Port) {
                    elevatorTested = e;
                    break;
                }
            }

            System.out.println("5");
            assertEquals(elevatorTested.isBroken(), testBroken);
            assertEquals(elevatorTested.getCurrentFloor(), testFloor);
            assertEquals(elevatorTested.getNumberOfPassengers(), testNumberOfPassengers);
            assertEquals(elevatorTested.isUpwards(), testBroken);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }


    @Test
    void updateElevators() {
        int schedulerRequestPort = 20002;
        int schedulerInfoPort = 20001;

        int elevator1Port = 10001;
        int elevator2Port = 10002;
        int elevator3Port = 10003;
        int elevator4Port = 10004;
        ElevatorSchedulerData elevator1 = new ElevatorSchedulerData(elevator1Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator2 = new ElevatorSchedulerData(elevator2Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator3 = new ElevatorSchedulerData(elevator3Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator4 = new ElevatorSchedulerData(elevator4Port, InetAddress.getLoopbackAddress());

        ArrayList<ElevatorSchedulerData> initialElevatorList = new ArrayList<>();
        initialElevatorList.add(elevator1);
        initialElevatorList.add(elevator2);
        initialElevatorList.add(elevator3);
        initialElevatorList.add(elevator4);

        int testFloor = 10;
        int testNumberOfPassengers = 3;
        boolean testDirection = true;
        boolean testBroken = true;
        ElevatorSchedulerData testElevator = new ElevatorSchedulerData(elevator2Port, InetAddress.getLoopbackAddress());
        testElevator.setCurrentFloor(10);
        testElevator.setUpwards(testDirection);
        testElevator.setNumberOfPassengers(testNumberOfPassengers);
        testElevator.setBroken(testBroken);

        try {
            SchedulerSubsystem testSubsystem = new SchedulerSubsystem(schedulerRequestPort, schedulerInfoPort, initialElevatorList);
            //Try with an existing elevator
            testSubsystem.updateElevators(testElevator);

            assertFalse(initialElevatorList.contains(elevator2));
            assertTrue(initialElevatorList.contains(testElevator));

            //Try with a new elevator not in list
            ElevatorSchedulerData secondTestElevator = new ElevatorSchedulerData(10005, InetAddress.getLoopbackAddress());
            secondTestElevator.setBroken(true);

            testSubsystem.updateElevators(secondTestElevator);

            assertFalse(initialElevatorList.contains(secondTestElevator));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

    @Test
    void checkForRequests() {

    }

    @Test
    void findElevator() {
        int schedulerRequestPort = 20002;
        int schedulerInfoPort = 20001;

        int elevator1Port = 10001;
        int elevator2Port = 10002;
        int elevator3Port = 10003;
        int elevator4Port = 10004;
        ElevatorSchedulerData elevator1 = new ElevatorSchedulerData(elevator1Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator2 = new ElevatorSchedulerData(elevator2Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator3 = new ElevatorSchedulerData(elevator3Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator4 = new ElevatorSchedulerData(elevator4Port, InetAddress.getLoopbackAddress());

        ArrayList<ElevatorSchedulerData> initialElevatorList = new ArrayList<>();
        initialElevatorList.add(elevator1);
        initialElevatorList.add(elevator2);
        initialElevatorList.add(elevator3);
        initialElevatorList.add(elevator4);

        try {
            SchedulerSubsystem testScheduler = new SchedulerSubsystem(schedulerRequestPort, schedulerInfoPort, initialElevatorList);

            //Try with elevator that is in there
            assertEquals(elevator1, testScheduler.findElevator(elevator1Port));

            //Try with an elevator not in there
            assertNull(testScheduler.findElevator(12));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void selectElevator() {
        int schedulerRequestPort = 20002;
        int schedulerInfoPort = 20001;

        int elevator1Port = 10001;
        int elevator2Port = 10002;
        int elevator3Port = 10003;
        int elevator4Port = 10004;
        ElevatorSchedulerData elevator1 = new ElevatorSchedulerData(elevator1Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator2 = new ElevatorSchedulerData(elevator2Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator3 = new ElevatorSchedulerData(elevator3Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator4 = new ElevatorSchedulerData(elevator4Port, InetAddress.getLoopbackAddress());

        ArrayList<ElevatorSchedulerData> initialElevatorList = new ArrayList<>();
        initialElevatorList.add(elevator1);
        initialElevatorList.add(elevator2);
        initialElevatorList.add(elevator3);
        initialElevatorList.add(elevator4);

        try {
            SchedulerSubsystem testScheduler = new SchedulerSubsystem(schedulerRequestPort, schedulerInfoPort, initialElevatorList);

            RequestWrapper testRequest = new RequestWrapper(new Request(2, 10, 7), null);

            //Set the state of the elevators
            //Case where all are broken or full
            while (!elevator1.isFull()) {
                elevator1.incrementNumberOfPassengers();
            }
            elevator2.setBroken(true);
            elevator3.setBroken(true);
            elevator4.setBroken(true);

            assertFalse(testScheduler.selectElevator(testRequest));

            //Case when there's only one elevator
            elevator3.setBroken(false);

            DatagramSocket elevator3TestReceiveSocket = new DatagramSocket(elevator3Port);
            elevator3TestReceiveSocket.setSoTimeout(1000);


            assertTrue(testScheduler.selectElevator(testRequest));

            DatagramPacket testReceivePacket = new DatagramPacket(new byte[1024], 1024);
            elevator3TestReceiveSocket.receive(testReceivePacket);
            Request receivedRequest = Request.parsePacket(testReceivePacket);
            assertEquals(receivedRequest.getRequestID(), testRequest.getRequest().getRequestID());


            //Case when there is only one elevator going in the right direction
            elevator1.decrementNumberOfPassengers();
            elevator2.setBroken(false);
            elevator3.setBroken(false);
            elevator4.setBroken(false);

            elevator1.setCurrentFloor(12);
            elevator1.setUpwards(false);
            elevator2.setUpwards(true);
            elevator3.setUpwards(true);
            elevator4.setUpwards(true);

            DatagramSocket elevator1TestReceiveSocket = new DatagramSocket(elevator1Port);
            elevator1TestReceiveSocket.setSoTimeout(1000);
            DatagramPacket test2ReceivePacket = new DatagramPacket(new byte[1024], 1024);

            assertTrue(testScheduler.selectElevator(testRequest));


            elevator1TestReceiveSocket.receive(test2ReceivePacket);

            receivedRequest = Request.parsePacket(test2ReceivePacket);
            assertEquals(receivedRequest.getRequestID(), testRequest.getRequest().getRequestID());

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void findClosestElevator() {
        int schedulerRequestPort = 20002;
        int schedulerInfoPort = 20001;

        int elevator1Port = 10001;
        int elevator2Port = 10002;
        int elevator3Port = 10003;
        int elevator4Port = 10004;
        ElevatorSchedulerData elevator1 = new ElevatorSchedulerData(elevator1Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator2 = new ElevatorSchedulerData(elevator2Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator3 = new ElevatorSchedulerData(elevator3Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator4 = new ElevatorSchedulerData(elevator4Port, InetAddress.getLoopbackAddress());

        ArrayList<ElevatorSchedulerData> initialElevatorList = new ArrayList<>();
        initialElevatorList.add(elevator1);
        initialElevatorList.add(elevator2);
        initialElevatorList.add(elevator3);
        initialElevatorList.add(elevator4);

        try {
            SchedulerSubsystem testScheduler = new SchedulerSubsystem(schedulerRequestPort, schedulerInfoPort, initialElevatorList);

            RequestWrapper testRequest = new RequestWrapper(new Request(2, 5, 7), null);

            elevator1.setCurrentFloor(4);
            //Check that it selects the closest elevator
            assertEquals(elevator1, testScheduler.findClosestElevator(testScheduler.getElevatorList(), testRequest));

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void outputConsole() {
        int schedulerRequestPort = 20002;
        int schedulerInfoPort = 20001;

        int elevator1Port = 10001;
        int elevator2Port = 10002;
        int elevator3Port = 10003;
        int elevator4Port = 10004;
        ElevatorSchedulerData elevator1 = new ElevatorSchedulerData(elevator1Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator2 = new ElevatorSchedulerData(elevator2Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator3 = new ElevatorSchedulerData(elevator3Port, InetAddress.getLoopbackAddress());
        ElevatorSchedulerData elevator4 = new ElevatorSchedulerData(elevator4Port, InetAddress.getLoopbackAddress());

        ArrayList<ElevatorSchedulerData> initialElevatorList = new ArrayList<>();
        initialElevatorList.add(elevator1);
        initialElevatorList.add(elevator2);
        initialElevatorList.add(elevator3);
        initialElevatorList.add(elevator4);

        try {
            SchedulerSubsystem testScheduler = new SchedulerSubsystem(schedulerRequestPort, schedulerInfoPort, initialElevatorList);

            RequestWrapper testFinishedWrapper = new RequestWrapper(new Request(1, 3, 5),
                    new ElevatorSchedulerData(elevator1Port, InetAddress.getLoopbackAddress()));

            testScheduler.outputConsole();
            assertTrue(true);

            Thread.sleep(100000); //Test it more than a minute later

            testFinishedWrapper.complete();

            testScheduler.getCompleteRequestList().add(testFinishedWrapper);

            elevator2.incrementNumberOfPassengers();
            elevator2.incrementNumberOfPassengers();
            elevator1.setCurrentFloor(12);

            testScheduler.outputConsole();

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}