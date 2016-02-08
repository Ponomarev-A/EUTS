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
public class EmulatorEUTS {

    private static ConnectionManager manager;

    public static void main(String[] args) {

        UART uart;
        ModBus modBus = new ModBus();

        textWelcome();
        uart = textInitUART();

        manager = new ConnectionManager(uart, modBus);
        manager.init();

        textListOfCommands();

        while (true) {

            textReceivePacket();

            int N = textChooseCommand();

            if (N > 0) {
                Command cmd = Command.values()[N - 1];
                int data = textEnterData();

                Packet packet = new Packet(cmd, data);

                textSendedPacket(packet);
            }
        }

    }

    private static void textReceivePacket() {

        Packet receivePacket;
        if ((receivePacket = manager.receivePacket()) != null) {
            ConsoleHelper.writeMessageLn("################################");
            ConsoleHelper.writeMessage("Received packet: ");
            ConsoleHelper.writeMessageLn(receivePacket.toString());
            ConsoleHelper.writeMessageLn("################################");
        }

    }

    private static void textSendedPacket(Packet packet) {
        ConsoleHelper.writeMessageLn("_________________________________");
        ConsoleHelper.writeMessageLn("Packet created and ready to send. ");
        ConsoleHelper.writeMessageLn(packet.toString());

        if (manager.sendPacket(packet))
            ConsoleHelper.writeMessageLn("Packet was successfully sent.");
        else
            ConsoleHelper.writeMessageLn("*** Packet send FAILED ***");
        ConsoleHelper.writeMessageLn("_________________________________");
    }

    private static int textEnterData() {
        ConsoleHelper.writeMessage("Please, enter the data (integer ONLY): ");
        return ConsoleHelper.readInt();
    }

    private static int textChooseCommand() {
        ConsoleHelper.writeMessageLn("");
        ConsoleHelper.writeMessage("Please, enter command number and press ENTER: ");
        int N = ConsoleHelper.readInt();

        if (N > 0 && N <= Command.values().length)
            return N;
        else
            ConsoleHelper.writeMessageLn("Command not found!");

        return -1;
    }

    private static void textListOfCommands() {
        ConsoleHelper.writeMessageLn("Available commands:");

        Command[] values = Command.values();
        for (int i = 1; i <= values.length; i++) {
            Command command = values[i-1];
            ConsoleHelper.writeMessageLn(i+". "+command.toString());
        }
    }

    private static UART textInitUART() {

        UART uart = null;

        ConsoleHelper.writeMessageLn("*** Initialization UART ***");
        ConsoleHelper.writeMessageLn("List of all available COM-ports on your PC:");

        String[] portNames = UART.getPortNames();
        for (int i = 1; i <= portNames.length; i++) {
            String portName = portNames[i-1];
            ConsoleHelper.writeMessageLn(i + ". " + portName);
        }


        while (true) {
            ConsoleHelper.writeMessage("Please, enter COM-port number and press ENTER: ");
            int N  = ConsoleHelper.readInt();

            if (N > 0 && N <= portNames.length) {
                uart = new UART(portNames[N - 1]);
                break;
            } else {
                ConsoleHelper.writeMessageLn("COM-port not found!");
            }
        }

        ConsoleHelper.writeMessageLn("*************************");
        return uart;
    }

    private static void textWelcome() {
        ConsoleHelper.writeMessageLn("***************************************************");
        ConsoleHelper.writeMessageLn("ELECTRONIC UNITS TEST STAND (emulator)");
        ConsoleHelper.writeMessageLn("***************************************************");
    }
}
