# weaveform-editor


## Simple Waveform Editor

A simple Android application that allows:
1. visualizing an audio waveform from a file
2. visually select a slice of it 
3. export the selected slice as a new file in 
the device’s Downloads directory
4. Handles wrong file inputs with error messages

## App Demo
<img src="https://github.com/KlodianKambo/weaveform-editor/assets/7395096/107d0882-4019-49be-8436-70cb597b4caf" width="50%" height="50%"/>


## Details

The representation of an audio waveform envelope is a pair of floating point values (separated by a space) in the interval [-1.0, 1.0] per line. 
Each such pair of values represents the minimum and maximum of the waveform envelope for a given interval of time.

For example a well formatted file can be represented as:

0.0 0.0
-0.6 0.5
-0.1 0.2
-0.9 1.0
-0.3 0.4


1. The files should be imported from the device storage

2. Once a file is selected, the waveform it represents is displayed in the view, and the selected slice
is initially equal to the whole waveform length. The selected slice can be modified by touching the view.

3. The content of the waveform view is custom-drawn, by using the Canvas and related graphics APIs.

4. The waveform is always drawn as to extend for the whole width of the view, and with the [-1.0, 1.0] 
vertical range corresponding to the height of the view.

5. The selection of the slice must be performed by handling touch directly in the onTouchEvent(...) 
for the custom view.

6. A new file containing only the data for the selected slice will be saved to the device’s Downloads 
folder after pressing a button

7. Selecting another waveform will load the new waveform and reset the slice to the full waveform length.