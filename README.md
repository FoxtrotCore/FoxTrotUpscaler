# Welcome to FoxTrotUpscaler

Copyright 2018 Christian Devile

Thank you to Dmitri McGuckin for FFMPEG command syntax help and general advice.  
Thank you to Alex Thurman for loaning his computer for long term testing use.  
Thank you to Rave for beta testing.

## Purpose

This program is a controller to efficiently use FFMPEG, Waifu2x-caffe, and RawTherapee together to create a upscaled video. While you could easily replace the main functionality of this program with a batch script, it leaves a lot to be desired. Due to the time consuming nature of this process, this program aims to get started upscaling quickly, automate the process for multiple queued videos, provide basic statistics for the speed your computer hardware upscales, and provide some remote monitoring over your clients. For upscaling only a video occasionally, this may not feel so useful. For a whole team attempting to upscale entire tv shows on multiple computers, the extra tools offered should make a world of a difference.

## Method Explanation

FoxTrotUpscaler (FTU) renders videos in what seems like a extremely inefficient way. However, on the right videos, it works great, and is completely free. Recent breakthroughs in Computer Science have created algorithms that are capable of upscaling video with a reasonable degree of accuracy to a significantly higher resolution. There is some complex stuff going on in this process that even I don't quite understand myself, but the program Waifu2x-caffe, is able to take a photo, and upscale it guessing a fair bit of detail correctly. But it only is usable on photos, which is a obviously not video. So FTU uses FFMPEG, to decompress a video into each of its individual frames, process them all with the algorithm, and re-compress it all into a video. Also, since you are going to have all the photos there, the program gives the option to run the frames through RawTherapee, if you wanted to do some extra general touch-ups on the frames. Thats it, the video can then be combined with audio, subtitles, and meta data that the user chooses to be included. This process even with $2000 hardware, can take more then 24 hours running full blast for just a 30 minute video. This is the main reason for FTU's existence, not just to make this technology accessible to the computer illiterate, but to reduce computer idle time to zero, in between these 3-4 stages.

## Prerequisites

   1. Windows Environment
   2. Recent NVIDIA Graphics Card
   3. Significant amount of Storage Space dedicated JUST for FTU (at least 1TB)
   4. Reasonable CPU
   5. FFMPEG
   6. Microsoft Visual C++ 2013
   7. CUDA Toolkit
   8. Waifu2x-caffe
   9. RawTherapee (Optional)
      Optional, Expected File Name: (rawtherapee-cli.exe)
   10. cuDNN
      Optional NVIDIA library, but practically required to upscale in any reasonable time.
      Important to verify if this is installed correctly. Use the GUI version of Waifu2x-caffe, and press the "Check cudnn" button to verify if installed correctly

## Installation

   1. Install FFMPEG
         1. Go to the website linked below, and download the build desired
         2. Extract the files, and place them on your computer, like in /Program Files
         3. Keep track of the location of "ffmpeg.exe" and "ffprobe.exe"
   2. Install Microsoft Visual C++ 2013 Packages
         1. Go to the website linked below, and download the latest version
		 2. Run the Installer
   3. Install CUDA Toolkit
         1. Go to the website linked below, and download the latest version
		 2. Follow the instructions of the Installation Guide
   4. Install cuDNN
         1. Go to the website linked below, and create a NVIDIA developer account.
         2. With this account, you can now download a version of cuDNN appropriate for your Graphics Card.
         3. Read the installation guide to know which build is appropriate, and how to Install the program properly
            May have to update drivers. Also read extra instructions from Waifu2x-caffe github on how to connect Waifu to cuDNN
         4. If everything was installed correctly, you should be able to open the GUI version of Waifu, and the "check cuDNN" button should return successful
   5. Install Waifu2x-caffe
         1. Go to the website linked below, and download the latest version
         2. Extract the file, and place them on your computer, like in /Documents
            Potential Issues when installing into protected directories such as Program Files
         3. Keep track of the location of "waifu2x-caffe-cui.exe"
   6. Optional, Install RawTherapee
         1. Go to the website linked below, and download the latest version
         2. Run the installer
         3. Keep track of the location of "rawtherapee-cli.exe"
   7. Setup FoxTrotUpscaler
         1. Download the latest version, or the version compatible with the version of FTUServerBot you intend to connect with.
         2. Put the jar in its own folder, on a drive with at least 1TB of available space.
         3. Run the jar
         4. In the prompts, give FTU the locations of the previous programs you have installed as it asks for them.
         5. If all the required ones are recognized, FTU will ask for the location of RawTherapee, which you can opt out of.
         6. FTU will now actually start, and you will be greeted to a in program version of this document, and a kind of file structure will appear in the folder FTU is in. 
         7. Read through the rest of the pages to familiarize yourself with the program.

## Planning an Upscale

While its easy to upscale when given a "config.txt" and "rawTherapee.pp3", creating your custom configuration for a specific video type will take some testing. You need to change the values in "config.txt" to match the content you plan to upscale, the default one is not the best solution. The "config.txt" help section will explain everything you need to know the file, and what it does, but the only way to find the right config is through testing. The Waifu2x-caffe website has recommendations on which settings are probably best for certain computers and content types, but the guide is not perfect. It is best to create multiple configs, and upscale the same video with each of them, and pick which one looks the best. After, create derivative configs that have the same quality settings, but have different speed settings, to see how far you can reduce the time without being unstable, or compromising quality. This will finally get you to your custom "config.txt" that will work best for your situation. After, if you want to apply color correction on all of your frames, you can do so with RawTherapee. You will have to refer to this programs documentation on how, but you need to generate a .pp3 file, which will contain all the steps to color correct any input image. You can share these files with others, and split the workload accordingly. Some assistance in working with other computers and people can be found with FTUServerBot. You can read its documentation on github (eventually). Now you can make use of FTU, and start upscaling on your own! The rest of the documentation will teach, on how to use the program.

## External Links

* Project Page: [https://github.com/Christian77777/VideoUpscaler](https://github.com/Christian77777/VideoUpscaler)
* FFMPEG Main page: [https://ffmpeg.org/](https://ffmpeg.org/)
* FFMPEG Documentation: [https://ffmpeg.org/ffmpeg-all.html](https://ffmpeg.org/ffmpeg-all.html)
* Waifu2x-caffe Releases: [https://github.com/lltcggie/waifu2x-caffe/releases](https://github.com/lltcggie/waifu2x-caffe/releases)
* Waifu2x-caffe Documentation: [https://github.com/lltcggie/waifu2x-caffe](https://github.com/lltcggie/waifu2x-caffe)
* Microsoft Visual C++ 2013 Download: [https://www.microsoft.com/en-US/download/details.aspx?id=40784](https://www.microsoft.com/en-US/download/details.aspx?id=40784)
* CUDA Download Page: [http://developer.nvidia.com/cuda-downloads](http://developer.nvidia.com/cuda-downloads)
* CUDA Installation Guide: [http://docs.nvidia.com/cuda/cuda-installation-guide-microsoft-windows/index.html](http://docs.nvidia.com/cuda/cuda-installation-guide-microsoft-windows/index.html)
* cuDNN Download Page: [https://developer.nvidia.com/cuDNN](https://developer.nvidia.com/cuDNN)
* cuDNN Installation Guide: [http://docs.nvidia.com/deeplearning/sdk/cudnn-install/index.html#installcuda-windows](http://docs.nvidia.com/deeplearning/sdk/cudnn-install/index.html#installcuda-windows)
* RawTherapee Website: [http://rawtherapee.com/](http://rawtherapee.com/)
* RawTherapee Releases: [http://rawtherapee.com/downloads](http://rawtherapee.com/downloads)
* Inspiration for Project: [https://foxtrotfanatics.info/](https://foxtrotfanatics.info/)

##### If you're reading this, then congratulations, you made it to the end! Also, Christian is a dingus! Be sure to tell him that but don't tell him I told you to tell him that.
