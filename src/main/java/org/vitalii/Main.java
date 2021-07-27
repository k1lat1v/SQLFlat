package org.vitalii;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/flat?serverTimezone=Europe/Kiev";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    private static final Scanner scanner = new Scanner(System.in);

    private static Connection conn;

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
            createTable();

            while (true) {
                System.out.println("1: list flats");
                System.out.println("2: add flat");
                System.out.println("3: add random flats");
                System.out.println("4: delete flats");
                System.out.println("5: change flat");
                System.out.print("-> ");

                String s = scanner.nextLine();
                switch (s) {
                    case "1":
                        listFlats();
                        break;
                    case "2":
                        addFlat();
                        break;
                    case "3":
                        addRandomFlats();
                        break;
                    case "4":
                        deleteFlat();
                        break;
                    case "5":
                        changeFlat();
                        break;
                    default:
                        return;
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try {
                conn.close();
            }catch (SQLException e){
                e.printStackTrace();
            }
            scanner.close();
        }
    }

    private static void createTable() throws SQLException{
        try(Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS Flats");
            st.execute("CREATE TABLE Flats (" +
                    "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "district VARCHAR(20)," +
                    "address VARCHAR(40)," +
                    "area FLOAT," +
                    "rooms TINYINT," +
                    "price FLOAT)");

        }
    }

    private static void addFlat() throws SQLException{
        System.out.println("Enter district:");
        String district = scanner.nextLine();
        System.out.println("Enter address:");
        String address = scanner.nextLine();
        System.out.println("Enter area:");
        String area = scanner.nextLine();
        System.out.println("Enter rooms:");
        String rooms = scanner.nextLine();
        System.out.println("Enter price:");
        String price = scanner.nextLine();

        try(PreparedStatement ps = conn.prepareStatement("INSERT INTO Flats(district, address, area, rooms, price) VALUES (?, ?, ?, ?, ?)")){
            ps.setString(1, district);
            ps.setString(2, address);
            ps.setString(3, area);
            ps.setString(4, rooms);
            ps.setString(5, price);
            ps.executeUpdate();
        }
    }

    private static void addRandomFlats() throws SQLException{
        System.out.println("Enter flats count:");
        String strCount = scanner.nextLine();
        int count = Integer.parseInt(strCount);
        Random rnd = new Random();

        conn.setAutoCommit(false);
        try(PreparedStatement ps = conn.prepareStatement("INSERT INTO Flats(district, address, area, rooms, price) VALUES (?, ?, ?, ?, ?)")){
                for(int i=0; i<count; i++){
                    ps.setString(1, "District" + i);
                    ps.setString(2, "Address" + i);
                    ps.setInt(3, rnd.nextInt(100));
                    ps.setInt(4, rnd.nextInt(6));
                    ps.setInt(5, rnd.nextInt(100000));
                    ps.executeUpdate();
                }
                conn.commit();
        }catch (SQLException e){
            conn.rollback();
        }finally {
            conn.setAutoCommit(true);
        }
    }

    private static void deleteFlat() throws SQLException{
        System.out.println("Enter flat address:");
        String address = scanner.nextLine();

        try(PreparedStatement ps = conn.prepareStatement("DELETE FROM Flats WHERE address = ?")){
            ps.setString(1, address);
            ps.executeUpdate();
        }
    }

    private static void changeFlat() throws SQLException{
        System.out.println("Enter flat address:");
        String address = scanner.nextLine();

        try(Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM Flats");

            ResultSetMetaData rsmd = rs.getMetaData();

            System.out.println("Select what do you want to change:");
            for(int i=2; i<=rsmd.getColumnCount(); i++){
                System.out.println(i-1 + ". " + rsmd.getColumnName(i));
            }

            String choice = scanner.nextLine();
            String resChoice = rsmd.getColumnName(Integer.parseInt(choice)+1);

            System.out.println("Enter new value:");
            String value = scanner.nextLine();
            try(PreparedStatement ps = conn.prepareStatement("UPDATE Flats SET " + resChoice + " = ? WHERE address = \" " + address + " \" ")){
                ps.setString(1, value);
                ps.executeUpdate();
            }
        }
    }

    public static void listFlats() throws SQLException{
        try(Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM Flats")){

            ResultSetMetaData rsmd = rs.getMetaData();

            System.out.println("Select sort method:");
            System.out.println(0 + ". List all flats");

            for(int i=2; i<=rsmd.getColumnCount(); i++){
                System.out.println(i-1 + ". " + rsmd.getColumnName(i));
            }

            String choice = scanner.nextLine();

            if(choice.equals("0")){
                printFlats(rs);
                return;
            }

            String columnName;

            try {
                columnName = rsmd.getColumnName(Integer.parseInt(choice) + 1);
            }catch (SQLException e){
                return;
            }

            String request = sort(columnName);

            ResultSet newRS = st.executeQuery(request);

            printFlats(newRS);
        }
    }

    private static String sort(String parameter){
        String request = "SELECT * FROM Flats ";
        if(parameter.equals("district")) {
            request += "ORDER BY district";
        }else if(parameter.equals("address")) {
            request += "ORDER BY address";
        }else if(parameter.equals("area") || parameter.equals("rooms") || parameter.equals("price")) {
            System.out.println("Enter minimum " + parameter + ": ");
            String min = scanner.nextLine();
            System.out.println("Enter maximum " + parameter + ": ");
            String max = scanner.nextLine();
            request += "WHERE " + parameter + " BETWEEN " + min + " AND " + max + " ORDER BY " + parameter;
        }
        return request;
    }

    private static void printFlats(ResultSet rs) throws SQLException{
        ResultSetMetaData rsmd = rs.getMetaData();

        for(int i=1; i<=rsmd.getColumnCount(); i++){
            System.out.print(rsmd.getColumnName(i) + "\t\t");
        }

        System.out.println();

        while(rs.next()){
            for(int i=1; i<=rsmd.getColumnCount(); i++){
                System.out.print(rs.getString(i) + "\t\t");
            }
            System.out.println();
        }
    }
}
