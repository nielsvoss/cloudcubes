package osbourn.cloudcubes.infrastructure;

import software.amazon.awscdk.App;

public class CloudCubesApp {
    public static void main(String[] args) {
        App app = new App();

        new CloudCubesStack(app, "cloudcubes");

        app.synth();
    }
}
