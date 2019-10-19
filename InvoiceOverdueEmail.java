import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;

public class InvoiceOverdueEmail {

    public static void main(String args[]){
        getOverdueInvoice();
    }

    // Get overdue invoices from database
    public static void getOverdueInvoice(){
        Connection conn=null;
        try {
            String userName = "root";
            String password = "your-password";
            // Get domain space for database connection
            String url = "jdbc:mysql://localhost:3306/vidsi?autoReconnect=true&useSSL=false";
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Make connection
            conn = DriverManager.getConnection(url,userName,password);
        } catch(ClassNotFoundException e) {
            // Driver Exception
            System.out.println("Driver Exceptions: " + e);
        } catch( SQLException es) {
            // SQL Exceptions
            System.out.println("SQLException: " + es.getMessage());
            System.out.println("SQLState: " + es.getSQLState());
            System.out.println("VendorError: " + es.getErrorCode());
        }
        // Declare callable statement for stored procedure
        CallableStatement callableStatement = null;
        // To store results from database
        ResultSet resultSet = null;
        try {
            // Call to stored procedure invoice_overdue
            callableStatement = conn.prepareCall("{call invoice_overdue()}");
            // Check if any results were returned (-1) if none
            Boolean hadResults = callableStatement.execute();
            // Loop through results if query returned results
            while (hadResults) {
                resultSet = callableStatement.getResultSet();
                // Check for results
                if (resultSet.next()) {
                    // Send email with required parameters for subscriber
                    sendEmail(resultSet.getString("email"), resultSet.getInt("invoice_number"),
                            resultSet.getString("first_name"), resultSet.getDouble("balance"),
                            resultSet.getString("due_date")
                    );
                } else {
                    // Jump out of while loop, no more results
                    break;
                }
            }
        } catch (SQLException esx) {
            System.out.println("Exception: " + esx);
        }
        // Release resources in a finally{} block in the order they were used
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException sqlEx) { } // ignore
            resultSet = null;
        }
        if (callableStatement != null) {
            try {
                callableStatement.close();
            } catch (SQLException sqlEx) { } // ignore
            callableStatement = null;
        }
    }

    // Send email to users who have over due invoices
    private static void sendEmail(String email, int invoiceNumber, String firstName, Double balance, String dueDate) {
        System.out.println("\n-------------------------------------------------");
        String emailHeader = "From: account@vidsi.com\n" +
                "To: " + email +
                "\nsubject: Cancellation Notice - Outstanding Balance\n\n";
        String message = emailHeader + firstName + ",\nOur records show that we haven’t yet received payment of " +
                "$" + balance + " for Invoice #" + invoiceNumber + ", which was due on " + dueDate + ". " +
                "Failure to pay will lead to cancellation of your account. ";
        System.out.println(message);
    }
}
