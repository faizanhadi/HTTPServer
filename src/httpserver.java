/**
 * Created by faizan on 9/4/16.
 */
import javax.activation.MimetypesFileTypeMap;
import java.awt.datatransfer.MimeTypeParseException;
import java.io.IOException;
import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.*;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.lang.*;

public class httpserver extends Thread {

    //String httpResponse;

    public static void main(String args[] ) throws IOException {

        ServerSocket server = new ServerSocket(0);
        InetAddress ip;
        ip = InetAddress.getLocalHost();
        String hostname = ip.getHostName();
        System.out.println("Listening for connection on " + hostname +" at port "+server.getLocalPort());
//        System.out.println("Listening for connection on " +hostname +" at port "+ip);

        while (true) {
            Socket clientSocket = server.accept();
            //System.out.println("Connection accepted");

            (new httpserver(clientSocket)).start();


        }
    }

    Socket connectedclient;
    BufferedReader inFromClient = null;
    DataOutputStream outToClient = null;
    //static String[][] count= new String [10][2];
    public static Map<String,Integer> count = new HashMap<String, Integer>();



    public httpserver (Socket client){

        connectedclient = client;
    }


    public void run() {
        try {

            InputStreamReader isr = new InputStreamReader(connectedclient.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            outToClient = new DataOutputStream(connectedclient.getOutputStream());

            String requestString = reader.readLine();
            String headerLine = requestString;

            StringTokenizer tokenizer = new StringTokenizer(headerLine);
            String httpMethod = tokenizer.nextToken();
            String httpstring = tokenizer.nextToken();
            String httpversion = tokenizer.nextToken();


            String line = reader.readLine();
            while (!line.isEmpty()) {
                // System.out.println(line);
                line = reader.readLine();
            }
            /*
            System.out.println(httpMethod );
            System.out.println(httpstring );
            System.out.println(httpversion );
            */

            String workingdir = System.getProperty("user.dir");
            //String absoluterdir = workingdir + File.separator + "src" + File.separator + "www" + httpstring;
            String checkdir = workingdir + File.separator + "www";
            if (!(new File(checkdir).exists())) {
                System.out.println("Error. 'www' directory does not exists");
                System.exit(0);

            }
            String absoluterdir = workingdir + File.separator + "www" + httpstring;

            if (httpMethod.equals("GET")) {
                if (new File(absoluterdir).isFile()) {

                    StringBuilder fileRequested = new StringBuilder(httpstring);
                    fileRequested.deleteCharAt(0);

                    System.out.print(httpstring + "|");
                    System.out.print(connectedclient.getInetAddress().getHostAddress() + "|");

                    if (count.containsKey(httpstring) == true) {
                        count.put(httpstring, count.get(httpstring) + 1);
                    } else {
                        count.put(httpstring, 1);
                    }

                    //for(String key : count.keySet())
                    {
                        System.out.println(count.get(httpstring));
                    }

                    String statusCode;
                    if (new File(absoluterdir).isFile()) {
                        //System.out.println("present file" );
                        statusCode = "200 OK";

                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat(
                                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        String dateTime = dateFormat.format(calendar.getTime());
                        //System.out.println("date " + dateFormat.format(calendar.getTime()));
                        String serverName = "Oasis";

                        File filemodified = new File(absoluterdir);
                        String modi = dateFormat.format(filemodified.lastModified());
                        //System.out.println("modi date " + modi);

                        String contentType= identifyFileType (httpstring );

                        double contentLength = filemodified.length();
                        String DContent;

                        FileInputStream fileInputStream = null;
                        byte[] bfile = new byte[(int) filemodified.length()];

                        try {
                            fileInputStream = new FileInputStream(filemodified);
                            fileInputStream.read(bfile);
                            fileInputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String header = "HTTP/1.1 200 OK\r\nDate: " + dateTime + "\r\nServer: " + serverName + "\r\nLast-Modified: " + modi +
                                "\r\nContent-Type: " +contentType +
                                "\r\nContent-Length: " + contentLength + "\r\n\r\n";
                        byte[] bHeader = header.getBytes();

                        byte[] httpResponse = new byte[bHeader.length + bfile.length];
                        System.arraycopy(bHeader, 0, httpResponse, 0, bHeader.length);
                        System.arraycopy(bfile, 0, httpResponse, bHeader.length, bfile.length);

                        outToClient.write(httpResponse);
                        outToClient.close();

                    }
                }
                else{
                    //statusCode = "404 Not Found";
                    String response404 = "HTTP/1.0 404 Not Found\r\n\r\n"+"<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n" +
                            "<html><head>\n" +
                            "<title>400 Bad Request</title>\n" +
                            "</head><body>\n" +
                            "<h1>Bad Request</h1>\n" +
                            "<p>Your browser sent a request that this server could not understand.<br />\n" +
                            "</p>\n" +
                            "</body></html>";
                    outToClient.writeBytes(response404);
                    outToClient.close();
                }
            }
        }
        catch (Exception e) {
            System.out.print("");
        }
    }


    public  String identifyFileType (String filename ){
        MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
        return fileTypeMap.getContentType(filename);
    }

}



