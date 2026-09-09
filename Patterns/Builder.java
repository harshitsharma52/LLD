package Patterns;


class User {

    private String name;
    private int age;
    private String city;

    // Private constructor
    private User(Builder builder) {
        this.name = builder.name;
        this.age = builder.age;
        this.city = builder.city;
    }

    // Builder class
    static class Builder {

        private String name;
        private int age;
        private String city;

        public Builder setName(String name) {
            this.name = name;
//             Then why return this?

// Because it enables method chaining.
            return this;
        }

        public Builder setAge(int age) {
            this.age = age;
            return this;
        }

        public Builder setCity(String city) {
            this.city = city;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    public void show() {
        System.out.println(name + " " + age + " " + city);
    }
}

public class Builder {

     public static void main(String[] args) {

        User user = new User.Builder()
                .setName("Harshit")
                .setAge(22)
                .setCity("Noida")
                .build();

        user.show();
    }
    
}


// why static inner builder class bcz non static innner class always need object of outer classs but whole purpose of builder class to create object of product class but now you first need product object
// aslo if builder is outside the class then constructor must be public but we want to make it private so that no one can create object of product class without builder class


// Why is Builder a static inner class?

// then Java requires a User object before you can create Builder:

// Why put Builder inside User?

// "This Builder is specifically used to construct a User."

// And there's another important reason in your code.

// Your User constructor is:

// private User(Builder builder) {
//     this.name = builder.name;
//     this.age = builder.age;
//     this.city = builder.city;
// }

// Because Builder is inside User, it has access to the private constructor.