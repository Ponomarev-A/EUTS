package packet;

/**
 *  Available commands enumeration
 */
public enum Command {
    FREQUENCY_DEVICE(1),
    GAIN_DEVICE(2),
    TYPE_OF_SIGNAL_DEVICE(3),
    BOTTOM_SENSOR_DEVICE(4),
    MODE_DEVICE(5),
    SOUND_DEVICE(6),
    BACKLIGHT_DEVICE(7),
    EXT_SENSOR_DEVICE(8),
    CHECK_DISPLAY_DEVICE(9),
    CHECK_KEYBOARD_DEVICE(10),
    CHECK_CONNECTION_DEVICE(11),
    INVALID_CRC_DEVICE(12),
    INVALID_CMD_DEVICE(13),
    INVALID_DATA_DEVICE(14),
    ERROR_DEVICE(15),

    FREQUENCY_STAND(16),
    VOLTAGE_STAND(17),
    TYPE_OF_SIGNAL_STAND(18),
    EXT_SENSOR_STAND(19),
    CHECK_CONNECTION_STAND(20),
    INVALID_CRC_STAND(21),
    INVALID_CMD_STAND(22),
    INVALID_DATA_STAND(23),
    ERROR_STAND(24);

    private final int id;

    Command(int id) {
        this.id = id;
    }

    public static Command getCommand(int id) {
        for (Command cmd : Command.values()) {
            if (cmd.getId() == id)
                return cmd;
        }

        return null;
    }

    public int getId() {
        return id;
    }
}
