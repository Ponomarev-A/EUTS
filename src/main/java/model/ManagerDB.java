package model;

import model.tests.TestManager;

import java.sql.*;

/**
 * Database manager class for control History DB
 */
class ManagerDB {

    private static final String NAME = "sa";
    private static final String PASSWORD = "";
    private static final String URL = "jdbc:h2:file:./data/sample";
    private final Receiver receiver;
    private final Model model;
    private final TestManager testManager;

    ManagerDB(Model model) {
        this.model = model;
        receiver = model.getReceiver();
        testManager = model.getTestManager();
        createDB();
    }

    private void createDB() {
        try {
            Class.forName("org.h2.Driver").newInstance();
            Connection conn = DriverManager.getConnection(URL, NAME, PASSWORD);

            Statement st = conn.createStatement();
            st.execute("CREATE TABLE RECEIVER(ID INT PRIMARY KEY, MODEL VARCHAR(255), PCB VARCHAR(255), FIRMWARE VARCHAR(255))");
            st.execute("CREATE TABLE TESTS(ID INT PRIMARY KEY, PASS ARRAY, FAIL ARRAY, SKIP ARRAY)");
            st.execute("CREATE TABLE SESSION(ID INT PRIMARY KEY, DATETIME TIMESTAMP)");

            String q = "INSERT INTO RECEIVER VALUES(?, '?', '?', '?')";
            PreparedStatement prSt = conn.prepareStatement(q);
            prSt.setInt(1, receiver.getID());
            prSt.setString(2, receiver.getModel());
            prSt.setString(3, receiver.getScheme());
            prSt.setString(4, receiver.getFirmware());
            prSt.execute();

            ResultSet result;
            result = st.executeQuery("SELECT * FROM RECEIVER");
            while (result.next()) {
                System.out.println(String.format("ID: %d Model: %s PCB: %s Firmware: %s",
                        result.getInt("ID"),
                        result.getString("MODEL"),
                        result.getString("PCB"),
                        result.getString("FIRMWARE")));
            }

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
