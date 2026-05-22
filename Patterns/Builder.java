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
