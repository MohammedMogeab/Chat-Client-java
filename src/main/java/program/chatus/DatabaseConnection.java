package program.chatus;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static  final String url="jdbc:mysql://localhost:3306/Chatus";

    private static final String username="root";
    private static final String password="mohammedmogeab";
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url,username,password);
    }




}

