package Patterns;


interface Logistics {
    void send();
}
class RoadLogistics implements Logistics {
    @Override
    public void send() {
        System.out.println("Sending goods by road...");
    }
}
class SeaLogistics implements Logistics {
    @Override
    public void send() {
        System.out.println("Sending goods by sea...");
    }
}
class LogisticsFactory {

    // Factory method to create Logistics instances based on type 
    public static Logistics getLogistics(String type) 
    {
        if (type.equalsIgnoreCase("road")) 
        {
            return new RoadLogistics();
        } else if (type.equalsIgnoreCase("sea"))
        {
            return new SeaLogistics();
        }
        throw new IllegalArgumentException("Unknown logistics type");
    }
}


class LogisticsService {
    public static void main(String[] args) {

        /* Using the Logistics Factory to get the 
        desired object based on the mode */
        Logistics roadLogistics = LogisticsFactory.getLogistics("road");
        roadLogistics.send();

        Logistics seaLogistics = LogisticsFactory.getLogistics("sea");
        seaLogistics.send();
       
    }
}