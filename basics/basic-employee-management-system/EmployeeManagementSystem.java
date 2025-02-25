import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.io.*;

class Employee {
    private int id;
    private String name;
    private String department;
    private double salary;

    public Employee(int id, String name, String department, double salary) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.salary = salary;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public double getSalary() { return salary; }
    public void setName(String name) { this.name = name; }
    public void setDepartment(String department) { this.department = department; }
    public void setSalary(double salary) { this.salary = salary; }

    @Override
    public String toString() {
        return String.format("ID: %d | Name: %s | Dept: %s | Salary: %.2f",
            id, name, department, salary);
    }
}

class EmployeeManager {
    // Use environment variables for flexibility in Docker
    private static final String DB_URL = System.getenv("DB_URL") != null ? 
        System.getenv("DB_URL") : "jdbc:mysql://mysql:3306/employees?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = System.getenv("DB_USER") != null ? 
        System.getenv("DB_USER") : "root";
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD") != null ? 
        System.getenv("DB_PASSWORD") : "password";
    private Connection conn;

    public EmployeeManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            // Create database and table if not exists
            stmt.execute("CREATE DATABASE IF NOT EXISTS employees");
            stmt.execute("USE employees");
            String sql = "CREATE TABLE IF NOT EXISTS employees (" +
                "id INTEGER PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "department VARCHAR(255) NOT NULL, " +
                "salary DOUBLE NOT NULL)";
            stmt.execute(sql);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addEmployee(Employee emp) {
        String sql = "INSERT INTO employees (id, name, department, salary) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, emp.getId());
            pstmt.setString(2, emp.getName());
            pstmt.setString(3, emp.getDepartment());
            pstmt.setDouble(4, emp.getSalary());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void editEmployee(Employee emp) {
        String sql = "UPDATE employees SET name = ?, department = ?, salary = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, emp.getName());
            pstmt.setString(2, emp.getDepartment());
            pstmt.setDouble(3, emp.getSalary());
            pstmt.setInt(4, emp.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeEmployee(int id) {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                employees.add(new Employee(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("department"),
                    rs.getDouble("salary")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public List<Employee> filterByDepartment(String department) {
        return getAllEmployees().stream()
            .filter(emp -> emp.getDepartment().equalsIgnoreCase(department))
            .collect(Collectors.toList());
    }

    public List<Employee> filterBySalaryRange(double min, double max) {
        return getAllEmployees().stream()
            .filter(emp -> emp.getSalary() >= min && emp.getSalary() <= max)
            .collect(Collectors.toList());
    }

    public void exportToCSV(String filename) {
        List<Employee> employees = getAllEmployees();
        try (PrintWriter pw = new PrintWriter(new File(filename))) {
            pw.println("ID,Name,Department,Salary");
            for (Employee emp : employees) {
                pw.printf("%d,\"%s\",\"%s\",%.2f%n",
                    emp.getId(), emp.getName(), emp.getDepartment(), emp.getSalary());
            }
            System.out.println("Exported to " + filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class EmployeeManagementSystem {
    private static EmployeeManager manager = new EmployeeManager();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(manager::closeConnection));
        
        while (true) {
            displayMenu();
            int choice = getIntInput("Enter your choice: ");
            switch (choice) {
                case 1 -> addEmployee();
                case 2 -> editEmployee();
                case 3 -> removeEmployee();
                case 4 -> listAllEmployees();
                case 5 -> filterByDepartment();
                case 6 -> filterBySalaryRange();
                case 7 -> exportToCSV();
                case 8 -> {
                    System.out.println("Exiting...");
                    manager.closeConnection();
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\n=== Employee Management System ===");
        System.out.println("1. Add Employee");
        System.out.println("2. Edit Employee");
        System.out.println("3. Remove Employee");
        System.out.println("4. List All Employees");
        System.out.println("5. Filter by Department");
        System.out.println("6. Filter by Salary Range");
        System.out.println("7. Export to CSV");
        System.out.println("8. Exit");
    }

    private static void addEmployee() {
        int id = getIntInput("Enter ID: ");
        String name = getStringInput("Enter Name: ");
        String dept = getStringInput("Enter Department: ");
        double salary = getDoubleInput("Enter Salary: ");
        manager.addEmployee(new Employee(id, name, dept, salary));
        System.out.println("Employee added.");
    }

    private static void editEmployee() {
        int id = getIntInput("Enter ID to edit: ");
        String name = getStringInput("Enter new Name: ");
        String dept = getStringInput("Enter new Department: ");
        double salary = getDoubleInput("Enter new Salary: ");
        manager.editEmployee(new Employee(id, name, dept, salary));
        System.out.println("Employee updated.");
    }

    private static void removeEmployee() {
        int id = getIntInput("Enter ID to remove: ");
        manager.removeEmployee(id);
        System.out.println("Employee removed.");
    }

    private static void listAllEmployees() {
        manager.getAllEmployees().forEach(System.out::println);
    }

    private static void filterByDepartment() {
        String dept = getStringInput("Enter Department: ");
        manager.filterByDepartment(dept).forEach(System.out::println);
    }

    private static void filterBySalaryRange() {
        double min = getDoubleInput("Enter minimum salary: ");
        double max = getDoubleInput("Enter maximum salary: ");
        manager.filterBySalaryRange(min, max).forEach(System.out::println);
    }

    private static void exportToCSV() {
        String filename = getStringInput("Enter CSV filename: ");
        manager.exportToCSV(filename);
    }

    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. " + prompt);
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    private static double getDoubleInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            System.out.print("Invalid input. " + prompt);
            scanner.next();
        }
        double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }

    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
