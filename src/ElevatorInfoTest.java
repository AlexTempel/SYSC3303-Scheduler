import org.junit.jupiter.api.Test;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ElevatorInfoTest {
    /**
     * Test for both parsePacket() and convertToPacket(), the two functions work together so the tests were joined
     */
    @Test
    void parsePacket() {
        ElevatorInfo testInfo = new ElevatorInfo(4,1,true,false);
        DatagramPacket testPacket = testInfo.convertToPacket();
        ElevatorInfo resultInfo = ElevatorInfo.parsePacket(testPacket);

        assertEquals(testInfo.getFloor(),resultInfo.getFloor());
        assertEquals(testInfo.getNumberOfPassengers(),resultInfo.getNumberOfPassengers());
        assertEquals(testInfo.goingUpwards(),resultInfo.goingUpwards());
        assertEquals(testInfo.isBroken(),resultInfo.isBroken());

    }
}