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
    //String response = "200 OK\n";
    String response = "{ \"statusCode\": \"500\", \"message\": \"No Data Found!\"}";
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
//      Connection conn = DriverManager.getConnection("jdbc:mysql://pickmefood.cn4g5pawgjm1.us-east-1.rds.amazonaws.com:3306/pickmefood?useSSL=false", "admin", "OgXqylVqq7LldFMq1tY8");
      Connection conn = DriverManager.getConnection("jdbc:mysql://"+System.getenv("DBHOST"), System.getenv("USERNAME"), System.getenv("PW"));

      // Execute a query and print the result
      String statementString = "SELECT name,description,rating,menu FROM Restaurant";
      if(!paramObj.get("name").toString().replaceAll("\"", "").equals("")) {
        logger.log("paramObj Name has content: " + paramObj.get("name").toString());
        statementString += " WHERE name LIKE ?";
      }
      if(!paramObj.get("rating").toString().replaceAll("\"", "").equals("")) {
        logger.log("paramObj rating has content: " + paramObj.get("name").toString() +","+paramObj.get("rating").toString());
        if(!paramObj.get("name").toString().replaceAll("\"", "").equals("")) {
          statementString += " AND";
        }
        statementString += " WHERE rating = ?";
      }
      logger.log("statementString: " + statementString);
      PreparedStatement stmt = conn.prepareStatement(statementString);
      if(!paramObj.get("name").toString().replaceAll("\"", "").equals("") && !paramObj.get("rating").toString().replaceAll("\"", "").equals("")) {
        logger.log("paramObj Name and rating has content 2: " + paramObj.get("name").toString() +","+paramObj.get("rating").toString());
        stmt.setString(1,paramObj.get("name").toString()+ "%");
        stmt.setFloat(2,paramObj.get("rating").getAsFloat());
      }else if(!paramObj.get("name").toString().replaceAll("\"", "").equals("")) {
        logger.log("name only 2: " + paramObj.get("name").toString().replaceAll("\"", ""));
        stmt.setString(1,paramObj.get("name").toString().replaceAll("\"", "")+ "%");
      }else if(!paramObj.get("rating").toString().replaceAll("\"", "").equals("")) {
        logger.log("rating only 2: " + paramObj.get("rating").toString());
        stmt.setFloat(1,paramObj.get("rating").getAsFloat());
      }

      ResultSet rs = stmt.executeQuery();
      String resultString = "[";
      while (rs.next()) {
//        System.out.println(rs.getString(1));
        logger.log("results: " + rs.getString(1));
        resultString += "\"{ \"name\": \""+rs.getString(1)+"\", \"description\": \""+rs.getString(2)+"\", \"rating\": "+rs.getFloat(3)+"}\",";
        logger.log("results String: " + resultString);
        logger.log("results String: " + resultString);
        //JsonObject resultObject = new Gson().fromJson(resultString, JsonObject.class);

      }


      // Close the connection
      conn.close();
      return resultString.substring(0, resultString.length() - 1) +"]";
    } catch (Exception e) {
      logger.log("Exception:: "+e.getMessage());
      e.printStackTrace();
    }

    //return json
    //JsonObject responseObject = new Gson().fromJson(response, JsonObject.class);
    return response;
  }
}