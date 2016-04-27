package model;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.custom.HookType;
import com.healthmarketscience.sqlbuilder.dbspec.basic.*;
import controller.Controller;
import view.LogPanel;

import java.sql.*;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Database manager class for control History DB
 */
public class ManagerDB {

    // DB fields constants
    private static final String TABLE_RECEIVER = "RECEIVER";
    private static final String TABLE_SESSION = "SESSION";
    private static final String COLUMN_ID = "_ID";
    private static final String COLUMN_MODEL = "MODEL";
    private static final String COLUMN_SCHEME = "SCHEME";
    private static final String COLUMN_FIRMWARE = "FIRMWARE";
    private static final String COLUMN_DEPTH_COEFFS = "DEPTH_COEFFS";
    private static final String COLUMN_CURRENT_COEFFS = "CURRENT_COEFFS";
    private static final String COLUMN_R_ID = "RECEIVER_ID";
    private static final String COLUMN_TIMESTAMP = "TIMESTAMP";
    private static final String COLUMN_PASS = "PASS";
    private static final String COLUMN_FAIL = "FAIL";
    private static final String COLUMN_SKIP = "SKIP";

    private static final String NAME = "sa";
    private static final String PASSWORD = "";

    private static final String PREF_URL = "url";
    private static final String DB_EXTENSION = ".mv.db";

    private final Controller controller;

    // DB fields
    private DbTable dbTableReceiver;
    private DbTable dbTableSession;
    private DbColumn dbColumnReceiverID;
    private DbColumn dbColumnModel;
    private DbColumn dbColumnScheme;
    private DbColumn dbColumnFirmware;
    private DbColumn dbColumnDepthCoeffs;
    private DbColumn dbColumnCurrentCoeffs;
    private DbColumn dbColumnSessionID;
    private DbColumn dbColumnSessionR_id;
    private DbColumn dbColumnTimestamp;
    private DbColumn dbColumnPass;
    private DbColumn dbColumnFail;
    private DbColumn dbColumnSkip;
    private Connection connection;
    private String URL;

    ManagerDB(Controller controller) {
        this.controller = controller;
    }

    public String getURL() {
        return URL + DB_EXTENSION;
    }

    public void connectToDefaultURL() {
        String url = loadURLFromPrefs();
        if (url == null) {
            controller.askPathToDatabase();
        } else {
            connect(url);
        }
    }

    private String loadURLFromPrefs() {
        Preferences prefs = Preferences.userNodeForPackage(ManagerDB.class);
        return prefs.get(PREF_URL, null);
    }

    /**
     * Connect to database file with specific URL.
     * @param URL path to database file
     */
    public void connect(String URL) {
        if (URL == null)
            return;

        this.URL = URL;
        saveURLToPrefs(this.URL);

        try {
            // Close old connection
            disconnect();

            // if URL is empty string, then user choose don't use database option
            if (this.URL.isEmpty()) {
                return;
            }

            Class.forName("org.h2.Driver").newInstance();
            connection = DriverManager.getConnection(
                    "jdbc:h2:file:" + this.URL + ";",
                    NAME,
                    PASSWORD
            );

            createTables();
            controller.updateLog("Successfully connected to database.\nPath: " + this.URL + DB_EXTENSION);
        } catch (SQLException | InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            controller.showErrorMessage("Connect to database", "Can't connect to database.\nPath: " + this.URL + DB_EXTENSION, e);
        }
    }

    private void saveURLToPrefs(String url) {
        Preferences prefs = Preferences.userNodeForPackage(ManagerDB.class);
        prefs.put(PREF_URL, url);
    }

    void disconnect() {
        if (connection == null)
            return;

        try {
            if (!connection.isClosed())
                connection.close();

            controller.updateLog("Successfully disconnected from database.");
        } catch (SQLException e) {
            controller.updateLog("Disconnect from database failed!", LogPanel.BOLD, LogPanel.RED);
        }
    }

    private void createTables() throws SQLException {

        DbSpec spec = new DbSpec();
        DbSchema schema = spec.addDefaultSchema();

        dbTableReceiver = schema.addTable(TABLE_RECEIVER);
        dbColumnReceiverID = new DbColumn(dbTableReceiver, COLUMN_ID, "int");
        dbColumnModel = new DbColumn(dbTableReceiver, COLUMN_MODEL, "varchar", 32);
        dbColumnScheme = new DbColumn(dbTableReceiver, COLUMN_SCHEME, "varchar", 64);
        dbColumnFirmware = new DbColumn(dbTableReceiver, COLUMN_FIRMWARE, "varchar", 32);
        dbColumnDepthCoeffs = new DbColumn(dbTableReceiver, COLUMN_DEPTH_COEFFS, "array");
        dbColumnCurrentCoeffs = new DbColumn(dbTableReceiver, COLUMN_CURRENT_COEFFS, "array");


        String sqlCreateReceiverTable = new CreateTableQuery(dbTableReceiver, true).
                addCustomization(CreateTableQuery.Hook.TABLE, HookType.SUFFIX, "IF NOT EXISTS ").
                addColumns(dbColumnReceiverID, dbColumnModel, dbColumnScheme, dbColumnFirmware, dbColumnDepthCoeffs, dbColumnCurrentCoeffs).
                addColumnConstraint(dbColumnReceiverID, "PRIMARY KEY").
                addColumnConstraint(dbColumnModel, "DEFAULT '' NOT NULL").
                addColumnConstraint(dbColumnScheme, "DEFAULT '' NOT NULL").
                addColumnConstraint(dbColumnFirmware, "DEFAULT '' NOT NULL").
                addColumnConstraint(dbColumnDepthCoeffs, "DEFAULT ()").
                addColumnConstraint(dbColumnCurrentCoeffs, "DEFAULT ()").
                validate().toString();

        dbTableSession = schema.addTable(TABLE_SESSION);
        dbColumnSessionID = new DbColumn(dbTableSession, COLUMN_ID, "int");
        dbColumnSessionR_id = new DbColumn(dbTableSession, COLUMN_R_ID, "int");
        dbColumnTimestamp = new DbColumn(dbTableSession, COLUMN_TIMESTAMP, "timestamp");
        dbColumnPass = new DbColumn(dbTableSession, COLUMN_PASS, "array");
        dbColumnFail = new DbColumn(dbTableSession, COLUMN_FAIL, "array");
        dbColumnSkip = new DbColumn(dbTableSession, COLUMN_SKIP, "array");
        DbForeignKeyConstraint foreignKeyConstraint = new DbForeignKeyConstraint(
                dbTableSession, COLUMN_R_ID,
                dbTableReceiver,
                new DbColumn[]{dbColumnSessionR_id}, new DbColumn[]{dbColumnReceiverID});


        String sqlCreateSessionTable = new CreateTableQuery(dbTableSession, true).
                addCustomization(CreateTableQuery.Hook.TABLE, HookType.SUFFIX, "IF NOT EXISTS ").
                addColumns(dbColumnSessionID, dbColumnSessionR_id, dbColumnTimestamp, dbColumnPass, dbColumnFail, dbColumnSkip).
                addColumnConstraint(dbColumnSessionID, "AUTO_INCREMENT PRIMARY KEY").
                addColumnConstraint(dbColumnSessionR_id, "NOT NULL").
                addColumnConstraint(dbColumnTimestamp, "DEFAULT CURRENT_TIMESTAMP NOT NULL").
                addColumnConstraint(dbColumnPass, "DEFAULT ()").
                addColumnConstraint(dbColumnFail, "DEFAULT ()").
                addColumnConstraint(dbColumnSkip, "DEFAULT ()").
                addConstraints(foreignKeyConstraint).
                validate().toString();

        try (Statement st = connection.createStatement()) {
            st.execute(sqlCreateReceiverTable);
            st.execute(sqlCreateSessionTable);
        } catch (SQLException e) {
            throw new SQLException("Create new tables in database failed");
        }
    }

    void destroy() {
        if (connection == null)
            return;

        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS DELETE FILES");
            disconnect();
        } catch (SQLException ignored) {
        }
    }

    int insert(Receiver receiver, Object[] depthCoeffs, Object[] currentCoeffs) throws SQLException {
        String sql = new InsertQuery(dbTableReceiver).
                addColumn(dbColumnReceiverID, receiver.getID()).
                addColumn(dbColumnModel, receiver.getModel()).
                addColumn(dbColumnScheme, receiver.getScheme()).
                addColumn(dbColumnFirmware, receiver.getFirmware()).
                addPreparedColumns(dbColumnDepthCoeffs, dbColumnCurrentCoeffs).
                validate().toString();

        try (PreparedStatement prSt = connection.prepareStatement(sql)) {
            prSt.setObject(1, depthCoeffs);
            prSt.setObject(2, currentCoeffs);
            return prSt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Insert new receiver to database failed");
        }
    }

    int insert(Integer receiverID, Object[] passed, Object[] failed, Object[] skipped) throws SQLException {

        String sql = new InsertQuery(dbTableSession).
                addColumn(dbColumnSessionR_id, receiverID).
                addPreparedColumns(dbColumnPass, dbColumnFail, dbColumnSkip).
                validate().toString();

        try (PreparedStatement prSt = connection.prepareStatement(sql)) {
            prSt.setObject(1, passed);
            prSt.setObject(2, failed);
            prSt.setObject(3, skipped);
            return prSt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Insert new session to database failed");
        }
    }

    int update(Receiver receiver, Object[] depthCoeffs, Object[] currentCoeffs) throws SQLException {
        UpdateQuery updateQuery = new UpdateQuery(dbTableReceiver).
                addSetClause(dbColumnDepthCoeffs, '?').
                addSetClause(dbColumnCurrentCoeffs, '?');

        if (receiver != null) {
            if (receiver.getID() != null)
                updateQuery.addCondition(BinaryCondition.equalTo(dbColumnReceiverID, receiver.getID()));
            if (receiver.getModel() != null)
                updateQuery.addCondition(BinaryCondition.equalTo(dbColumnModel, receiver.getModel()));
            if (receiver.getScheme() != null)
                updateQuery.addCondition(BinaryCondition.equalTo(dbColumnScheme, receiver.getScheme()));
            if (receiver.getFirmware() != null)
                updateQuery.addCondition(BinaryCondition.equalTo(dbColumnFirmware, receiver.getFirmware()));
        }

        String sql = updateQuery.validate().toString();

        // Change SQL query for prepare statement validation: replace '?' to ?
        sql = sql.replaceAll("(\\'\\?\\')", "?");

        try (PreparedStatement prSt = connection.prepareStatement(sql)) {
            prSt.setObject(1, depthCoeffs);
            prSt.setObject(2, currentCoeffs);
            return prSt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Update receiver to database failed");
        }
    }

    ResultSet select(Receiver receiver, String afterDate, String beforeDate) throws SQLException {

        SelectQuery selectQuery = new SelectQuery().
                addColumns(dbColumnReceiverID, dbColumnModel, dbColumnScheme, dbColumnFirmware).
                addCustomColumns(
                        new CustomSql("ISNULL(" + dbColumnTimestamp.getColumnNameSQL() + ",0)"),
                        new CustomSql("ISNULL(" + dbColumnPass.getColumnNameSQL() + ",())"),
                        new CustomSql("ISNULL(" + dbColumnFail.getColumnNameSQL() + ",())"),
                        new CustomSql("ISNULL(" + dbColumnSkip.getColumnNameSQL() + ",())")).
                addJoin(SelectQuery.JoinType.LEFT_OUTER, dbTableReceiver, dbTableSession, dbColumnReceiverID, dbColumnSessionR_id);

        if (receiver != null) {
            if (receiver.getID() != null)
                selectQuery.addCondition(BinaryCondition.equalTo(dbColumnReceiverID, receiver.getID()));
            if (receiver.getModel() != null)
                selectQuery.addCondition(BinaryCondition.equalTo(dbColumnModel, receiver.getModel()));
            if (receiver.getScheme() != null)
                selectQuery.addCondition(BinaryCondition.equalTo(dbColumnScheme, receiver.getScheme()));
            if (receiver.getFirmware() != null)
                selectQuery.addCondition(BinaryCondition.equalTo(dbColumnFirmware, receiver.getFirmware()));
        }

        if (afterDate != null) {
            selectQuery.addCondition(BinaryCondition.greaterThan(dbColumnTimestamp, afterDate, true));
        }

        if (beforeDate != null) {
            selectQuery.addCondition(BinaryCondition.lessThan(dbColumnTimestamp, beforeDate, true));
        }
        String sql = selectQuery.validate().toString();

        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            throw new SQLException("Select test sessions data from database failed");
        }
    }

    ResultSet select(Receiver receiver) throws SQLException {
        SelectQuery selectQuery = new SelectQuery().
                addFromTable(dbTableReceiver).
                addCustomColumns(
                        new CustomSql("ISNULL(" + dbColumnDepthCoeffs.getColumnNameSQL() + ",())"),
                        new CustomSql("ISNULL(" + dbColumnCurrentCoeffs.getColumnNameSQL() + ",())")
                );

        if (receiver != null) {
            if (receiver.getID() != null)
                selectQuery.addCondition(BinaryCondition.equalTo(dbColumnReceiverID, receiver.getID()));
            if (receiver.getModel() != null)
                selectQuery.addCondition(BinaryCondition.equalTo(dbColumnModel, receiver.getModel()));
            if (receiver.getScheme() != null)
                selectQuery.addCondition(BinaryCondition.equalTo(dbColumnScheme, receiver.getScheme()));
            if (receiver.getFirmware() != null)
                selectQuery.addCondition(BinaryCondition.equalTo(dbColumnFirmware, receiver.getFirmware()));
        }

        String sql = selectQuery.validate().toString();

        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException e) {
            throw new SQLException("Select receiver data from database failed");
        }
    }

    public boolean exist() {
        try {
            return connection != null && connection.isValid(0);
        } catch (SQLException e) {
            return false;
        }
    }

    String[] getModels() throws SQLException {
        return selectParameter(dbColumnModel, COLUMN_MODEL);
    }

    private String[] selectParameter(DbColumn column, String columnName) throws SQLException {
        String sql = new SelectQuery().
                setIsDistinct(true).
                addColumns(column).
                addFromTable(dbTableReceiver).
                addOrdering(column, OrderObject.Dir.ASCENDING).
                validate().toString();

        try (ResultSet rs = connection.createStatement().executeQuery(sql)) {

            ArrayList<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getString(columnName));
            }
            return result.toArray(new String[]{});

        } catch (SQLException e) {
            throw new SQLException("Select parameters or receiver from database failed");
        }
    }

    String[] getSchemes() throws SQLException {
        return selectParameter(dbColumnScheme, COLUMN_SCHEME);
    }

    String[] getFirmwares() throws SQLException {
        return selectParameter(dbColumnFirmware, COLUMN_FIRMWARE);
    }

    Integer getNextUniqueID() throws SQLException {
        String[] receiverIDs = getIDs();
        Integer lastUsedID = (receiverIDs.length > 0) ? Integer.valueOf(receiverIDs[receiverIDs.length - 1]) : 0;
        return ++lastUsedID;
    }

    String[] getIDs() throws SQLException {
        return selectParameter(dbColumnReceiverID, COLUMN_ID);
    }
}
