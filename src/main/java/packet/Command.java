package packet;

/**
 *  Available commands enumeration
 */
public enum Command {
    FREQUENCY_DEVICE,           // 1
    GAIN_DEVICE,                // 2
    TYPE_OF_SIGNAL_DEVICE,      // 3
    BOTTOM_SENSOR_DEVICE,       // 4
    MODE_DEVICE,                // 5
    SOUND_DEVICE,               // 6
    BACKLIGHT_DEVICE,           // 7
    EXT_SENSOR_DEVICE,          // 8
    CHECK_DISPLAY_DEVICE,       // 9
    CHECK_KEYBOARD_DEVICE,      // 10
    CHECK_CONNECTION_DEVICE,    // 11
    INVALID_CRC_DEVICE,         // 12
    INVALID_CMD_DEVICE,         // 13
    INVALID_DATA_DEVICE,        // 14
    GET_INFO_DEVICE,      // 15
    ERROR_DEVICE,               // 16

    FREQUENCY_STAND,            // 17
    VOLTAGE_STAND,              // 18
    TYPE_OF_SIGNAL_STAND,       // 19
    EXT_SENSOR_STAND,           // 20
    CHECK_CONNECTION_STAND,     // 21
    INVALID_CMD_STAND,          // 22
    INVALID_CRC_STAND,          // 23
    INVALID_DATA_STAND,         // 24
    GET_INFO_STAND,             // 25
    ERROR_STAND;                // 26

    private int id = 0;

    Command() {
        id = ordinal() + 1;
    }

    public static Command getCommand(int id) {
        return  Command.values()[id-1];
    }

    public int getId() {
        return id;
    }
}
