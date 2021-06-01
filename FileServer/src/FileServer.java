import java.io.*;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileServer {
    ServerSocket clientConn;
    public FileServer(int port){
        System.out.println("server connecting to port:" +port);
        try{
            clientConn = new ServerSocket(port);

        }catch (Exception exc){
            System.out.println("server connecting error, exception: "+exc);
            System.exit(1);
        }
        System.out.println("server connected to port: "+port);
    }

    public void listenLoop(){
        while(true){
            try{
                /// CONNECTION
                Socket clientReqSocket;
                ObjectInputStream inStream;
                ObjectOutputStream outStream;
                clientReqSocket = clientConn.accept();
                System.out.println("Server, connection from: "+clientReqSocket.getInetAddress().getHostName());

                //STREAMS
                outStream = new ObjectOutputStream(clientReqSocket.getOutputStream());
                inStream = new ObjectInputStream(clientReqSocket.getInputStream());

                //READ CHOICE
                Object choice = inStream.readObject();
                if(choice.equals("s"))
                {
                    //GET LOCAL PATH
                    File f = new File(inStream.readObject().toString());
                    Path path = FileSystems.getDefault().getPath(f.getName());
                    byte[] content = (byte[])inStream.readObject();
                    Files.write(path,content);

                    // SEND RESPONSE
                    Object response = "File saved successfully";
                    outStream.writeObject(response);
                }
                if(choice.equals("l"))
                {
                    Object flag = false;  // FLAG TO CHECK IF NAME IS OCCUPIED
                    // GET SERVER LIST OF FILES
                    File folder = new File(".");
                    File[] listOfFiles = folder.listFiles();
                    outStream.writeObject(listOfFiles);

                    // GET CLIENT LIST OF FILES
                    File[] filesOnClient = (File[]) inStream.readObject();
                    String filename = inStream.readObject().toString();

                    // CHECK IF FILE TO LOAD IS ALREADY OCCUPIED ON CLIENT
                    for(int i = 0; i < filesOnClient.length;i++)
                    {
                        // IF IT IS DONT SEND FILE AND SEND RESPONSE
                        if(filesOnClient[i].getName().equals(filename))
                        {
                            Object response = "Sorry, but this name is already occupied.";
                            flag = true;
                            outStream.writeObject(flag);
                            outStream.writeObject(response);
                            break;
                        }
                    }
                    // IF IS NOT SEND FILE AND RESPONSE
                    if((boolean)flag==false)
                    {
                        outStream.writeObject(flag);
                        Path path = FileSystems.getDefault().getPath(filename);
                        byte[] content = Files.readAllBytes(path);
                        outStream.writeObject(filename);
                        outStream.writeObject(content);
                        Object response = "File loaded successfully.";
                        outStream.writeObject(response);
                    }
                }
                if(choice.equals("g"))
                {
                    //FLAG TO CHECK IF FILE EXISTS
                    boolean flag = false;

                    //GET LIST OF FILES ON SERVER AND SEND IT TO CLIENT
                    File folder = new File(".");
                    File[] listOfFiles = folder.listFiles();
                    outStream.writeObject(listOfFiles);

                    //GET FILE NAME FROM CLIENT
                    String fileName = inStream.readObject().toString();

                    //CHECK IF IT EXISTS
                    for(int i = 0; i<listOfFiles.length;i++)
                    {
                        // IF YES, SEND FILE SIZE
                        if(listOfFiles[i].getName().equals(fileName))
                        {
                            flag = true;
                            double size = (double)listOfFiles[i].length();
                            Object response = String.format("Size of file: %s is %f B",fileName,size);
                            outStream.writeObject(response);
                            break;
                        }
                    }
                    //IF NOT SEND "-1"
                    if(flag==false)
                    {
                        Object response = "-1";
                        outStream.writeObject(response);
                    }
                }
            }catch (Exception exc){
                System.out.println("server error in listenloop(), exception: "+exc);
            }
        }
    }

    public static void main(String[] args) {
        int portNr = 54321;
        FileServer server;
        server = new FileServer(portNr);
        server.listenLoop();
    }
}
