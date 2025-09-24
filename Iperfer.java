import java.io.*;
import java.net.*;

public class Iperfer {
    private static final int CHUNK_SIZE = 1000; 
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Error: missing or additional arguments");
            System.exit(1);
        }
        
        // if we see -c, use client mode
        if (args[0].equals("-c")) {
            if (args.length != 7) {
                System.out.println("Error: missing or additional arguments");
                System.exit(1);
            }
            
            // check all arguments
            if (!args[1].equals("-h") || !args[3].equals("-p") || !args[5].equals("-t")) {
                System.out.println("Error: missing or additional arguments");
                System.exit(1);
            }
            
            String serverHost = args[2];
            int serverPort = 0;
            int time = 0;
            
            try {
                serverPort = Integer.parseInt(args[4]);
                time = Integer.parseInt(args[6]);
            } catch (NumberFormatException e) {
                System.out.println("Error: missing or additional arguments");
                System.exit(1);
            }
            
            // make sure all port fit in the range
            if (serverPort < 1024 || serverPort > 65535) {
                System.out.println("Error: port number must be in the range 1024 to 65535");
                System.exit(1);
            }
            
            runClient(serverHost, serverPort, time);
            
        } // if we see -s, we use server mode
        else if (args[0].equals("-s")) {
            if (args.length != 3) {
                System.out.println("Error: missing or additional arguments");
                System.exit(1);
            }

            // check all arguments
            if (!args[1].equals("-p")) {
                System.out.println("Error: missing or additional arguments");
                System.exit(1);
            }
            
            int listenPort = 0;
            
            try {
                listenPort = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.out.println("Error: missing or additional arguments");
                System.exit(1);
            }
            
            if (listenPort < 1024 || listenPort > 65535) {
                System.out.println("Error: port number must be in the range 1024 to 65535");
                System.exit(1);
            }
            
            runServer(listenPort);
            
        } else {
            System.out.println("Error: missing or additional arguments");
            System.exit(1);
        }
    }
    
    private static void runClient(String serverHost, int serverPort, int time) {
        Socket socket = null;
        OutputStream out = null;
        
        try {
            // create socket and connect it to server
            socket = new Socket(serverHost, serverPort);
            out = socket.getOutputStream();
            
            // create buffer of zeros within the chunk size
            byte[] data = new byte[CHUNK_SIZE];
            
            long totalBytesSent = 0;
            long startTime = System.nanoTime();
            // convert seconds to nanoseconds and set the end time
            long endTime = startTime + (long)time * 1_000_000_000L;
            
            // send data for time
            while (System.nanoTime() < endTime) {
                out.write(data);
                totalBytesSent += CHUNK_SIZE;
            }
            
            long actualEndTime = System.nanoTime();
            double elapsedSeconds = (actualEndTime - startTime) / 1_000_000_000.0;
            
            // calculate and print stat we want
            double totalKB = totalBytesSent / 1000.0;
            double totalMegabits = (totalBytesSent * 8.0) / 1_000_000.0;
            double rate = totalMegabits / elapsedSeconds;
            
            System.out.printf("sent=%d KB rate=%.3f Mbps%n", (long)totalKB, rate);
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } finally {
            try {
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                // ignore other errors
            }
        }
    }
    
    private static void runServer(int listenPort) {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        InputStream in = null;
        
        try {
            // create socket and listen for connections
            serverSocket = new ServerSocket(listenPort);
            clientSocket = serverSocket.accept();
            in = clientSocket.getInputStream();
            
            // read data the client sent
            byte[] buffer = new byte[CHUNK_SIZE];
            long totalBytesReceived = 0;
            int bytesRead;
            
            long startTime = 0;
            boolean firstByteReceived = false;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                if (!firstByteReceived) {
                    startTime = System.nanoTime();
                    firstByteReceived = true;
                }
                totalBytesReceived += bytesRead;
            }
            
            long endTime = System.nanoTime();
            
            if (firstByteReceived) {
                double elapsedSeconds = (endTime - startTime) / 1_000_000_000.0;
                
                // calculate and print stats we want
                double totalKB = totalBytesReceived / 1000.0;
                double totalMegabits = (totalBytesReceived * 8.0) / 1_000_000.0;
                double rate = totalMegabits / elapsedSeconds;
                
                System.out.printf("received=%d KB rate=%.3f Mbps%n", (long)totalKB, rate);
            } else {
                System.out.println("received=0 KB rate=0.000 Mbps");
            }
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } finally {
            try {
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                // ignore other errors
            }
        }
    }
}