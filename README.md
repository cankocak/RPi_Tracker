# RPi_Tracker
Android RSS reader app with notification capabilities targeting rpilocator.com feeds

* Can be used as a generic RSS reader with notification template application
* Can also be used as a MVVE, RecyclerView example
* Background update is managed via Timer of the ViewModel, background worker would be a nicer solution

Motivation:
I needed a RPi4 and few CM4s. I found this nice website: https://rpilocator.com/ to track Raspberry Pi stock all over the world, but I realized that after 200+ RPi stock arrives, it becomes sold out less than 15 minutes. Therefore, I decided to write this application to get stock notifications for the products I am interested in. However, Don't expect too much, I only wrote this app in few hours and I am not a mobile application developer. It worked for me and I got my RPis, I am only sharing it to help other people.

Features:
* Notifications for new feeds
* Filter with keywords to get notifications for only certain feeds
* Adjustable background feed update periods
* Pull down to refresh the list on the GUI
