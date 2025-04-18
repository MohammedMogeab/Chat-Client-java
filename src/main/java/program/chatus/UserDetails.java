package program.chatus;

import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDetails {

    public boolean registerUser(String username,String password ,String email){
        try {
            Connection conn =DatabaseConnection.getConnection();
            String sql="Insert into users(username) values(?)";

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,username);

            return preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String loginUser(String username) throws SQLException, IOException {
       try {
           Connection conn = DatabaseConnection.getConnection();
           String query = "select * from users where username=? ";

           PreparedStatement preparedStatement = conn.prepareStatement(query);
           preparedStatement.setString(1, username);

           var resultSet = preparedStatement.executeQuery();
       }catch (SQLException e){
           System.out.println("the result not found");

       }
       SocketClients.setSocket(new Socket("localhost",1234));
return username;
    }

//    public List<Chat> chatList (int Userid) throws SQLException {
//        List<Chat>chatuser=new ArrayList<>();
//        String query="select c.chat_id,c.chat_name,c.chat_type  from chat c join user_chat uc on c.chat_id=uc.chat_id where uc.user_id=?";
//        Connection connection = DatabaseConnection.getConnection()  ;
//        PreparedStatement preparedStatement = connection.prepareStatement(query );
//        preparedStatement.setInt(1,Userid);
//        try(var resultset=preparedStatement.executeQuery()) {
//            while (resultset.next()){
//                int chatId=resultset.getInt("chat_id");
//                String chatName=resultset.getString("chat_name");
//                String  chatType=resultset.getString("chat_type");
//                chatuser.add(new Chat(chatId,chatName,chatType));
//            }

//        } catch (SQLException e) {
//        e.printStackTrace();
//    }
//    return chatuser;
//
//    }
//
//    public boolean sendMessage(int chatId, int senderId, String messageText) {
//        String query = "INSERT INTO message (chat_id, sender_id, message_text) VALUES (?, ?, ?)";
//        try (Connection connection = DatabaseConnection.getConnection();
//             PreparedStatement statement = connection.prepareStatement(query)) {
//
//            statement.setInt(1, chatId);
//            statement.setInt(2, senderId);
//            statement.setString(3, messageText);
//
//            int rowsInserted = statement.executeUpdate();
//            return rowsInserted > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//
//    }






}
