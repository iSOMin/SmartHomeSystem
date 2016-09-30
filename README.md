# SmartHomeSystem
동남권 융복합 캡스톤디자인(LINC USR) 작품<br>
##Interphone.ino
  sending RF signal when the doorbell pressed.
<br>
##smartclock.ino
  getting RF signal by Interphone and pass it to the android app by bluetooth.<br>
  getting bluetooth signal by android app.<br>
  when it gets any signal, it sprays perfume with mini water pump motor and show status on the TFT LCD.
<br>
##FinalSmartHome
  getting bluetooth signal by smartclock and change the light color of Philips Hue.<br>
  sending bluetooth signal to smartclock when "CALL" button pressed. <br>
  when the "CALL" button pressed, it change the light color of Philips Hue.<br>
  when "RANDOM" button pressed, it change the light color randomly.
<br>
##additional code
###smartclockWithLEDMatrix.ino
  getting only bluetooth signal by andriod app and sparys perfume.<br>
  it uses 8x16 adafruit LED Matrix.
<br>
###clock_matrix.ino
  it shows only time.
