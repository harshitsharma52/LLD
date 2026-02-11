
import java.util.ArrayList;
import java.util.List;

// Observer Interface
// ==============================
interface Subscriber {
    void update(String videoTitle);
}


// ==============================
// Subject Interface
// ==============================
interface Channel {
    void subscribe(Subscriber subscriber);
    void unsubscribe(Subscriber subscriber);
    void notifySubscribers(String videoTitle);
}

class EmailSubscriber implements Subscriber {
    private String email;

    public EmailSubscriber(String email) {
        this.email = email;
    }

    @Override
    public void update(String videoTitle) {
        System.out.println("Email sent to " + email + ": New video uploaded - " + videoTitle);
    }
}


class MobileAppSubscriber implements Subscriber {
    private String username;

    public MobileAppSubscriber(String username) {
        this.username = username;
    }

    @Override
    public void update(String videoTitle) {
        System.out.println("In-app notification for " + username + ": New video - " + videoTitle);
    }
}

class YoutubeChannel implements Channel 
{
    private List<Subscriber> subscribers;
    private String channelName;

    public YoutubeChannel(String channelName) {
        this.channelName = channelName;
        this.subscribers = new ArrayList<>();
    }

    @Override
    public void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void unsubscribe(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void notifySubscribers(String videoTitle) {
        for (Subscriber subscriber : subscribers) {
            subscriber.update(videoTitle);
        }
    }

    public void uploadVideo(String videoTitle) 
    {
        System.out.println("Channel " + channelName + " uploaded a new video: " + videoTitle);
        notifySubscribers(videoTitle);
    }
}

public class Observer {
    public static void main(String[] args) 
    
    {
        YoutubeChannel channel = new YoutubeChannel("Tech Tutorials");
        Subscriber emailSubscriber = new EmailSubscriber("user@example.com");
        Subscriber mobileSubscriber = new MobileAppSubscriber("john_doe");
        
        channel.subscribe(emailSubscriber);
        channel.subscribe(mobileSubscriber);
        
        channel.uploadVideo("Java Design Patterns Explained");
    }
}
