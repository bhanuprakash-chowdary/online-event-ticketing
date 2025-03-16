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

        TicketService service= new TicketService();
        ExecutorService pool = Executors.newFixedThreadPool(5);
        service.addEvent(new Event(1,"ColdPlay", 100));

        for (int i = 1; i <= 10; i++) {
            String user = "User" + i;
            pool.submit(()-> System.out.println(service.bookTicket(user, 1, 2)));
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }
}