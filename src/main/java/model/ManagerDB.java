package model;

import controller.Controller;
import model.tests.TestManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.prefs.Preferences;

/**
 * Database manager class for control History DB
 */
class ManagerDB {

    private static final String DEFAULT_URL = "./database/history";

    private static final String PREF_URL = "url";

    private static final String NAME = "sa";
    private static final String PASSWORD = "";
    private static final String MESSAGE_CONNECT_TITLE = "Connect to database";

    private final Receiver receiver;
    private final Model model;
    private final Controller controller;
    private final TestManager testManager;
    private Connection connection;
    private String database_url;

    ManagerDB(Model model, Controller controller) {
        this.model = model;
        this.controller = controller;

        receiver = model.getReceiver();
        testManager = model.getTestManager();
    }

//            Statement st = connection.createStatement();
//            st.execute("CREATE TABLE RECEIVER(ID INT PRIMARY KEY, MODEL VARCHAR(255), PCB VARCHAR(255), FIRMWARE VARCHAR(255))");
//            st.execute("CREATE TABLE TESTS(ID INT PRIMARY KEY, PASS ARRAY, FAIL ARRAY, SKIP ARRAY)");
//            st.execute("CREATE TABLE SESSION(ID INT PRIMARY KEY, DATETIME TIMESTAMP)");

//            String q = "INSERT INTO RECEIVER VALUES(?, '?', '?', '?')";
//            PreparedStatement prSt = conn.prepareStatement(q);
//            prSt.setInt(1, receiver.getID());
//            prSt.setString(2, receiver.getModel());
//            prSt.setString(3, receiver.getScheme());
//            prSt.setString(4, receiver.getFirmware());
//            prSt.execute();
//
//            ResultSet result;
//            result = st.executeQuery("SELECT * FROM RECEIVER");
//            while (result.next()) {
//                System.out.println(String.format("ID: %d Model: %s PCB: %s Firmware: %s",
//                        result.getInt("ID"),
//                        result.getString("MODEL"),
//                        result.getString("PCB"),
//                        result.getString("FIRMWARE")));
//            }


    /**
     * Connect to database file with specific URL.
     * <p>Note: if there is no database by URL path, then file chooser windows has been opened.</p>
     *
     * @param url path to database file
     * @return true - if connection opened, or false in other case.
     */
    boolean connect(String url) {
        try {
            Class.forName("org.h2.Driver").newInstance();
            connection = DriverManager.getConnection(
                    "jdbc:h2:file:" + url + ";" + "IFEXISTS=TRUE",
                    NAME,
                    PASSWORD
            );

            database_url = url;
            controller.showMessage(MESSAGE_CONNECT_TITLE, "Successfully connected to database.\n" + (!url.isEmpty() ? "Path: " : "") + database_url);

        } catch (SQLException e) {
            e.printStackTrace();

            // DB file not found, choose file from another DEFAULT_URL or create new
            url = controller.getPathToDatabase();
            if (!url.isEmpty())
                connect(url);
            else {
                controller.showErrorMessage(MESSAGE_CONNECT_TITLE, "Impossible connect to database.", e);
                return false;
            }

        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();

            controller.showErrorMessage(MESSAGE_CONNECT_TITLE, "Impossible connect to database.\n" + (!url.isEmpty() ? "Path: " : "") + url, e);
            return false;
        }

        return true;
    }

    boolean connect() {
        Preferences prefs = Preferences.userNodeForPackage(ManagerDB.class);
        String loadedURL = prefs.get(PREF_URL, ManagerDB.DEFAULT_URL);

        return connect(loadedURL);
    }

    boolean disconnect() {
        if (connection == null)
            return true;

        boolean isClosed = false;
        try {
            connection.close();
            isClosed = connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();

        }

        if (isClosed) {
            Preferences prefs = Preferences.userNodeForPackage(ManagerDB.class);
            prefs.put(PREF_URL, database_url);
        }

        return isClosed;
    }
}
