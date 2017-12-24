package server;

import java.sql.*;

public class Storage {
    private Connection connection;

    public Storage() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void save(long time, String author, String message) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO messages (time, author, text) VALUES (?, ?, ?);");

            ps.setLong(1, time);
            ps.setString(2, author);
            ps.setString(3, message);

            ps.executeUpdate();

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public StringBuffer getAllMessages() {
        StringBuffer result = new StringBuffer();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT time, author, text FROM messages;");

            while (rs.next()) {
                result.append(new Date(rs.getLong(1)));
                result.append(" ");
                result.append(new Time(rs.getLong(1)));
                result.append(" ");
                result.append(rs.getString(2));
                result.append(": ");
                result.append(rs.getString(3));
                result.append("\n");
            }

            rs.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public synchronized void clear() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM messages;");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public StringBuffer findMessagesBy(String author) {
        StringBuffer result = new StringBuffer("Messages by ");
        result.append(author);
        result.append("\n");

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT date, text FROM messages WHERE author = ?;");

            ps.setString(1, author);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                result.append(new Date(rs.getLong(1)));
                result.append(" ");
                result.append(new Time(rs.getLong(1)));
                result.append(" ");
                result.append(rs.getString(2));
                result.append("\n");
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}