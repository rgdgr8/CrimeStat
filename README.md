CrimeStat records the details of office crimes, for improving office discipline. 

With CrimeStat, you can make a record of a crime including a title, a date, and a photo. You can also send a report through any messaging app. After documenting and reporting a crime, you can proceed with your work free of resentment and ready to focus on the business at hand.

The crime reports(excluding the photo) are stored in a sqlite database and the photos are stored in the app's private files.

Note:
1. Uses Firebase for auth
2. Firebase real time database is used for the storage of human readable data 
3. Image files are stored in Firebase cloud storage
4. User specific image files are also stored in persistent local storage using FileProvider, but other data is stored in firebase only
