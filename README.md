# QPA

This project requires a `google-services.json` file for Firebase and Google services configuration.

## Getting Started

1. Obtain your own Firebase project configuration from the [Firebase console](https://console.firebase.google.com/).
2. Download the `google-services.json` file and place it in the `app/` directory of this project.
3. The file is ignored by Git, so you must provide it yourself when setting up the project.


The project also needs a Google Maps API key. Configure it by creating a `local.properties` file in the project root with the line `MAPS_API_KEY=YOUR_KEY`. The key is read at build time by the secrets Gradle plugin and injected into `AndroidManifest.xml`.

An example `secrets.defaults.properties` is included with a placeholder value. Replace it with your real key (or provide a `local.properties` file) before building the app.
