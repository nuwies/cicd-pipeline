import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import java.sql.*;
import java.io.*;
import java.time.LocalDate;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.StringBuilder;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.text.*;
import java.nio.*;

@MultipartConfig
public class FileUploadServlet extends HttpServlet {
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
      response.setContentType("text/html");
      response.setCharacterEncoding("UTF-8");
      PrintWriter out = response.getWriter();
      out.println(
            "<!DOCTYPE html>" +
                  "<head>" +
                  "<title>File Upload Form</title>" +
                  "</head>" +
                  "<body>" +
                  "<h1>Upload file</h1>" +
                  "<form method=\"POST\" action=\"upload\"" +
                  "enctype=\"multipart/form-data\">" +
                  "<input type=\"file\" name=\"FileName\"/>" +
                  "Caption: <input type=\"text\" name=\"Question\"/>" +
                  "Date: <input type=\"date\" name=\"UploadDate\"/>" +
                  "<input type=\"submit\" value=\"Submit\"/>" +
                  "</form>" +
                  "</body>" +
                  "</html>");
   }

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
      Part filePart = request.getPart("FileName");
      String question = request.getParameter("Question");
      String uploadDate = request.getParameter("UploadDate");
      String fileName = filePart.getSubmittedFileName();
      if (uploadDate.equals(""))
         uploadDate = "2020-10-10";
      if (question.equals(""))
         question = "No Question";
      System.out.println(">>>>>" + question + uploadDate + fileName);
      Connection con = null;
      try {
         Class.forName("com.mysql.cj.jdbc.Driver");
      } catch (Exception ex) {
         System.out.println("Message: " + ex.getMessage());
         return;
      }
      try {
         con = DriverManager.getConnection("jdbc:mysql://localhost:3306/assignment1", "system", "mysql1");
         PreparedStatement preparedStatement = con
               .prepareStatement("INSERT INTO trivias (ID,Question, ContentPath, Content) VALUES (?,?,?,?)");
         UUID uuid = UUID.randomUUID();
         preparedStatement.setBytes(1, asBytes(uuid));
         preparedStatement.setString(2, question);
         preparedStatement.setString(3, "image/jpeg");
         preparedStatement.setBinaryStream(4, filePart.getInputStream());
         int row = preparedStatement.executeUpdate();
         preparedStatement.close();
         // con.close();
      } catch (SQLException ex) {
         while (ex != null) {
            System.out.println("Message: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("ErrorCode: " + ex.getErrorCode());
            ex = ex.getNextException();
            System.out.println("");
         }
      }
      /*
       * <img src="data:image/gif;base64,R0lGODlhEAAOALMAAOazToeHh0tLS/7LZv/0jvb29t/
       * f3//Ub//ge8WSLf/rhf/3kdbW1mxsbP//mf///yH5BAAAAAAALAAAAAAQAA4AAARe8L1Ekyky67
       * QZ1hLnjM5UUde0ECwLJoExKcppV0aCcGCmTIHEIUEqjgaORCMxIC6e0CcguWw6aFjsVMkkIr7g7
       * 7ZKPJjPZqIyd7sJAgVGoEGv2xsBxqNgYPj/gAwXEQA7"
       * width="16" height="14">
       */
      byte bArr[] = null;
      UUID sid = null;
      try {
         Statement stmt2 = con.createStatement();
         ResultSet rs = stmt2.executeQuery("SELECT id, question, contentpath, content FROM trivias");
         rs.next();
         byte[] raw = rs.getBytes(1);
         sid = asUuid(raw);
         question = rs.getString(2);
         String contentPath = rs.getString(3);
         Blob b = rs.getBlob(4);
         bArr = b.getBytes(1, (int) b.length());
         stmt2.close();
         con.close();
      } catch (SQLException ex) {
         while (ex != null) {
            System.out.println("Message: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("ErrorCode: " + ex.getErrorCode());
            ex = ex.getNextException();
            System.out.println("");
         }
      }
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      String page = "<!DOCTYPE html><html><body>" +
            "<img src=\"data:image/jpeg;base64," +
            Base64.getEncoder().encodeToString(bArr) + "\"" +
            " width=\"500\" height=\"500\"></img>" +
            "</body></html>";
      System.out.println(page);
      out.println(page);
   }

   public static byte[] asBytes(UUID uuid) {
      ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
      bb.putLong(uuid.getMostSignificantBits());
      bb.putLong(uuid.getLeastSignificantBits());
      return bb.array();
   }

   public static UUID asUuid(byte[] bytes) {
      ByteBuffer bb = ByteBuffer.wrap(bytes);
      long firstLong = bb.getLong();
      long secondLong = bb.getLong();
      return new UUID(firstLong, secondLong);
   }
}
