/*
 * Author : Onkar Singh
 * Author : Samya Ranjan
 * Written on : 12/5/2016
 * Last Modified : 1/6/2016*/
package sockets;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.io.*;
import java.net.SocketException;
import java.net.URISyntaxException;
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

public class CalendarClient {

	 public static 	 String s;
		 public static BufferedReader in;
	  public  static PrintWriter out;
	  public static  String ics_file;
      public static  String temp_ics_file ;
      public static String logFile ;
      public static  String templogFile;
      public static  Calendar calendar ;
      public static   Calendar tempCal ;
      public static  int VersionNo;
      public static  int PresentOp;
      public static Socket socket;
      public static String temp_remove ;
		 
    private static String getServerAddress() {
        return "10.8.12.228";
    }
    private static String clientID = "1";
    //setting the properties of a calendar
    private static void set_properties(Calendar calendar) {
        calendar.getProperties().add(new ProdId("Calendar Log"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
    }

    public static Calendar createCalendar(String ics_file) throws ParserException{
        net.fortuna.ical4j.model.Calendar calendar = null;
        //checking if the file exists , if exists, what is the file length
        File file = new File(ics_file);
        if(!file.exists() || file.length() == 0){
            //if file doesn't exist, instantiate a new calendar object
            calendar = new net.fortuna.ical4j.model.Calendar();
            set_properties(calendar);
        }
        else{
            //if file exists, put all events present in file to the present calendar object
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
    public static Calendar createTempCalendar(String temp_ics_file) throws ParserException{
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
    
    //options, that user can select
    public static void options(Calendar calendar, String ics_file, String logFile) throws URISyntaxException{
        String s = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while(true){
        try {
            System.out.println("1. Add an event");
            System.out.println("2. Delete an event");
            System.out.println("3. List events");
            System.out.println("4. List time-stamps of all events");
                s = in.readLine().trim();
            
            int choice = Integer.parseInt(s);
            switch(choice){
                case 1 : add_an_event(calendar, ics_file, logFile);
                    break;
                case 2 : delete_an_event();
                    break;
                case 3 : list_events();
                    break;
                case 4 : list_event_time_stamps();
                    break;                        
                default : System.out.println("Oops !! You have chosen a wrong choice");
                    break;
            }
        } catch (NumberFormatException e) {
            System.err.println("Please Enter valid number");
            options(calendar, ics_file, logFile);
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        }
    }
    public static void input(StringBuilder s1, StringBuilder s2 ,StringBuilder s3, StringBuilder s4, StringBuilder s5, StringBuilder s6){ 
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String s = null;
		System.out.println("Event Name : ");
		try {
			s = in.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		s1.delete(0, s1.length());
      s1.append(s);
		System.out.println("Location : ");
		try {
			s = in.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		s2.delete(0, s2.length());
      s2.append(s);
		System.out.println("Description : ");
		try {
			s = in.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		s3.delete(0, s3.length());
      s3.append(s);
		System.out.println("Host Email ID : ");
		try {
			s = in.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		s4.delete(0, s4.length());
      s4.append(s);
      System.out.println("Start TimeStamp in format (YYYY/MM/DD/HH/MM) : ");
		try {
			s = in.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		s5.delete(0, s5.length());
      s5.append(s);
      System.out.println("End TimeStamp in format (YYYY/MM/DD/HH/MM) : ");
		try {
			s = in.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		s6.delete(0, s6.length());
      s6.append(s);
  }

    public static VEvent createEvent() throws URISyntaxException, SocketException {
		VEvent meeting = null;
		
		//time variables
		java.util.Calendar startCal = java.util.Calendar.getInstance();
		java.util.Calendar endCal = java.util.Calendar.getInstance();
		
		//String variables -> properties
		StringBuilder subject = new StringBuilder("");
		StringBuilder location = new StringBuilder("");
		StringBuilder description = new StringBuilder("");
		StringBuilder hostEmail = new StringBuilder("default");
		StringBuilder sDate = new StringBuilder("");
		StringBuilder eDate = new StringBuilder("");
		
		input(subject,location,description,hostEmail,sDate,eDate);
		
		try{
			Integer t = Integer.parseInt(sDate.toString().substring(0,4));
			Integer t1 = Integer.parseInt(sDate.toString().substring(5,7));
			Integer t2 = Integer.parseInt(sDate.toString().substring(8,10));
			Integer t3 = Integer.parseInt(sDate.toString().substring(11,13));
			Integer t4 = Integer.parseInt(sDate.toString().substring(14));
			startCal.set(t,t1-1,t2,t3,t4,0); 	//starting time of meeting

			t = Integer.parseInt(eDate.toString().substring(0,4));
			t1 = Integer.parseInt(eDate.toString().substring(5,7));
			t2 = Integer.parseInt(eDate.toString().substring(8,10));
			t3 = Integer.parseInt(eDate.toString().substring(11,13));
			t4 = Integer.parseInt(eDate.toString().substring(14));
			endCal.set(t,t1-1,t2,t3,t4,0);		//ending time of meeting
		}
		catch(Exception e){
			return null;
		}	
		DateTime start = new DateTime(startCal.getTime());
		DateTime end = new DateTime(endCal.getTime());
		
		meeting = new VEvent(start,end,subject.toString());
		UidGenerator ug = new UidGenerator("1");
		meeting.getProperties().add(ug.generateUid());
		//Adding location and description to the meeting event											
		meeting.getProperties().add(new Location(location.toString()));
		meeting.getProperties().add(new Description());

		try {
			meeting.getProperties().getProperty(Property.DESCRIPTION).setValue(description.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//Adding emailID of host to meeting event
		try {
			meeting.getProperties().add(new Organizer("MAILTO:"+hostEmail.toString() + "@gmail.com"));
		} catch (URISyntaxException e) {
			meeting.getProperties().add(new Organizer("MAILTO:"+"default@gmail.com"));
		}
		
		return meeting;
	}


  

	

	private static void delete_an_event() {
			temp_delete();
			RemoveEvent();
	}
	
	public static void sync(){
		String versionno= Integer.toString(VersionNo);
		out.println("sync_"+versionno);
	}
	
	private static void list_event_time_stamps() {
        java.util.Calendar start = java.util.Calendar.getInstance();
        java.util.Calendar end = java.util.Calendar.getInstance();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s = null, s1 = null;
        System.out.println("Start TimeStamp in format (YYYY/MM/DD/HH/MM) : ");
        try {
            s = in.readLine();
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        System.out.println("End TimeStamp in format (YYYY/MM/DD/HH/MM) : ");
        try {
            s1 = in.readLine();
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        try{
            Integer t = Integer.parseInt(s.toString().substring(0,4));
            Integer t1 = Integer.parseInt(s.toString().substring(5,7));
            Integer t2 = Integer.parseInt(s.toString().substring(8,10));
            Integer t3 = Integer.parseInt(s.toString().substring(11,13));
            Integer t4 = Integer.parseInt(s.toString().substring(14));
            start.set(t,t1-1,t2,t3,t4,0);     //starting time of meeting
            t = Integer.parseInt(s1.toString().substring(0,4));
            t1 = Integer.parseInt(s1.toString().substring(5,7));
            t2 = Integer.parseInt(s1.toString().substring(8,10));
            t3 = Integer.parseInt(s1.toString().substring(11,13));
            t4 = Integer.parseInt(s1.toString().substring(14));
            end.set(t,t1-1,t2,t3,t4,0);        //ending time of meeting
        }
        catch(Exception e){
            System.out.println("Error : Invalid time-stamps");
            return;
        }    
    
        Period period = new Period(new DateTime(start.getTime()), new DateTime(end.getTime()));
        for (Object o : calendar.getComponents("VEVENT")) {
            Component c = (Component)o;
            PeriodList list = c.calculateRecurrenceSet(period);
            for (Object po : list) {
                System.out.println(po);
            }
        }
    }



//list the events between two time-stamps
    public static void list_events(){
        java.util.Calendar start = java.util.Calendar.getInstance();
        java.util.Calendar end = java.util.Calendar.getInstance();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s = null, s1 = null;
        System.out.println("Start TimeStamp in format (YYYY/MM/DD/HH/MM) : ");
        try {
            s = in.readLine();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        System.out.println("End TimeStamp in format (YYYY/MM/DD/HH/MM) : ");
        try {
            s1 = in.readLine();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try{
            Integer t = Integer.parseInt(s.toString().substring(0,4));
            Integer t1 = Integer.parseInt(s.toString().substring(5,7));
            Integer t2 = Integer.parseInt(s.toString().substring(8,10));
            Integer t3 = Integer.parseInt(s.toString().substring(11,13));
            Integer t4 = Integer.parseInt(s.toString().substring(14));
            start.set(t,t1-1,t2,t3,t4,0);     //starting time of meeting
            t = Integer.parseInt(s1.toString().substring(0,4));
            t1 = Integer.parseInt(s1.toString().substring(5,7));
            t2 = Integer.parseInt(s1.toString().substring(8,10));
            t3 = Integer.parseInt(s1.toString().substring(11,13));
            t4 = Integer.parseInt(s1.toString().substring(14));
            end.set(t,t1-1,t2,t3,t4,0);        //ending time of meeting
        }
        catch(Exception e){
            System.out.println("Error : Invalid time-stamps");
            return;
        }    
        // create a period starting now with a duration of one (1) day..
        Period period = new Period(new DateTime(start.getTime()), new DateTime(end.getTime()));
        Filter filter = new Filter(new PeriodRule(period));

        ComponentList eventsToday = (ComponentList) filter.filter(calendar.getComponents(Component.VEVENT));
        System.out.println(eventsToday);
        return;
    }



	
    //client temp file
    public static void temp_delete(){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s = null;
        //taking date as input
        System.out.println("Start TimeStamp in format (YYYY/MM/DD/HH/MM) : ");
        try {
            s = in.readLine();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        java.util.Calendar startCal = java.util.Calendar.getInstance();
        java.util.Calendar endCal = java.util.Calendar.getInstance();
        try{
            Integer t = Integer.parseInt(s.toString().substring(0,4));
            Integer t1 = Integer.parseInt(s.toString().substring(5,7));
            Integer t2 = Integer.parseInt(s.toString().substring(8,10));
            Integer t3 = Integer.parseInt(s.toString().substring(11,13));
            Integer t4 = Integer.parseInt(s.toString().substring(14));
            startCal.set(t,t1-1,t2,t3,t4,0);    
            endCal.set(10000,12,31,23,59,00);
        }
        catch(Exception e){
            System.out.println("Error : Invalid time-stamps");
            temp_delete();
        }    
        VEvent event = null;
        DateTime start = new DateTime(startCal.getTime());
        DateTime end = new DateTime(endCal.getTime());
       // String temp_remove = "temp_remove_file.txt";
        event = new VEvent(start,end,"Test event");
        try(FileWriter fw = new FileWriter(temp_remove, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                   out.print(event.getProperties().getProperty(Property.DTSTART));
            } catch (IOException e) {
                //exception handling left as an exercise for the reader
            }
        return;
    }
    
    //function for server
    private static VEvent find_event_in_file( String s) throws IOException {
        //DTSTART:11111111T112900
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
        for (Object o : calendar.getComponents("VEVENT")) {
            Component c = (Component)o;
            if(c.getProperties().getProperty(Property.DTSTART).equals(event.getProperties().getProperty(Property.DTSTART))){    
                return (VEvent) c;
            }
        }
        return null;
    }

    //server function
    //deleting records from client temp file after broadcast
    public static void delete_from_server(String temp_remove) throws IOException{
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
            try(FileWriter fw = new FileWriter(logFile, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw))
                {
                    out.print(clientID + "\nevent deleted\n");
                    out.print(meeting.getProperties().getProperty(Property.DTSTART));
                    out.print(meeting.getProperties().getProperty(Property.DTEND));
                    
                } catch (IOException e) {
                    //exception handling left as an exercise for the reader
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

    



	public static boolean add_an_event(Calendar calendar, String ics_file, String logFile){
        System.out.println("You have chosen for adding an event");
       
        VEvent meeting = null;
        try {
            meeting = createEvent();
            if(meeting == null){
                System.out.println("Error : Invalid time-stamps");
                return false;
            }
        } catch (SocketException | URISyntaxException e1) {
            System.err.println("Error : null event");
        }
    
        calendar.getComponents().add(meeting);
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
//        System.out.println("Meeting Added to Calendar ");
        try(FileWriter fw = new FileWriter(logFile, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.print(clientID + "\tevent added\t");
        
                out.print(meeting.getProperties().getProperty(Property.DTSTART));
                out.print("\t" + meeting.getProperties().getProperty(Property.DTEND));
                
            } catch (IOException e) {
                //exception handling left as an exercise for the reader
            }
        PresentOp=1;
       // sync();
        AddEvent();
        return true;
    }

	private static  String getUserNamePassword() throws IOException {
   	 
   	 FileInputStream fis = new FileInputStream("/home/onkar/workspace/sockets/src/"+s);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line = br.readLine();
		br.close();
		fis.close();
		return line ;
 
   }
    public static void moveBtoF(Calendar calendar, String ics_file, Calendar tempCal, String temp_ics_file,boolean x) throws IOException{
        Component event = null;
        synchronized(calendar){
        while((event = tempCal.getComponents().getComponent(Component.VEVENT)) != null){
                tempCal.getComponents().remove(event);
                if(x){
                calendar.getComponents().add((CalendarComponent) event);
                VersionNo++;
                writeversiontofile();
//                System.out.println(VersionNo);
                }
                else
                	calendar.getComponents().remove((CalendarComponent) event);
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
        
        boolean success = (new File
                 (temp_ics_file)).delete();
                 if (success) {
//                    System.out.println("The file has been successfully deleted");
                 }
      //  System.out.println("Moving Done");
    }
    
    public static  void writeversiontofile() throws IOException{
    	try(   
        	    PrintWriter outt = new PrintWriter("/home/onkar/workspace/sockets/client_version"+ s +".txt")){	
        	    	 outt.println(VersionNo);
        	         
        	}
        	catch(IOException e){
        		e.printStackTrace();
        	}
    	 
    }
    public static synchronized void Addtofile() throws ParserException, IOException{
    	String BroadcastRecv="/home/onkar/workspace/sockets/BroadCast"+temp_ics_file;
 	   Recievefile(BroadcastRecv);
 	 Calendar  newcal1=createCalendar(BroadcastRecv);
 	 //  moveBtoF( calendar,ics_file, newcal1 ,BroadcastRecv,true);
 	   move(calendar,ics_file, newcal1 ,BroadcastRecv);
    }
    
    public static void move(Calendar calendar, String ics_file, Calendar tempCal, String temp_ics_file) throws ParserException, IOException{
        Component event = null;
        String startevent = null;
        String endevent = null;
        String startc = null;
        String endc = null;
     
        synchronized(calendar){
        while((event = tempCal.getComponents().getComponent(Component.VEVENT)) != null){
            startevent = event.getProperties().getProperty(Property.DTSTART).toString().trim().substring(8);
            endevent = event.getProperties().getProperty(Property.DTEND).toString().trim().substring(6);
            boolean overlap = false;
            for (Object o : calendar.getComponents("VEVENT")) {
                Component c = (Component)o;
                
                startc = c.getProperties().getProperty(Property.DTSTART).toString().trim().substring(8);
                endc = c.getProperties().getProperty(Property.DTEND).toString().trim().substring(6);

                if(startevent.compareTo(startc) < 0 && endevent.compareTo(startc) < 0){    
                   // System.out.println("1. " + startevent + " " + endevent + " " + startc + " " + endc);
                    overlap = false;
                }
                else if(startevent.compareTo(endc) > 0 && endevent.compareTo(endc) > 0){
                  //  System.out.println("2. " + startevent + " " + endevent + " " + startc + " " + endc);
                    overlap = false;
                }
                else{
                   // System.out.println("3. " + startevent + " " + endevent + " " + startc + " " + endc);
                    overlap = true;
                    break;
                }
            }
            if(overlap == false){
            
                tempCal.getComponents().remove(event);
                calendar.getComponents().add((CalendarComponent) event);
                VersionNo++;
                writeversiontofile();
//                System.out.println(VersionNo);

            }
            else{
                tempCal.getComponents().remove(event);
            }
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
            
        boolean success = (new File
             (temp_ics_file)).delete();
             if (success) {
//                System.out.println("The file has been successfully deleted");
             }

  
       // System.out.println("Moving Done");
    }
    
    public static void write_in_file(Calendar calendar, String ics_file){
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
    }

    public static int readversionfromfile() throws IOException{
    	try{
    	BufferedReader bis = new BufferedReader(new FileReader("/home/onkar/workspace/sockets/client_version"+ s +".txt"));
    	 String linee;
    	 linee = bis.readLine();
    	 int server_versionno = Integer.parseInt(linee);
    	 return server_versionno;
    	}catch(FileNotFoundException e){
    		return 0;
    	}
    	 
    }
    
    public static void moveBtoFremove(String temp, String Btemp) {
    	try{
        Scanner sc = new Scanner(new File(Btemp));
        List<String> lines1 = new ArrayList<String>();
        while (sc.hasNextLine()) {
          lines1.add(sc.nextLine());
        }
        sc = new Scanner(new File(temp));
        List<String> lines2 = new ArrayList<String>();
        while (sc.hasNextLine()) {
          lines2.add(sc.nextLine());
        }
        List<String> intersection = new ArrayList<String>(lines2);
        intersection.retainAll(lines1);        
        lines2.removeAll(intersection);
        
        PrintWriter writer = new PrintWriter(temp);
        
        for(String s : lines2){
            writer.println(s);
        }
        writer.close();
    	}
    	catch(FileNotFoundException e){
    		
    	}
  }

    
    public static void RecieveInputStream() throws ParserException, IOException{
    	
    	  while(true){
              String line = in.readLine();
              if(line ==null){
              	System.out.println("Disconnected, Server down, try reconnecting later");
              	break;
              }
              else if (line.startsWith("SubmitUserNameAndPassword")) {
              	String userpass = getUserNamePassword();
              	String[] splited = userpass.split(" ");
                  out.println(splited[0]);
                  out.println(splited[1]);
              } else if (line.startsWith("ClientAccepted")) {
                  System.out.print("Accepted by server ");
                   ics_file = "perm_client_"+s+".ics";
                   temp_ics_file = "temp_calendar_"+s+".ics";
                   logFile = "perm_log_"+s+".txt";
                   templogFile = "temp_log_"+s+".txt";
                   temp_remove = s+"_remove.txt";
        
                   calendar = createCalendar(ics_file);
                   tempCal = createTempCalendar(temp_ics_file);
                   write_in_file(tempCal, temp_ics_file );
                   VersionNo=readversionfromfile() ;
                   PresentOp=0;
                  //sync with server
                   // sync();
                  new Handler(socket,"outputtype",s).start();
                  new ConnectionHandler().start();
               //   break;
              } else if (line.startsWith("ClientRejected")) {
                  System.out.print("Rejected by server, wrong username and password ");
                  break;
              }
              else if(line.startsWith("ClientAlreadyRunning")){
           	   System.out.println("Already Connected to the server with same username");
                  break; 
              }
              else if(line.startsWith("ADDACK")){
            	  long zz= getFileSize("/home/onkar/workspace/sockets/"+temp_ics_file);
           	   out.println("SendingFile_"+zz);
           	   //System.out.println("/home/onkar/workspace/sockets/"+temp_ics_file);
           	   Sendfile("/home/onkar/workspace/sockets/"+temp_ics_file);
              }
              else if(line.startsWith("RemoveACK")){
            	
           	   out.println("sendingFiletoremove");
           	   
           	   Sendfile(temp_remove);
              }
              else if(line.startsWith("Broadcastingfile")){
            	  String[] splited1 = line.split("_");
            	  if(splited1[1].equals("add"))
           	           Addtofile();
            	  else if(splited1[1].equals("delete")){
            		  String BroadcastRecv="/home/onkar/workspace/sockets/BroadCastremove"+temp_ics_file;
            	 	   Recievefile(BroadcastRecv);
            	 	   
            	 	  delete_from_server( BroadcastRecv);
            	 	 moveBtoFremove(temp_remove, BroadcastRecv);
            	 	 boolean success = (new File
                             (BroadcastRecv)).delete();
     
                             if (!success) {
//                                System.out.println("The file has not  been deleted"); 
                             }
            	  }
           	    
              }
              else if(line.startsWith("Sendingfile")){
            	 
           	   String ss= "/home/onkar/workspace/sockets/"+s+"deleteit.ics";
           	   Recievefile(ss);
           	   Calendar  newcal2=createCalendar(ss);
           	   moveBtoF( tempCal,temp_ics_file, newcal2 ,ss,false);
          	    
             }
              else if(line.startsWith("Syncfile")){
            	  String[] splited1 = line.split("_");
            	  int xx= Integer.parseInt(splited1[1]);
            	  System.out.println(xx);
            	  VersionNo= VersionNo+xx;
            	  writeversiontofile();
            	  System.out.println("Syncing add file");
           	   String sss= "/home/onkar/workspace/sockets/"+s+"syncit.ics";
           	   Recievefile(sss);
           	   Calendar  newcal3=createCalendar(sss);
           	  // moveBtoF( calendar,ics_file, newcal3 ,sss,true);
           	   move(calendar,ics_file, newcal3 ,sss);
           	   out.println("Donewithsyncadd");
              }
              else if(line.startsWith("delSyncfile")){
                  System.out.println("Syncing delete file");
              	   String sss= "/home/onkar/workspace/sockets/"+s+"syncit.txt";
              	   Recievefile(sss);
              	   //delete_from_server(sss);
              	 VersionNo=VersionNo+CheckRemoveEvents(sss);
              	 System.out.println(CheckRemoveEvents(sss));
              	 writeversiontofile();
              	 boolean success = (new File
                         (sss)).delete();
 
                         if (!success) {
//                            System.out.println("The file has not  been deleted"); 
                         }
              	  
                 }
              else if(line.startsWith("Alreadyuptodate")){
            	  //donothing;
              }
              else {
           	   System.out.println(line);
              }
              }
    	
    }

    private  void ConnectToServer() throws ParserException{
    	try{
           
            Scanner inn = new Scanner(System.in);
            System.out.println("Enter name");
            this.s = inn.nextLine();
           
           
           String serverAddress = getServerAddress();
            socket = new Socket(serverAddress, 9001);
           in = new BufferedReader(new InputStreamReader(
                   socket.getInputStream()));
           out = new PrintWriter(socket.getOutputStream(), true);
           RecieveInputStream();
            inn.close();
           // inn.close();
          //  out.close();
        	}
        	 catch(ConnectException ee){
    	        	System.out.println("Connect Exception");
    	        	//ee.printStackTrace();
    	        }
    	        catch(NoRouteToHostException se){
    	        	System.out.println("NoRouteToHost Exception");
    	        	//se.printStackTrace();
    	        }
    	        catch(SocketException sce){
    	        	System.out.println("Socket  Exception");
    	        }
    	        catch(IOException e){
    	        	System.out.println("IOException");
    	        	e.printStackTrace();
    	        }
    }
    public static void AddEvent(){
    	boolean reachable=false;
    	 try {
		        Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 10.8.12.228");
		        int returnVal = p1.waitFor();
		         reachable = (returnVal==0);
		         
		    } catch (Exception e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
    	 if(reachable)
    	out.println("AddEvent");
    }
    public static void RemoveEvent(){
    	boolean reachable=false;
    	 try {
		        Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 10.8.12.228");
		        int returnVal = p1.waitFor();
		         reachable = (returnVal==0);
		         
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
    	 if(reachable)
    		 out.println("DeleteEvent");
    }
    public static int getcount(String file) throws ParserException{
        int count = 0;
        Calendar cal = createCalendar(file);
        for (Object o : cal.getComponents("VEVENT")) {
            Component c = (Component)o;
            count++;
        }
        return count;
    }
    public static int CheckpendingEvents() throws ParserException{
      return getcount(temp_ics_file);
    }

    public static int CheckpendingRemoveEvents() throws ParserException, IOException{
    	try{
    	BufferedReader bis = new BufferedReader(new FileReader(temp_remove));
        int x=0;
        String linee;
        while ((linee = bis.readLine()) != null) {
	    	  x++;
	       }
        return x;
    	}
    	catch(FileNotFoundException e){
    		return 0;
    	}
      }
    public static int CheckRemoveEvents(String s) throws ParserException, IOException{
    	try{
    	BufferedReader bis = new BufferedReader(new FileReader(s));
        int x=0;
        String linee;
        while ((linee = bis.readLine()) != null) {
	    	  x++;
	       }
        return x;
    	}
    	catch(FileNotFoundException e){
    		return 0;
    	}
      }
    public static void Sendfile(String s) throws IOException{
    	try{BufferedReader bis = new BufferedReader(new FileReader(s));
	      String linee;
	      while ((linee = bis.readLine()) != null) {
	    	  out.println(linee);
	       }
	      out.println("stop");
	      bis.close();
    	}
    	catch(FileNotFoundException e){
    		
    	}
    }
    
    public static long getFileSize(String filename) {
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
           System.out.println("File doesn\'t exist");
           return -1;
        }
        return file.length();
     }
    public static void Recievefile(String s) throws IOException{
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

    /**
     * Runs the client as an application with a closeable frame.
     * @throws ParserException 
     */
    public static void main(String[] args) throws ParserException {
    	 CalendarClient Client = new CalendarClient();
    	Client.ConnectToServer();
    	
    }
}
class Handler extends Thread  {
	 private String ThreadType,ClientName;
     private Socket socket;
     private BufferedReader in;
     private PrintWriter out;
	
     public Handler(Socket socket, String ThreadType,String ClientName) {
         this.socket = socket;
         this.ThreadType = ThreadType;
         this.ClientName= ClientName;
         
         try{
     		in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
         }
         catch(ConnectException ee){
	        	System.out.println("Connect Exception");
	        //	ee.printStackTrace();
	        }
	        catch(NoRouteToHostException se){
	        	System.out.println("NoRouteToHost Exception");
	        	//se.printStackTrace();
	        }
            catch(SocketException sce){
	        	System.out.println("Socket  Exception");
	        }
	        catch(IOException e){
	        	System.out.println("IOException");
	        	//e.printStackTrace();
	        }
         
     }
     
     
     

	
	public void run() {
	
	
			try {
				CalendarClient.options(CalendarClient.tempCal, CalendarClient.temp_ics_file, CalendarClient.templogFile);
			} catch (URISyntaxException e) {
			
				e.printStackTrace();
			}

	}

}

class ConnectionHandler extends Thread  {
	 
	public void run() {
		 boolean reachable= false;
		 boolean flag=false;
		 while(true){
		 try {
		        Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 10.8.12.228");
		        int returnVal = p1.waitFor();
		         reachable = (returnVal==0);
		         
		    } catch (Exception e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
		 if(reachable){
			 if(!flag){
				 System.out.println("Trying to sync file after disconnection");
			 CalendarClient.sync();
			 //call add event and remove event also
			 try {
				if(CalendarClient.CheckpendingEvents()>0){
					 CalendarClient.AddEvent();
				 }
				if(CalendarClient.CheckpendingRemoveEvents()>0){
					CalendarClient.RemoveEvent();
				 }
				//CalendarClient.out.println("DeleteEvent");;
			} catch (ParserException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//	 CalendarClient.AddEvent();
			 flag= true;
			 }
		 }
		 else{
			 flag=false;
//			 System.out.println("You are disconnected");
		 }
		 try {
			    Thread.sleep(10000);                 //sleep for 10 secs
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		 }
	}

}