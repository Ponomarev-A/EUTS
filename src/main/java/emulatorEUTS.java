import connections.ConnectionManager;
import connections.ModBus;
import connections.UART;
import packet.Command;
import packet.Packet;

/**
 * Created by ponomarev on 24.11.2015.
 *
 * Main Electronic Units Test Stand (EUTS) class
 */
public class emulatorEUTS {

    public static void main(String[] args) {

        ConnectionManager manager = new ConnectionManager(new UART(), new ModBus());
        manager.init();

        while (true) {
            Packet sendPacket = new Packet(Command.FREQUENCY_DEVICE, 1024);
            manager.sendPacket(sendPacket);
        }
    }
}
