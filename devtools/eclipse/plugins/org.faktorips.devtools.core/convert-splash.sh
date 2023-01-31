#!/bin/bash
# Note: you have to install inkscape and imagemagick to run this skript

inkscape --export-filename=splash.png splash.svg
convert splash.png -verbose bmp3:splash.bmp
rm splash.png
