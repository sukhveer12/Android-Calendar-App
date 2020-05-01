# School Calendar App

## Description
The School Calendar App allows users to see all upcoming activities in the school. It retrieves this data from an external server (provided by the teacher), and displays it to the user in a list format. The app allows the user to indicate their participation in each activity, share the activity information with others (via SMS), and even modify the activity including its name and meeting times (provided that they are authorized to do so). 

This app was developed for the Android platform, and the code was written in Java.

## Installation
The app can be tested using the [Android Emulator](https://developer.android.com/studio/run/emulator). Note that while any virtual device can be used, the app was developed and tested using a Nexus 5 virtual device running Android 5.1. Simply download the project and launch it using Android Studio.

## Usage
***Note:** The server that the app relies on is currently unavailable, which limits certain functionalities of the app. However, the app allows for a demo mode that shows the key features of the app. There are also a few on-screen popup messages that alert the user when they try to do something that the demo mode does not support.*

* When the app is first launched, the user must enter their credentials in order to access the service. 
  * Default credentials:
  * Username: opso
  * Password: none

Once authenticated, the app will fetch a list of activities from the server and display them to the user. The user can click on any one of the activities to view more details, edit the activities (if they are authorized), and indicate whether they will be attending. The user can also filter the list of activities using parameters such as the name and date. Finally, they can share details of an event with someone else via SMS using the share button beside each activity. 
