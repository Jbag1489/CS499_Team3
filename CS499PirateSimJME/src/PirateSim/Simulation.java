/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pirateSim;

import java.util.Date;
import java.util.ArrayList;
import java.util.Random;

class Simulation {
    Vec2 size;
    Cell[][] cells;
    ArrayList<Ship> ships;
    ArrayList<Ship> deadShips;
    static final int CARGO = 0, PIRATE = 1, PATROL = 2, CAPTURED = 3, WRECK = 4;
    double probNewShip [];
    int timestep;
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
        probNewShip = new double[3];
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
        for (Ship ship : ships) ship.move();
        for (int i = 0; i < 3; i++) generateShip(i);
        deleteDeadShips();
        for (Ship ship : ships) ship.doDefeat();
        deleteDeadShips();
        for (Ship ship : ships) ship.doCapAndResc();
        deleteDeadShips();
    }
    void deleteDeadShips() {
        for (Ship ship : deadShips) {
            ships.remove(ship);
            if (inBounds(ship.position.x, ship.position.y)) cells[ship.position.x][ship.position.y].ships.remove(ship);
        }
        deadShips.clear();
    }
    Date getElapsedTime() {return new Date(0, 0, 0, 0, timestep*5);}
    int random(int maxValue) {return Math.abs(rand.nextInt() % (maxValue + 1));}
    boolean test(double prob) {return Math.abs(rand.nextInt()) < prob*Integer.MAX_VALUE;}
    boolean inBounds(int x, int y) {return (x >= 0) && (x < size.x) && (y >= 0) && (y < size.y);}
    int getID() {return nextID++;}
    class Cell {
        ArrayList<Ship> ships;
        Cell() {ships = new ArrayList();}
    }
    
    class Ship {
        Vec2 position;
        Vec2 velocity;
        int type;
        int age;
        int ID;
        
        Ship(int cType) {
            position = new Vec2();
            velocity = new Vec2();
            type = cType;
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
            ID = getID();
        }
        void move() {
            if (type == WRECK) {
                if (age > 5) deadShips.add(this);
                age++;
                cells[position.x][position.y].ships.add(this);
                return;
            }
            position.plus(velocity);
            if (inBounds(position.x, position.y))
                cells[position.x][position.y].ships.add(this);
            else deadShips.add(this);
        }
        void doDefeat() {
            if (type == PIRATE) if (getNeighbor(PATROL) != null)
                type = WRECK;
        }
        void doCapAndResc() {
            switch (type) {
            case CARGO:
                Ship pirate = getNeighbor(PIRATE);
                if (pirate != null) {
                    type = CAPTURED;
                    velocity.x = 0;
                    velocity.y = -1;
                    deadShips.add(pirate);
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
    }
    void generateShip(int type) {
        if (test(probNewShip[type])) {
            Ship ship = new Ship(type);
            ships.add(ship);
            cells[ship.position.x][ship.position.y].ships.add(ship);
        }
    }
    class Vec2 {
        int x, y;
        Vec2() {x = 0; y = 0;}
        Vec2(int cx, int cy) {x = cx; y = cy;}
        void plus(Vec2 operand) {x += operand.x; y += operand.y;}
        void assign(Vec2 operand) {x = operand.x; y = operand.y;}
    }
    
    void textDisplay() {
        for(int j = cells[0].length - 1; j > -1; j--) {
            for(int i = 0; i < cells.length; i++) {
                if (!cells[i][j].ships.isEmpty()) {
                    Ship ship = cells[i][j].ships.get(0);
                    switch (ship.type) {
                    case CARGO:
                        System.out.print("OO");
                        break;
                    case PIRATE:
                        System.out.print("XX");
                        break;
                    case PATROL:
                        System.out.print("88");
                        break;
                    case CAPTURED:
                        System.out.print("LL");
                        break;
                    case WRECK:
                        System.out.print("##");
                        break;
                    }
                } else System.out.print("^^");
            }
            System.out.print("\n\t");
        }
        for(int i = 0; i < cells.length; i++) System.out.print("__");
        System.out.print("\n\t");
    }
}