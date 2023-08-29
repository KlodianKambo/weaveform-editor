# weaveform-editor


## Test Assignment - Waveform Editor

Create a simple Android application that allows visualizing an audio waveform from a file, 
visually select a slice of it and export the selected slice as a new file in 
the device’s Downloads directory. For a reference, this waveform view should behave somewhat like 
the one in the sampler pad editor in BandLab’s MixEditor.

To see how this view looks and behaves, you can reach the sampler pad editor by following 
the steps in this video: https://drive.google.com/file/d/12ySBW_b2J5TPF_OMnM-YQ936IKrUs7pO/view?usp=sharing


## Details

You will be provided with a few text files, each containing the representation of an audio waveform
envelope, with a pair of floating point values (separated by a space) in the range [-1.0, 1.0] per line. 
Each such pair of values represents the minimum and maximum of the waveform envelope for a given interval of time.

For example, a text file containing the following data:

0.0 0.0
-0.6 0.5
-0.1 0.2
-0.9 1.0
-0.3 0.4


The files should be imported from the device storage, and can be downloaded beforehand to the device 
storage from here: https://drive.google.com/drive/folders/1jdavtPN-dEFAQWfAdo-H4i-jrRKws4Qu?usp=sharing

Once a file is selected, the waveform it represents is displayed in the view, and the selected slice
is initially equal to the whole waveform length. The selected slice can be modified by touching the view.

The content of the waveform view must be custom-drawn, by using the Canvas and related graphics APIs.

The waveform is always drawn as to extend for the whole width of the view, and with the [-1.0, 1.0] 
vertical range corresponding to the height of the view.

The selection of the slice must be performed by handling touch directly in the onTouchEvent(...) 
for the custom view. The currently selected slice and its edges can be represented in the way you prefer.

There should be a button that triggers the export: when the user clicks it, a new file containing 
only the data for the selected slice will be saved to the device’s Downloads folder.

Selecting another waveform will load the new waveform and reset the slice to the full waveform length.