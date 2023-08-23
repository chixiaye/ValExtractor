package org.jfree.data.jdbc;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import org.jfree.chart.event.DatasetChangeInfo;
import org.jfree.data.pie.DefaultPieDataset;
import org.jfree.data.pie.PieDataset;

public class JDBCPieDataset extends DefaultPieDataset  {
  final static long serialVersionUID = -8753216855496746108L;
  private transient Connection connection;
  public JDBCPieDataset(Connection con) {
    super();
    if(con == null) {
      throw new NullPointerException("A connection must be supplied.");
    }
    this.connection = con;
  }
  public JDBCPieDataset(Connection con, String query) throws SQLException {
    this(con);
    executeQuery(query);
  }
  public JDBCPieDataset(String url, String driverName, String user, String password) throws SQLException, ClassNotFoundException {
    super();
    Class.forName(driverName);
    this.connection = DriverManager.getConnection(url, user, password);
  }
  public void close() {
    try {
      this.connection.close();
    }
    catch (Exception e) {
      System.err.println("JdbcXYDataset: swallowing exception.");
    }
  }
  public void executeQuery(String query) throws SQLException {
    executeQuery(this.connection, query);
  }
  public void executeQuery(Connection con, String query) throws SQLException {
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      statement = con.createStatement();
      resultSet = statement.executeQuery(query);
      ResultSetMetaData metaData = resultSet.getMetaData();
      int columnCount = metaData.getColumnCount();
      if(columnCount != 2) {
        throw new SQLException("Invalid sql generated.  PieDataSet requires 2 columns only");
      }
      int columnType = metaData.getColumnType(2);
      double value = Double.NaN;
      while(resultSet.next()){
        Comparable key = resultSet.getString(1);
        switch (columnType){
          case Types.NUMERIC:
          case Types.REAL:
          case Types.INTEGER:
          case Types.DOUBLE:
          case Types.FLOAT:
          case Types.DECIMAL:
          case Types.BIGINT:
          value = resultSet.getDouble(2);
          setValue(key, value);
          break ;
          case Types.DATE:
          case Types.TIME:
          case Types.TIMESTAMP:
          Timestamp date = resultSet.getTimestamp(2);
          value = date.getTime();
          setValue(key, value);
          break ;
          default:
          java.io.PrintStream var_3981 = System.err;
          var_3981.println("JDBCPieDataset - unknown data type");
          break ;
        }
      }
      fireDatasetChanged(new DatasetChangeInfo());
    }
    finally {
      if(resultSet != null) {
        try {
          resultSet.close();
        }
        catch (Exception e) {
          System.err.println("JDBCPieDataset: swallowing exception.");
        }
      }
      if(statement != null) {
        try {
          statement.close();
        }
        catch (Exception e) {
          System.err.println("JDBCPieDataset: swallowing exception.");
        }
      }
    }
  }
}