#!/usr/bin/sh

find . -name '*pbm' | xargs -l -i basename '{}' '.pbm' | xargs -l -i convert -flip -scale 400% '{}.pbm' '{}.pbm'

