#!/usr/bin/sh

ffmpeg -i 'frame%05d.pbm' pbm.avi
find . -name '*pbm' -delete

