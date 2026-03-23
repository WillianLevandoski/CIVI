package com.civi.globe.app;

public final class AppLauncher {

    private AppLauncher() {
    }

    public static void main(String[] args) {
        GlobeApp.launch(GlobeApp.class, args);
    }
}
