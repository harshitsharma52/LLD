// 1️⃣ State Interface
interface FanState {
    void pressButton(Fan fan);
}



class OffState implements FanState {
    public void pressButton(Fan fan) {
        System.out.println("Fan is turning ON");
        fan.setState(new OnState());  // change state
    }
}


class OnState implements FanState {
    public void pressButton(Fan fan) {
        System.out.println("Fan is turning OFF");
        fan.setState(new OffState());  // change state
    }
}

// 2️⃣ Context Class
class Fan {
    private FanState state;

    public Fan() {
        state = new OffState(); // default state
    }

    public void setState(FanState state) {
        this.state = state;
    }

    public void pressButton() {
        state.pressButton(this);
    }
}



// 5️⃣ Main Class
public class State {
    public static void main(String[] args) {

        Fan fan = new Fan(); // by defualt OFF state

        fan.pressButton(); // OFF → ON
        fan.pressButton(); // ON → OFF
        fan.pressButton(); // OFF → ON
    }
}
