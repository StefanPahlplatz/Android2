# Table of Contents
1. [Requirements](#requirements)
2. [Subtasks](#subtasks)
3. [Extensions](#extensions)
4. [Documentation](#documentation)
5. [Planning](#planning)


## Requirements

> Create an app that can detect whether certain persons are physically in your neighborhood.
  In general terms, the minimal required functionalities are:
  
- [x] Show your own location with a marker on a map
- [x] Show also the location of the other users of this app with markers on the map, including their
      names (preferably use different colors, one for your own marker and another one for other
      users).
- [x] Give an audible or vibration signal when one of these users comes within 10 meters to your
      location; also show the name of that user in a notification.

## Subtasks

> In order to achieve this, you will have to do the following subtasks:

- [x] Set up GIT account; invite teacher as a Master; commit frequently.
- [x]  Define the screenflow, and design an appropriate GUI using the Material Design Guidelines.
The app must run on a phone and on a tablet (using the available screen size optimally).
- [x] Research how to get your current location from the device.
- [x] Research how a map can be shown (using Google Maps, or other app that can show a map).
An implicit Intent has to be used for this.
- [x] Research what kind of server you will use (DB server, WCF server, webserver, GCM, or other).
- [x] Communication with the server must be done asynchronously.
- [x] Research how the distance between two points can be calculated.
- [x] Research how to play a sound, or generate a vibration.
- [x] Test the app on some tablets and phones, using at least 3 different Android versions.
- [ ] Deploy the app

## Extensions

> Depending on your time left, and the capabilities within the group, you could consider implementing
  the following:

- [ ] The ability to filter what type of people you are interested in, using for example: age, gender,
profession, etc. This automatically means that users have to enter this information into a
profile.
- [x] The ability to see a picture of the persons near to you.
- [ ] An arrow on the screen that shows the direction where a certain person is (using the
compass sensor).
- [ ] A chat functionality, that enables you to send messages to other users.

## Documentation

> Documentation has to be provided, that contains the following information:

- [ ] Show and explain the GUI.
- [ ] Explain which kind of server you used, and why.
- [ ] When one user is added (or changes position), how do you update the other users?
- [ ] Show and explain the Database structure (how data is stored and retrieved).
- [ ] How is the Notification done?
- [ ] Any interesting implementation details, e.g.: how do you calculate the distance between you
and other users?
- [ ] What kind of testing frameworks have you used and tests run?
- [ ] How did you deploy your app?
- [ ] Explain clearly all the features that you added, and that were not strictly required; and which
mark you think you deserve for the end product.

## Planning

| Week | Steps to do                                |                         Subresult to be shown                         |
|------|--------------------------------------------|-----------------------------------------------------------------------|
| 1    | Define screenflow Design GUI in detail     | -                                                                     |
| 2    | Get own location, and show on map          | Show screen, and screen-flow prototype; no functionality required yet |
| 3    | Setup server, and client-server connection | Show own location on map                                              |
| 4    | Handle server communication asynchronously | -                                                                     |
| 5    | Calculate distance from other users        | Show location of others on map                                        |
| 6    | Generate sound at close proximity          | Show distance to others on map                                        |
| 7    | Test and deploy the app                    | -                                                                     |
| 8    | Hand in project result                     | Demo complete app                                                     |