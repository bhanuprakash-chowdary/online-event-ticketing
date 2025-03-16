package com.oetp;

import com.oetp.domain.Event;
import com.oetp.service.TicketService;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        //command line info
        TicketService service= new TicketService();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        Scanner scanner=new Scanner(System.in);
        service.addEvent(new Event(1,"ColdPlay", 100));


//        int userCount = 100;
//        CountDownLatch latch = new CountDownLatch(userCount);
//        for (int i = 1; i <= userCount; i++) {
//            String user = "StressUser" + i;
//            pool.submit(() -> {
//                try {
//                    service.bookTicket(user, 1, 2);
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//        latch.await();
//        System.out.println("Stress test done. Tickets: " + service.getEvent(1).getAvailableTickets());

        System.out.println("OETP CLI Started");
        while(true) {
            System.out.println("\nCommands: list, add <id> <name> <tickets>, book <user> <eventId> <tickets>, exit");
            String input = scanner.nextLine().trim();
            String [] parts=input.split("\\s");

            if(parts[0].equals("exit")){
                pool.shutdown();
                break;
            }
            else if (parts[0].equals("list")) {
                service.listEvents();
            }else if(parts[0].equals("add") && parts.length==4){
                int id = Integer.parseInt(parts[1]);
                String name = parts[2];
                int tickets = Integer.parseInt(parts[3]);
                service.addEvent(new Event(id,name,tickets));
                System.out.println("Added: " + name);
            }else if(parts[0].equals("book") && parts.length==4){

                String user=parts[1];
                int eventId=Integer.parseInt(parts[2]);
                int tickets=Integer.parseInt(parts[3]);
                service.bookTicket(user,eventId,tickets).thenAccept(System.out::println);//Async
                System.out.println(user + " booking queued...");

            }else {
                System.out.println("Invalid command");
            }
        }
        scanner.close();

    }
}