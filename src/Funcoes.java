import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;

public class Funcoes {

    private static String serverAddress = "localhost";
    private static String clientAddress = "localhost";
    private static Integer serverPort = 3000;
    private static Integer clientPort = 8000;
    private static Integer maxPacketSize = 256;
    private static Integer maxDataSize = 10;
    private static InetAddress address1;
    private static Integer menorSeqNum = 0;
    private static Integer maxSeqNum = 20;
    private static Integer maxMessageSize = 512;

    public static Integer getMaxDataSize() {
        return maxDataSize;
    }

    public static Integer getMenorSeqNum() {
        return menorSeqNum;
    }

    public static Integer getMaxMessageSize(){
        return maxMessageSize;
    }

    public static Integer getMaxSeqNum() {
        return maxSeqNum;
    }

    public static String getClientAddress() {
        return clientAddress;
    }

    public static void send(DatagramSocket udpSocket, String message, String address, Integer port){
        System.out.println("[Enviado]      " + message);
        try {
            address1 = InetAddress.getByName(address);
        } catch (UnknownHostException e){
            e.printStackTrace();
            return;
        }
        DatagramPacket pacote = new DatagramPacket(message.getBytes(), message.length(), address1, port);
        try {
            udpSocket.send(pacote);
        } catch(IOException e){
            e.printStackTrace();
            return;
        }

        return;
    }

    public static Integer getMaxPacketSize() {
        return maxPacketSize;
    }

    public static Thread sleepFor(Integer time){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        });

        return thread;
    }

    public static String getServerAddress(){
        return serverAddress;
    }

    public static Integer getServerPort(){
        return serverPort;
    }

    public  static Integer getClientPort(){
        return clientPort;
    }

    public static Integer getRandomNumber(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
