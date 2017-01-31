/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PirateSim;

import java.util.Date;
import java.util.ArrayList;
import java.util.Random;

class Simulation { //What are you doing git?
    Vec2 size;
    Cell[][] cells;
    ArrayList<Ship> ships;
    private ArrayList<Ship> deadShips;
    static final int CARGO = 0, CAPTURED = 1, PATROL = 2, PIRATE = 3, ESCORTPIRATE = 4, WRECK = 5, ESCORTWRECK = 6;
    static final int NUM_SHIP_TYPES = 7;
    static final int INTERPOLATION_SAMPLES = 4;
    static final int BORDER_SIZE = 5;
    double probNewShip [];
    int timeStep;
    Random rand;
    int nextID;
    
    Simulation(int xSize, int ySize, double cProbNewCargo, double cProbNewPirate, double cProbNewPatrol, long seed) {
        size = new Vec2();
        size.x = xSize;
        size.y = ySize;
        cells = new Cell[size.x][size.y];
        for(int i = 0; i < cells.length; i++) {
            for(int j = 0; j < cells[i].length; j++) cells[i][j] = new Cell();
        }
        ships = new ArrayList();       
        deadShips = new ArrayList();
        probNewShip = new double[NUM_SHIP_TYPES];
        probNewShip[CARGO] = cProbNewCargo;
        probNewShip[PIRATE] = cProbNewPirate;
        probNewShip[PATROL] = cProbNewPatrol;
        rand = new Random(seed);
    }
    void tick() {
        for(int i = 0; i < cells.length; i++) {
            for(int j = 0; j < cells[i].length; j++)
                cells[i][j].ships.clear();
        }
        for (Ship ship : ships) ship.addPreviousState();
        for (Ship ship : ships) ship.move();
        for (int i = 0; i < NUM_SHIP_TYPES; i++) generateShip(i);
        deleteDeadShips();
        for (Ship ship : ships) ship.doDefeat();
        deleteDeadShips();
        for (Ship ship : ships) ship.doCapAndResc();
        deleteDeadShips();
        timeStep++;
    }
    void deleteDeadShips() {
        for (Ship ship : deadShips) {
            ships.remove(ship);
            if (inBounds(ship.position.x, ship.position.y)) cells[ship.position.x][ship.position.y].ships.remove(ship);
        }
        deadShips.clear();
    }
    Date getElapsedTime() {return new Date(0, 0, 0, 0, timeStep*5);}
    int random(int maxValue) {return Math.abs(rand.nextInt() % (maxValue + 1));}
    boolean test(double prob) {return Math.abs(rand.nextInt()) < prob*Integer.MAX_VALUE;}
    boolean inBounds(int x, int y) {return (x >= 0) && (x < size.x) && (y >= 0) && (y < size.y);}
    boolean inBorder(int x, int y) {return (x >= -BORDER_SIZE) && (x < size.x + BORDER_SIZE) && (y >= -BORDER_SIZE) && (y < size.y + BORDER_SIZE);}
    class Cell {
        ArrayList<Ship> ships;
        Cell() {ships = new ArrayList();}
    }
    class Ship {
        Vec2 position;
        Vec2 velocity;
        int type;
        int age;
        ArrayList<Ship> previousStates;
        
        Ship(int cType) {
            position = new Vec2();
            velocity = new Vec2();
            type = cType;
            previousStates = new ArrayList();
            switch (type) {
            case CARGO:
                velocity.x = 1;
                velocity.y = 0;
                break;
            case PIRATE:
                velocity.x = 0;
                velocity.y = 1;
                break;
            case PATROL:
                velocity.x = -2;
                velocity.y = 0;
                break;
            }
            if (velocity.x > 0) position.x = 0;
            else if (velocity.x < 0) position.x = size.x - 1;
            else position.x = random(size.x - 1);
            if (velocity.y > 0) position.y = 0;
            else if (velocity.y < 0) position.y = size.y - 1;
            else position.y = random(size.y - 1);
        }
        Ship(Vec2 pPosition, Vec2 pVelocity, int pType, int pAge) {
            position = new Vec2(pPosition.x, pPosition.y);
            velocity = new Vec2(pVelocity.x, pVelocity.y);
            type = pType;
            age = pAge;
        }
        void move() {
            if (type == WRECK || type == ESCORTWRECK) {
                if (age > 5) deadShips.add(this);
                age++;
                return;
            }
            position.plus(velocity);
            if (inBounds(position.x, position.y))
                cells[position.x][position.y].ships.add(this);
            else if (!inBorder(position.x, position.y)) {
                deadShips.add(this);
            }
        }
        void doDefeat() {
            if (type == PIRATE || type == ESCORTPIRATE) if (getNeighbor(PATROL) != null)
                if (type == PIRATE) type = WRECK;
                else type = ESCORTWRECK;
        }
        void doCapAndResc() {
            switch (type) {
            case CARGO:
                Ship pirate = getNeighbor(PIRATE);
                if (pirate != null) {
                    type = CAPTURED;
                    velocity.x = 0;
                    velocity.y = -1;
                    pirate.type = ESCORTPIRATE;
                    pirate.velocity.y = -1;
                    pirate.position.x = position.x;
                    pirate.position.y = position.y;
                }
                break;
            case CAPTURED:
                if (getNeighbor(PATROL) != null) {
                    type = CARGO;
                    velocity.x = 1;
                    velocity.y = 0;
                }
                break;
            }
        }
        Ship getNeighbor(int searchType) {
            ArrayList<Ship> neighbors = findNeighbors();
            for (Ship ship : neighbors) if (ship.type == searchType) return ship;
            return null;
        }
        ArrayList<Ship> findNeighbors() {
            ArrayList<Ship> neighbors = new ArrayList();
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    int x = this.position.x + i;
                    int y = this.position.y + j;
                    if (inBounds(x, y)) {
                        neighbors.addAll(cells[x][y].ships);
                    }
                }
            }
            neighbors.remove(this);
            return neighbors;
        }
        void addPreviousState() {previousStates.add(this.clone());}
        @Override
        protected Ship clone() {return new Ship(position, velocity, type, age);}
    }
    void generateShip(int type) {
        if (test(probNewShip[type])) {
            Ship ship = new Ship(type);
            ships.add(ship);
            for (int i = 0; i < INTERPOLATION_SAMPLES; i++) {
                Ship interpState = ship.clone();
                interpState.position.minus(new Vec2(interpState.velocity.x*(INTERPOLATION_SAMPLES - i), interpState.velocity.y*(INTERPOLATION_SAMPLES - i)));
                ship.previousStates.add(interpState);
            }
            cells[ship.position.x][ship.position.y].ships.add(ship);
        }
    }
    class Vec2 {
        int x, y;
        Vec2() {x = 0; y = 0;}
        Vec2(int cx, int cy) {x = cx; y = cy;}
        void plus(Vec2 operand) {x += operand.x; y += operand.y;}
        void minus(Vec2 operand) {x -= operand.x; y -= operand.y;}
        void assign(Vec2 operand) {x = operand.x; y = operand.y;}
        void mult(int scaler) {x *= scaler; y *= scaler;}
    }
    void textDisplay() {
        for(int j = cells[0].length - 1; j > -1; j--) {
            for(int i = 0; i < cells.length; i++) {
                if (!cells[i][j].ships.isEmpty()) {
                    Ship ship = cells[i][j].ships.get(0);
                    switch (ship.type) {
                    case CARGO:     System.out.print("OO"); break;
                    case PIRATE:    System.out.print("XX"); break;
                    case PATROL:    System.out.print("88"); break;
                    case CAPTURED:  System.out.print("LL"); break;
                    case WRECK:     System.out.print("##"); break;
                    }
                } else System.out.print("^^");
            }
            System.out.print("\n\t");
        }
        for(int i = 0; i < cells.length; i++) System.out.print("__");
        System.out.print("\n\t");
    }
}