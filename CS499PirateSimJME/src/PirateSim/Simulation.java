package PirateSim;

import java.util.Date;
import java.util.ArrayList;
import java.util.Random;

/** * Runs a grid based simulation as described in the CS499 Somali Pirate Simulation document.
 * Grid cells are nominally 1 square mile and each tick represents five minutes of real time. */
class Simulation {
    /** * The size of the simulation area */
    Vec2 size;
    /** * An array of grid cells for efficiently finding ships based on location */
    Cell[][] cells;
    /** * The list of currently active ships */
    ArrayList<Ship> ships;
    /** * List of ships that were deleted while iterating over ships */
    private ArrayList<Ship> deadShips;
    /** * Constants to reference each type of ship. */
    static final int CARGO = 0, CAPTURED = 1, PATROL = 2, PIRATE = 3, ESCORTPIRATE = 4, WRECK = 5, ESCORTWRECK = 6;
    static final int NUM_SHIP_TYPES = 7;
    /** * The number of position samples that an interpolation function will need to interpolate ship positions.
     * 4 samples for Catmull-Rom interpolation. */
    static final int INTERPOLATION_SAMPLES = 4;
    /** * The width of the border around the simulation area where ships will be displayed but not interact with eachother. */
    static final int BORDER_SIZE = 5;
    /** * An array of the probability of a given type of ship being generated. */
    double probNewShip [];
    /** * The current timestep. */
    int timeStep;
    private Random rand;
    private int nextID = 0;
    
    /** * Constructs a simulation object.
     * @param xSize the East-West extent of the simulation area in cells
     * @param ySize the North-South extent of the simulation area in cells
     * @param cProbNewCargo the probability of a cargo vessel being generated on a given tick
     * @param cProbNewPirate the probability of a pirate vessel being generated on a given tick
     * @param cProbNewPatrol the probability of a patrol vessel being generated on a given tick
     * @param seed the seed for the random number generator */
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
    /** * Updates the simulation, the time increment is nominally five minutes. */
    void tick() {
        for(int i = 0; i < cells.length; i++) {
            for(int j = 0; j < cells[i].length; j++)
                cells[i][j].ships.clear();
        }
        for (Ship ship : ships) ship.addPreviousState();
        for (Ship ship : ships) ship.move();
        for (Ship ship : deadShips) { //ships that exited the border or sunk completely when ship.move() was called are now actually deleted
            ships.remove(ship);
            if (inBounds(ship.position.x, ship.position.y)) cells[ship.position.x][ship.position.y].ships.remove(ship);
        }
        deadShips.clear();
        for (int i = 0; i < NUM_SHIP_TYPES; i++) generateShip(i);
        for (Ship ship : ships) ship.doDefeat();
        for (Ship ship : ships) ship.doCapAndResc();
        timeStep++;
    }
    /** * Gets the number of days that have elapsed since the simulation started.
     * @return the elapsed time */
    float getElapsedDays(float alpha) {return 5/(60*24)*(timeStep + alpha);}

    /** * Represents a ship in the simulation. */
    class Ship {
        /** * The ship's position relative to the South Western corner of the grid. */
        Vec2 position;
        /** * The ship's velocity. */
        Vec2 velocity;
        /** * The type of ship, one of the constants defined in #Simulation. */
        int type;
        /** * The number of ticks that have elapsed since the ship last moved. */
        int ticksSinceLastMove;
        /** * A unique identifier for the ship. */
        int ID;
        /** * A list of this Ship's state on previous ticks. */
        ArrayList<Ship> previousStates;
        
        /** * Create a ship object, the velocity and starting position are determined from the type.
         * @param cType the type of ship, one of the constants defined in #Simulation */
        Ship(int cType) {
            position = new Vec2();
            velocity = new Vec2();
            type = cType;
            previousStates = new ArrayList();
            ID = getNewID();
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
        /** * Create a ship object and specify its state.
         * @param pPosition
         * @param pVelocity
         * @param pType
         * @param pTicksSinceLastMove
         * @param pID */
        Ship(Vec2 pPosition, Vec2 pVelocity, int pType, int pTicksSinceLastMove, int pID) {
            position = new Vec2(pPosition.x, pPosition.y);
            velocity = new Vec2(pVelocity.x, pVelocity.y);
            type = pType;
            ticksSinceLastMove = pTicksSinceLastMove;
            ID = pID;
        }
        /** * Update the ship's position according to the rules of the game. */
        void move() {
            if (type == WRECK || type == ESCORTWRECK) {
                if (ticksSinceLastMove > 5) deadShips.add(this);
                ticksSinceLastMove++;
                return;
            }
            position.plus(velocity);
            if (inBounds(position.x, position.y))
                cells[position.x][position.y].ships.add(this);
            else if (!inBorder(position.x, position.y)) {
                deadShips.add(this);
            }
        }
        /** * See if the ship should be defeated. */
        void doDefeat() {
            if (type == PIRATE || type == ESCORTPIRATE) if (getNeighbor(PATROL) != null)
                if (type == PIRATE) type = WRECK;
                else type = ESCORTWRECK;
        }
        /** * See if the ship should be captured or rescued. */
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
        /** * Find ships of a given type in adjacent cells.
         * @param searchType
         * @return all ships of searchType in adjacent cells */
        private Ship getNeighbor(int searchType) {
            ArrayList<Ship> neighbors = findNeighbors();
            for (Ship ship : neighbors) if (ship.type == searchType) return ship;
            return null;
        }
        /** * Find all ships in adjacent cells.
         * @return all adjacent ships */
        private ArrayList<Ship> findNeighbors() {
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
        /** * Add the current state to the previousStates ArrayList. */
        private void addPreviousState() {previousStates.add(this.clone());}
        /** * Clone the ship object, the data in previousStates is lost.
         * @return the cloned ship */
        @Override
        protected Ship clone() {return new Ship(position, velocity, type, ticksSinceLastMove, ID);}
    }
    /** * Generate a new ship with the correct probabilty for the type.
     * @param type the type of ship to attempt to generate */
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
    /** * Get the ship's unique ID
     * @return the unique ID */
    int getNewID() {return nextID++;}
    /** * Generate a random number from 0 to maxValue
     * @param maxValue
     * @return the random number */
    private int random(int maxValue) {return Math.abs(rand.nextInt() % (maxValue + 1));}
    /** * return true with probability prob
     * @param prob the probabilty of success
     * @return whether the test was successful or not */
    private boolean test(double prob) {return Math.abs(rand.nextInt()) < prob*Integer.MAX_VALUE;}
    /** * Test to see if the position is within the simulation grid
     * @param x
     * @param y
     * @return whether the position is within the simulation grid or not */
    boolean inBounds(int x, int y) {return (x >= 0) && (x < size.x) && (y >= 0) && (y < size.y);}
    /** * Test to see if the position is within the border around the simulation grid
     * @param x
     * @param y
     * @return whether the position is within the border around the simulation grid or not */
    boolean inBorder(int x, int y) {return (x >= -BORDER_SIZE) && (x < size.x + BORDER_SIZE) && (y >= -BORDER_SIZE) && (y < size.y + BORDER_SIZE);}
    /** * Utility class to store all the ships present in a given cell of the simulation grid. */
    class Cell {
        ArrayList<Ship> ships;
        Cell() {ships = new ArrayList();}
    }
    /** * Helper class to store 2 vectors. */
    class Vec2 {
        int x, y;
        Vec2() {x = 0; y = 0;}
        Vec2(int cx, int cy) {x = cx; y = cy;}
        void plus(Vec2 operand) {x += operand.x; y += operand.y;}
        void minus(Vec2 operand) {x -= operand.x; y -= operand.y;}
        void assign(Vec2 operand) {x = operand.x; y = operand.y;}
        void mult(int scaler) {x *= scaler; y *= scaler;}
        public boolean equals(Vec2 operand) {return x == operand.x && y == operand.y;}
    }
    /** * Print out the current simulation state to the terminal. */
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