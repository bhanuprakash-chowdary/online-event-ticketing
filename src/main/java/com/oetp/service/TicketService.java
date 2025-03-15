package com.oetp.service;

import com.oetp.domain.Event;

import java.util.concurrent.locks.ReentrantLock;

public class TicketService {

    private final Event event;
    private final ReentrantLock lock=new ReentrantLock();

    public TicketService(Event event){
        this.event=event;
    }

    public void bookTicket(String user,int quantity) { //synchronize locks whole function

        System.out.println(user + " checking " + quantity + " for " + event.getName() + "( Available at the moment " + event.getAvailableTickets() + ")");

        if (event.getAvailableTickets() >= quantity) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.lock();//lock only reduce Tickets (not whole method including sleep )
            try {
                if (event.getAvailableTickets() >= quantity) {

                    event.reduceTickets(quantity);
                    System.out.println(user + " booked " + quantity + ". Remaining: " + event.getAvailableTickets());

                } else {
                    System.out.println(user + " failed - not enough tickets (race detected).");
                }
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println(user + " failed - not enough tickets.");
        }
    }
}
