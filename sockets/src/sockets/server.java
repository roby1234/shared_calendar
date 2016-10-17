/* 
 * Author : Onkar Singh and Samya Ranjan Patro
 * Created At: 12/05/2016
 * Updated At: 01/05/2016
 * Accepts connections from clients , communicates to the clients via messages
 * and files.The Server has a permanent calendar which stores all the events.And 
 * a log file maintains the log of clients .
 * 
 * */


package sockets;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
import net.fortuna.ical4j.model.property.Uid;

public class server {
	//Port no to accept connections and informations from clients
    private static   final int PORT = 9001;
    //Version no of the calendar 
    public static   int VersionNo;
    
    /* 
     * names- to store the user names of each connected client; 
     * writers- to store output streams of all connected users to broadcast
     * 
     * */
    private static   HashSet<String> names = new HashSet<String>();
    private static   HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    
    /*
     * Function to read version from file when server starts
     * */
    public static int readversionfromfile() throws IOException{
    	try{
    	BufferedReader bis = new BufferedReader(new FileReader("/home/samya/workspace/SharedCalendar/server_version.txt"));
    	 String linee;
    	 linee = bis.readLine();
    	 int server_versionno = Integer.parseInt(linee);
    	 return server_versionno;
    }catch(FileNotFoundException e){
		return 0;
	}    	  
    }
    
	 /*
	  * Accepts connections from clients on PORT-9001, and creates a new thread to 
	  * handle each client separately 
	  */
    public static  void main(String[] args) throws Exception {
        System.out.println("The Calendar server is running.");
       VersionNo= readversionfromfile();

        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
            	Socket sockk=listener.accept();
                new Handler(sockk).start();               
            }
        } finally {
            listener.close();
        }
    }

    
    /*Handler Class for handling each clients separately 
     * */
    private  static class Handler extends Thread {
        private   String username;
		private String password;
        private   Socket socket;
        private   BufferedReader in;
        private   PrintWriter out;
        private  static  String ics_file;
        private    String temp_ics_file ="/home/samya/workspace/SharedCalendar/server_temp_"+username+".ics";
        private   static Calendar calendar ;
        private    static Calendar tempCal ;
        private   static String serverlogFile = "/home/samya/workspace/SharedCalendar/server_perm_log_file.txt";
        private   int clientno;
       
        public Handler(Socket socket) {
            this.socket = socket;   
        }
        /*
         * To Set Properties of calendar
         * */
        private   void set_properties(Calendar calendar) {
            calendar.getProperties().add(new ProdId("Calendar Log"));
            calendar.getProperties().add(Version.VERSION_2_0);
            calendar.getProperties().add(CalScale.GREGORIAN);
        }
        /*
         * Creates Calendar Object from the ics file give in function arguments
         * */
        public   Calendar createCalendar(String ics_file) throws ParserException{
            net.fortuna.ical4j.model.Calendar calendar = null;
          
            File file = new File(ics_file);
            if(!file.exists() || file.length() == 0){
                calendar = new net.fortuna.ical4j.model.Calendar();
                set_properties(calendar);
            }
            else{               
                FileInputStream fin = null;
                try {
                    fin = new FileInputStream(ics_file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                CalendarBuilder builder = new CalendarBuilder();
                try {
                    calendar = builder.build(fin);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return calendar;
        }
        public   Calendar createTempCalendar(String temp_ics_file) throws ParserException{
            net.fortuna.ical4j.model.Calendar tempCal = null;
            //checking if the file exists , if exists, what is the file length
            File file = new File(temp_ics_file);
            if(!file.exists() || file.length() == 0){
                //if file doesn't exist, instantiate a new calendar object
                tempCal = new net.fortuna.ical4j.model.Calendar();
                set_properties(tempCal);
            }
            else{
                //if file exists, put all events present in file to the present calendar object
                FileInputStream fin = null;
                try {
                    fin = new FileInputStream(temp_ics_file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                CalendarBuilder builder = new CalendarBuilder();
                try {
                    tempCal = builder.build(fin);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return tempCal;
        }
        
       /*
        * Finds events in file , arguments passed- time stamp 
        * */
        private static VEvent find_event_in_file( String s) throws IOException {
            
            java.util.Calendar startCal = java.util.Calendar.getInstance();
            java.util.Calendar endCal = java.util.Calendar.getInstance();
            try{
                Integer t = Integer.parseInt(s.toString().substring(8,12));
                Integer t1 = Integer.parseInt(s.toString().substring(12,14));
                Integer t2 = Integer.parseInt(s.toString().substring(14,16));
                Integer t3 = Integer.parseInt(s.toString().substring(17,19));
                Integer t4 = Integer.parseInt(s.toString().substring(19,21));
                startCal.set(t,t1-1,t2,t3,t4,0);    
                endCal.set(9999,12,31,23,59,00);
            }
            catch(Exception e){
                System.out.println("Error : Invalid time-stamps");
                return null;
            }    
            VEvent event = null;
            DateTime start = new DateTime(startCal.getTime());
            DateTime end = new DateTime(endCal.getTime());
            
            event = new VEvent(start,end,"Test event");
        
          //  System.out.println(event.getProperties().getProperty(Property.DTSTART));
    
            for (Object o : calendar.getComponents("VEVENT")) {
                Component c = (Component)o;
             
                if(c.getProperties().getProperty(Property.DTSTART).equals(event.getProperties().getProperty(Property.DTSTART))){  
                    return (VEvent) c;
                }
            }
            return null;
        }
       /*
        * Remove Events from the permanent file
        * */
        
        public  void delete_from_server( String temp_remove) throws IOException{
            VEvent meeting = null;
            Scanner sc = new Scanner(new File(temp_remove));
            List<String> lines = new ArrayList<String>();
            while (sc.hasNextLine()) {
              lines.add(sc.nextLine());
            }
            String[] arr = lines.toArray(new String[0]);
            for(String line : arr){
                System.out.println(line);
                meeting = find_event_in_file(line);
                if(meeting == null){
                    System.out.println("Event with given time-stamp does not exist in calendar");
                    return;
                }
                calendar.getComponents().remove(meeting);
                VersionNo++;
                writeversiontofile();
                try(FileWriter fw = new FileWriter(serverlogFile, true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw))
                    {
                        out.print(username + "\nevent deleted\n");
                        out.print(meeting.getProperties().getProperty(Property.DTSTART));
                        out.print(meeting.getProperties().getProperty(Property.DTEND));
                        out.println();
                        
                    } catch (IOException e) {
                       e.printStackTrace();
                    }
            }
            FileOutputStream fout = null;
            CalendarOutputter outputter = new CalendarOutputter();
            try {
                fout = new FileOutputStream(ics_file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
              
            outputter.setValidating(false);
                    
            try {
                outputter.output(calendar, fout);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            return;
        }
        
        /*
         * Reading Log file for syncing
         * */
        public  int  readlogfile( int cversion, String addfile,String delfile) throws IOException, ParserException{
            Scanner sc = new Scanner(new File(serverlogFile));
            List<String> lines = new ArrayList<String>();
            while (sc.hasNextLine()) {
              lines.add(sc.nextLine());
            }
           int count=0;
            Calendar addcal = createCalendar(addfile);
            String[] arr = lines.toArray(new String[0]);
            for(int row=(5*cversion + 1); row<arr.length; row += 5){
                if(arr[row].equals("event added")){
                    VEvent event = find_event_in_file(arr[row+1]);
                    if(event != null)
                        addcal.getComponents().add(event);
                    else
                    	count++;
                }
                else if(arr[row].equals("event deleted")){
                    try(FileWriter fw = new FileWriter(delfile, true);
                            BufferedWriter bw = new BufferedWriter(fw);
                            PrintWriter out = new PrintWriter(bw))
                        {
                            out.println(arr[row+1]);
                        } catch (IOException e) {
                        }
                }
            }
            FileOutputStream fout = null;
            CalendarOutputter outputter = new CalendarOutputter();
            try {
                fout = new FileOutputStream(addfile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
              
            outputter.setValidating(false);
                    
            try {
                outputter.output(addcal, fout);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            
            return count;
        }
        /*
         * Function to send email along with attachment
         * */
        public static boolean send_email_attachment(String email, String subject, String description,String filename){
            final String username = "cs546.dcs@gmail.com";
            final String password = "cs546.dcs1695";
            Properties props = System.getProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            
            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator(){
                        protected PasswordAuthentication getPasswordAuthentication(){
                            return new PasswordAuthentication(username,password);
                        }
            } );
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("cs546.dcs@gmail.com"));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(email));
                message.setSubject(subject);
                
                Multipart multipart = new MimeMultipart();
                BodyPart body = new MimeBodyPart();
                body.setText(description);
                multipart.addBodyPart(body);

                // Part two is attachment
                body = new MimeBodyPart();
          
                DataSource source = new FileDataSource(filename);
                body.setDataHandler(new DataHandler(source));
                body.setFileName(filename);
                multipart.addBodyPart(body);

                // Send the complete message parts
                message.setContent(multipart);

                // Send message
                Transport.send(message);
                System.out.print("Attachment successfully sent to " + email);
            }
            catch (MessagingException me) {
                me.printStackTrace();
                return false;
            }
            return true;
        }

       /*
        * Moves each event from temporary ics file to permanent ics file
        * */
        public   synchronized void move(Calendar calendar, String ics_file, Calendar tempCal, String temp_ics_file) throws ParserException, IOException{
            Component event = null;
            String startevent = null;
            String endevent = null;
            String startc = null;
            String endc = null;
            boolean flag11=false;
            String evntsclashed="/home/samya/workspace/SharedCalendar/server_eventsclashed"+username+".txt";

            net.fortuna.ical4j.model.Calendar newCal = new net.fortuna.ical4j.model.Calendar();
            set_properties(newCal);
            
            while((event = tempCal.getComponents().getComponent(Component.VEVENT)) != null){
                startevent = event.getProperties().getProperty(Property.DTSTART).toString().trim().substring(8);
                endevent = event.getProperties().getProperty(Property.DTEND).toString().trim().substring(6);
                boolean overlap = false;
                for (Object o : calendar.getComponents("VEVENT")) {
                    Component c = (Component)o;
                    
                    startc = c.getProperties().getProperty(Property.DTSTART).toString().trim().substring(8);
                    endc = c.getProperties().getProperty(Property.DTEND).toString().trim().substring(6);

                    if(startevent.compareTo(startc) < 0 && endevent.compareTo(startc) < 0){    
                     
                        overlap = false;
                    }
                    else if(startevent.compareTo(endc) > 0 && endevent.compareTo(endc) > 0){
                      
                        overlap = false;
                    }
                    else{
                      
                        overlap = true;
                        break;
                    }
                }
                if(overlap == false){
                	 try(FileWriter fw = new FileWriter(serverlogFile, true);
                             BufferedWriter bw = new BufferedWriter(fw);
                             PrintWriter out = new PrintWriter(bw))
                         {                        
                             out.print(username + "\nevent added\n");
                             out.print(event.getProperties().getProperty(Property.DTSTART));
                             out.print(event.getProperties().getProperty(Property.DTEND));
                             out.println();
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                    tempCal.getComponents().remove(event);
                    calendar.getComponents().add((CalendarComponent) event);
                    VersionNo++;
                    writeversiontofile();
                    System.out.println(VersionNo);
                    newCal.getComponents().add((CalendarComponent) event);
                }
                else{
                    tempCal.getComponents().remove(event);
                   
                   flag11=true;
                    try(FileWriter fw = new FileWriter(evntsclashed, true);
                            BufferedWriter bw = new BufferedWriter(fw);
                            PrintWriter out = new PrintWriter(bw))
                        {                        
                            out.print(username + "\nevent clashed\n");
                            out.println(event);
                            out.println();
                        } catch (IOException e) {
                            System.out.println("IOException");
                        }
                }
                
                
            }
            if(flag11){
            	send_email_attachment("samya.ranjan.patro@gmail.com","Events clashed"+username," .....",evntsclashed);
            	out.println("Event clashed");
                boolean success1 = (new File
                        (evntsclashed)).delete();
                        if ( !success1) {
                           System.out.println("The file has not  been deleted"); 
                        }
            }
            FileOutputStream fout = null;
            CalendarOutputter outputter = new CalendarOutputter();
            try {
                fout = new FileOutputStream(ics_file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
              
            outputter.setValidating(false);
                    
            try {
                outputter.output(calendar, fout);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            fout = null;
            outputter = new CalendarOutputter();
            try {
                fout = new FileOutputStream(temp_ics_file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
              
            outputter.setValidating(false);
                    
            try {
                outputter.output(newCal, fout);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            

        }

      
        
        public void writeversiontofile() throws IOException{
        	
        	try(   
            	    PrintWriter outt = new PrintWriter("/home/samya/workspace/SharedCalendar/server_version.txt")){	
            	    	 outt.println(VersionNo);
            	         
            	}
            	catch(IOException e){
            		e.printStackTrace();
            	}
        	 
        }

        
        
        public    void Sendfile(String s) throws IOException{

        	BufferedReader bis = new BufferedReader(new FileReader(s));
  	      String linee;
  	      while ((linee = bis.readLine()) != null) {
  	    	  out.println(linee);
  	
  	       }
  	      out.println("stop");
  	      bis.close();

        }
        
        public   void get_last_x_events(Calendar calendar, int VERSION_NO_OF_SERVER, int VERSION_NO) throws ParserException{
        	System.out.println(VERSION_NO_OF_SERVER);
        	System.out.println(VERSION_NO);
            String sync_file = "/home/samya/workspace/SharedCalendar/server_temp_sync"+username+".ics";
            int count = VERSION_NO_OF_SERVER;
            int diff = VERSION_NO_OF_SERVER - VERSION_NO; 
            Calendar sync = createCalendar(sync_file);
            for (Object o : calendar.getComponents("VEVENT")) {
                Component c = (Component)o;
                count--;
                if(count <= (diff - 1) && count >= 0){
                    sync.getComponents().add((CalendarComponent) c);
                }
            }
            
        FileOutputStream fout = null;
        CalendarOutputter outputter = new CalendarOutputter();
        try {
            fout = new FileOutputStream(sync_file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
          
        outputter.setValidating(false);
                
        try {
            outputter.output(sync, fout);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ValidationException e) {
            e.printStackTrace();
        }
        }
        
        
        public long getFileSize(String filename) {
            File file = new File(filename);
            if (!file.exists() || !file.isFile()) {
               System.out.println("File doesn\'t exist");
               return -1;
            }
            return file.length();
         }

        
        public void Sendfiletosync(String s) throws IOException{
        	
        	BufferedReader bis = new BufferedReader(new FileReader(s));
  	      String linee;
  	      while ((linee = bis.readLine()) != null) {
  	    	  out.println(linee);
  	    
  	       }
  	      out.println("stop");
  	      bis.close();
        }
        
        public synchronized  void Broadcastfile(String s,String msg) throws IOException{
     
        	for (PrintWriter writer : writers) {
            	
                writer.println("Broadcastingfile_"+msg);
                BufferedReader bis = new BufferedReader(new FileReader(s));
        	      String linee;
        	      while ((linee = bis.readLine()) != null) {
        	    	  writer.println(linee);
        	   
        	       }
        	      writer.println("stop");
        	      bis.close();
                
            }
        	

        	
        } 
        
        public   void Broadcastmessage(String s){
        	for (PrintWriter writer : writers) {
            	
                writer.println("MESSAGE " + username + ": " + s);
            }
        } 
        
        public   void Recievefile(String s) throws IOException{
        	try(    BufferedWriter bw = new BufferedWriter(new FileWriter(s, true));
            	    PrintWriter outt = new PrintWriter(bw)){
            	    while(true){
            	    	String input1 = in.readLine();
            	   
            	    	if(input1.equals("stop"))
            	    		break;
            	    	 outt.println(input1);
            	         
            	    }
            	}
            	catch(IOException e){
            		e.printStackTrace();
            	}

        }
 
        
        public   void copyFileUsingStream(File source, File dest) throws IOException {
            Files.copy(source.toPath(), dest.toPath());
        }
        public   void AddingData() throws IOException{
        	 
        	Recievefile(temp_ics_file);
        	File source = new File(temp_ics_file);
        	String destin ="/home/samya/workspace/SharedCalendar/server_temp_new"+username+".ics"; 
        	File dest = new File(destin);
        	copyFileUsingStream(source,dest);
      	 
      try {
			tempCal = createTempCalendar(temp_ics_file);
		} catch (ParserException e) {
			
			e.printStackTrace();
		}
      
      try {
			move(calendar, ics_file, tempCal, temp_ics_file);
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      Broadcastfile(temp_ics_file,"add");
      out.println("Sendingfile");
      Sendfile(destin);
      boolean success = (new File
              (temp_ics_file)).delete();
      boolean success1 = (new File
              (destin)).delete();
              if (!success || !success1) {
                 System.out.println("The file has not  been deleted"); 
              }
        	
        } 

        public void run() {
            try {

                boolean flag3= false;
            	 in = new BufferedReader(new InputStreamReader(
                         socket.getInputStream()));
                     out = new PrintWriter(socket.getOutputStream(), true);
                
                    out.println("SubmitUserNameAndPassword");
                    username = in.readLine();
                    System.out.println(username+"    "+getId());
                    password = in.readLine();
                    
                    if (username == null || password == null) {
                        return;
                    }else {
                        FileInputStream fis = new FileInputStream("/home/samya/workspace/SharedCalendar/src/authenticate.txt");
                		//Construct BufferedReader from InputStreamReader
                		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                			 	boolean flag1=true;
                			 	boolean flag2 = true;
                				String line = null;
                				while ((line = br.readLine()) != null) {
                					String[] splited = line.split(" ");
//                					System.out.println(splited[0]);
//                                    System.out.println(splited[1]);
                					if(username.equals(splited[0]) && password.equals(splited[1]) ){
                						if(names.contains(username)){
                							flag2=false;
                							break;
                						}
                						flag1=false;
                					//	System.out.println("Hello1");
                						break;
                					}
                				}
                				br.close();
                				if(!flag2){
                				//	System.out.println("I got in");
                					flag3=true;
                					out.println("ClientAlreadyRunning");
                				}
                				else if(flag1){
                					out.println("ClientRejected");
                				}
                				else{
                					 synchronized (names) {
                	                    	
                	                        if (!names.contains(username)) {
                	                            names.add(username);
                	                            out.println("ClientAccepted");
                	                            writers.add(out);
                	                        }
                	                    }
                					 
                					 ics_file = "perm_server_.ics";
                		                temp_ics_file = "/home/samya/workspace/SharedCalendar/server_temp_"+username+".ics";
                		               
                		                
                		                //request version no;
                		                //VersionNo =0;
                		                try {
											calendar = createCalendar(ics_file);
										} catch (ParserException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
                		               
                					 
                				}                				
                    }
                   
                
               


                

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                    if(!flag3)
                while (true) {
                    String input = in.readLine();
                   //System.out.println(input+ username+getId());
                    if (input == null) {
                    	   if (username != null) {
                           	System.out.println(username + " Disconnected");
                               names.remove(username);
                           }
                           if (out != null) {
                               writers.remove(out);
                           }
                           try {
                               socket.close();
                           } catch (IOException e) {
                           }
                        return;
                    }
                    else if (input.startsWith("AddEvent")){
                    	//System.out.println(input);
                    	out.println("ADDACK");
                    }
                    else if (input.startsWith("SendingFile")){
                    	
                    		AddingData();
                    	
                    }
                    else if(input.startsWith("sync")){
                    	String[] splited1 = input.split("_");
                    	int client_versionno = Integer.parseInt(splited1[1]);
                    	if(client_versionno < VersionNo){
                    	//get_last_x_events(calendar, VersionNo, client_versionno);
                    		
                    	 String add_file = "/home/samya/workspace/SharedCalendar/server_temp_sync_add"+username+".ics";
                    	 String del_file = "/home/samya/workspace/SharedCalendar/server_temp_sync_del"+username+".txt";
                    	 System.out.println("Inside Sync of Server");
                    	int xx= readlogfile(  client_versionno, add_file, del_file);
                    	 out.println("Syncfile_"+xx);
                    	Sendfiletosync(add_file);
                    	String input1= in.readLine();
                    	if(input1.startsWith("Donewithsyncadd")){
                    		 File file = new File(del_file);
                    		if(!(file.length()==0 || !file.exists())){
                    		 System.out.println("Inside delSync of Server");
                    		out.println("delSyncfile");
                    		Sendfiletosync(del_file);
                    		}
                    	}
                    	boolean success1 = (new File
                                (add_file)).delete();
                    	boolean success2 = (new File
                                (del_file)).delete();
                                if ( !success1 || !success2) {
                                   System.out.println("The file has not  been deleted"); 
                                }
                    	}
                    	else{
                    		out.println("Alreadyuptodate");
                    	}
                    } else if (input.startsWith("DeleteEvent")){
                    	//System.out.println(input);
                    	out.println("RemoveACK");
                    }
                    else if (input.startsWith("sendingFiletoremove")){
                    //	out.println("RemoveACK");
                    	//System.out.println("helloim");
                    	 String remove_file = "/home/samya/workspace/SharedCalendar/server_temp_remove"+username+".txt";
                    	 Recievefile(remove_file);
                    	 delete_from_server(remove_file);
                    	  Broadcastfile(remove_file,"delete");
                         
                          boolean success = (new File
                                  (remove_file)).delete();
          
                                  if (!success) {
                                     System.out.println("The file has not  been deleted"); 
                                  }
                    	 
                    }
                    else{
                    	//System.out.println(input);
                    }
                    
                    
                }
            } catch (IOException | ParserException e) {
                System.out.println(e);
            } finally {

             
            }
        }
    }
}