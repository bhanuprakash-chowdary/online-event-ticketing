package com.oetp;

import com.oetp.domain.Event;
import com.oetp.service.TicketService;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        //command line info
        TicketService service= new TicketService();
        Scanner scanner=new Scanner(System.in);
        service.addEvent(new Event(1,"ColdPlay", 100));

        while(true) {
            System.out.println("\nCommands: list, add <id> <name> <tickets>, book <user> <eventId> <tickets>, exit");
            String input = scanner.nextLine().trim();
            String [] parts=input.split("\\s");

            if(parts[0].equals("exit"))break;
            else if (parts[0].equals("list")) {
                service.listEvents();
            }else if(parts[0].equals("add") && parts.length==4){
                int id = Integer.parseInt(parts[1]);
                String name = parts[2];
                int tickets = Integer.parseInt(parts[3]);
                service.addEvent(new Event(id,name,tickets));
                System.out.println("Added: " + name);
            }else if(parts[0].equals("book") && parts.length==4){

                String name=parts[1];
                int eventId=Integer.parseInt(parts[2]);
                int quantity=Integer.parseInt(parts[3]);
                service.bookTicket(name,eventId,quantity);
            }else {
                System.out.println("Invalid command");
            }
        }
        scanner.close();

    }
}