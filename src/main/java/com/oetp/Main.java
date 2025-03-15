package com.oetp;

import com.oetp.domain.Event;
import com.oetp.service.TicketService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        Event concert = new Event(1, "ColdPlay Concert", 10);

        TicketService service = new TicketService(concert);

        ExecutorService pool= Executors.newFixedThreadPool(5);

        pool.submit(() -> service.bookTicket("User1", 3));
        pool.submit(() -> service.bookTicket("User2", 3));
        pool.submit(() -> service.bookTicket("User3", 3));
        pool.submit(() -> service.bookTicket("User4", 3));
        pool.submit(() -> service.bookTicket("User5", 3));
        pool.submit(() -> service.bookTicket("User6", 3));
        pool.submit(() -> service.bookTicket("User7", 3));
        pool.submit(() -> service.bookTicket("User8", 3));
        pool.submit(() -> service.bookTicket("User9", 3));


        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS); // Wait up to 5s
        System.out.println("All bookings done. Final tickets: " + concert.getAvailableTickets());

//        Thread thread1 = new Thread(() -> service.bookTicket("User 1", 3));
//
//        Thread thread2 = new Thread() {
//            @Override
//            public void run() {
//                service.bookTicket("User 2", 3);
//            }
//        };
//
//        Thread thread3 = new Thread(() -> service.bookTicket("User 3", 3));
//        Thread thread4 = new Thread(() -> service.bookTicket("User 4", 3));
//        Thread thread5 = new Thread(() -> service.bookTicket("User 5", 3));
//
//        thread1.start();
//        thread2.start();
//        thread3.start();
//        thread4.start();
//        thread5.start();
    }
}