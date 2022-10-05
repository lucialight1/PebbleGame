package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class PebbleGame {

    // ATTRIBUTES
    private volatile Bag blackX;
    private volatile Bag blackY;
    private volatile Bag blackZ;
    private volatile Bag whiteA;
    private volatile Bag whiteB;
    private volatile Bag whiteC;

    private final int numberOfPlayers;

    private ArrayList<Player> players = new ArrayList<>();

    // METHODS

    public static void main(String[] args) {

        // get the number of players playing
        int numberOfPlayers = getNumberOfPlayers();

        // get all csv files for the 3 black bags
        ArrayList<File> listOfFiles = loadFiles(numberOfPlayers);

        // construct pebble game
        PebbleGame pebbleGame = new PebbleGame(numberOfPlayers, listOfFiles);

        // start the game by constructing the players and starting their threads
        System.out.println("Beginning Game");
        pebbleGame.beginGame(numberOfPlayers);
    }

    public static int getNumberOfPlayers(){
        int noOfPlayers = 0;
        boolean correctNumber = false;
        Console console = System.console();

        // repeat until a positive integer is typed
        while(!correctNumber) {
            System.out.println("Please enter the number of players or 'E' to exit: ");
            String userInput = console.readLine();

            // check user hasn't attempted to exit
            if(userInput.equals("E")){
                System.exit(0);
            }

            try {
                // check if the inputted value is a number
                noOfPlayers = Integer.parseInt(userInput);

                // check it's greater than 0
                if (!(noOfPlayers > 0)) {
                    System.out.println("Please enter only a positive integer");
                }
                else{
                    correctNumber = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter only a positive integer");
            }
        }

        return noOfPlayers;
    }

    public static ArrayList<File> loadFiles(int noOfPlayers){
        ArrayList<File> fileList = new ArrayList<>();
        boolean correctFileType;
        Console console = System.console();

        // for each black bag file repeat
        for(int i = 0; i < 3; i++){
            correctFileType = false;

            // repeat until file is correct format
            while(!correctFileType) {
                System.out.println("Please enter location of bag number " + (i + 1) + " to load: ");
                String fileInput = console.readLine();

                // check user hasn't attempted to exit
                if(fileInput.equals("E")){
                    System.exit(0);
                }

                else{
                    try{
                        // attempt to make input into a file object
                        File file = new File(fileInput);
                        Scanner fileScanner = new Scanner(file);
                        fileScanner.close();

                        // check file is a csv
                        if(fileInput.endsWith(".csv")){

                            // run check file, to check csv is the right size and has only positive integers in it
                            if(checkFile(file, noOfPlayers)) {
                                correctFileType = true;

                                // add correct file to list to return
                                fileList.add(file);
                            }
                            else{
                                System.out.println("Please enter a file that only contains enough integer values, separated by comments.");
                            }
                        }

                    }catch(NullPointerException | FileNotFoundException e){
                        System.out.println("Please enter a correct file name.");
                    }
                }

            }
        }
        return fileList;
    }

    public static boolean checkFile(File file, int noOfPlayers){
        boolean fileIsGood = true;
        ArrayList<Integer> fileContents = new ArrayList<>();
        Scanner fileReader = null;

        try {
            // open file
            fileReader = new Scanner(file);
            fileReader.useDelimiter(",");

            // check contents of file one by one
            while (fileReader.hasNext())
            {
                try{
                    // check it is an integer
                    int currentValue = Integer.parseInt(fileReader.next());

                    // check its greater than zero
                    if(currentValue > 0){
                        fileContents.add(currentValue);
                    }
                    else{
                        fileIsGood = false;
                    }

                }catch(NumberFormatException e){
                    fileIsGood = false;
                }
            }
            fileReader.close();

            // check number of pebbles isn't less than the specified number
            if(fileContents.size() < noOfPlayers*11){
                fileIsGood = false;
            }

        } catch (FileNotFoundException e) {
            fileIsGood = false;
        }


        return fileIsGood;
    }

    public static ArrayList<ArrayList<Integer>> loadBagContents(ArrayList<File> files){

        // create lists representing the pebbles in each bag
        ArrayList<Integer> bagX = new ArrayList<>();
        ArrayList<Integer> bagY = new ArrayList<>();
        ArrayList<Integer> bagZ = new ArrayList<>();

        // create a list of lists of bags
        ArrayList<ArrayList<Integer>> returnList = new ArrayList<>();

        returnList.add(bagX);
        returnList.add(bagY);
        returnList.add(bagZ);

        int counter = 0;

        // get data from each file
        for(File file : files){
            Scanner fileReader = null;

            try {
                // open file
                fileReader = new Scanner(file);
                fileReader.useDelimiter(",");

                // add contents of file one by one to relevant list
                while (fileReader.hasNext())
                {
                    returnList.get(counter).add(Integer.parseInt(fileReader.next()));
                }

                fileReader.close();

            } catch (FileNotFoundException ignored) {}

            counter++;
        }

        return returnList;
    }

    public void beginGame(int numberOfPlayers){
        // construct Player objects equal to number of players

        for(int i = 0; i < numberOfPlayers; i++){
            players.add(new Player(i));
        }

        // make sure all players have their initial hand before beginning game
        synchronized (this) {
            for (Player player : players) {
                player.setName("Player " + (player.playerNo + 1));
                player.initialMove();
            }
        }

        // start all player object threads
        for (Player player : players) {
            player.start();
        }
    }

    // CONSTRUCTOR

    public PebbleGame(int numberOfPlayers, ArrayList<File> listOfFiles){
        this.numberOfPlayers = numberOfPlayers;

        // extract data from all 3 csv files
        ArrayList<ArrayList<Integer>> allBagContents = loadBagContents(listOfFiles);

        // set up list for all white bags to be constructed with
        ArrayList<Integer> emptyList = new ArrayList<>();

        // instantiate all bags
        this.blackX = new Bag('X', allBagContents.get(0), Bag.Colour.BLACK);
        this.blackY = new Bag('Y', allBagContents.get(1), Bag.Colour.BLACK);
        this.blackZ = new Bag('Z', allBagContents.get(2), Bag.Colour.BLACK);
        this.whiteA = new Bag('A', emptyList, Bag.Colour.WHITE);
        this.whiteB = new Bag('B', emptyList, Bag.Colour.WHITE);
        this.whiteC = new Bag('C', emptyList, Bag.Colour.WHITE);

        // set each bag up with a paired bag of the opposite colour
        blackX.setPair(whiteA);
        blackY.setPair(whiteB);
        blackZ.setPair(whiteC);
        whiteA.setPair(blackX);
        whiteB.setPair(blackY);
        whiteC.setPair(blackZ);
    }

    // NESTED CLASSES

    class Player extends Thread{
        // ATTRIBUTES

        volatile ArrayList<Integer> playersPebbles = new ArrayList<>();
        int playerNo;
        volatile Bag bagLastDrawnFrom;

        // METHODS

        public void initialHand(){
            Bag bagToChooseFrom;
            Random rand = new Random();
            int random = rand.nextInt(3);

            // choose a random bag to get player's hand from
            if(random == 0){
                bagToChooseFrom = blackX;
            }
            else if(random == 1){
                bagToChooseFrom = blackY;
            }
            else{
                bagToChooseFrom = blackZ;
            }

            // pick out 10 pebbles from the chosen bag and add them to the player's hand
            synchronized (this) {
                for (int i = 0; i < 10; i++) {
                    playersPebbles.add(bagToChooseFrom.removePebbleFromBag());
                }
            }

            // remember the bag the player just drew from
            bagLastDrawnFrom = bagToChooseFrom;
        }

        public synchronized void writeMoveToTextFile(String outputText){
            // open file
            try {
                String outputFileName = "player" + (playerNo+1) + "_output.txt";
                FileWriter fileWriter = new FileWriter(outputFileName, true);

                // write output text as first line
                fileWriter.write(outputText+"\n");

                // write contents of pebbles list in second line
                fileWriter.write("Player " + (playerNo+1) + " hand is: " + playersPebbles + " TOTAL = "+ checkHandValue() +"\n");
                fileWriter.close();

            } catch (IOException e) {
                System.out.println("Unable to print to players " + (playerNo+1) + "'s text file");
            }
        }

        public synchronized int checkHandValue(){
            int count = 0;

            for(int pebble : playersPebbles){
                count = count + pebble;
            }
            return count;
        }

        public synchronized void winnerChecker(int handValue){
            if(handValue == 100){
                System.out.println("PLAYER " + (playerNo+1) + " HAS WON!!!");

                // stop all threads
                for(Player player : players){
                    try {
                        player.interrupt();
                    }catch (Exception e){

                    }
                    System.out.println(player.getName() + " thread has been interrupted and stopped");
                }

                // end the game
                System.out.println("GAME OVER");

                System.exit(0);
            }
        }

        public synchronized void discardPebble(){

            // choose a random pebble in hand
            Random rand = new Random();
            int random = rand.nextInt(playersPebbles.size());

            // remove pebble from hand
            int discardedPebble = playersPebbles.remove(random);

            // add pebble to paired white bag
            bagLastDrawnFrom.getPair().addPebbleToBag(discardedPebble);

            // write move to text file
            String outputText = "Player " + (playerNo+1) + " has discarded a " + discardedPebble + " to bag " + bagLastDrawnFrom.getPair().getName();
            writeMoveToTextFile(outputText);
        }

        public synchronized void drawPebble(){
            Bag bagToChooseFrom;
            Random rand = new Random();

            // choose random bag to draw from
            int random = rand.nextInt(3);

            if (random == 0) {
                bagToChooseFrom = blackX;
            } else if (random == 1) {
                bagToChooseFrom = blackY;
            } else {
                bagToChooseFrom = blackZ;
            }

            // get drawn value
            int drawnPebble = bagToChooseFrom.removePebbleFromBag();

            if(drawnPebble != -1) {

                // add drawn pebble to players hand
                playersPebbles.add(drawnPebble);

                // write move to text file
                String outputText = "Player " + (playerNo + 1) + " has drawn a " + drawnPebble + " from bag " + bagToChooseFrom.getName();
                writeMoveToTextFile(outputText);

                // remember bag player just drew from
                bagLastDrawnFrom = bagToChooseFrom;
            }

            // if the drawn value is -1, means an attempt to draw from an empty bag
            else{
                try {
                    // wait for bag to be refilled then attempt to draw again
                    wait();
                    drawPebble();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void initialMove(){
            initialHand();
            winnerChecker(checkHandValue());
        }

        public synchronized void playersMove(){
            discardPebble();
            drawPebble();
            winnerChecker(checkHandValue());
        }

        public void run(){

            // repeat a player making a move until someone wins
            for(;;){
                playersMove();
            }
        }

        // CONSTRUCTOR
        public Player(int playerNo){
            this.playerNo = playerNo;
        }
    }
}
