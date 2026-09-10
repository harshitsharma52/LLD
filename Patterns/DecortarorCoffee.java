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

// Abstract Decorator -> We do not want users to create a plain decorator object directly because decorator itself is incomplete and meaningless.
// CoffeeDecorator only provides common logic shared by all decorators:


// But an abstract class can say:

// "I'm not a complete implementation yet. My child classes will finish the remaining methods."
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


// You didn't modify BasicCoffee.

// You didn't create a MilkSugarCoffee subclass.

// You simply composed objects dynamically.

// Coffee coffee = new BasicCoffee();

// if (userSelectedMilk) {
//     coffee = new Milk(coffee);
// }

// if (userSelectedSugar) {
//     coffee = new Sugar(coffee);
// }

// if (userSelectedWhippedCream) {
//     coffee = new WhippedCream(coffee);
// }


// And because the wrapper also implements Coffee, you can keep wrapping it indefinitely.



// Each decorator object holds a reference to another Coffee object, which can itself be another decorator.

// In your code:

// protected Coffee coffee;

// That reference creates the chain.

// coffee
//   ↓
// WhippedCream
//       |
//       ↓
//     Sugar
//       |
//       ↓
//      Milk
//       |
//       ↓
//  BasicCoffee


//  coffee.getCost();


//  WhippedCream.getCost()

// executes:

// return coffee.getCost() + 7;

// But its coffee points to Sugar, so:

// WhippedCream.getCost()
//         ↓
// Sugar.getCost()
//         ↓
// Milk.getCost()
//         ↓
// BasicCoffee.getCost()
//         ↓
// 20



// interface
//    ↓
// Declares methods

//        ↓

// abstract class implements interface
//    ↓
// Can leave methods unimplemented
//    ↓
// Child class must implement them

//        ↓

// normal/concrete class implements interface
//    ↓
// MUST implement all abstract interface methods



// So in your Decorator Pattern:

// abstract class CoffeeDecorator implements Coffee

// is useful because CoffeeDecorator is just a common base/wrapper. It provides:

// protected Coffee coffee;

// and:

// CoffeeDecorator(Coffee coffee)

// but lets each actual decorator (Milk, Sugar, WhippedCream) decide how cost and description should change.

// That's exactly why making CoffeeDecorator abstract makes sense.|



// .QUESTION  Why does CoffeeDecorator implement Coffee?

// it says:

// A CoffeeDecorator is also a Coffee.

// This is important because a decorator wraps another Coffee.

// For example:

// Coffee coffee = new BasicCoffee();

// coffee = new Milk(coffee);

// Why not just do this?

// You might ask:

// abstract class CoffeeDecorator {
//     protected Coffee coffee;
// }

// Then:

// class Milk extends CoffeeDecorator implements Coffee {
//     ...
// }

// Technically, this would work.

// But then every decorator would have to separately write:

// Instead, we put it once:

// abstract class CoffeeDecorator implements Coffee

// Then all subclasses automatically become Coffee:

//              Coffee(Interface )
//                 ↑
//                 |
//        CoffeeDecorator(abstarct class implemnts interface ans It's just a common parent that stores: protected Coffee coffee; )
//           ↑     ↑     ↑
//           |     |     |
//         Milk  Sugar  WhippedCream





// This is the key difference
// Inheritance approach ❌

// You create a new class for every combination:

// Email
//  ↓
// EncryptedEmail
//  ↓
// EncryptedLoggedEmail
//  ↓
// EncryptedLoggedRetryEmail
// Decorator approach ✅

// You create independent features:

// Email
// LoggingDecorator
// EncryptionDecorator
// RetryDecorator

// And combine them:

// Retry
//   ↓
// Encryption
//   ↓
// Logging
//   ↓
// Email