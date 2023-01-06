package org.pickme;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.Map;
import com.google.gson.JsonElement;

// Handler value: example.Handler
public class Handler implements RequestHandler<Map<String,String>, String>{
  Gson gson = new GsonBuilder().setPrettyPrinting().create();
  @Override
  public String handleRequest(Map<String,String> event, Context context)
  {
    LambdaLogger logger = context.getLogger();
    String response = "200 OK\n";
    logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
    logger.log("CONTEXT: " + gson.toJson(context));
    logger.log("EVENT: " + gson.toJson(event));
    logger.log("EVENT TYPE: " + event.getClass());

    JsonElement evenParams = gson.toJson(event);
    try {

      // Connect to the database
      Connection conn = DriverManager.getConnection("jdbc:mysql://pickmefood.cn4g5pawgjm1.us-east-1.rds.amazonaws.com:3306/pickmefood", "admin", "OgXqylVqq7LldFMq1tY8");

      // Execute a query and print the result
      String statementString = "SELECT name,description,rating FROM Restaurant";
      if(evenParams.name != null) {
        statementString += " WHERE name LIKE ?";
      }
      if(evenParams.rating != null) {
        if(evenParams.name != null) {
          statementString += " AND";
        }
        statementString += " WHERE name LIKE ?";
      }

      PreparedStatement stmt = conn.prepareStatement(statementString);
      if(evenParams.name != null && evenParams.rating != null) {
        stmt.setString(1,evenParams.name+ "%");
        stmt.setString(2,event.rating+ "%");
      }else if(evenParams.name != null) {
        stmt.setString(1,evenParams.name+ "%");
      }else if(evenParams.rating != null) {
        stmt.setString(1,evenParams.rating+ "%");
      }

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
//        System.out.println(rs.getString(1));
        return rs.getString(1) + "\n";
      }

      // Close the connection
      conn.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return response;
  }
}