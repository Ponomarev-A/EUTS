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
    ERROR_DEVICE(12),

    FREQUENCY_STAND(13),
    VOLTAGE_STAND(14),
    TYPE_OF_SIGNAL_STAND(15),
    EXT_SENSOR_STAND(16),
    CHECK_CONNECTION_STAND(17),
    ERROR_STAND(18);

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
