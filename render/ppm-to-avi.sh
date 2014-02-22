#!/usr/bin/sh

ffmpeg -i 'frame%05d.ppm' ppm.avi
find . -name '*.ppm' -delete

