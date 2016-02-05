import connections.ConnectionManager;
import connections.ModBus;
import connections.UART;
import packet.Command;
import packet.Packet;

import java.util.concurrent.TimeUnit;

/**
 * Created by ponomarev on 24.11.2015.
 *
 * Main Electronic Units Test Stand (EUTS) class
 */
public class emulatorEUTS {

    public static final int TIMEOUT = 200;

    public static void main(String[] args) {

        ConnectionManager manager = new ConnectionManager(new UART(), new ModBus());
        manager.init();

        while (true) {
            try {
                manager.sendPacket(new Packet(Command.FREQUENCY_DEVICE, 1024));
                TimeUnit.MILLISECONDS.sleep(TIMEOUT);
                manager.sendPacket(new Packet(Command.FREQUENCY_DEVICE, 8192));
                TimeUnit.MILLISECONDS.sleep(TIMEOUT);
//                manager.sendPacket(new Packet(Command.FREQUENCY_DEVICE, 50));
//                TimeUnit.MILLISECONDS.sleep(TIMEOUT);
//                manager.sendPacket(new Packet(Command.FREQUENCY_DEVICE, 100));
//                TimeUnit.MILLISECONDS.sleep(TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
