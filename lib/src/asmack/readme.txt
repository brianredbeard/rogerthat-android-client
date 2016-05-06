Project
  http://code.google.com/p/asmack/

Steps to get src code (NOT NEEDED TO DO THIS EVERY TIME)
* Download src http://asmack.googlecode.com/files/asmack-2010.05.07-source.zip
* Strip out unnecessary packages and fix compilation errors
* This code is in this git repository

Steps to build
* Java (not Android) Eclipse project which is based in this 'asmack folder' and uses src code in this folder - compiler level 1.6
* Add external jar android.jar in project (use correct api level - here I used android-8)
* Eclipse Menu - File | Export | Java-Jar
** Only src folder should be exported
** Check "Export generated class files and resources"
** Check "Compress the contents of the JAR file"
** uncheck all other boxes (e.g. no src code, ...)
* Check that the result is about 1 MB in size
