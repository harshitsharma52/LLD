package Patterns;

interface Button {
    void render();
}
interface Checkbox {
    void render();
}


// light theme buttons and checkboxes

class LightButton implements Button {
    @Override
    public void render() {
        System.out.println("Rendering light button");
    }
}

class LightCheckbox implements Checkbox {
    @Override
    public void render() {
        System.out.println("Rendering light checkbox");
    }
}


// dark theme buttons and checkboxes
class DarkButton implements Button {
    @Override
    public void render() {
        System.out.println("Rendering dark button");
    }
}  

class DarkCheckbox implements Checkbox {
    @Override
    public void render() {
        System.out.println("Rendering dark checkbox");
    }
}

// abstarct factory interface
interface UIFactory {
    Button createButton();
    Checkbox createCheckbox();
}


// concrete factory for light theme
class LightThemeFactory implements UIFactory {
    @Override
    public Button createButton() {
        return new LightButton();
    }

    @Override
    public Checkbox createCheckbox() {
        return new LightCheckbox();
    }
}
// concrete factory for dark theme
class DarkThemeFactory implements UIFactory {
    @Override
    public Button createButton() {
        return new DarkButton();
    }

    @Override
    public Checkbox createCheckbox() {
        return new DarkCheckbox();
    }
}



public class AbstractFactory {

    public static void main(String[] args) {
        
        UIFactory factory = new LightThemeFactory();

        Button button = factory.createButton();
        Checkbox checkbox = factory.createCheckbox();

        button.render(); // Rendering light button
        checkbox.render(); // Rendering light checkbox

        factory = new DarkThemeFactory();

        button = factory.createButton();
        checkbox = factory.createCheckbox();

        button.render(); // Rendering dark button
        checkbox.render(); // Rendering dark checkbox
    }
    
}
