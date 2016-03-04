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
    CHECK_DISPLAY_DEVICE(8),
    CHECK_KEYBOARD_DEVICE(9),
    CHECK_CONNECTION_DEVICE(10),
    INVALID_CRC_DEVICE(11),
    INVALID_CMD_DEVICE(12),
    INVALID_DATA_DEVICE(13),
    GET_INFO_DEVICE(14),
    GET_EXT_SENSOR_DEVICE(15),
    GET_GAIN_DEVICE(16),
    GET_LEVELS_DEVICE(17),
    ERROR_DEVICE(18),

    /********************************
     * Reserve values prior to 50
     *******************************/

    FREQUENCY_STAND(50),
    VOLTAGE_STAND(51),
    TYPE_OF_SIGNAL_STAND(52),
    EXT_SENSOR_STAND(53),
    CHECK_CONNECTION_STAND(54),
    INVALID_CMD_STAND(55),
    INVALID_CRC_STAND(56),
    INVALID_DATA_STAND(57),
    GET_INFO_STAND(58),
    ERROR_STAND(59);

    private final int id;

    Command(int id) {
        this.id = id;
    }

    public static Command getCommand(int id) {
        for (Command command : values()) {
            if (command.getId() == id)
                return command;
        }
        return null;
    }

    public int getId() {
        return id;
    }
}
