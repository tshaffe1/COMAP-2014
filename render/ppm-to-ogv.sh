#!/usr/bin/sh

ffmpeg -i 'frame%05d.ppm' ppm.ogv
find . -name '*.ppm' -delete

