#!/usr/bin/sh

find . -name '*pbm' | xargs -l -i basename '{}' '.pbm' | xargs -l -i convert -transpose -scale 800% '{}.pbm' '{}.png'

