package Patterns;

interface Coffee {
    int getCost();
    String getDescription();
}

// Base Coffee
class BasicCoffee implements Coffee {
    public int getCost() {
        return 20;
    }

    public String getDescription() {
        return "Basic Coffee";
    }
}

// Abstract Decorator
abstract class CoffeeDecorator implements Coffee {
    protected Coffee coffee;

    public CoffeeDecorator(Coffee coffee) {
        this.coffee = coffee;
    }
}

// Milk Add-on
class Milk extends CoffeeDecorator {

    public Milk(Coffee coffee) {
        super(coffee);
    }

    public int getCost() {
        return coffee.getCost() + 5;
    }

    public String getDescription() {
        return coffee.getDescription() + " + Milk";
    }
}

// Sugar Add-on
class Sugar extends CoffeeDecorator {

    public Sugar(Coffee coffee) {
        super(coffee);
    }

    public int getCost() {
        return coffee.getCost() + 3;
    }

    public String getDescription() {
        return coffee.getDescription() + " + Sugar";
    }
}

// Whipped Cream Add-on
class WhippedCream extends CoffeeDecorator {

    public WhippedCream(Coffee coffee) {
        super(coffee);
    }

    public int getCost() {
        return coffee.getCost() + 7;
    }

    public String getDescription() {
        return coffee.getDescription() + " + Whipped Cream";
    }
}
class DecoratorCoffee {
    public static void main(String[] args) {

        
        Coffee coffee = new BasicCoffee();

        // Add Milk
        coffee = new Milk(coffee);

        // Add Sugar
        coffee = new Sugar(coffee);

        // Add Whipped Cream
        coffee = new WhippedCream(coffee);

        System.out.println("Cost: " + coffee.getCost());
        System.out.println("Description: " + coffee.getDescription());

     
    }
}