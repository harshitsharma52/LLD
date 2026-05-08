import java.util.*;

/////////////////////////////////////////////////////////

enum Direction {
    UP,
    DOWN,
    IDLE
}

/////////////////////////////////////////////////////////

class ExternalRequest {

    int floor;
    Direction direction;

    ExternalRequest(int floor, Direction direction) {
        this.floor = floor;
        this.direction = direction;
    }
}

/////////////////////////////////////////////////////////

interface ElevatorSelectionStrategy {

    Elevator selectElevator(
        List<Elevator> elevators,
        ExternalRequest request
    );
}

/////////////////////////////////////////////////////////

class NearestElevatorStrategy
    implements ElevatorSelectionStrategy {

    @Override
    public Elevator selectElevator(
        List<Elevator> elevators,
        ExternalRequest request
    ) {

        Elevator best = null;

        int minDistance = Integer.MAX_VALUE;

        for (Elevator e : elevators) {

            int distance =
                Math.abs(e.currentFloor - request.floor);

            if (distance < minDistance) {

                minDistance = distance;

                best = e;
            }
        }

        return best;
    }
}

/////////////////////////////////////////////////////////

class Elevator implements Runnable {

    int id;

    int currentFloor;

    Direction direction;

    PriorityQueue<Integer> upStops;

    PriorityQueue<Integer> downStops;

    Elevator(int id) {

        this.id = id;

        this.currentFloor = 1;

        this.direction = Direction.IDLE;

        upStops = new PriorityQueue<>();

        downStops =
            new PriorityQueue<>((a, b) -> b - a);
    }

    /////////////////////////////////////////////////////

    // THREAD SAFE
    public synchronized void addRequest(int floor) {

        System.out.println(
            Thread.currentThread().getName()
                + " adding request "
                + floor
                + " to Elevator "
                + id
        );

        if (floor > currentFloor) {
            upStops.add(floor);
        } else {
            downStops.add(floor);
        }
    }

    /////////////////////////////////////////////////////

    private void moveUp() {

        currentFloor++;

        System.out.println(
            "Elevator "
                + id
                + " moving UP -> "
                + currentFloor
        );
    }

    /////////////////////////////////////////////////////

    private void moveDown() {

        currentFloor--;

        System.out.println(
            "Elevator "
                + id
                + " moving DOWN -> "
                + currentFloor
        );
    }

    /////////////////////////////////////////////////////

    @Override
    public void run() {

        while (true) {

            //////////////////////////////////////////////////
            // PROCESS UP REQUESTS
            //////////////////////////////////////////////////

            while (!upStops.isEmpty()) {

                direction = Direction.UP;

                if (upStops.peek() == currentFloor) {

                    System.out.println(
                        "Elevator "
                            + id
                            + " STOPPED at "
                            + currentFloor
                    );

                    upStops.poll();

                } else {

                    moveUp();
                }

                sleep();
            }

            //////////////////////////////////////////////////
            // PROCESS DOWN REQUESTS
            //////////////////////////////////////////////////

            while (!downStops.isEmpty()) {

                direction = Direction.DOWN;

                if (downStops.peek() == currentFloor) {

                    System.out.println(
                        "Elevator "
                            + id
                            + " STOPPED at "
                            + currentFloor
                    );

                    downStops.poll();

                } else {

                    moveDown();
                }

                sleep();
            }

            //////////////////////////////////////////////////

            direction = Direction.IDLE;

            sleep();
        }
    }

    /////////////////////////////////////////////////////

    private void sleep() {

        try {

            Thread.sleep(1000);

        } catch (Exception e) {
        }
    }
}

/////////////////////////////////////////////////////////

class ElevatorSystem {

    List<Elevator> elevators;

    ElevatorSelectionStrategy strategy;

    ElevatorSystem(
        int numberOfElevators,
        ElevatorSelectionStrategy strategy
    ) {

        this.strategy = strategy;

        elevators = new ArrayList<>();

        for (int i = 1; i <= numberOfElevators; i++) {

            Elevator elevator = new Elevator(i);

            elevators.add(elevator);

            Thread t = new Thread(
                elevator,
                "Elevator-Thread-" + i
            );

            t.start();
        }
    }

    /////////////////////////////////////////////////////

    public Elevator handleExternalRequest(
        ExternalRequest request
    ) {

        Elevator elevator =
            strategy.selectElevator(
                elevators,
                request
            );

        System.out.println(
            "\nSystem assigned Elevator "
                + elevator.id
                + " for floor "
                + request.floor
        );

        elevator.addRequest(request.floor);

        return elevator;
    }


     public static void main(String[] args)
        throws Exception 
        {

        ElevatorSystem system =
            new ElevatorSystem(
                2,
                new NearestElevatorStrategy()
            );

        //////////////////////////////////////////////////
        // REQUEST 1
        //////////////////////////////////////////////////

        Elevator e1 =
            system.handleExternalRequest(
                new ExternalRequest(
                    3,
                    Direction.UP
                )
            );

        //////////////////////////////////////////////////
        // REQUEST 2
        //////////////////////////////////////////////////

        Elevator e2 =
            system.handleExternalRequest(
                new ExternalRequest(
                    6,
                    Direction.DOWN
                )
            );

        //////////////////////////////////////////////////
        // REQUEST 3
        //////////////////////////////////////////////////

        Elevator e3 =
            system.handleExternalRequest(
                new ExternalRequest(
                    2,
                    Direction.UP
                )
            );

        //////////////////////////////////////////////////
        // INSIDE ELEVATOR REQUESTS
        //////////////////////////////////////////////////

        Thread.sleep(4000);

        System.out.println(
            "\nInside Elevator "
                + e1.id
                + " user pressed 8"
        );

        e1.addRequest(8);

        Thread.sleep(2000);

        System.out.println(
            "\nInside Elevator "
                + e2.id
                + " user pressed 1"
        );

        e2.addRequest(1);
    }
}

// “Each elevator runs independently in its own thread. Meanwhile requests are added by external threads like API/user threads. Since both threads access the same request queues, 
// synchronization is required to avoid concurrent modification issues.”



// This SAME elevator object is accessed by:

// Main thread	addRequest()
// Elevator thread	run()

// NOW BOTH THREADS TOUCH SAME QUEUE

// with synchronized only ONE thread modifies safely.

// Main Thread:
//     add(7)

// Elevator Thread:
//     poll()

// BOTH modify queue together

// ❌ corruption possible
// // /////////////////////////////////////////////////////////


