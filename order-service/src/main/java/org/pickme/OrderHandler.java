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
        String response = "200 OK\n";
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

            // Connect to the database
            Connection conn = DriverManager.getConnection("jdbc:mysql://pickmefood.cn4g5pawgjm1.us-east-1.rds.amazonaws.com:3306/pickmefood", "admin", "OgXqylVqq7LldFMq1tY8");
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
            ResultSet resultSet = preparedStmtCus.executeQuery(cusQuery);
            logger.log("rsCus  "+ resultSet.toString());

            if(true){
                PreparedStatement preparedStmt = conn.prepareStatement(query);
                preparedStmt.setInt (1, 1);
                preparedStmt.setInt (2, 1);
                preparedStmt.setInt   (3, 1);
                preparedStmt.setString(4, "Pending");
                preparedStmt.setInt    (5, 1);
                preparedStmt.setString(6, "1000");
                logger.log("quary  "+ query);
                boolean execute = preparedStmt.execute();
                logger.log("rs Order =>>>> "+ execute);
            }





//             Execute a query and print the result
            Statement statement = conn.createStatement();
            logger.log("quary  " + "INSERT INTO OrderDetails" + "(`idCustomer`,`isAccepted`,`isActive`,`deliveryStatus`,`idRestaurant`)" + "VALUES (1, 1, 1,"+paramObj.get("name")+", 1)");
            ResultSet rs = statement.executeQuery("INSERT INTO OrderDetails" + "(`idCustomer`,`isAccepted`,`isActive`,`deliveryStatus`,`idRestaurant`)" + "VALUES (1, 1, 1,"+paramObj.get("name")+", 1)");
//            int rs = statement.executeUpdate("INSERT INTO OrderDetails " + "VALUES (1, 1, 1,`Delivered.`, 1, 2)");
            logger.log("rs"+ rs);

//            while (rs.next()) {
//                return rs.getString(1) + "\n";
//            }

            // Close the connection
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}