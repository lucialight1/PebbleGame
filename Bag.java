package com.company;

import java.util.ArrayList;
import java.util.Random;

public class Bag {
    enum Colour{
        BLACK,
        WHITE
    }

    // ATTRIBUTES
    private char name;
    private Bag pair;
    private volatile ArrayList<Integer> pebbles;
    private Colour colour;

    // METHODS
    public synchronized ArrayList<Integer> getPebbles(){
        return pebbles;
    }

    public synchronized void setPebbles(ArrayList<Integer> newPebbleList){
        pebbles.clear();
        pebbles = newPebbleList;
    }

    public synchronized char getName(){
        return name;
    }

    public synchronized void setPair(Bag pair){
        this.pair = pair;
    }

    public synchronized Bag getPair(){
        return pair;
    }

    public synchronized void emptyPebbles(){
        // create an intermediate list the same as pebble list
        ArrayList<Integer> bufferList = new ArrayList<>(pebbles);

        // set the pair's pebbles to the same
        pair.setPebbles(bufferList);

        // empty the current bag pebble list
        pebbles.clear();

        // wake up any waiting threads to let them know an empty bag has been refilled
        notifyAll();
    }

    public synchronized void addPebbleToBag(int pebble){ // DISCARDING, make sure atomic
        pebbles.add(pebble);
    }

    public synchronized int removePebbleFromBag(){ // DRAWING, make sure atomic
        Random rand = new Random();

        // check bag is empty, and if so refill it
        if(pebbles.size() == 0){
            pair.emptyPebbles();
        }

        try {
            int noOfPebblesInBag = pebbles.size();
            int random = rand.nextInt(noOfPebblesInBag);
            return pebbles.remove(random);
        }catch(Exception e){
            return -1;
        }

    }

    // CONSTRUCTOR
    public Bag(char name, ArrayList<Integer> pebbles, Colour colour){
        this.name = name;
        this.pebbles = pebbles;
        this.colour = colour;
    }
}
