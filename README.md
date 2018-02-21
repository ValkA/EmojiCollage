# Emoji Collage
This app gets an image as input (from camera or gallery) and creates an "Emojified" image out of it.

From:

![alt tag](https://raw.githubusercontent.com/ValkA/EmojiCollage/master/1from.jpeg)

To:
![alt tag](https://raw.githubusercontent.com/ValkA/EmojiCollage/master/1to.jpeg)

You can choose which type of emojis you want to use, the "scattering algorithm", or perhaps scan all faces in you gallery and use them instead of emojis.

## Download on Google Play

The app made with <3 for fun, it can be downloaded from Google Play:

https://play.google.com/store/apps/details?id=valka.emojicollage

## Technical stuff
The way that the image is created is as following:
1. first we index the emoji database into a KDTree.
    * The key of each emoji is 3 dimentional vector. 
    * Its value is the mean (red,green,blue) values of the emoji pixels weightened by the alpha channel.
2. we choose a scattering algorithm - an algorithm that chooses regions of interest (ROI) in the input image.
3. we create a new empty black output image.
4. For each ROI:
    1. we calculate the average (red,green,blue) vector of its pixels
    2. we find in the KDTree the emoji with the nearest key to the above vector
    3. we draw the emoji on the ROI