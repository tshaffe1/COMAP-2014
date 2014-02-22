#!/usr/bin/sh

find . -name '*ppm' | xargs -l -i basename '{}' '.ppm' | xargs -l -i convert -flip -scale 800% '{}.ppm' '{}.ppm'

