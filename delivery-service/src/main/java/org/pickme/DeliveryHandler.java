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

public class DeliveryHandler implements RequestHandler<Map<String,String>, String>{
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

//    JSONObject paramObj = new JSONObject(evenParams);

    try {

      // Connect to the database
      Connection conn = DriverManager.getConnection("jdbc:mysql://pickmefood.cn4g5pawgjm1.us-east-1.rds.amazonaws.com:3306/pickmefood?useSSL=false", "admin", "OgXqylVqq7LldFMq1tY8");

      // Execute a query and print the result
      String statementString = "SELECT isAccepted,isActive,deliveryStatus,idRestaurant FROM OrderDetails";
      if(!paramObj.get("orderId").toString().replaceAll("\"", "").equals("")) {
        statementString += " WHERE idOrder = ?";
      }

      logger.log("statementString: " + statementString);
      PreparedStatement stmt = conn.prepareStatement(statementString);
      if(!paramObj.get("orderId").toString().replaceAll("\"", "").equals("")) {
        stmt.setInt(1,paramObj.get("orderId").getAsInt());
      }

      ResultSet rs = stmt.executeQuery();
      String resultString = "[";
      while (rs.next()) {
//        System.out.println(rs.getString(1));
        logger.log("results: " + rs.getString(1));
        resultString += "\"{ \"isAccepted\": \""+rs.getBoolean(1)+"\", \"isActive\": \""+rs.getBoolean(2)+"\", \"deliveryStatus\": "+rs.getString(3)+", \"idRestaurant\":"+rs.getInt(4)+"}\",";
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