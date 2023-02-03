package org.pickme;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.sql.*;
import java.util.Map;

public class OrderHandler implements RequestHandler<Map<String,String>, String>{
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Override
    public String handleRequest(Map<String,String> event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        String response200 = "200 OK\n Transaction Success";
        String response417 = "417 OK\n Transaction Failed";
        logger.log(String.valueOf(event));
        String evenParams = gson.toJson(event);
        JsonObject paramObj = new Gson().fromJson(evenParams, JsonObject.class);
        logger.log("paramObj: " + paramObj);
        logger.log("paramObj Name: " + paramObj.get("name"));
//
//        String evenParams = gson.toJson(event);
//        JsonObject paramObj = new Gson().fromJson(evenParams, JsonObject.class);
//        logger.log("paramObj: " + paramObj);

        try {
            ResultSet execute;

            // Connect to the database
            Connection conn = DriverManager.getConnection("jdbc:mysql://"+System.getenv("DBHOST"), System.getenv("USERNAME"), System.getenv("PW"));
            //            conn.setAutoCommit(false);
            String cusQuery = " insert into Customer (name, phoneNumber, address)"
                    + " values (?, ?, ?)";
            String query = " insert into OrderDetails (idCustomer, isAccepted, isActive, deliveryStatus, idRestaurant, price)"
                    + " values (?, ?, ?, ?, ?, ?)";


            PreparedStatement preparedStmtCus = conn.prepareStatement(cusQuery);
            preparedStmtCus.setString(1, paramObj.get("cusName").toString());
            preparedStmtCus.setString(2, paramObj.get("cusTp").toString());
            preparedStmtCus.setString(3, paramObj.get("cusAddress").toString());
            logger.log("quary  "+ cusQuery);
            int executeCus = preparedStmtCus.executeUpdate();
            logger.log("rsCus  "+ executeCus);

            if(executeCus == 1){
                PreparedStatement preparedStmt = conn.prepareStatement(query);
                preparedStmt.setInt (1, executeCus);
                preparedStmt.setInt (2, 1);
                preparedStmt.setInt   (3, 1);
                preparedStmt.setString(4, "Pending");
                preparedStmt.setInt    (5, 1);
                preparedStmt.setInt(6, paramObj.get("idRest").getAsInt());
                logger.log("quary  "+ query);
                execute = preparedStmt.executeQuery(query);
                logger.log("rs Order =>>>> "+ execute);
            }
//            if(execute == 1){
////                conn.setAutoCommit(false);
//            }else {
////                conn.rollback();
//            }


////             Execute a query and print the result
//            Statement statement = conn.createStatement();
//            logger.log("quary  " + "INSERT INTO OrderDetails" + "(`idCustomer`,`isAccepted`,`isActive`,`deliveryStatus`,`idRestaurant`)" + "VALUES (1, 1, 1,"+paramObj.get("name")+", 1)");
//            ResultSet rs = statement.executeQuery("INSERT INTO OrderDetails" + "(`idCustomer`,`isAccepted`,`isActive`,`deliveryStatus`,`idRestaurant`)" + "VALUES (1, 1, 1,"+paramObj.get("name")+", 1)");
//            logger.log("rs"+ rs);
//


            // Close the connection
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            return response417;
        }
        return response200;
    }
}