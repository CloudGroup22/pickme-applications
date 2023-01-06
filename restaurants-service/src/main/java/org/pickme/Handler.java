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
import com.google.gson.JsonObject;



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

    String evenParams = gson.toJson(event);
    JsonObject paramObj = new Gson().fromJson(evenParams, JsonObject.class);
    logger.log("paramObj: " + paramObj);
    logger.log("paramObj: " + paramObj.get("name"));
    logger.log("paramObj: " + paramObj.get("rating"));

//    JSONObject paramObj = new JSONObject(evenParams);

    try {

      // Connect to the database
      Connection conn = DriverManager.getConnection("jdbc:mysql://pickmefood.cn4g5pawgjm1.us-east-1.rds.amazonaws.com:3306/pickmefood?useSSL=false", "admin", "OgXqylVqq7LldFMq1tY8");

      // Execute a query and print the result
      String statementString = "SELECT name,description,rating FROM Restaurant";
      if(!paramObj.get("name").toString().replaceAll("\"", "").equals("")) {
        logger.log("paramObj Name has content: " + paramObj.get("name").toString());
        statementString += " WHERE name LIKE ?";
      }
      if(!paramObj.get("rating").toString().replaceAll("\"", "").equals("")) {
        logger.log("paramObj rating has content: " + paramObj.get("name").toString() +","+paramObj.get("rating").toString());
        if(!paramObj.get("name").toString().replaceAll("\"", "").equals("")) {
          statementString += " AND";
        }
        statementString += " WHERE rating LIKE ?";
      }
      logger.log("statementString: " + statementString);
      PreparedStatement stmt = conn.prepareStatement(statementString);
      if(!paramObj.get("name").toString().replaceAll("\"", "").equals("") && !paramObj.get("rating").toString().replaceAll("\"", "").equals("")) {
        logger.log("paramObj Name and rating has content 2: " + paramObj.get("name").toString() +","+paramObj.get("rating").toString());
        stmt.setString(1,paramObj.get("name").toString()+ "%");
        stmt.setFloat(2,paramObj.get("rating").toFloat()+ "%");
      }else if(!paramObj.get("name").toString().replaceAll("\"", "").equals("")) {
        logger.log("name only 2: " + paramObj.get("name").toString().replaceAll("\"", ""));
        stmt.setString(1,paramObj.get("name").toString().replaceAll("\"", "")+ "%");
        logger.log("name only 2: " + stmt);
        logger.log("name only 2: " + stmt);
        logger.log("name only 2: " + stmt);
      }else if(!paramObj.get("rating").toString().replaceAll("\"", "").equals("")) {
        logger.log("rating only 2: " + paramObj.get("rating").toString());
        stmt.setFloat(1,paramObj.get("rating").toFloat()+ "%");
      }

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
//        System.out.println(rs.getString(1));
        logger.log("results: " + rs.getString(1));
        return rs.getString(1) + "\n";
      }

      // Close the connection
      conn.close();
    } catch (Exception e) {
      logger.log("Exception: "+e.getMessage());
      e.printStackTrace();
    }
    return response;
  }
}