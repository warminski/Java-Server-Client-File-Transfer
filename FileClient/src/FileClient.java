import java.io.*;
import java.net.Socket;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class FileClient {
    //Connection from labs
    Socket toServerSocket;
    public FileClient(String host, int port){
        System.out.println("Client connecting to host: "+host+" port: "+port);
        try{
            toServerSocket = new Socket(host,port);
        }catch (Exception exc ){
            System.out.println("Client connecting error, exception: "+exc);
            System.exit(1);
        }
        System.out.println("Client connected successfully");
    }
    public void saveFile(){
        try{
            //CHOICE
            Object choice = "s";

            // STREAMS
            ObjectInputStream inStream;
            inStream = new ObjectInputStream(toServerSocket.getInputStream());
            ObjectOutputStream outStream;
            outStream = new ObjectOutputStream(toServerSocket.getOutputStream());

            // SEND CHOICE TO SERVER
            outStream.writeObject(choice);

            // READING USER INPUT
            System.out.println("please insert file name you want to send");
            Scanner scanner = new Scanner(System.in);
            String filename = scanner.nextLine();

            // GET FILE PATH
            Path path = FileSystems.getDefault().getPath(filename);

            // GET ALL BYTES OF FILE
            byte[] content = Files.readAllBytes(path);

            // SEND BYTES AND NAME TO SERVER
            outStream.writeObject(filename);
            outStream.writeObject(content);

            // GET RESPONSE
            System.out.println(inStream.readObject().toString());



        }catch (Exception exc){}

    }
    public void loadFile(){
        try{
            // CHOICE
            Object choice = "l";

            // STREAMS
            ObjectInputStream inStream;
            inStream = new ObjectInputStream(toServerSocket.getInputStream());
            ObjectOutputStream outStream;
            outStream = new ObjectOutputStream(toServerSocket.getOutputStream());
            outStream.writeObject(choice);

            //PRINT AVAILABLE FILES ON SERVER
            System.out.println("FILES AVAILABLE TO LOAD FROM SERVER: ");

            //GET LIST OF FILES FROM SERVER
            File[] listOfFiles = (File[])inStream.readObject();
            for(int i = 0; i < listOfFiles.length;i++)
            {
                    System.out.println(listOfFiles[i].getName());
            }


            System.out.println("please insert file name you want to load");

            //SEND TO SERVER LIST OF FILES FROM CLIENT
            File checkName = new File(".");
            File[] listOfOccupiedNames = checkName.listFiles();
            outStream.writeObject(listOfOccupiedNames);

            //SEND TO SERVER FILE NAME TO LOAD
            Scanner scanner = new Scanner(System.in);
            Object filenameSend = scanner.nextLine();
            outStream.writeObject(filenameSend);

            // IF ITS OCCUPIED, GET RESPONSE FROM SERVER
            if((boolean)inStream.readObject()==true)
            {
                System.out.println(inStream.readObject().toString());
            }

            // IF ITS NOT, LOAD FILE AND GET RESPONSE
            else
            {
                File f = new File(inStream.readObject().toString());
                Path path = FileSystems.getDefault().getPath(f.getName());
                byte[] content = (byte[])inStream.readObject();
                Files.write(path,content);
                System.out.println(inStream.readObject().toString());
            }





        }catch (Exception exc){}

    }
    public void getFileSize(){
        try{
            //CHOICE
            Object choice = "g";

            // STREAMS
            ObjectInputStream inStream;
            inStream = new ObjectInputStream(toServerSocket.getInputStream());
            ObjectOutputStream outStream;
            outStream = new ObjectOutputStream(toServerSocket.getOutputStream());

            // SEND CHOICE TO SERVER
            outStream.writeObject(choice);


            //GET FILE LIST FROM SERVER
            System.out.println("FILES AVAILABLE ON SERVER: ");
            File[]fileList = (File[]) inStream.readObject();
            for(int i = 0; i < fileList.length;i++)
            {
                System.out.println(fileList[i].getName());
            }

            //SEND TO SERVER FILE NAME TO CHECK SIZE
            Scanner scanner = new Scanner(System.in);
            Object filenameSend = scanner.nextLine();
            outStream.writeObject(filenameSend);

            //GET RESPONSE
            System.out.println(inStream.readObject().toString());

        }catch (Exception exc){}

    }


    public static void main(String[] args) {
        String host = "127.0.0.1";
        int portNr = 54321;
        FileClient client;
        client = new FileClient(host,portNr);
        System.out.println("CHOSE OPTION:  s - to save file, l - to load file, g - to get file size \n");
        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine();
        if(choice.equals("s"))
        {
            System.out.println("FILES AVAILABLE TO SAVE: \n");
            // PRINT LIST OF AVAILABLE FILES IN CLIENT DIRECTORY
            File folder = new File(".");
            File[] listOfFiles = folder.listFiles();
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    System.out.println(file.getName());
                }
            }
            client.saveFile();
        }
        if(choice.equals("l"))
        {
            client.loadFile();
        }
        if(choice.equals("g"))
        {
            client.getFileSize();
        }

    }
}
