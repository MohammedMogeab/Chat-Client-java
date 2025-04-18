package program.chatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClients {
    public static    Socket sockets;

    static {
        try {
            sockets = new Socket("localhost",1234);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public SocketClients(Socket socket){
        sockets=socket;
    }
    public static Socket getSocket(){
        return sockets;
    }
    public static void  setSocket(Socket socket) throws IOException {


        sockets=socket;
    }

}
