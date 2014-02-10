#!/usr/bin/sh

ffmpeg -i 'frame%05d.pbm' pbm.ogv
find . -name '*pbm' -delete

