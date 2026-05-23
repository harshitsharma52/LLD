package Patterns;

// Subsystem 1
class TV {
    void turnOn() {
        System.out.println("TV is ON");
    }
}

// Subsystem 2
class SoundSystem {
    void turnOn() {
        System.out.println("Sound System is ON");
    }
}

// Subsystem 3
class Lights {
    void dim() {
        System.out.println("Lights are dimmed");
    }
}

// FACADE CLASS
// This class hides all complexity
class HomeTheaterFacade {

    // Creating subsystem objects
    private TV tv;
    private SoundSystem sound;
    private Lights lights;

    // Constructor
    public HomeTheaterFacade() {
        tv = new TV();
        sound = new SoundSystem();
        lights = new Lights();
    }

    // Simple method for user
    // Internally calls many systems
    public void watchMovie() {

        System.out.println("Starting Movie...");

        lights.dim();
        tv.turnOn();
        sound.turnOn();
    }
}

// Main Class
public class FacadePatternDemo {

    public static void main(String[] args) {

        // User only talks to facade
        HomeTheaterFacade homeTheater = new HomeTheaterFacade();

        // One simple method
        homeTheater.watchMovie();
    }
}